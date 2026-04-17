# Knusrae 백엔드 소스 검토 보고서

검토 일자: 2026-03-03  
대상: `backend/` (auth-service, member-service, cook-service, common-service)

---

## 1. 아키텍처 및 구성

### 1.1 서비스 구성
- **auth-service** (8081): 소셜 로그인(네이버/구글/카카오), JWT 발급·갱신·로그아웃, 테스트 로그인
- **member-service** (8083): 회원 프로필, 팔로우/언팔로우, 문의(Inquiry)
- **cook-service** (8082): 레시피, 카테고리, 검색, 인기/테마, 북마크, 댓글 등
- **common-service**: 공통 도메인(Member, Follow), JWT/보안, 예외 처리, 유틸

모든 서비스가 `common-service`를 의존하고, JPA + PostgreSQL, Spring Boot 3.5.3, Java 21 기반입니다.

### 1.2 설정
- `application.yml`: 프로파일(`local`/`dev`/`prod`), `application-common.yml` optional import
- 포트·DB·JWT·CORS 등은 프로파일별 yml에서 분리

---

## 2. 인증·토큰 (Auth)

### 2.1 정상 동작
- 소셜 로그인 후 Access/Refresh 토큰 발급, 쿠키 설정, 리다이렉트 처리
- Refresh 토큰 로테이션(기존 토큰 삭제 후 새 토큰 저장)
- 로그아웃 시 Access 토큰 블랙리스트, Refresh 토큰 삭제
- 테스트 로그인은 `app.test-login.enabled`로 제어

### 2.2 **[심각] Refresh 토큰 동시성 이슈 (카카오 로그인 오류 원인)**

**증상**  
동일 Refresh 토큰으로 **동시에** 갱신 요청이 들어오면:
1. `ObjectOptimisticLockingFailureException` / `StaleObjectStateException`: 한 트랜잭션이 삭제한 row를 다른 트랜잭션이 다시 삭제/갱신 시도
2. `DataIntegrityViolationException` (duplicate key): 삭제가 커밋되기 전에 새 토큰 insert가 먼저 실행되어, 같은 PK(token)로 중복 insert 시도

**원인**  
- `RefreshToken` 엔티티 PK가 `token` 문자열
- `TokenService.refreshAccessToken()` 흐름: 기존 토큰 조회 → `delete(refreshToken)` → 새 토큰 `save()`
- 동시 요청 시: 요청 A·B가 같은 토큰 조회 → A가 delete 예약, B도 delete 예약 → flush 시 순서에 따라 한쪽이 이미 삭제된 row를 건드리거나, 새 토큰이 두 번 insert됨

**“한 번만 로그인했는데도” 동시성이 발생하는 이유**  
로그인 버튼을 한 번만 눌러도, **프론트엔드에서 Refresh 요청이 동시에 두 번 나갈 수 있습니다.**

1. 카카오 로그인 완료 후 브라우저가 프론트 주소(예: `/auth/kakao/callback?success=true`)로 리다이렉트됩니다.
2. 해당 페이지가 로드되면 Vue 앱이 뜨고, **동시에** 다음 두 곳에서 인증 확인이 실행됩니다.
   - **App.vue** `onMounted`: `authStore.checkAuth()` → `refreshToken()` 호출 (**요청 1**)
   - **router** `beforeEach`: 아직 `isInitialized`가 `false`이면 `authStore.checkAuth()` → `refreshToken()` 호출 (**요청 2**)
3. `checkAuth()`가 끝나기 전에는 `isInitialized`가 `true`로 바뀌지 않으므로, 첫 화면 진입 시 **같은 Refresh 토큰으로 POST /api/auth/refresh 가 두 번** 나갈 수 있습니다.
4. 백엔드에서는 동일한 Refresh 토큰에 대해 두 요청이 거의 동시에 “조회 → 삭제 → 새 토큰 저장”을 수행하면서, 위의 낙관적 락/중복 키 오류가 발생합니다.

따라서 **사용자는 한 번만 로그인했어도**, **앱 초기화 + 라우터 가드**가 같은 타이밍에 refresh를 호출해 동시성이 발생한 것으로 보는 것이 맞습니다.

**권장 수정 방향**
1. **동시 갱신 방지**: 같은 refreshToken 문자열에 대해 DB 락 또는 유니크 제약을 활용한 “한 번만 갱신” 처리  
   - 예: `refreshTokenRepository.findByToken(refreshTokenString)` 후 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 또는 `SELECT ... FOR UPDATE`로 해당 row 잠금 후 삭제·삽입
2. **삭제 후 삽입 순서 보장**: 같은 트랜잭션 내에서 `delete`를 먼저 flush한 뒤 새 토큰 insert (필요 시 `entityManager.flush()` 호출)
3. **재시도/에러 처리**: 동시 갱신으로 인한 예외(`ObjectOptimisticLockingFailureException`, `DataIntegrityViolationException`) 발생 시 클라이언트에 “한 번만 재시도” 또는 “재로그인 필요” 메시지로 401 등 반환

(구체적인 코드 수정 예시는 `TokenService.refreshAccessToken` 및 `RefreshTokenRepository`에 적용 가능합니다.)

### 2.3 JWT
- Access/Refresh 키·TTL 분리, HS256
- `JwtAuthenticationFilter`: 쿠키에서 accessToken 추출, 블랙리스트 확인, 인증 실패 시 JSON 응답

### 2.4 기타
- `/api/auth/refresh`는 Access 검증 생략, Refresh만 사용하는 구조 적절
- `/api/auth/jwt/token` (GET, user_id 등): 개발용으로 보이며, 운영에서는 비활성화 또는 제거 권장

---

## 3. 보안

### 3.1 긍정
- Stateless 세션, CSRF 비활성화(API 특성상 타당)
- CORS 설정 존재, Credentials·헤더·메서드 제한
- 인증 실패 시 401 + JSON body
- 테스트 로그인·테스트 계정 목록은 플래그로 차단 가능

### 3.2 개선 권장
- **application-local.yml**: 네이버/구글/카카오 client id·secret 기본값이 평문으로 있음. 로컬도 가능하면 환경 변수만 사용하고, 기본값은 제거하거나 placeholder 권장
- **JwtAuthenticationFilter**: `tokenBlacklistRepository`를 `BeanFactory.getBean("tokenBlacklistRepository")`로 조회. 공통 모듈에서 auth 전용 빈 이름에 의존하지 않고, 인터페이스 주입(예: `TokenBlacklistRepository`)으로 바꾸면 이식성·테스트 용이

---

## 4. 예외 처리

### 4.1 GlobalExceptionHandler (common)
- `IllegalArgumentException` → 400  
- `MethodArgumentNotValidException` → 400 (첫 필드 에러 메시지)  
- `AccessDeniedException` → 403  
- `AuthenticationException` 등 → 401  
- `ResourceNotFoundException` → 404  
- `EntityNotFoundException` → 404  
- `Exception` → 500, 메시지 고정

### 4.2 AuthController
- refresh: `IllegalArgumentException` → 401, 그 외 → 500
- **보완**: `ObjectOptimisticLockingFailureException`, `DataIntegrityViolationException` 등 토큰 갱신 동시성 예외를 401 또는 409로 명시 처리하면, 클라이언트에서 “재시도 또는 재로그인”으로 유도하기 좋음

---

## 5. 도메인·리포지토리

### 5.1 Member (common)
- 엔티티 필드·Auditing·프로필 수정·팔로우 카운트 증감 메서드 적절
- `MemberRepository.findByEmailAndSocialRole` 반환 타입이 `Member`(nullable). 네이버/구글/카카오 서비스에서 `ObjectUtils.isEmpty(member)`로 null 체크 후 사용 중이라 동작은 맞음. 다만 `Optional<Member>`로 통일하면 NPE 방지와 API 일관성에 유리

### 5.2 FollowService (member-service)
- `follow`/`unfollow`에서 `Member`를 `findById`로 로드한 뒤 `increaseFollowerCount`/`decreaseFollowerCount`만 호출. 같은 트랜잭션 내 영속 엔티티 변경이므로 flush 시 반영되며, 별도 `save` 불필요

### 5.3 RefreshToken (auth-service)
- PK가 `token` 문자열, `userId`, `expiresAt`, `createdAt` 구성. 만료 삭제용 `deleteExpiredTokens` 등으로 정리 가능

---

## 6. API·컨트롤러

### 6.1 인증
- 콜백은 GET, 로그아웃/갱신/테스트 로그인은 POST 적절
- 성공 시 Set-Cookie + 302 리다이렉트, 실패 시 쿼리 파라미터로 에러 전달

### 6.2 회원·팔로우·문의
- `/api/member/me`, `/{memberId}`, `/profile` (PUT): 인증·경로 일치
- 팔로우/언팔로우/목록·체크: SecurityConfig의 permitAll/authenticated 구분과 맞음
- 문의(Inquiry): 타입·길이·이미지 개수 제한 등 서비스 레벨 검증 있음

### 6.3 Cook
- 레시피 CRUD, 검색, 카테고리, 테마, 북마크, 댓글 등 역할 분리되어 있음 (상세 비즈니스 로직은 필요 시 별도 검토)

---

## 7. 설정·환경

- `spring.config.import: optional:classpath:application-common.yml`: 파일 없어도 기동 가능
- 프로파일별 DB·JWT·CORS·frontend URL 등 분리
- **공통**: JWT secret, DB 비밀번호, OAuth 클라이언트 시크릿은 프로덕션에서 반드시 환경 변수(또는 시크릿 매니저) 사용 권장

---

## 8. 요약 및 우선 조치 권장

| 구분 | 내용 | 우선순위 |
|------|------|----------|
| **Refresh 토큰 동시성** | 동시 갱신 시 낙관적 락/중복 키 예외 → 락 또는 flush 순서·재시도 처리로 해결 | 높음 |
| **Auth 예외 처리** | 갱신 시 동시성 관련 예외를 401/409로 명시 처리 | 높음 |
| **설정 보안** | local yml 기본 시크릿 제거, 프로덕션은 환경 변수 필수 | 중간 |
| **JwtAuthenticationFilter** | 블랙리스트 조회를 빈 이름이 아닌 인터페이스 주입으로 변경 | 낮음 |
| **MemberRepository** | `findByEmailAndSocialRole` 반환을 `Optional<Member>`로 통일 | 낮음 |

전반적으로 레이어 분리·예외 처리·보안 설정이 갖춰져 있고, **카카오 로그인 직후 발생한 오류는 Refresh 토큰 갱신의 동시 요청**에서 비롯된 것으로 보는 것이 타당합니다. 위의 “Refresh 토큰 동시성” 조치를 적용하면 해당 오류를 줄일 수 있습니다.
