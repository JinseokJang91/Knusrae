# Knusrae AWS 배포 작업 계획서

## 1. 문서 목적

이 문서는 아래 두 자료를 기준으로 작성되었다.

- **아키텍처 정의서**: Knusrae의 목표 서비스 구조와 기술 스택
- **현재 AWS 구성 문서**: 현재까지 생성된 AWS 리소스와 설정 현황

이 문서의 목적은 다음 3가지다.

1. 현재 구성 상태를 점검한다.
2. 앞으로 해야 할 작업을 **우선순위와 순서대로** 정리한다.
3. 인프라 초보자도 놓치지 않도록 **체크리스트 기반 실행 순서**를 제공한다.

---

## 2. 먼저 확인한 핵심 사실

## 2.1 아키텍처 기준점

아키텍처 문서 기준으로 Knusrae는 다음 구조를 전제로 한다.

- 프론트엔드: **Vue 3 SPA**
- 백엔드: **Spring Boot 기반 서비스들**
- 데이터베이스: **PostgreSQL**
- 캐시: **Redis**
- 인증: **OAuth + JWT**
- 사용자/브라우저 접근: **HTTPS 기반 웹 서비스**

즉, 배포 관점에서 최소한 아래 요소가 자연스럽게 따라와야 한다.

- 프론트 배포 위치
- 백엔드 실행 환경
- DB 엔진 일치 여부
- Redis 필요 여부
- 도메인/HTTPS 처리
- 환경변수/시크릿 주입 방식
- 로그/모니터링/백업

---

## 2.2 현재 AWS 구성에서 확인된 사항

현재는 다음이 준비되어 있다.

- EC2 1대 생성 완료
- S3 버킷 생성 완료
- RDS 생성 완료
- Secrets Manager 생성 완료
- IAM Role 생성 및 EC2 부여 완료
- Elastic IP 할당 완료
- KMS는 삭제 처리

즉, **기본 리소스 뼈대는 어느 정도 마련된 상태**다.

다만, 실제 서비스 배포 관점에서는 아직 검토 및 보완이 필요한 항목이 꽤 있다.

---

## 2.3 가장 먼저 보이는 중요한 불일치 / 누락

### A. DB 엔진 일치 여부 확인

아키텍처 문서는 **PostgreSQL** 로 수정된 상태이며,
현재 AWS에는 **PostgreSQL RDS** 가 생성되어 있다.

즉, 아키텍처와 AWS 리소스는 동일한 엔진으로 정렬된 상태다. 다만 아래를 한 번 더 확인하는 것이 좋다.

- **실제 애플리케이션 코드·설정이 PostgreSQL 기준인지** (드라이버, dialect, SQL 문법)
- application.yml, 마이그레이션 스크립트, 운영 점검 문서가 모두 PostgreSQL 기준인지

만약 과거에 MySQL 기준으로 개발된 코드가 남아 있다면, RDS를 MySQL로 새로 만들거나 코드를 PostgreSQL에 맞게 수정해야 한다. 현재는 **아키텍처·AWS 모두 PostgreSQL** 이므로, 코드와 설정만 동일하게 맞추면 된다.

### B. Redis가 아직 없음

아키텍처 문서에는 Redis가 포함되어 있는데 현재 AWS 구성에는 없다.

Redis가 실제 코드에서 사용 중이라면 다음 중 하나가 필요하다.

- ElastiCache for Redis 생성
- 또는 임시로 EC2 내부 Redis 설치

운영 안정성과 관리 편의성을 생각하면 **ElastiCache** 가 더 적절하다.

### C. 프론트 배포 방식이 아직 미정 또는 미구현

Vue 3 SPA는 보통 아래 둘 중 하나로 배포한다.

- **S3 + CloudFront**
- 또는 **EC2/Nginx** 로 정적 파일 서빙

현재 S3는 만들어져 있지만,
문서상으로는 **정적 웹 호스팅 / CloudFront / 배포 구조** 가 아직 정리되지 않았다.

### D. HTTPS 운영 구성이 아직 완성되지 않음

EC2 보안 그룹에 443은 열려 있지만,
실제 HTTPS 운영을 위해서는 보통 아래가 필요하다.

- 도메인
- ACM 인증서
- ALB 또는 CloudFront 또는 Nginx + Certbot

즉, **포트 443 개방 = HTTPS 완료** 는 아니다.

### E. 운영 보안이 아직 과권한 상태

EC2 IAM Role 에 아래와 같은 강한 권한이 포함되어 있었다.

- AmazonEC2FullAccess
- AmazonS3FullAccess
- AWSKeyManagementServicePowerUser

초기 실습 단계에서는 가능하지만,
실서비스 배포 전에는 **최소 권한 원칙(Least Privilege)** 으로 줄이는 것이 좋다.

### F. 배포 자동화/운영 자동화가 아직 없음

현재 문서 기준으로는 다음이 아직 보이지 않는다.

- CI/CD
- 로그 수집 체계
- 모니터링/알람
- 백업/복구 전략
- 장애 대응 절차

즉, 지금 상태는 **리소스 생성 단계** 에 가깝고,
**실서비스 운영 준비 단계** 는 아직 남아 있다.

---

## 3. 전체 진행 전략

지금부터의 작업은 아래 순서로 진행하는 것이 가장 안전하다.

1. **아키텍처-인프라 불일치 해소**
2. **네트워크/보안 최소 정리**
3. **DB/시크릿/런타임 연결 완성**
4. **애플리케이션 실제 배포 가능 상태 만들기**
5. **프론트/도메인/HTTPS 연결**
6. **운영 안정화(로그, 모니터링, 백업, 자동배포)**

핵심은,
**“서버는 켜져 있는데 서비스는 불안정한 상태”** 를 피하고,
**“작지만 배포 흐름이 완성된 상태”** 를 먼저 만드는 것이다.

---

## 4. 권장 작업 순서 (실행 순서 기준)

## STEP 0. 배포 기준 확정

가장 먼저 아래를 확정해야 한다.

### 해야 할 일

- 실제 백엔드가 사용하는 DB가 **PostgreSQL로 확정되어 있는지** 및 코드·설정과 일치하는지 확인
- Redis가 **실제로 필요한지 코드 기준으로 확정**
- 프론트 배포를
  - S3 + CloudFront 로 할지,
  - EC2 + Nginx 로 할지 결정
- 백엔드 배포 단위를
  - 하나의 EC2에 단일 프로세스로 올릴지,
  - 여러 서비스 JAR를 각각 띄울지,
  - Docker Compose를 쓸지 결정

### 추천

현재가 토이프로젝트이고 처음 인프라를 구성하는 단계라면,
우선 아래 형태가 가장 무난하다.

- 프론트: **S3 + CloudFront**
- 백엔드: **EC2 + Nginx + Spring Boot JAR(또는 Docker Compose)**
- DB: **RDS**
- 시크릿: **Secrets Manager**
- 캐시: **필요 시 ElastiCache Redis**

### 완료 기준

- “우리 서비스의 실제 배포 구조” 를 한 문단으로 설명할 수 있어야 함
- DB 엔진(PostgreSQL)이 코드·아키텍처 문서·AWS RDS 모두 일치해야 함

---

## STEP 1. 현재 리소스 재점검 및 정리

## 1-1. EC2 점검

### 확인 항목

- Elastic IP가 실제 EC2에 연결되어 있는지
- 보안 그룹이 의도대로 적용되어 있는지
- Ubuntu 접속이 안정적으로 되는지
- 디스크 8GB가 충분한지
- 시간대/로케일 설정이 적절한지

### 권장 조치

- 운영 로그, 빌드 산출물, Docker 이미지 등을 고려하면 **8GB는 다소 빠듯할 수 있음**
- 가능하면 **EBS** 볼륨을 **20GB 이상** 으로 보는 것이 안전함  
  - **EBS(Elastic Block Store)**: EC2 인스턴스에 붙이는 **디스크(블록 스토리지)**. OS·앱·로그·Docker 이미지 등이 저장된다. 루트 볼륨이 8GB면 여유가 적으므로, 볼륨 크기 확장 또는 인스턴스 재생성 시 20GB 이상으로 설정하는 것을 권장한다.
- SSH는 반드시 **내 IP만 허용 유지**
- RDS 포트 5432(PostgreSQL)를 EC2 보안 그룹 기준으로만 허용하도록 재확인

### 완료 기준

- EC2 접속, 재부팅, 디스크 여유, 보안 그룹 상태 모두 점검 완료

## 1-2. IAM 재정리

### 해야 할 일

현재 EC2 Role 권한을 최소화한다.

### 권장 방향

- S3 접근: 특정 버킷에 대한 필요한 권한만 허용
- Secrets Manager 접근: 특정 Secret 읽기만 허용
- KMS를 이미 삭제했다면 관련 과권한 제거
- EC2/RDS FullAccess 같은 관리 권한은 EC2 실행 역할에서는 제거

### 추천 정책 예시 방향

- `s3:GetObject`, `s3:PutObject`, `s3:ListBucket` (필요 버킷 한정)
- `secretsmanager:GetSecretValue` (특정 secret 한정)

### IAM 콘솔에서 최소 권한 정책 적용하는 방법

**AmazonS3FullAccess** 같은 관리형 정책은 “모든 S3 버킷에 대한 전체 권한”을 주므로, 최소 권한을 쓰려면 **직접 정책을 만들어** 붙여야 한다.

1. **AWS 콘솔** → **IAM** → **역할(Roles)** → EC2에 부여한 역할 선택
2. **권한 추가(Add permissions)** → **인라인 정책 생성(Create inline policy)** 선택
3. **JSON** 탭 선택 후, 아래처럼 필요한 권한만 넣은 정책을 작성한다. (버킷 이름·Secret ARN은 실제 값으로 바꾼다.)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3KnusraeBuckets",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::knusrae-app-assets",
        "arn:aws:s3:::knusrae-app-assets/*",
        "arn:aws:s3:::knusrae-frontend",
        "arn:aws:s3:::knusrae-frontend/*"
      ]
    },
    {
      "Sid": "SecretsManagerKnusrae",
      "Effect": "Allow",
      "Action": "secretsmanager:GetSecretValue",
      "Resource": "arn:aws:secretsmanager:ap-northeast-2:계정ID:secret:knusrae/prod-*"
    }
  ]
}
```

4. **다음** → 정책 이름 입력(예: `KnusraeEC2MinimalPolicy`) → **정책 생성**
5. 기존 **AmazonS3FullAccess** 등 과권한 정책은 해당 역할에서 **권한 제거(Detach)** 한다.

#### Resource 항목에 넣어야 할 값

| 리소스 종류 | 형식 | 넣을 값 예시 |
|-------------|------|--------------|
| **S3 버킷(목록)** | `arn:aws:s3:::버킷이름` | `arn:aws:s3:::knusrae-app-assets` |
| **S3 버킷 내 객체** | `arn:aws:s3:::버킷이름/*` | `arn:aws:s3:::knusrae-app-assets/*` |
| **Secrets Manager** | `arn:aws:secretsmanager:리전:계정ID:secret:시크릿이름-*` | `arn:aws:secretsmanager:ap-northeast-2:123456789012:secret:knusrae/prod-AbCdEf` |

- **버킷 이름**: 실제 생성한 S3 버킷 이름. 글로벌에서 유일해야 하므로 `knusrae-app-assets` 대신 `knusrae-app-assets-계정ID` 처럼 붙일 수 있음.
- **계정 ID**: AWS 콘솔 우측 상단 계정 메뉴 → **계정 ID** 에서 확인. 12자리 숫자.
- **리전**: Secrets Manager를 둔 리전(예: `ap-northeast-2`). RDS·EC2와 같은 리전 권장.
- **시크릿 ARN**: Secrets Manager 콘솔에서 해당 시크릿 선택 시 **ARN** 필드에 전체 ARN이 보임. 마지막에 랜덤 접미사가 붙으므로 정책에는 `knusrae/prod-*` 처럼 와일드카드(`*`)를 쓰면 해당 이름으로 시작하는 시크릿만 허용 가능.

정책 검색: `s3:GetObject` 같은 **개별 권한(Action)** 은 AWS 관리형 정책 목록에는 “이름으로” 나오지 않는다. **고객 관리형 정책** 또는 **인라인 정책**을 만들 때 JSON에 직접 `Action`, `Resource`를 적어서 사용한다. IAM 정책 편집기에서 **정책 시각적 편집**으로 서비스(S3, Secrets Manager)를 고른 뒤, 필요한 작업(GetObject, PutObject 등)과 리소스(버킷 ARN)만 선택해도 된다.

### 완료 기준

- 애플리케이션 실행에 필요한 권한만 남아 있음

## 1-3. S3 목적 재정의

S3가 무엇을 위한 버킷인지 분명히 해야 한다.

가능한 용도:

- 사용자 업로드 이미지 저장
- 프론트 정적 파일 배포
- 로그/백업 저장

버킷 하나에 다 넣기보다는 목적 분리가 좋다.

### 추천

- `knusrae-app-assets` : 사용자 업로드
- `knusrae-frontend` : 프론트 정적 배포
- 필요 시 별도 백업 버킷

### S3 버킷 정책 예시

버킷별로 **버킷 정책(Bucket policy)** 을 두면, “누가 이 버킷에만 접근할 수 있는지”를 S3 단에서 제한할 수 있다. (IAM 역할 정책과 함께 사용.)

#### 이미지 저장용 버킷 (`knusrae-app-assets`)

- **목적**: 사용자 업로드 이미지 저장. 외부에 직접 노출하지 않고, **백엔드(EC2 역할)만** 읽기·쓰기.
- **퍼블릭 액세스**: 차단 유지. 버킷 정책으로 EC2 역할만 허용.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowEC2RoleOnly",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::계정ID:role/EC2에부여한역할이름"
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::knusrae-app-assets",
        "arn:aws:s3:::knusrae-app-assets/*"
      ]
    }
  ]
}
```

- `계정ID`: 12자리 AWS 계정 ID.
- `EC2에부여한역할이름`: EC2 인스턴스에 연결한 IAM 역할 이름(예: `KnusraeEC2Role`).
- 이 버킷은 **퍼블릭 액세스 차단**이 켜져 있어야 하며, 위 정책으로 해당 역할만 접근 가능.

#### 프론트엔드 배포용 버킷 (`knusrae-frontend`)

- **목적**: Vue 빌드 산출물(HTML, JS, CSS) 저장. **CloudFront만** 이 버킷에서 객체를 읽어 사용자에게 전달.
- **퍼블릭 액세스**: 차단 유지. CloudFront는 OAC(Origin Access Control)로 버킷에 접근하므로, 버킷 정책에서 CloudFront 서비스만 허용.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCloudFrontOAC",
      "Effect": "Allow",
      "Principal": {
        "Service": "cloudfront.amazonaws.com"
      },
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::knusrae-frontend/*",
      "Condition": {
        "StringEquals": {
          "AWS:SourceArn": "arn:aws:cloudfront::계정ID:distribution/배포ID"
        }
      }
    }
  ]
}
```

- `계정ID`: 12자리 AWS 계정 ID.
- `배포ID`: CloudFront 배포 생성 후 **배포 ID**(예: `E1234ABCD5678`). 콘솔 **CloudFront** → 해당 배포 → 상단 **ID** 에서 확인.
- CloudFront 배포 생성 시 **Origin** 으로 이 S3 버킷을 지정하고, **Origin access** 를 **Origin access control (OAC)** 로 설정한 뒤, 위 정책을 적용해야 한다. OAC 생성 시 안내되는 정책을 그대로 써도 된다.

### 완료 기준

- S3 버킷 용도가 명확히 정의됨

---

## STEP 2. 데이터 계층 확정

## 2-1. RDS 엔진 일치 확인

아키텍처와 AWS가 모두 **PostgreSQL** 로 정렬된 상태이므로, 아래만 점검하면 된다.

### 점검 항목 (PostgreSQL 기준)

- Spring Boot 의존성에 PostgreSQL 드라이버 사용 중인지
- JPA/Hibernate dialect 설정이 PostgreSQL 기준인지
- SQL 문법이 PostgreSQL에서 문제 없는지
- application.yml·운영 문서가 PostgreSQL 연결 정보를 사용하는지

### (참고) 만약 MySQL로 통일하는 경우

과거에 MySQL 기준으로 개발된 코드만 남아 있는 경우에만 고려한다. 이 경우 기존 PostgreSQL RDS 정리 후 MySQL RDS를 새로 생성하고, 애플리케이션 설정·마이그레이션을 MySQL 기준으로 변경한다. **현재는 아키텍처가 PostgreSQL로 확정되었으므로, 코드·설정을 PostgreSQL에 맞추는 방향을 권장한다.**

### 완료 기준

- 애플리케이션 로컬/배포 환경의 DB 엔진(PostgreSQL)이 완전히 일치함

## 2-2. DB 접속 정책 정리

### 해야 할 일

- RDS를 퍼블릭 오픈하지 않고 EC2에서만 접근 가능하게 유지
- DB 보안 그룹은 **EC2 보안 그룹 소스 참조 방식** 으로 제한
- DB 계정은 마스터 계정 외에 애플리케이션 전용 계정 분리 고려

### 추천

- 마스터 계정은 관리자 작업 전용
- 애플리케이션은 별도 사용자 계정 사용
- 운영에서는 DBeaver 직접 접속보다 SSH 터널 방식 유지

### 완료 기준

- DB 접근 경로가 “EC2 → RDS” 중심으로 제한됨

## 2-3. 마이그레이션 도입 권장

현재는 문서상 DB 생성까지만 보이는데,
실제 배포에서는 스키마 관리가 중요하다.

### 추천

- Flyway 또는 Liquibase 도입

### 이유

- 로컬/개발/운영 DB 스키마를 일관되게 관리 가능
- 배포 시 누락 방지
- 롤백 및 변경 이력 추적이 쉬움

### 완료 기준

- 애플리케이션 시작 시 스키마가 재현 가능하거나,
  최소한 버전 관리된 SQL 마이그레이션이 존재함

## 2-4. Redis 필요 여부 확정

### 점검 질문

- 캐시 사용 코드가 이미 존재하는가?
- 로그인/세션/토큰 블랙리스트/인기 데이터 캐시에 Redis가 필요한가?

### 분기

- **필수라면**: ElastiCache Redis 생성
- **아직 선택 사항이라면**: 1차 배포에서는 제외 가능

### 추천

토이프로젝트 첫 배포라면,
Redis 의존 기능이 핵심이 아니라면 **1차 배포에서는 제외** 하고,
필수 기능만 살리는 것도 좋은 전략이다.

### 완료 기준

- Redis가 필요한지/불필요한지 코드 기준으로 명확히 결정됨

---

## STEP 3. 시크릿/환경변수 주입 구조 완성

현재 Secrets Manager 에 환경변수를 저장한 것은 좋다.
하지만 중요한 것은 **애플리케이션 실행 시 실제로 주입되는지** 다.

## 3-1. 환경변수 항목 정리

### 일반적으로 필요한 항목

- DB host
- DB port
- DB name
- DB username
- DB password
- JWT secret
- OAuth client id / secret / redirect uri
- S3 bucket name
- AWS region
- Spring profile
- Redis host/port (사용 시)

### 해야 할 일

- 환경변수 목록을 표로 정리
- dev / prod 구분 여부 결정
- Secrets Manager secret 구조를 일관되게 정리

### 완료 기준

- 어떤 값이 어디에 있는지 한눈에 보임

## 3-2. Secrets Manager 주입 방식 구현

Secrets Manager 는 “저장소”일 뿐이고,
애플리케이션이 자동으로 읽도록 별도 구성이 필요하다.

### 가능한 방식

1. **애플리케이션 시작 스크립트에서 AWS CLI로 secret 조회 후 export**  
   호스트(또는 진입점 스크립트)에서 `aws secretsmanager get-secret-value` 실행 → 값을 `.env` 등으로 저장 후 앱에 전달.
2. **Spring Cloud AWS 또는 SDK를 통해 앱 내부에서 조회**  
   Spring Boot 기동 시 Secrets Manager API를 호출해 시크릿을 읽고, 설정으로 사용.
3. **Docker 실행 시 주입 스크립트 사용**  
   `docker-compose up` 전에 또는 컨테이너 진입점에서 시크릿 조회 → `environment` / `env_file` 로 컨테이너에 전달.

### 프론트(S3+CloudFront) / 백엔드(Docker Compose) 구성일 때 권장

백엔드를 **Docker Compose**로 띄우는 경우, **방식 2 (Spring Cloud AWS 또는 SDK로 앱 내부 조회)** 를 우선 권장한다.

| 비교 | 방식 1·3 (스크립트 → env 파일) | 방식 2 (앱 내부에서 조회) |
|------|--------------------------------|----------------------------|
| **Docker Compose** | 호스트에서 스크립트 실행 후 `.env` 생성 → `env_file` 로 전달. 스크립트·env 파일 관리 필요. | EC2 인스턴스 프로필(IAM Role)을 컨테이너에서 쓰게 하면, 컨테이너 안 앱이 바로 Secrets Manager 호출 가능. 호스트 스크립트 불필요. |
| **보안** | `.env` 파일이 디스크에 잠깐 남을 수 있음. | 시크릿을 파일로 쓰지 않고 메모리에서만 사용 가능. |
| **구성** | 배포 스크립트·entrypoint에서 AWS CLI 호출 필요. | 앱에 Spring Cloud AWS 또는 AWS SDK 의존성 추가 후, 설정(예: `spring.config.import`) 또는 초기화 코드에서 조회. |
| **서비스별 시크릿** | 한 번 조회한 값을 여러 컨테이너에 공유할 수 있음. | 서비스마다 필요한 시크릿만 앱에서 조회 가능. |

**권장 이유**: EC2에서 Docker Compose를 쓰면 EC2 IAM Role을 컨테이너에 넘겨 줄 수 있다. 그럴 때 각 Spring Boot 서비스가 시작 시점에 Secrets Manager를 직접 호출하면, 별도 스크립트나 env 파일 없이 동작하고, 12‑factor와 컨테이너 환경에 잘 맞는다.  
선택적으로 **방식 3**도 가능하다. “컨테이너 기동 전 한 번만 스크립트로 시크릿 조회 → `docker-compose` 의 `env_file` 에 넣어서 전달”하면, 앱 코드에는 AWS 의존성을 넣지 않아도 되지만, 스크립트와 env 파일을 유지·보안해야 한다.

### 초보자 기준 추천 (JAR 직접 실행 시)

Docker가 아닌 **JAR + systemd** 로만 쓸 때는, 처음에는 아래처럼 단순하게 가도 된다.

- **배포 스크립트에서 secret 조회 → env 파일 생성 → 앱 실행**

예시 흐름:

1. EC2에서 AWS CLI 사용 가능 상태 확인
2. `aws secretsmanager get-secret-value` 로 secret 조회
3. 필요한 값들을 `.env` 또는 systemd EnvironmentFile 로 변환
4. Spring Boot 실행

### 완료 기준

- 서버 재시작 후에도 수동 입력 없이 앱이 정상 기동됨

---

## STEP 4. EC2 런타임 환경 구성

## 4-1. 기본 런타임 설치

### 설치 대상 예시

- Java 21
- Nginx
- Git (필요 시)
- unzip / curl / jq
- AWS CLI
- Docker / Docker Compose (도입 시)

### 완료 기준

- 백엔드 애플리케이션을 실행할 수 있는 런타임 준비 완료

## 4-2. 애플리케이션 실행 방식 선택

### 선택지 A: JAR 직접 실행

- systemd로 서비스 등록
- Nginx가 reverse proxy

장점:

- 단순함
- 처음 구축하기 쉬움

### 선택지 B: Docker Compose

- 서비스 컨테이너로 실행
- 배포 재현성이 좋음

장점:

- 환경 일관성 높음
- 향후 확장 쉬움

### 추천

처음이면 **JAR + systemd + Nginx** 가 가장 직관적이다.
다만 이미 Docker에 익숙하면 Docker Compose도 좋다.

### 완료 기준

- 서버 부팅 시 애플리케이션 자동 실행 가능

## 4-3. 프로세스 분리 전략

아키텍처 문서상 백엔드는 4개 서비스다.

여기서 반드시 확인해야 한다.

- 실제로도 4개의 독립 실행 애플리케이션인가?
- 아니면 현재는 하나의 통합 백엔드로 운영하는가?

### 만약 4개 독립 서비스라면

필요한 것:

- 각 서비스별 포트 분리
- systemd 서비스 4개 또는 컨테이너 4개
- Nginx 라우팅 설정
- 공통 설정/로그 분리

### 만약 현재는 단일 서비스로 통합 배포라면

- 이 경우 현재 배포 구조 기준으로 문서를 맞추는 것이 중요

### 완료 기준

- 실제 배포 단위가 명확히 정리됨

---

## STEP 5. 백엔드 배포 구성 완성

## 5-1. 애플리케이션 빌드 및 배포 절차 만들기

### 최소 절차 예시

1. 코드 pull
2. 빌드 수행
3. 산출물 생성(JAR)
4. 기존 프로세스 중지
5. 새 버전 배포
6. 헬스체크
7. 실패 시 이전 버전 롤백

### 추천 폴더 구조 예시

```text
/home/ubuntu/knusrae/
  ├── app/
  ├── releases/
  ├── scripts/
  ├── logs/
  └── current -> releases/20260314xxxx/
```

### 완료 기준

- 배포 절차가 반복 가능하고 문서화되어 있음

## 5-2. systemd 서비스 등록

### 목적

- 서버 재부팅 후 자동 실행
- 로그/재시작 정책 관리

### 권장 항목

- `Restart=always`
- 환경파일 참조
- 실행 사용자 명시
- 로그 경로 확인

### 완료 기준

- `systemctl status` 로 앱 상태 확인 가능

## 5-3. Nginx reverse proxy 구성

### 역할

- 외부 80/443 요청 수신
- 내부 Spring Boot 포트로 전달
- 정적 응답/압축/보안 헤더 처리 가능

### 예시 구조

- 사용자 → Nginx(80/443) → Spring Boot(8080 등)

### 완료 기준

- 퍼블릭 도메인 또는 IP로 API 호출 가능

---

## STEP 6. 프론트엔드 배포 구성

Vue SPA 기준으로는 프론트 배포를 별도 정리해야 한다.

## 6-1. 프론트 배포 위치 결정

### 추천 1: S3 + CloudFront

장점:

- SPA 정적 배포에 적합
- 성능/캐시/HTTPS 구성 편리

### 대안 2: EC2 + Nginx

장점:

- 단일 서버 운영이 쉬움
- 초기에 이해하기 쉬움

### 추천

프론트와 백엔드를 분리해서 보고 싶다면 **S3 + CloudFront** 가 더 좋다.

## 6-2. 환경별 API 엔드포인트 연결

프론트에서 아래를 반드시 점검해야 한다.

- API Base URL
- OAuth Redirect URL
- 정적 리소스 URL
- CORS 정책

### 완료 기준

- 브라우저에서 로그인, API 호출, 페이지 새로고침 모두 정상 동작

## 6-3. SPA 라우팅 처리

Vue Router history 모드라면,
새로고침 시 404가 나지 않도록 처리해야 한다.

### S3 + CloudFront 인 경우

- error document / rewrite 설정 필요

### Nginx 인 경우

- `try_files` 설정 필요

### 완료 기준

- 직접 URL 접근 및 새로고침 시 정상 표시

---

## STEP 7. 도메인 / HTTPS / 외부 공개 구성

## 7-1. 도메인 연결

### 해야 할 일

- 도메인 구매 또는 보유 도메인 사용
- DNS 설정
- 프론트/백엔드 서브도메인 전략 결정

### 추천 예시

- `www.knusrae.com` → 프론트
- `api.knusrae.com` → 백엔드

## 7-2. HTTPS 인증서 구성

### 추천 방식

#### 프론트가 S3 + CloudFront 인 경우
- ACM 인증서 + CloudFront 연결

#### 백엔드가 ALB/Nginx 인 경우
- ALB + ACM
- 또는 Nginx + Certbot

### 추천

처음에는 아래 둘 중 하나로 단순화 가능

- 프론트: CloudFront + ACM
- 백엔드: Nginx + Certbot

혹은

- 백엔드도 ALB 앞단 배치

## 7-3. CORS / OAuth Redirect URI 최종 정리

로그인 기능이 있으므로 특히 중요하다.

### 점검 항목

- 네이버/구글/카카오 Redirect URI에 실제 운영 도메인 등록
- 백엔드 CORS 허용 origin 수정
- 프론트 환경변수 수정

### 완료 기준

- 운영 도메인에서 소셜 로그인까지 정상 동작

---

## STEP 8. 보안 강화

## 8-1. 보안 그룹 최소화

### 점검 기준

- SSH: 내 IP만 허용
- DB 포트: 공개 금지
- 백엔드 내부 포트(8080 등): 외부 공개 금지
- 외부 공개는 80/443만 유지

### 완료 기준

- 외부에 불필요한 포트가 노출되지 않음

## 8-2. 애플리케이션 계정/권한 분리

### 권장

- DB 마스터 계정과 앱 계정 분리
- 관리자 콘솔 IAM 사용자와 앱 실행 IAM Role 분리
- 운영용 AWS 사용자에 MFA 적용

### 완료 기준

- 사람 권한과 서버 권한이 분리됨

## 8-3. 시크릿/민감정보 관리

### 해야 할 일

- `.env` 를 Git에 올리지 않기
- PEM 키 권한 제한
- 로그에 secret 값 출력되지 않게 점검
- OAuth secret / JWT secret 노출 여부 확인

### 완료 기준

- 민감정보가 코드/로그/저장소에 남지 않음

---

## STEP 9. 운영 관측성(로그/모니터링/알람)

## 9-1. 로그 수집

### 최소 기준

- Nginx access/error 로그
- Spring Boot 애플리케이션 로그
- 배포 로그

### 추천

처음에는 파일 로그로 시작해도 되지만,
가능하면 **CloudWatch Logs** 연동을 검토한다.

### 완료 기준

- 장애 시 로그 위치를 즉시 알 수 있음

## 9-2. 모니터링

### 확인 대상

- EC2 CPU / 메모리 / 디스크
- RDS CPU / Storage / Connection
- 애플리케이션 헬스체크

### 추천

- CloudWatch 기본 메트릭 확인
- 디스크/메모리는 CloudWatch Agent 설치 고려

### 완료 기준

- 서버/DB 상태를 숫자로 볼 수 있음

## 9-3. 알람

### 최소 알람 예시

- EC2 CPU 과다
- RDS 스토리지 부족
- 상태 확인 실패
- 애플리케이션 헬스체크 실패

### 완료 기준

- 장애 징후를 사전에 받을 수 있음

---

## STEP 10. 백업 / 복구 / 운영 안정화

## 10-1. RDS 백업 정책 확인

### 점검 항목

- 자동 백업 활성화 여부
- 보존 기간
- 스냅샷 수동 생성 전략

### 완료 기준

- 장애 시 DB 복구 방법을 알고 있음

## 10-2. EC2 복구 관점 정리

EC2는 되도록 “언제든 다시 만들 수 있게” 운영하는 것이 좋다.

### 해야 할 일

- 서버 설정 스크립트화
- 배포 스크립트 보관
- Nginx/systemd 설정 백업

### 완료 기준

- 새 EC2에서도 재현 가능한 수준의 문서/스크립트 보유

## 10-3. S3 백업/수명주기

### 필요 시

- 사용자 업로드 자산 백업 정책
- 수명주기 정책
- 버전 관리 여부 검토

### 완료 기준

- 파일 데이터 보호 전략이 있음

---

## STEP 11. 배포 자동화(CI/CD)

이 단계는 필수는 아니지만, 수동 배포 실수를 줄여준다.

## 11-1. 가장 단순한 자동화

### 후보

- GitHub Actions → EC2 배포
- CodeDeploy / CodePipeline

### 초보자 추천

- **GitHub Actions + SSH 배포** 또는
- **GitHub Actions + S3/CloudFront 프론트 배포**

## 11-2. 백엔드 자동배포 최소 흐름

1. main 브랜치 push
2. 테스트/빌드
3. EC2 접속
4. 새 JAR 업로드 또는 pull
5. 서비스 재시작
6. 헬스체크

## 11-3. 프론트 자동배포 최소 흐름

1. build
2. S3 sync
3. CloudFront invalidation

### 완료 기준

- 반복 배포가 클릭 몇 번 또는 push 한 번으로 가능

---

## 5. 우선순위 기준 즉시 해야 할 일 (중요도 순)

아래는 “지금 당장” 기준의 우선순위다.

## 1순위: 반드시 먼저

- [v] **DB 엔진 일치 여부 확정** (아키텍처·코드·AWS 모두 PostgreSQL 일치 확인)
- [v] **실제 배포 구조 확정** (프론트/백엔드/Redis 포함 여부)
- [ ] **Secrets Manager 값을 앱이 실제 읽도록 주입 구조 구현**
- [ ] **EC2에서 애플리케이션 수동 기동 성공**
- [ ] **Nginx reverse proxy까지 연결**

## 2순위: 외부 공개 전 필수

- [ ] 도메인 연결
- [ ] HTTPS 적용
- [ ] OAuth redirect URI 운영 주소 반영
- [ ] CORS 설정 반영
- [ ] 보안 그룹 최소화
- [ ] IAM Role 최소 권한화

## 3순위: 안정적 운영을 위해 권장

- [ ] CloudWatch 로그/모니터링
- [ ] RDS 백업 점검
- [ ] 배포 스크립트 정리
- [ ] CI/CD 구축
- [ ] Redis 또는 ElastiCache 도입 여부 확정

---

## 6. 추천 최종 배포 형태 (현재 상황 기준)

처음 AWS 배포를 완성하는 목적이라면, 아래 구성을 추천한다.

### 권장 1차 운영안

#### 프론트
- Vue 빌드 산출물 → **S3 + CloudFront**

#### 백엔드
- Spring Boot → **EC2 1대**
- Nginx reverse proxy
- systemd로 자동 실행

#### 데이터
- **RDS** (실제 코드와 맞는 엔진으로 확정)
- Redis는 필요할 때만 추가

#### 비밀값
- **Secrets Manager**
- EC2 IAM Role 통해 읽기

#### 네트워크
- Elastic IP 또는 도메인
- HTTPS 적용

#### 운영
- CloudWatch 모니터링
- RDS 자동 백업
- 이후 GitHub Actions 추가

이 구성이 가장 복잡도를 낮추면서도,
실제 서비스 배포 흐름을 경험하기 좋다.

---

## 7. 실제 작업 체크리스트

아래 체크리스트 순서대로 진행하면 된다.

## Phase 1. 설계 확정

- [v] DB 엔진 최종 확정
  - postgreSQL
- [v] Redis 필요 여부 확정
  - 현재 미구현 상태
  - 도입 예정
  - 확인 필요 사항 → **답변**: 토큰 블랙리스트는 **현재 PostgreSQL(DB)** 로 구현되어 있음 (`TokenBlacklist` 엔티티, `token_blacklist` 테이블, JPA 저장). Redis는 코드베이스에 미사용. 따라서 **우선 현재 버전으로 배포해도 됨**. 추후 Redis 도입 시 `TokenBlacklistChecker` 구현체만 Redis 기반으로 추가/교체하면 되며, 기존 엔티티·테이블은 이력용 유지 또는 제거 선택 가능.
- [v] 프론트 배포 위치 확정
  - S3 + CloudFront
- [v] 백엔드 실행 방식(JAR/Docker) 확정
  - docker-compose
- [v] 서비스가 단일 백엔드인지, 4개 독립 서비스인지 확정
  - 모노레포 아키텍처 구조로 port만 분리되어 있음
  - 확인 필요 사항 → **답변**: **단일 백엔드** = 하나의 JAR·하나의 프로세스가 모든 API를 한 포트(예: 8080)로 제공. **독립 서비스** = 서비스별로 다른 JAR·다른 프로세스·다른 포트(auth 8081, member 8083, cook 8082)에서 동작. Knusrae는 **독립 서비스 3개**(auth-service, member-service, cook-service) 구조이며, common-service는 공용 라이브러리. 문서의 "4개"는 common 포함 또는 아키텍처 문서 기준일 수 있음.

## Phase 2. AWS 리소스 정리

- [ ] EC2 디스크/접속/보안 그룹 점검
- [ ] RDS 보안 그룹 재점검
- [v] IAM Role 최소 권한화
  - 인라인 정책 생성
    - 236~263 Line JSON 입력
- [v] S3 버킷 용도 분리 여부 결정
  - S3 버킷 총 2개 생성
    - 이미지 파일 저장 : knusrae-bucket
    - 프론트엔드 배포 : knusrae-frontend
- [ ] 필요 시 Redis 생성

## Phase 3. 런타임 준비

- [ ] Java 21 설치
- [ ] Nginx 설치
- [ ] AWS CLI 설치
- [ ] jq/curl/unzip 등 설치
- [ ] 배포 폴더 구조 생성

## Phase 4. 시크릿/환경변수

- [ ] 환경변수 목록 표 작성
- [ ] Secrets Manager secret 구조 정리
- [ ] secret 조회 스크립트 작성
- [ ] 앱 실행 시 환경변수 주입 확인

## Phase 5. 백엔드 기동

- [ ] JAR 빌드
- [ ] EC2 업로드
- [ ] 수동 실행 성공
- [ ] DB 연결 성공
- [ ] S3 연결 성공
- [ ] OAuth 설정 확인
- [ ] 헬스체크 엔드포인트 확인

## Phase 6. 서비스화

- [ ] systemd 등록
- [ ] Nginx reverse proxy 연결
- [ ] 재부팅 후 자동 기동 확인
- [ ] 로그 위치 확인

## Phase 7. 프론트 공개

- [ ] 프론트 build 성공
- [ ] S3 또는 EC2에 배포
- [ ] API 주소 연결
- [ ] SPA 라우팅 확인
- [ ] 로그인/새로고침 동작 확인

## Phase 8. 운영 공개 준비

- [ ] 도메인 연결
- [ ] HTTPS 인증서 적용
- [ ] OAuth redirect URI 운영 주소 등록
- [ ] CORS 허용 주소 수정
- [ ] 외부 공개 테스트

## Phase 9. 운영 안정화

- [ ] CloudWatch 로그/지표 확인
- [ ] 알람 설정
- [ ] RDS 백업 정책 확인
- [ ] 배포 스크립트 정리
- [ ] 롤백 절차 문서화

## Phase 10. 자동화

- [ ] GitHub Actions 워크플로 작성
- [ ] 프론트 자동배포
- [ ] 백엔드 자동배포
- [ ] 배포 후 헬스체크 자동화

---

## 8. 마지막으로: 지금 문서를 보고 내리는 결론

현재 상태는 **AWS 기본 리소스를 잘 만들기 시작한 단계** 이고,
배포 완성을 위해서는 아직 아래 4가지를 꼭 마무리해야 한다.

1. **DB 엔진 불일치 해결**
2. **Secrets Manager → 애플리케이션 주입 흐름 완성**
3. **백엔드 실제 기동 + Nginx 연결**
4. **프론트 배포 + 도메인/HTTPS/OAuth 운영 설정 정리**

이 4가지를 끝내면,
비로소 “리소스를 만든 상태” 에서 “서비스를 실제로 배포한 상태” 로 넘어간다.

---

## 9. 가장 추천하는 다음 액션

가장 먼저 아래 순서로 진행하는 것을 추천한다.

1. **실제 코드 기준으로 DB가 PostgreSQL로 확정·일치하는지 확인**
2. **EC2에서 Spring Boot 앱을 Secrets Manager 값으로 실제 기동**
3. **Nginx로 외부 요청 연결**
4. **프론트를 S3+CloudFront 또는 Nginx로 배포**
5. **도메인/HTTPS/OAuth/CORS 마무리**
6. **CloudWatch와 백업, CI/CD 추가**

이 순서가 가장 덜 꼬이고, 실패했을 때 원인도 찾기 쉽다.
