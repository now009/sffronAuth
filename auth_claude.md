
1. Project 생성
- Springboot OAuth application을 위한 maven Project를 생성
- 인증 Web Port는 8090
- 사용자 정보는 우선 resource에 secret.yml 파일을 생성하여
  아이디 admin , 비번 1234!를 저장해놓고 인증용으로 사용자 확인

"Login form 구현, secret.yml username/password로 인증, 성공시 JWT 발급하여 http://localhost:8080/main 으로 redirect"

클라이언트에서 admin 1234!로 인증요청이 올것이고 secret.yml에 확인해줘. 이부분은 이후에 DB에서 Query로 확인하는 부분으로 변경할것이니깐 별도의 java service 에서 담당할수 있도록 해줘 

아이디 비번은 client에서 입력할것이 이 서버는 아이디 비번 받아서 인증한후 backend 서버 /main으로 전달해야되 

- Client (8080/8081) — 로그인 폼 보유, credentials POST
- Auth 서버 (8090) — 인증 후 JWT 발급 → backend /main으로 redirect
- Login form (login.html) — 불필요 (client가 직접 보유)      

  Client (8080/8081)
  └─ POST http://localhost:8090/auth/login                                                                                                                                            
  { username=admin, password=1234! }
  │                                                                                                                                                                         
  ▼                                             
  AuthController
  AuthenticationManager → UserAuthService.loadUserByUsername()
  JwtTokenService.generate()
  │
  ▼ 302 redirect
  http://localhost:8080/main?token=<JWT>


┌─────────────────────┬─────────────────────────────────────────────────────────────────────────────────┐
│        파일         │                                      변경                                       │
├─────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
│ JwtTokenService     │ JWT 생성 로직 전담 서비스 (신규)                                                │
├─────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
│ AuthController      │ POST /auth/login 엔드포인트 (신규)                                              │
├─────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
│ LoginSuccessHandler │ JwtTokenService 사용하도록 정리                                                 │
├─────────────────────┼─────────────────────────────────────────────────────────────────────────────────┤
│ SecurityConfig      │ form login 제거, CORS(8080/8081 허용), CSRF 예외, AuthenticationManager 빈 추가 │
└─────────────────────┴─────────────────────────────────────────────────────────────────────────────────┘


현재 최종 구조입니다:

controller/                                                                                                                                                                           
AuthController.java       ← POST /auth/login
config/                                                                                                                                                                               
AuthorizationServerConfig.java                          
AuthSecretProperties.java
SecurityConfig.java
service/
JwtTokenService.java      ← JWT 생성
UserAuthService.java      ← 사용자 인증 (secret.yml → 추후 DB)

--------------------------------------------------------
1. DB 관련정보를 yml에 설정하고 
   - database : mariadb://localhost:3306/saffron
2. login 요청온 사용자 아이디 / 비번을 user_info userId,password 컬럼 에서 확인 
   비번은 - -- 암호화 : BCryptPasswordEncoder를 이용

   userId
   deptId
   userName
   email

front에서 아이디 비번 입력후 auth서버에서 아이디비번 입력확인 한후 
JWT에 user_info table의    userId,deptId,userName,email 담아
fornt에서 활용할수 있는 방법은 어떤것이 있는가? 
이 과정에서 backend의 역할은?
access_token은 JWT에 담아 사용하는가?

현재 로그인 API가 있는데 JWT 토큰 생성 기능을 추가해줘.

조건:
-. DB 관련정보를 yml에 설정하고
  - database : mariadb://localhost:3306/saffron
  - backend yml부분 예시
    datasource:
      url: jdbc:p6spy:mariadb://localhost:3306/saffron?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
      username: now009
      password: 2799
      driver-class-name: com.p6spy.engine.spy.P6SpyDriver
  - 
- POST /auth/login (userId, password) 요청 받음  -- 지금 코드에서 받은 변수명과 동일한지 확인, 기존변수명 우선
- user_info 테이블에서 userId, password 검증
- 검증 성공 시 JWT access_token 생성
  - payload에 포함할 항목: userId, deptId, userName, email
  - 만료시간: 1시간
  - 알고리즘: HS256
- 생성된 access_token을 담아
  http://localhost:8080/main?access_token={token} 으로 redirect -- 기존 url 코드가 있으면 유지

user_info 테이블 구조:
    CREATE TABLE user_role (
    userId      VARCHAR(50) NOT NULL  COMMENT '사용자ID',
    roleCode    VARCHAR(50) NOT NULL  COMMENT '권한코드',
    createdUser VARCHAR(20) DEFAULT 'system'          COMMENT '생성자',
    createdDate TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    PRIMARY KEY (userId, roleCode)
    ) COMMENT '사용자권한매핑';

JWT secret key는 application.yml에서 관리할 것

- service/UserAuthService.java — yml 인메모리 사용자 → UserInfoRepository.findById() 로 전환
- service/JwtTokenService.java — RSA(JwtEncoder) → HS256(jjwt) 재작성, payload userId/deptId/userName/email, 1시간 만료
- controller/AuthController.java — 인증 후 UserInfo 조회해 JWT 발급, redirect URL 그대로 유지
- 
secret.yml의 auth.users: 제거 
JWT secret: secret.yml

--------------------------------------------------

Spring Boot(Java, Maven) 백엔드에 HTTP 요청/응답 로깅 필터를 추가해줘.

[구현 방식 — 반드시 아래 방식으로 구현]
1. OncePerRequestFilter 를 상속한 HttpLoggingFilter 클래스 생성
2. 요청 body 는 ContentCachingRequestWrapper 를 쓰지 말 것.
   대신 필터 진입 즉시 StreamUtils.copyToByteArray(request.getInputStream()) 로
   body 전체를 byte[] 에 읽어두고, HttpServletRequestWrapper 를 상속한 내부 클래스
   (CachedBodyRequest)로 ByteArrayInputStream 을 재포장해서 chain 에 전달.
   이렇게 해야 컨트롤러(@RequestBody)가 body 를 다시 읽을 수 있음.
3. 응답 body 는 ContentCachingResponseWrapper 로 캐싱한 뒤,
   finally 블록에서 반드시 copyBodyToResponse() 호출.
4. 요청 로그는 chain.doFilter 실행 전(body 확보 직후) 출력.
5. 응답 로그는 chain.doFilter 완료 후 출력.

[로그 포맷]
요청: >>> REQ  POST /api/recipes  [application/json]
{"name":"TEST","content":"..."}
응답: <<< RES  200 (12ms)  [application/json]
{"id":4,"name":"TEST",...}

[필터 제외 대상]
- /api/ 로 시작하지 않는 경로 (정적 리소스 등)
- WebSocket Upgrade 요청 (Upgrade: websocket 헤더)
- 폴링용 헬스체크 엔드포인트 (yml 설정으로 제어)

[yml 설정으로 제어]
logging:
http:
enabled: true           # false → 필터 완전 비활성
max-body-size: 4096     # 출력 최대 바이트 (초과 시 truncated 표시)
log-status-check: false # 폴링 헬스체크 로그 여부

[주의사항]
- ContentCachingRequestWrapper 의 getContentAsByteArray() 는
  Jackson 이 InputStream 을 소비한 뒤 호출하면 빈 배열을 반환하는 버그가 있음.
  반드시 위 방식(byte[] 선읽기 + ByteArrayInputStream 재포장)을 사용할 것.
- 바이너리 Content-Type(image/, octet-stream 등)은 body 내용 대신
  [binary N bytes] 로 표시.
- copyBodyToResponse() 는 finally 에서 항상 실행되어야
  클라이언트에 응답이 정상적으로 전달됨.