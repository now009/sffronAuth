package com.saffron.auth.filter;

import com.saffron.auth.config.HttpLoggingProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class HttpLoggingFilter extends OncePerRequestFilter {

    private final HttpLoggingProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 필터 제외 대상 (현재는 없음)
        // 예시) /api/ 로 시작하지 않는 정적 리소스 등은 아래처럼 필터링 제외
        // String uri = request.getRequestURI();
        // if (!uri.startsWith("/api/")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, jakarta.servlet.ServletException {

        String contentType = request.getContentType();

        // form-urlencoded / multipart 는 body 캐싱하면 servlet container 의
        // form 파싱이 깨져 request.getParameter(...) 가 빈 값이 됨
        // (Spring Auth Server /oauth2/token, @RequestParam 컨트롤러 등이 영향).
        // → 이런 콘텐트 타입은 wrap 하지 않고 통과시킨다.
        boolean cacheBody = isCacheableBody(contentType);

        HttpServletRequest forwardRequest;
        byte[] requestBody;
        if (cacheBody) {
            // 요청 body 선읽기 (ContentCachingRequestWrapper 미사용 — Jackson이 InputStream을 소비하면
            // getContentAsByteArray()가 빈 배열을 반환하는 이슈 회피)
            requestBody = StreamUtils.copyToByteArray(request.getInputStream());
            forwardRequest = new CachedBodyRequest(request, requestBody);
        } else {
            requestBody = null;
            forwardRequest = request;
        }

        // 응답 캐싱
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // 요청 로그 (chain.doFilter 전)
        long start = System.currentTimeMillis();
        logRequest(forwardRequest, requestBody, cacheBody);

        try {
            chain.doFilter(forwardRequest, wrappedResponse);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            // 응답 로그 (chain.doFilter 후)
            logResponse(wrappedResponse, elapsed);
            // copyBodyToResponse() 는 finally 에서 반드시 실행 — 클라이언트에 응답이 정상 전달됨
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request, byte[] body, boolean cachedBody) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUri = (query != null) ? uri + "?" + query : uri;
        String contentType = request.getContentType();

        String bodyStr;
        if (cachedBody) {
            bodyStr = formatBody(body, contentType);
        } else {
            // form-urlencoded / multipart: body 대신 파라미터 맵을 표시
            bodyStr = formatParams(request);
        }
        log.info(">>> REQ  {} {}  [{}]\n      {}", method, fullUri, contentType, bodyStr);
    }

    private boolean isCacheableBody(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase();
        if (ct.startsWith("application/x-www-form-urlencoded")) return false;
        if (ct.startsWith("multipart/")) return false;
        return true;
    }

    private String formatParams(HttpServletRequest request) {
        java.util.Map<String, String[]> params = request.getParameterMap();
        if (params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (sb.length() > 0) sb.append('&');
            sb.append(k).append('=');
            // password 류는 마스킹
            if (k.toLowerCase().contains("password") || k.toLowerCase().contains("secret")) {
                sb.append("***");
            } else {
                sb.append(String.join(",", v));
            }
        });
        return sb.toString();
    }

    private void logResponse(ContentCachingResponseWrapper response, long elapsedMs) {
        int status = response.getStatus();
        String contentType = response.getContentType();
        byte[] body = response.getContentAsByteArray();

        String bodyStr = formatBody(body, contentType);
        log.info("<<< RES  {} ({}ms)  [{}]\n      {}", status, elapsedMs, contentType, bodyStr);
    }

    private String formatBody(byte[] body, String contentType) {
        if (body == null || body.length == 0) {
            return "";
        }
        if (isBinary(contentType)) {
            return "[binary " + body.length + " bytes]";
        }
        int max = properties.getMaxBodySize();
        if (body.length > max) {
            String head = new String(body, 0, max, StandardCharsets.UTF_8);
            return head + " ...(truncated, total " + body.length + " bytes)";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    private boolean isBinary(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase();
        return ct.startsWith("image/")
                || ct.startsWith("audio/")
                || ct.startsWith("video/")
                || ct.contains("octet-stream")
                || ct.contains("pdf")
                || ct.contains("zip");
    }

    /**
     * body byte[] 를 ByteArrayInputStream 으로 재포장하여
     * 컨트롤러(@RequestBody) 등에서 다시 읽을 수 있게 한다.
     */
    private static class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachedBodyRequest(HttpServletRequest request, byte[] cachedBody) {
            super(request);
            this.cachedBody = cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream buffer = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return buffer.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int read() {
                    return buffer.read();
                }
            };
        }

        @Override
        public java.io.BufferedReader getReader() {
            return new java.io.BufferedReader(
                    new java.io.InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
