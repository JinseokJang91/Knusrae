# 인터페이스 정의서

**작성 기준**: 2026-02-20

본 애플리케이션에서 **송신/수신하는 외부 인터페이스**를 정리한 문서입니다.  
(외부 시스템 API 호출, 연동 프로토콜, 요청/응답 형식 등)

---

## 1. 개요

| 구분 | 내용 |
|------|------|
| 연동 방식 | HTTP/HTTPS (REST) |
| 호출 주체 | 본 시스템 → 외부 시스템 (송신만 존재, 외부→본 시스템 수신 API 없음) |
| 사용 목적 | 소셜 로그인(OAuth 2.0): 토큰 발급 및 사용자 정보 조회 |

본 애플리케이션은 **외부로 나가는 호출**만 있으며, 네이버/구글/카카오 OAuth 서버에 토큰·사용자정보 요청을 보냅니다.  
외부 시스템이 본 애플리케이션을 호출하는 수신 인터페이스는 없습니다.

---

## 2. 외부 인터페이스 목록

| No | 연동 대상 | 방향 | 용도 | 비고 |
|----|-----------|------|------|------|
| 1 | 네이버 로그인(NAVER OAuth 2.0) | 송신 | 액세스 토큰 발급, 사용자 정보 조회 | auth-service |
| 2 | 구글 로그인(Google OAuth 2.0) | 송신 | 액세스 토큰 발급, 사용자 정보 조회 | auth-service |
| 3 | 카카오 로그인(Kakao OAuth 2.0) | 송신 | 액세스 토큰 발급, 사용자 정보 조회 | auth-service |

---

## 3. 인터페이스 상세

### 3.1 네이버 로그인 (NAVER OAuth 2.0)

| 항목 | 내용 |
|------|------|
| 연동 대상 | NAVER (nid.naver.com, openapi.naver.com) |
| 방향 | 본 시스템 → 네이버 (송신) |
| 구현 위치 | `auth-service` / `NaverAuthService` |
| 전송 방식 | RestTemplate, application/x-www-form-urlencoded / Bearer Token |

#### 3.1.1 액세스 토큰 발급

| 항목 | 내용 |
|------|------|
| URL | `https://nid.naver.com/oauth2.0/token` |
| Method | POST |
| 요청 | grant_type=authorization_code, client_id, client_secret, code, state |
| 응답 | JSON (access_token, refresh_token, token_type, expires_in 등) |

#### 3.1.2 사용자 정보 조회

| 항목 | 내용 |
|------|------|
| URL | `https://openapi.naver.com/v1/nid/me` |
| Method | GET |
| 헤더 | Authorization: Bearer {access_token} |
| 응답 | JSON (id, nickname, profile_image, email 등) |

---

### 3.2 구글 로그인 (Google OAuth 2.0)

| 항목 | 내용 |
|------|------|
| 연동 대상 | Google (oauth2.googleapis.com, www.googleapis.com) |
| 방향 | 본 시스템 → 구글 (송신) |
| 구현 위치 | `auth-service` / `GoogleAuthService` |
| 전송 방식 | RestTemplate, application/x-www-form-urlencoded / Bearer Token |

#### 3.2.1 액세스 토큰 발급

| 항목 | 내용 |
|------|------|
| URL | `https://oauth2.googleapis.com/token` |
| Method | POST |
| 요청 | grant_type=authorization_code, client_id, client_secret, code, redirect_uri |
| 응답 | JSON (access_token, refresh_token, token_type, expires_in 등) |

#### 3.2.2 사용자 정보 조회

| 항목 | 내용 |
|------|------|
| URL | `https://www.googleapis.com/oauth2/v2/userinfo` |
| Method | GET |
| 헤더 | Authorization: Bearer {access_token} |
| 응답 | JSON (id, email, name, picture 등) |

---

### 3.3 카카오 로그인 (Kakao OAuth 2.0)

| 항목 | 내용 |
|------|------|
| 연동 대상 | Kakao (kauth.kakao.com, kapi.kakao.com) |
| 방향 | 본 시스템 → 카카오 (송신) |
| 구현 위치 | `auth-service` / `KakaoAuthService` |
| 전송 방식 | RestTemplate, application/x-www-form-urlencoded / Bearer Token |

#### 3.3.1 액세스 토큰 발급

| 항목 | 내용 |
|------|------|
| URL | `https://kauth.kakao.com/oauth/token` |
| Method | POST |
| 요청 | grant_type=authorization_code, client_id, client_secret, code, redirect_uri |
| 응답 | JSON (access_token, refresh_token, token_type, expires_in 등) |

#### 3.3.2 사용자 정보 조회

| 항목 | 내용 |
|------|------|
| URL | `https://kapi.kakao.com/v2/user/me` |
| Method | GET |
| 헤더 | Authorization: Bearer {access_token} |
| 응답 | JSON (id, kakao_account, properties 등) |

---

## 4. 수신 인터페이스 (본 시스템이 제공하는 외부 연동용 API)

| No | 연동 대상 | 용도 | 비고 |
|----|-----------|------|------|
| (없음) | - | - | 외부 시스템이 본 애플리케이션을 호출하는 수신 API는 없음. (프론트·클라이언트용 REST API는 [api-list.md](./api-list.md) 참고) |

---

## 5. 공통 사항

- **인증**: OAuth 2.0 Authorization Code 플로우. 클라이언트에서 인가 코드를 받아 백엔드로 전달한 뒤, 백엔드에서 외부 토큰/사용자정보 API를 호출합니다.
- **설정**: 각 소셜별 `client.id`, `client.secret`, `redirect.uri` 등은 설정 파일(환경 변수)로 관리합니다.
- **에러 처리**: 외부 API 오류 시 로그인 실패로 처리하며, 상세 오류는 로그에 기록합니다.

---

## 6. 참고

- API 목록(내부 제공 API): [api-list.md](./api-list.md)
- API 상세 명세: [api-specification.md](./api-specification.md)
