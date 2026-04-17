# Redis 활용 검토

본 문서는 Knusrae 애플리케이션에서 Redis를 도입할 경우 **활용 가능 지점**과 **필요성**을 정리한 내용입니다.  
현재 Redis는 미구성 상태이며, 추후 트래픽·성능 요구에 따라 단계적으로 도입을 검토할 수 있습니다.

---

## 1. Redis 활용 가능 지점

### 1.1 JWT 토큰 블랙리스트 (우선 추천)

| 항목 | 내용 |
|------|------|
| **현재 구조** | 로그아웃 시 `token_blacklist` 테이블에 저장하고, **모든 인증 요청**마다 `JwtAuthenticationFilter`에서 DB 조회(`TokenBlacklistRepository.findByToken`) |
| **이슈** | API 호출마다 DB 조회가 발생하여 트래픽이 늘수록 DB 부하 증가 |
| **Redis 활용** | `key = 토큰(또는 해시)`, `TTL = 액세스 토큰 만료 시간`으로 저장. 조회는 O(1), 만료 시 자동 삭제로 DB·스케줄러 부담 감소 |
| **효과** | 인증이 필요한 모든 요청에서 DB 대신 Redis 1회 조회로 전환 가능 |

**관련 코드**

- `auth-service`: `TokenBlacklistRepository`, `TokenBlacklistCheckerImpl`
- `common-service`: `JwtAuthenticationFilter` — 매 요청 시 `isTokenBlacklisted(token)` 호출

---

### 1.2 오늘의 추천 레시피 (대시보드)

| 항목 | 내용 |
|------|------|
| **현재 구조** | `RecommendationService`에서 로그인 사용자는 최근 조회 카테고리·최근 본 레시피·후보 수집·인기 레시피·찜 개수 등 **여러 번의 DB 조회**, 비로그인은 인기 레시피 + 찜 개수 조회. 메인/대시보드 접속 시마다 매번 실행 |
| **Redis 활용** | 비로그인 사용자용 응답을 캐시(예: TTL 1시간). 로그인 사용자는 사용자별 캐시(예: TTL 10~30분) 또는 비로그인 캐시 + 개인화 보정으로 DB 조회 횟수 감소 |
| **효과** | 대시보드 첫 화면 로딩 경량화, DB 부하 감소 |

**관련 코드**

- `cook-service`: `RecommendationService`, `RecommendationController`
- API: `GET /api/cook/recipes/recommendations/today` (프론트에서 `refresh` 파라미터로 캐시 무시 가능 — 현재 백엔드 캐시 미구현)

---

### 1.3 인기 레시피 / 랭킹

| 항목 | 내용 |
|------|------|
| **현재 구조** | `PopularRecipeService`에서 인기순 조회 + 이전 순위(히스토리) 조회 등 매 요청마다 DB 접근 |
| **Redis 활용** | 기간별(24h / 7d / 30d) 결과를 캐시(예: TTL 5~15분). 배치/스케줄러로 인기 점수를 갱신하는 구조라면 그 주기에 맞춰 캐시 무효화 가능 |
| **효과** | 랭킹/인기 목록 조회 시 DB 부하 감소, 응답 시간 단축 |

**관련 코드**

- `cook-service`: `PopularRecipeService`, `RecipeController` — `GET /api/cook/recipe/popular`
- 화면: `/ranking` (기간별 24h / 7d / 30d)

---

### 1.4 테마·카테고리·공통코드

| 항목 | 내용 |
|------|------|
| **현재 구조** | 테마 목록, 트렌딩 카테고리, 공통코드 등은 변경 빈도가 낮음 |
| **Redis 활용** | 목록/트리 구조를 JSON 등으로 캐시(TTL 30분~수 시간). 관리자가 수정 시 해당 키만 삭제 또는 갱신 |
| **효과** | 자주 바뀌지 않는 마스터 데이터 조회 시 DB 접근 감소 |

**관련 API**

- `GET /api/cook/themes/active`, `GET /api/cook/themes/{themeId}/recipes`
- `GET /api/cook/categories/trending`, `GET /api/cook/categories/{codeId}/{detailCodeId}/recipes`
- `GET /api/cook/common-codes`

---

### 1.5 기타 (선택)

| 항목 | 내용 |
|------|------|
| **세션 / 동시 접속 제한** | 동일 사용자 중복 로그인 제한, 간단한 rate limiting 등에 Redis 카운터/세션 키 활용 가능 |
| **실시간성** | 추후 알림·실시간 카운트 등이 필요해지면 Pub/Sub 등으로 확장 가능 |

---

## 2. 도입 필요성 요약

| 구분 | 내용 |
|------|------|
| **현재** | Redis 없이도 서비스 가능. 소규모·중간 규모 트래픽이라면 DB만으로 동작에 무리는 없음. **필수는 아님** |
| **도입 검토 시점** | 동시 접속·API 호출이 늘어나거나, DB CPU/IO가 부담될 때. 메인·대시보드·랭킹·인기 목록 응답이 체감될 때. 로그아웃 후 토큰 검증으로 DB 부하를 줄이고 싶을 때 |
| **정리** | **없어도 되지만, 트래픽·성능 요구가 커지면 도입 가치가 있는 구성** |

---

## 3. 도입 시 권장 우선순위

1. **JWT 토큰 블랙리스트** — 모든 인증 요청에 영향, 구현 난이도·효과 대비 높음  
2. **오늘의 추천(대시보드)** — 메인 진입 시 빈번한 조회, 캐시 효과 큼  
3. **인기/랭킹** — 동일 패턴으로 캐시 적용 용이  
4. **테마·카테고리·공통코드** — 변경 빈도가 낮아 장기 TTL 캐시에 적합  
5. **세션/제한·실시간** — 요구사항 발생 시 추가 검토  

---

## 4. 참고 문서

- [아키텍처 정의서](../design/01-requirements/아키텍처정의서.md) — 시스템 구성, 기술 스택(Redis: 현재 미구성·추후 구현 가능)
- [요구사항 정의서](../design/01-requirements/요구사항정의서.md) — NFR-04 API 캐싱

---

## 5. 변경 이력

| 버전 | 일자 | 변경 내용 |
|------|------|-----------|
| 1.0 | 2026-03-14 | 최초 작성 — Redis 활용 가능 지점 및 도입 필요성 정리 |
