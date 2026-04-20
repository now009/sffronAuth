
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
