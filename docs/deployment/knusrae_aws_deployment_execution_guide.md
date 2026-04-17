# Knusrae AWS 배포 실행 가이드

## 문서 목적 및 사용 방법

이 문서는 [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md)(배포 작업 계획서)를 기준으로, **최종 애플리케이션 배포까지** 단계별로 무엇을 해야 하는지 상세히 정리한 **실행용 가이드**입니다.

- **계획서**: 배경, 전략, 옵션 비교, 참고 내용 정리
- **본 문서**: 실행 순서·체크리스트·결정 기록 — Phase 1부터 10까지 순서대로 진행하며 하나씩 체크

**결정이 필요한 단계**에서는 각 Phase의 **「결정 필요」** 블록에 질문과 선택지가 있습니다. 해당 단계 진행 시 결정을 내린 뒤 **「기록:」** 란에 내용을 채우거나, 에이전트에게 질문하여 결정 후 기록하면 됩니다.

**진행 순서**: Phase 1 → 2 → … → 10. 이전 Phase 완료 기준을 충족한 뒤 다음 Phase로 진행합니다.

### Phase 목차

| Phase | 제목 |
|-------|------|
| 1 | 설계 확정 |
| 2 | AWS 리소스 정리 |
| 3 | 런타임 준비 |
| 4 | 시크릿/환경변수 |
| 5 | 백엔드 기동 |
| 6 | 서비스화 |
| 7 | 프론트엔드 배포 |
| 8 | 운영 공개 준비 (도메인/HTTPS) |
| 9 | 운영 안정화 |
| 10 | 자동화 (CI/CD) |

---

## Phase 1. 설계 확정

### 목표

배포 구조(DB, Redis, 프론트/백엔드 방식, 서비스 단위)가 확정되고, "우리 서비스의 실제 배포 구조"를 한 문단으로 설명할 수 있는 상태가 됩니다.

### 확정 사항 요약 (Knusrae 현재 기준)

- **DB 엔진**: PostgreSQL (아키텍처·코드·AWS RDS 일치)
- **Redis**: 1차 배포에서는 미사용. 토큰 블랙리스트는 PostgreSQL(DB)로 구현되어 있음. 추후 필요 시 ElastiCache 도입
- **프론트 배포**: S3 + CloudFront
- **백엔드 실행 방식**: Docker Compose (EC2 1대에서 auth/member/cook 3개 서비스 컨테이너)
- **서비스 단위**: 독립 서비스 3개 (auth-service 8081, member-service 8083, cook-service 8082). common-service는 공용 라이브러리

### 단계별 작업

#### 1-1. 확정 사항 검증

- [🟢] **1.** 위 확정 사항이 실제 코드·AWS 구성과 일치하는지 확인한다. (계획서 Phase 1 체크리스트 참고)
- [🟢] **2.** DB 엔진이 코드(build.gradle, application.yml, dialect)와 RDS 엔진이 모두 PostgreSQL인지 확인한다.
- **확인 방법**: 루트 `build.gradle`의 `subprojects` 블록에 PostgreSQL 의존성(`org.postgresql:postgresql`)이 있으면 하위 백엔드 모듈(auth-service, member-service, cook-service)이 모두 이를 상속받음. `backend/*/src/main/resources/application*.yml`에 `jdbc:postgresql` 및 dialect 설정 확인.
- **참고**: 배포 시에는 **백엔드** 빌드 결과물(auth-service, member-service, cook-service의 bootJar 산출물)만 서버에 올라감. 루트 `build.gradle`은 공통 설정·의존성만 정의하고 실행 가능한 JAR를 만들지 않음. 루트와 모듈별 build.gradle이 나뉜 것은 Gradle 멀티프로젝트 구조라서 정상이며, 루트에서 PostgreSQL을 두면 모든 백엔드 모듈이 동일한 DB 엔진을 쓰게 됨.

### 완료 기준

- [🟢] "우리 서비스의 실제 배포 구조"를 한 문단으로 설명할 수 있다.
- [🟢] DB 엔진(PostgreSQL)이 코드·아키텍처·AWS RDS와 모두 일치한다.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 0, 7. Phase 1

---

## Phase 2. AWS 리소스 정리

### 목표

EC2, RDS, IAM, S3가 배포에 맞게 점검·정리되고, 보안 그룹·권한·버킷 용도가 명확해진 상태가 됩니다.

### 결정 필요

- **▶ 결정 1 — EC2 디스크**: 루트 볼륨 8GB를 유지할지, 20GB 이상으로 확장(또는 인스턴스 재생성)할지 정한다.  
  - 선택지: (A) 8GB 유지 (B) 20GB 이상 확장/재생성  
  - 권장: (B). 운영 로그·Docker 이미지·빌드 산출물을 고려하면 20GB 이상이 안전함.  
  - 기록: `(A) - 우선 8GB로 해보고 늘리던가 하자`

- **▶ 결정 2 — RDS DB 계정**: 마스터 계정만 사용할지, 애플리케이션 전용 DB 사용자를 생성할지 정한다.  
  - 선택지: (A) 마스터만 사용 (B) 앱 전용 사용자 생성  
  - 권장: (B). 마스터는 관리 작업용(스키마 변경, 사용자 관리), 앱은 별도 계정으로 제한된 권한만 부여하면 보안·감사에 유리함.  
  - 기록: `(B) - DB 사용자를 만들자`
  - **앱 전용 사용자 생성 위치**: **AWS 콘솔이 아님.** RDS 콘솔에서는 인스턴스 생성 시 **마스터** 사용자명·비밀번호만 지정할 수 있고, 추가 DB 사용자를 만드는 메뉴는 없다. 앱 전용 사용자는 **PostgreSQL 내부**에서 생성한다. 즉, 마스터 계정으로 RDS에 접속한 뒤 **SQL로** `CREATE ROLE`(또는 `CREATE USER`)·`GRANT`를 실행해 새 사용자를 만들고 권한을 부여한다. 접속 방법: EC2에서 `psql`로 접속하거나, 개발 PC에서 DBeaver + SSH 터널(EC2 경유)로 RDS에 접속한 다음 SQL 실행. (참고: [DBeaver-RDS-접속-가이드.md](DBeaver-RDS-접속-가이드.md))

- **▶ 결정 3 — S3 버킷 이름**: IAM·버킷 정책 예시에 사용할 실제 버킷 이름을 통일한다.  
  - 현재 문서·체크리스트에 `knusrae-app-assets` / `knusrae-bucket`(이미지), `knusrae-frontend`(프론트) 혼재. 실제 생성된 버킷 이름으로 통일할 것.  
  - 기록 (이미지용 버킷): `knusrae-bucket`  
  - 기록 (프론트용 버킷): `knusrae-frontend`

### 단계별 작업

#### 2-1. EC2 점검

- [🟢] **1.** Elastic IP가 해당 EC2에 연결되어 있는지 AWS 콘솔에서 확인한다.
- [🟢] **2.** 보안 그룹이 EC2에 의도대로 적용되어 있는지 확인한다. (SSH: 내 IP만, RDS 5432: EC2 보안 그룹 등)
  - **상세 확인 방법**
    1. **EC2에 붙은 보안 그룹 확인**  
       AWS 콘솔 → EC2 → 인스턴스 → 해당 인스턴스 선택 → 하단 **보안** 탭 → **보안 그룹** 링크 클릭(또는 EC2 왼쪽 메뉴 **보안 그룹**에서 해당 그룹 선택).
    2. **EC2 보안 그룹 인바운드 규칙 점검**
       - **SSH(22)**: **소스**가 "내 IP(My IP)" 또는 특정 CIDR(예: 사무실 IP 대역)인지 확인. `0.0.0.0/0`이면 전 세계에서 SSH 접속 가능하므로 보안상 위험. "내 IP"로 제한하는 것이 권장됨.
       - **HTTP(80) / HTTPS(443)**: Nginx 등 리버스 프록시를 쓰는 경우, 웹 트래픽용으로만 열려 있는지 확인. 필요 시 `0.0.0.0/0` 또는 CloudFront 등 허용된 소스만 지정.
       - **8081, 8082, 8083**: 백엔드 직접 노출 시에만 이 포트들을 열고, 가능하면 동일 VPC 또는 특정 소스로 제한. Nginx로 80/443만 열고 백엔드는 내부용으로 둘 경우 인바운드에 8081–8083이 없어도 됨.
    3. **RDS 5432와 EC2 보안 그룹**  
       "RDS 5432는 EC2 보안 그룹만 허용"하는 설정은 **RDS 인스턴스에 붙은 보안 그룹**에서 하며, **Phase 2의 2-4. RDS 보안 그룹 재점검**에서 확인한다. 여기서는 EC2 쪽 보안 그룹(EC2에 붙은 그룹)만 점검하면 됨.
- [🟢] **3.** SSH로 Ubuntu 접속이 안정적으로 되는지 확인한다.
  - **상세 확인 방법**
    1. 로컬 터미널에서 `ssh -i <PEM키경로> ubuntu@<Elastic-IP>` 실행. (Windows: PowerShell 또는 WSL, PEM 키 권한은 `chmod 400` 권장.)
    2. 비밀번호 없이 키 기반으로 로그인되는지, 프롬프트가 `ubuntu@...`로 뜨는지 확인.
    3. 끊김 없이 여러 번 접속·종료(`exit`)를 반복해 연결이 안정적인지 확인. 타임아웃·연결 거부가 나오면 보안 그룹(22번 포트)·Elastic IP·키 경로를 재확인.
- [🟢] **4.** 디스크 여유를 확인한다. (`df -h`. 결정 1에서 확장하기로 했으면 확장 또는 재생성 후 확인.)
  - **상세 확인 방법**
    1. EC2에 SSH 접속한 뒤 `df -h` 실행. 루트 볼륨(보통 `/dev/nvme0n1p1` 또는 `/dev/xvda1`이 `/`에 마운트됨)의 **사용률(Use%)**과 **남은 공간(Avail)** 확인.
    2. **기준**: 사용률 80% 미만, 여유 공간 수 GB 이상 권장. Docker 이미지·로그·빌드 산출물을 고려하면 여유가 넉넉할수록 안전함.
    3. **결정 1에서 (B) 확장 선택 시**: AWS 콘솔 → EC2 → 볼륨 → 해당 루트 볼륨 선택 → 작업 → 볼륨 수정 → 크기 조정 후, 인스턴스 내부에서 파티션 확장(예: `sudo growpart /dev/nvme0n1 1`, `sudo resize2fs /dev/nvme0n1p1` 등, OS/디바이스명에 따라 다름). 확장 후 다시 `df -h`로 용량 반영 여부 확인.
    4. **메모리(RAM + swap) 사용량 확인하기**
      - `free -h`
- [🟢] **5.** 시간대/로케일이 적절한지 필요 시 설정한다.
  - **상세 확인 방법**
    1. SSH 접속 후 `timedatectl`(또는 `date`)로 **타임존** 확인. 한국 서비스면 `Asia/Seoul`, UTC+9가 적절함.
    2. `locale` 또는 `locale -a`로 **로케일** 확인. UTF-8(예: `en_US.UTF-8`, `ko_KR.UTF-8`)이 있으면 로그·파일 인코딩에 유리함.
    3. **설정이 맞지 않을 때**: 타임존은 `sudo timedatectl set-timezone Asia/Seoul`, 로케일은 `sudo locale-gen ko_KR.UTF-8` 및 `sudo update-locale LANG=ko_KR.UTF-8` 등(OS 문서 참고). 변경 후 세션 재접속 또는 `source /etc/default/locale` 후 확인.
- **확인 방법**: `ssh ubuntu@<Elastic-IP>`, `df -h`, `timedatectl`, `locale`. 보안 그룹은 위 2번의 상세 확인 방법대로 EC2 콘솔에서 인바운드 규칙(SSH 내 IP만, 80/443·8081–8083 등)을 확인.

#### 2-2. IAM Role 최소 권한화

- [🟢] **1.** AWS 콘솔 → IAM → 역할(Roles) → EC2에 부여한 역할 선택.
- [🟢] **2.** 권한 추가 → 인라인 정책 생성(Create inline policy) → JSON 탭 선택.
- [🟢] **3.** 계획서 236~263라인 또는 아래와 같이 필요한 권한만 넣은 정책을 작성한다. **버킷 이름·계정 ID·리전·시크릿 이름은 실제 값으로 교체.**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3KnusraeBuckets",
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject", "s3:ListBucket"],
      "Resource": [
        "arn:aws:s3:::<이미지용버킷이름>",
        "arn:aws:s3:::<이미지용버킷이름>/*",
        "arn:aws:s3:::<프론트용버킷이름>",
        "arn:aws:s3:::<프론트용버킷이름>/*"
      ]
    },
    {
      "Sid": "SecretsManagerKnusrae",
      "Effect": "Allow",
      "Action": "secretsmanager:GetSecretValue",
      "Resource": "arn:aws:secretsmanager:<리전>:<계정ID>:secret:knusrae/prod-*"
    }
  ]
}
```

- [🟢] **4.** 정책 이름 입력(예: `KnusraeEC2MinimalPolicy`) 후 정책 생성.
- [🟢] **5.** 기존 AmazonS3FullAccess, AmazonEC2FullAccess, KMS 등 과권한 정책을 해당 역할에서 제거(Detach).
- **확인 방법**: 역할의 권한 탭에 위 인라인 정책만 남고, S3/Secrets Manager 접근에 필요한 최소 권한만 있는지 확인

#### 2-3. S3 버킷 용도 및 정책

- [🟢] **1.** 이미지 저장용 버킷과 프론트 배포용 버킷이 각각 존재하는지 확인한다. (결정 3에서 정한 이름 기준)
- [🟢] **2.** 이미지용 버킷: 퍼블릭 액세스 차단 유지. 버킷 정책에서 EC2 IAM 역할만 허용. (계획서 309~336라인 예시 참고. `계정ID`, `EC2에부여한역할이름` 치환.)
- [🟢] **3.** 프론트용 버킷: 퍼블릭 액세스 차단 유지. CloudFront OAC용 버킷 정책은 Phase 7에서 CloudFront 생성 후 적용. (계획서 343~366라인 참고.)
- **확인 방법**: S3 콘솔에서 각 버킷의 퍼블릭 액세스 차단 상태 및 버킷 정책 확인

#### 2-4. RDS 보안 그룹 재점검

- [🟢] **1.** RDS가 퍼블릭 접근이 아닌지 확인한다.
  - **상세 확인 방법**  
    AWS 콘솔 → RDS → 데이터베이스 → 해당 인스턴스 클릭 → **연결 및 보안** 탭의 엔드포인트 항목 선택. 하단 연결 및 보안의 **퍼블릭 액세스 가능**이 **아니오**인지 확인. "예"이면 인터넷에서 RDS 엔드포인트로 직접 접근 가능하므로 보안상 위험. VPC 내부(EC2 등)에서만 접속하려면 반드시 "아니오"로 두고, 접근은 보안 그룹(2번 항목)으로만 제어한다.
- [🟢] **2.** RDS 보안 그룹 인바운드에서 5432(PostgreSQL)가 **EC2 보안 그룹**을 소스로만 허용하는지 확인한다.
  - **상세 확인 방법**
    1. RDS 콘솔 → 해당 인스턴스 → **연결 및 보안** 탭에서 **VPC 보안 그룹** 링크 클릭(또는 EC2 → 보안 그룹에서 RDS 인스턴스에 붙어 있는 그룹 선택).
    2. **인바운드 규칙** 탭에서 **편집**.
    3. **5432** 포트에 대한 규칙이 다음을 만족하는지 확인.  
       - **유형**: PostgreSQL(또는 사용자 지정 TCP), **포트**: 5432.  
       - **소스**: **보안 그룹**을 선택하고, **EC2 인스턴스에 연결된 보안 그룹 ID**(예: `sg-xxxxx`)를 지정. `0.0.0.0/0` 또는 "내 IP" 등이면 외부에서 5432로 접근 가능하므로 제거하고 EC2 보안 그룹만 남긴다.
    4. 5432에 대한 규칙이 이 한 건(EC2 SG 소스)만 있는지 확인. 다른 소스(특히 0.0.0.0/0)가 있으면 제거.
  - **주의**: **EC2 보안 그룹에는 5432 인바운드 규칙을 넣지 않는다.** 5432는 PostgreSQL이 **받는** 포트이므로, 인바운드 규칙은 **RDS 인스턴스에 붙은 보안 그룹**에만 추가한다. EC2는 RDS에 **접속하는** 쪽이라 EC2 SG에는 SSH(22), 80/443 등만 있으면 되고, 5432 인바운드를 EC2에 넣으면 “EC2의 5432 포트로 들어오는 트래픽 허용”이 되어 의미가 없음.
  - **보안 그룹 정리 요약**
    - **직접 만든 SG**(예: `knusrae-server-security-group`): EC2와 RDS **양쪽에 모두** 붙인다. 해당 SG의 인바운드 규칙에 5432(PostgreSQL)를 추가하고, **소스**를 **같은 보안 그룹(자기 자신)** 으로 설정하면, EC2에서 RDS로 5432 접속이 가능하다. 새 SG를 만들 필요 없다.
    - **default**: VPC 기본 보안 그룹이다. 리스트에만 보이고 EC2/RDS에 붙이지 않았다면 그대로 두어도 된다. 삭제 시 기본 보안 그룹은 삭제 불가 처리라고 뜸
    - **RDS/EC2 마법사가 만든 SG**(예: `ec2-rds-1`, `rds-ec2-1`): 생성 시 자동으로 EC2 또는 RDS에 붙을 수 있으나, 인바운드 규칙이 비어 있는 경우가 많다. **삭제해도 되고**, 그대로 두어도 동작에는 영향이 없다. 정리하려면 인스턴스에서 해당 SG를 분리한 뒤(다른 리소스에서 쓰는지 확인 후) 보안 그룹을 삭제하면 된다.

- [🟢] **3.** (결정 2에서 앱 전용 사용자 선택 시) **PostgreSQL 안에서** 애플리케이션 전용 DB 사용자 생성 및 권한 부여. (AWS 콘솔이 아님. 마스터로 RDS 접속 후 SQL 실행.)
  - **접속 방법**: (1) EC2에 SSH 접속한 뒤 `psql -h <RDS엔드포인트> -U <마스터사용자> -d <DB명>` 로 접속하거나, (2) 로컬 PC에서 DBeaver로 **SSH 터널(EC2 경유)** 설정 후 RDS에 접속. (참고: [DBeaver-RDS-접속-가이드.md](DBeaver-RDS-접속-가이드.md))  
    - **DBeaver 사용 시**: 접속 후 **SQL 편집기(SQL Editor)** 를 열고 아래 SQL을 그대로 실행하면 된다. EC2에 `psql`을 설치할 필요 없음.
    - **psql 사용 시**: EC2(Ubuntu)에는 PostgreSQL 클라이언트가 기본 설치되어 있지 않을 수 있다. `psql`을 쓰려면 `sudo apt update && sudo apt install -y postgresql-client` 로 설치한다. **용량**: `postgresql-client`는 클라이언트만 설치하며(서버 아님), 보통 수십 MB 수준이라 EC2 디스크 부담은 거의 없다.
  - **실행 순서**: 마스터로 접속한 뒤 아래 SQL을 순서대로 실행. (DBeaver에서는 SQL 편집기에 붙여 넣고 실행하면 됨.)  
    `CREATE ROLE knusrae_app WITH LOGIN PASSWORD '앱용비밀번호';`  
    `GRANT CONNECT ON DATABASE knusrae_db TO knusrae_app;`  
    `GRANT USAGE ON SCHEMA public TO knusrae_app;`  
    `GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO knusrae_app;`  
    (필요 시 `ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO knusrae_app;` 로 이후 생성 테이블에도 권한 부여.)
  - **검증**: 동일한 클라이언트에서 `knusrae_app` 계정으로 로그인해 `SELECT 1;` 또는 기존 테이블 조회가 되는지 확인.
  - **적용**: 생성한 사용자·비밀번호는 Secrets Manager 등에 저장하고, 애플리케이션 설정(DB username/password)에서 마스터 대신 이 계정을 사용하도록 한다.
- **확인 방법**: 1번은 RDS 콘솔 연결 및 보안 탭에서 "퍼블릭 액세스 가능: 아니오". 2번은 RDS에 붙은 보안 그룹 → 인바운드 규칙에서 5432 소스가 EC2 보안 그룹만. 3번은 DB 클라이언트로 앱 전용 계정 로그인·쿼리 가능 여부로 확인.

#### 2-5. Redis (필요 시)

- [ ] **1.** 1차 배포에서는 Redis 미사용으로 두었다면 이 항목은 스킵. 추후 ElastiCache for Redis 생성 시 별도 가이드 참고.
- **확인 방법**: Redis 사용하지 않기로 했으면 완료로 체크

### 완료 기준

- [ ] EC2 접속·디스크·보안 그룹 점검 완료.
- [ ] IAM Role에 애플리케이션 실행에 필요한 최소 권한만 남아 있음.
- [ ] S3 버킷 용도가 명확하고, 이미지용 버킷 정책 적용 완료.
- [ ] RDS 접근 경로가 EC2 → RDS로 제한됨.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 1, 2-2
- [AWS-S3-버킷-설정-가이드.md](AWS-S3-버킷-설정-가이드.md)
- [AWS-EC2-배포-가이드.md](AWS-EC2-배포-가이드.md)

---

## Phase 3. 런타임 준비

### 목표

EC2에서 백엔드(Docker Compose)와 Nginx를 실행할 수 있도록 Java, Docker, Nginx, AWS CLI 등 필요한 소프트웨어가 설치되고, 배포용 디렉터리 구조가 준비된 상태가 됩니다.

### 결정 필요

- **▶ 결정 — 배포 디렉터리**: 권장 구조 `/home/ubuntu/knusrae/` 사용 여부.  
  - 선택지: (A) 권장 구조 사용 (B) 다른 경로 사용  
  - 권장: (A). 하위에 app, releases, scripts, logs, current(심볼릭 링크) 등.  
  - 기록: A(권장 구조) /home/ubuntu/knusrae/ 사용, 소유자 ubuntu:ubuntu, 권한은 디렉터리 755·파일 644·스크립트 750 적용
    - 디렉터리 (rwxr-xr-x)
      - 소유자(ubuntu): 읽기/쓰기/실행(7)
      - 그룹: 읽기/실행(5)
      - 기타 사용자: 읽기/실행(5)
      - 디렉터리에서 x는 “들어갈 수 있음(탐색)” 권한이라서 보통 디렉터리는 755를 많이 사용
    - 파일 (rw-r--r--)
      - 소유자: 읽기/쓰기(6)
      - 그룹: 읽기(4)
      - 기타: 읽기(4)
      - 일반 설정파일/문서파일에 흔한 기본값
    - 스크립트 (rwxr-x---)
      - 소유자: 읽기/쓰기/실행(7)
      - 그룹: 읽기/실행(5)
      - 기타: 권한 없음(0)
      - 스크립트를 아무나 실행 못 하게 제한할 때 적절

### 단계별 작업

#### 3-1. 기본 런타임 설치

- [🟢] **1.** 패키지 인덱스 갱신.
  - 커맨드 순서
    - `sudo apt update`
- [🟢] **2.** 공통 유틸 설치.
  - 커맨드 순서
    - `sudo apt install -y jq curl unzip ca-certificates gnupg lsb-release`
- [🟢] **3.** Java 21 설치. (루트 `build.gradle`의 toolchain·`sourceCompatibility`와 동일 버전)
  - 커맨드 순서
    - `sudo apt install -y openjdk-21-jdk` 또는 Amazon Corretto 21 등
    - `java -version`
- [🟢] **4.** Nginx 설치. (`sudo apt install -y nginx`)
  - 커맨드 순서
    - `sudo apt install -y nginx`
    - `nginx -v`
- [🟢] **5.** AWS CLI v2 설치. (Secrets Manager 조회 등에 사용. [AWS 공식 설치 가이드](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) 참고)
  - Command Line installer - Linux x86(64-bit) 가이드 순서대로 진행
  - 커맨드 순서
    - `curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"`
    - `unzip awscliv2.zip`
    - `sudo ./aws/install`
    - `aws --version`
- [🟢] **6.** Docker 및 Docker Compose 설치. (공식 Docker 엔진 설치 후 `docker compose` 사용 가능 여부 확인. 미인식 시 `docker-compose-plugin` 설치)
  - 커맨드 순서 - Docker
    - `sudo apt install -y docker.io`
    - `sudo systemctl enable --now docker`
    - `docker --version`
  - 커맨드 순서 - Docker Compose
    - `docker compose version`
      - 안되면 아래 순서로 진행 (docker: unknown command: docker compose)
        - 1) 사전 패키지
          - `sudo apt update && sudo apt install -y ca-certificates curl`
        - 2) Docker 공식 저장소 키/리포지토리 등록
          - `sudo install -m 0755 -d /etc/apt/keyrings`
          - `sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc`
          - `sudo chmod a+r /etc/apt/keyrings/docker.asc`
          - `echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo ${UBUNTU_CODENAME:-$VERSION_CODENAME}) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null`
        - 3) Compose 플러그인 설치
          - `sudo apt update && sudo apt install -y docker-compose-plugin`
        - 4) 확인
          - `docker compose version`
- [🟢] **7.** (선택) Git 설치. (`sudo apt install -y git`)
  - 커맨드 순서
    - `sudo apt install -y git`
    - `git --version`

- **확인 방법**: `java -version`, `nginx -v`, `aws --version`, `docker --version`, `docker compose version` (또는 `docker-compose --version`)

#### 3-2. 배포 폴더 구조 생성

- [🟢] **1.** 기본 경로 생성. (권장: `/home/ubuntu/knusrae/`)
  - 커맨드 순서
    - `sudo mkdir -p /home/ubuntu/knusrae`
    - `sudo chown -R ubuntu:ubuntu /home/ubuntu/knusrae`
- [🟢] **2.** 하위 디렉터리 생성. (`app/`, `releases/`, `scripts/`, `logs/`)
  - 커맨드 순서
    - `mkdir -p /home/ubuntu/knusrae/{app,releases,scripts,logs}`
- [🟢] **3.** 권한 정책 적용(첫 배포 전) (기본: 디렉터리 755, 파일 644, 스크립트 750)
  - 커맨드 순서
    - `find /home/ubuntu/knusrae -type d -exec chmod 755 {} \;`
    - `find /home/ubuntu/knusrae -type f -exec chmod 644 {} \;`
- [ ] **4.** 권한 정책 적용(첫 배포 후) (기본: 디렉터리 755, 파일 644, 스크립트 750)
    - `chmod 750 /home/ubuntu/knusrae/scripts/*.sh` (스크립트가 있을 때만)
- [ ] **5.** `current` 심볼릭 링크 정책 확인. (첫 배포 시점에 생성)
  - 예시 커맨드(첫 배포 후)
    - `ln -sfn /home/ubuntu/knusrae/releases/<release-id> /home/ubuntu/knusrae/current`

- **확인 방법**:
  - `ls -la /home/ubuntu/knusrae/` 로 디렉터리 생성 여부 확인
  - `stat -c "%U:%G %a %n" /home/ubuntu/knusrae /home/ubuntu/knusrae/app /home/ubuntu/knusrae/scripts` 로 소유자/권한 확인
  - (첫 배포 후) `ls -la /home/ubuntu/knusrae/current` 로 심볼릭 링크 확인

### 완료 기준

- [🟢] Java 21, Nginx, AWS CLI, Docker/Docker Compose, jq/curl/unzip 설치 완료.
- [ ] 배포용 디렉터리 구조 생성 완료.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 4-1, 5-1

---

## Phase 4. 시크릿/환경변수

### 목표

환경변수 목록이 정리되고, Secrets Manager에 저장된 값을 애플리케이션 실행 시 주입하는 방식이 구현된 상태가 됩니다. 서버 재시작 후에도 수동 입력 없이 앱이 기동되어야 합니다.

**Phase 10(CI/CD) 전제**: 지금 Phase에서는 **수동으로** “스크립트 실행 → `.env.secrets` 갱신 → `docker compose up`” 순서를 검증합니다. Phase 10에서는 **동일 순서**를 GitHub Actions(또는 선택한 CI)의 SSH 배포 단계에 옮깁니다. Phase 4에서 정한 **시크릿 ID·리전·출력 파일 경로·compose의 `env_file` 경로**가 Phase 10 원격 명령과 일치해야 합니다.

### 작업 위치(어디서 작업하나?)

- **로컬/IDE**: 백엔드 설정(`application*.yml`, 의존성) 수정
- **AWS 콘솔**: Secrets Manager 시크릿 값/구조 확인, IAM Role 권한 확인
- **EC2(SSH)**: 실제 컨테이너 기동 후 시크릿 주입 성공 여부 로그 확인

### 스크립트 파일을 EC2에 두는 방법 (`git clone`으로 `infra/`를 가져오지 않을 때)

애플리케이션 저장소에 `docs/`·`infra/`가 없거나 EC2에서 레포 전체를 clone 하지 않는 경우에도, **호스트 스크립트(4-3-A) 파일 하나**는 EC2의 고정 경로에 있어야 합니다. 아래 중 팀 표준을 하나 정합니다.

| 방식 | 요약 | 비고 |
|------|------|------|
| **SCP/rsync** | 로컬(또는 관리 PC)에서 `fetch-secrets-to-env.sh`를 `/home/ubuntu/knusrae/scripts/` 등으로 복사 | Phase 5의 `docker-compose.yml` 전송과 동일 패턴. 1회만 두고 재사용 가능 |
| **배포 번들** | `docker-compose.yml` + `*.sh`를 같은 폴더로 묶어 압축 후 EC2에 풀기 | 수동 배포 시 경로 일관성 유지에 유리 |
| **S3 비공개 객체** | 스크립트를 버킷에 올려 두고 EC2에서 `aws s3 cp s3://.../fetch-secrets-to-env.sh ...` | EC2 Role에 해당 객체에 대한 `s3:GetObject` 필요 |
| **서버에 직접 작성** | `nano`/`vi`로 스크립트 내용을 붙여넣기 | 파일이 짧을 때만 권장; 버전 관리는 별도 |
| **Phase 10에서 동기화** | CI가 체크아웃한 작업 디렉터리의 `infra/scripts/`를 SSH로 EC2에 `rsync`/`scp` | **권장**: 스크립트 원본이 CI에 있어야 매 배포마다 동일 절차 재현 가능 |

**저장소 구성 권장(Phase 10과 맞물림)**: `docs/`는 공개 여부와 별개로, **최소한 `infra/scripts/`(및 배포용 compose 예시)만 Git에 포함**하는 편이 안전합니다. 그렇지 않으면 CI는 “어디서 스크립트를 가져올지”를 매번 외부(S3, 수동 사본, 다른 비공개 레포)에 의존하게 됩니다. 로컬에만 있는 스크립트는 **팀이 단일 원본 위치(경로·해시)** 를 문서로 고정해 두는 것이 좋습니다.

### 결정 필요

- **▶ 결정 — 시크릿 주입 방식**: Docker Compose 환경에서 (A) 호스트 스크립트로 secret 조회 후 env 파일 생성 → docker-compose의 env_file로 전달, (B) 앱 내부에서 Spring Cloud AWS 또는 AWS SDK로 Secrets Manager 직접 조회.  
  - 선택지: (A) 스크립트 → env_file (B) 앱 내부 조회  
  - 권장: (B). EC2 IAM Role을 컨테이너에서 사용할 수 있게 하면 앱이 시작 시 Secrets Manager를 직접 호출 가능. 시크릿을 디스크에 쓰지 않아 보안상 유리.  
  - 기록: (A)

### 단계별 작업

#### 4-1. 환경변수 목록 정리

- [🟢] **1.** 백엔드·프론트에서 필요한 환경변수 목록을 표로 정리한다. (DB URL/username/password, JWT secret, OAuth client id/secret/redirect uri, S3 버킷, AWS region, Spring profile 등)
- [🟢] **2.** dev / prod 구분 여부를 결정하고, Secrets Manager 시크릿 이름·구조를 일관되게 정리한다. (예: `knusrae/prod` 키-값 JSON)
  - 현재는 dev/prod 구분은 없음 (dev 서버 별도로 존재하지 않음)
- **확인 방법**: 문서 또는 스프레드시트에 "변수명 / 설명 / 예시 / 비고" 표 작성 완료

#### 4-2. Secrets Manager 시크릿 구조 확인

- [🟢] **1.** AWS 콘솔 → Secrets Manager에서 사용할 시크릿이 존재하는지 확인한다.
- [🟢] **2.** 시크릿이 키-값(Key/value) JSON 형태로 저장되어 있는지 확인한다. (계획서·Secrets Manager 가이드의 JSON 예시 참고)
- [🟢] **3.** EC2 IAM Role에 해당 시크릿에 대한 `secretsmanager:GetSecretValue` 권한이 있는지 확인한다. (Phase 2에서 설정)
- **확인 방법**: Secrets Manager 콘솔에서 시크릿 조회, IAM 역할 정책 확인

#### 4-3. 시크릿 주입 방식 구현

아래 **4-3-A**는 결정 (A) 호스트 스크립트, **4-3-B**는 결정 (B) 앱 내부 조회이다. 현재 채택은 (A)이며, 추후 (B)로 바꿀 때는 4-3-B를 적용하고 compose의 시크릿용 `env_file`·해당 변수 의존을 정리하면 된다.

- [ ] **4-3-A. (A) 호스트 스크립트 → env 파일 → docker-compose** — *현재 채택*  
  - **현재 상태**: 로컬/IDE 정합성 점검은 완료, EC2 실행 검증(스크립트 실행·권한 600·키 이름 확인)은 미완료.
  - **목표(한 줄)**: EC2 호스트에서 Secrets Manager 값을 `.env.secrets`로 만들고, 이후 Compose가 그 파일을 읽을 수 있게 기준 경로를 고정한다.
  - **전제**: EC2 IAM Role에 `secretsmanager:GetSecretValue`가 있고(4-2), EC2 호스트에서 AWS CLI가 동작한다(Phase 2).
  - **중요 원칙**: Compose에는 **값 자체를 쓰지 않고**, `env_file`로 **파일 경로만** 지정한다.
  - **실행 위치별 체크리스트**
    1. [🟢] **로컬/IDE에서 할 일**
      - 시크릿 기준값을 문서에 고정: `SECRET_ID=<운영 시크릿 ID>`, `AWS_REGION=ap-northeast-2`, `OUTPUT_FILE=./.env.secrets` (compose 실행 디렉터리 기준)  
      (여기서 "문서"는 `.sh` 파일이 아니라 이 배포 실행 가이드 문서 또는 팀 운영 위키/스프레드시트 같은 설정 기준 문서를 의미)
      - `docker-compose.yml`의 백엔드 서비스(`auth-service`, `member-service`, `cook-service`) `env_file` 경로를 `./.env.secrets`로 동일하게 맞춘다.
      - `application-prod.yml`이 기대하는 변수명(`DB_URL`, `DB_USERNAME`, ...)과 Secrets Manager JSON 키 이름을 동일하게 맞춘다.
    2. [🟢] **EC2(호스트)에서 할 일**
       - 스크립트 파일을 고정 경로에 둔다(예: `/home/ubuntu/knusrae/scripts/fetch-secrets-to-env.sh`).
        - 하단 명령어를 PowerShell에서 실행(EC2 서버 아님)
        - (설명) 아래 3줄은 **로컬 PC → EC2로 스크립트를 배치**하는 과정이다.
        - `ssh -i "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" ubuntu@43.202.230.154 "mkdir -p /home/ubuntu/knusrae/scripts && chown -R ubuntu:ubuntu /home/ubuntu/knusrae/scripts && chmod 755 /home/ubuntu/knusrae/scripts"`
          - (무엇을 하나?) EC2에 `/home/ubuntu/knusrae/scripts` 디렉터리를 만들고, 소유자/권한을 설정한다.
        - `scp -i "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" "C:\JangJinSeok\Workspace\Knusrae\infra\scripts\fetch-secrets-to-env.sh" ubuntu@43.202.230.154:/home/ubuntu/knusrae/scripts/fetch-secrets-to-env.sh`
          - (무엇을 하나?) 로컬의 `fetch-secrets-to-env.sh`를 EC2의 고정 경로로 복사한다.
        - `ssh -i "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" ubuntu@43.202.230.154 "chmod 750 /home/ubuntu/knusrae/scripts/fetch-secrets-to-env.sh && ls -l /home/ubuntu/knusrae/scripts/fetch-secrets-to-env.sh"`
          - (무엇을 하나?) 스크립트를 실행 가능하게 만들고(`750`), 배치가 정상인지 파일 정보를 확인한다.
        - `ssh -i "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" ubuntu@43.202.230.154 "mkdir -p /home/ubuntu/knusrae/app && chown ubuntu:ubuntu /home/ubuntu/knusrae/app"`
          - (무엇을 하나?) Compose·`.env.secrets`를 둘 **배포 디렉터리**를 만든다. (5-2의 `mkdir`과 동일 목적이며, 여기서 먼저 만들면 이후 단계에서 생략 가능)
       - 호스트(ubuntu 사용자)에서 스크립트를 실행해 `.env.secrets`를 생성/갱신한다.
         - 하단 명령어를 PowerShell에서 실행(EC2 서버 아님)
         - (설명) 아래 1줄은 **EC2에서 스크립트를 실행해 `.env.secrets`를 생성/갱신**하고, 권한/소유자만 확인한다(값은 출력하지 않음).
         - `ssh -i "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" ubuntu@43.202.230.154 "cd /home/ubuntu/knusrae/app && SECRET_ID='knusrae/prod' AWS_REGION='ap-northeast-2' OUTPUT_FILE='./.env.secrets' /home/ubuntu/knusrae/scripts/fetch-secrets-to-env.sh && stat -c '%U:%G %a %n' ./.env.secrets"`
       - `.env.secrets`는 스크립트가 **기본적으로 권한 600**으로 생성한다. 그래도 운영 원칙상 `stat` 등으로 권한을 재확인하고, 필요하면 `chmod 600 ./.env.secrets`로 재적용한다. 값은 출력하지 말고 키 이름만 확인한다. (스크립트는 기본적으로 `.env` 키 규칙이 아닌 항목이 있으면 실패)
         - 권한 600 : 소유자만 읽기/쓰기
         - 권한/소유자만 깔끔하게 보기(문서에 있는 형태)
           - `stat -c "%U:%G %a %n" /home/ubuntu/knusrae/app/.env.secrets`
         - 기본 출력
           - `stat /home/ubuntu/knusrae/app/.env.secrets`
           - 다른 방법(참고) : `ls -l /home/ubuntu/knusrae/app/.env.secrets`
         - 생성된 `.env.secrets` 파일 읽기
           - 파일 경로 진입 : `cd /home/ubuntu/knusrae/app`
           - KEY만 확인 : `grep -v '^\s*#' ./.env.secrets | grep -v '^\s*$' | cut -d= -f1`
           - 전체 확인 : `cat ./.env.secrets` or `less ./.env.secrets`
           - 특정 키만 확인 : `grep '^DB_URL=' ./.env.secrets`
    3. [🟢] **AWS 콘솔에서 할 일**
       - Secrets Manager JSON 구조와 키 이름이 앱 설정과 일치하는지 확인한다.
       - EC2 IAM Role 권한(`GetSecretValue`, 필요 시 `kms:Decrypt`)을 확인한다.
  - **스크립트 동작(권장)**
    1. `aws secretsmanager get-secret-value --secret-id <ID> --region <REGION> --query SecretString --output text`로 JSON 문자열을 조회한다.
    2. `jq`로 `KEY=VALUE` 형태의 env 파일로 변환한다.
    3. 결과를 Compose가 읽는 경로에 저장한다(기본: **Compose 실행 디렉터리의** `./.env.secrets`).
    4. 파일 권한을 600으로 제한한다.
  - **Phase 4에서 어디까지 검증할지(팀 선택)**
    - **최소 검증(권장 시작점)**: 스크립트 실행 성공 + `.env.secrets` 생성/권한/키 이름 확인까지만 수행한다.
    - **확장 검증(문서 기본 흐름)**: 위 최소 검증 후 `docker compose up -d`까지 실행해 시크릿 누락 로그가 없는지 확인한다.
    - 현재 compose가 `build`를 포함해 EC2에 빌드 컨텍스트가 없다면, `up` 검증은 Phase 5에서 수행해도 된다.
  - **운영/자동화 공통 순서(Phase 10 연계)**
    - `cd <compose 디렉터리>` → (선택) `rsync`로 `infra/scripts/` 반영 → `SECRET_ID=... AWS_REGION=... OUTPUT_FILE=./.env.secrets /home/ubuntu/knusrae/scripts/fetch-secrets-to-env.sh` → `docker compose pull`(이미지 배포 시) → `docker compose up -d`
    - GitHub Secrets에는 AWS 액세스 키 대신 SSH 접속 정보(키/호스트)를 두고, Secrets 조회는 EC2 인스턴스 Role로 처리하는 구성을 권장한다.
  - **(선택) 디스크 노출 최소화**
    - `--env-file <(명령)` 같은 파이프 주입은 셸/compose 버전 차이가 있으므로 팀 표준 합의 후 적용한다. 기본은 제한 권한 파일 + 짧은 수명 정책으로 운영한다.

- [ ] **4-3-B. (B) 앱 내부에서 Secrets Manager 직접 조회** — *추후 전환 시*  
  - **전제**: 컨테이너 프로세스가 AWS 자격 증명(EC2 인스턴스 프로필·IMDS, 또는 이에 준하는 방식)을 **실제로 사용할 수 있는지** 먼저 검증한다. 기본 Docker 브리지 네트워크에서는 IMDS 접근이 막히는 환경이 있어, (B) 전환 시 **가장 먼저 EC2에서 컨테이너 안에서 자격 증명 체인을 확인**하는 것이 안전하다.
  - **1. 의존성(로컬 코드)**  
    - **방향 1**: `spring-cloud-aws-starter-secrets-manager`(버전은 Spring Boot 3.x·공식 문서와 호환되는 조합)로 `spring.config.import`에 Secrets Manager 연동.  
    - **방향 2**: `software.amazon.awssdk:secretsmanager` + 공통 모듈 또는 각 서비스에서 기동 초기에 조회 후 `Environment`/`PropertySource`에 등록.  
    - 서비스가 3개(auth, member, cook)이면 **공통 라이브러리에 한 번만 두고 각 JAR이 동일하게 쓰는지**, 또는 **서비스별 `application-prod.yml`에 동일 설정을 반복할지**를 정한다.
  - **2. 설정(로컬 코드)**  
    - Secrets Manager에 저장된 **JSON 키 이름**이 `application-prod.yml`의 `${DB_URL}` 등과 **그대로 매핑**되도록 한다(Spring Cloud AWS 사용 시 프리픽스·프로파일 규칙은 해당 버전 문서를 따른다).  
    - `local`/`dev` 프로파일에서는 Secrets Manager를 쓰지 않도록 **프로파일 분리**하여 로컬 개발이 깨지지 않게 한다.
  - **3. IAM**  
    - 인스턴스 Role(또는 컨테이너가 쓰는 자격 증명 주체)에 `secretsmanager:GetSecretValue` 및 필요 시 `kms:Decrypt`가 있다.  
    - 시크릿을 **하나의 JSON**으로 두면 API 호출·정책 관리가 단순하다(서비스마다 다른 시크릿 ARN을 쓰면 정책·설정이 늘어난다).
  - **4. docker-compose(EC2)**  
    - 자격 증명이 컨테이너에 닿도록 **네트워크/환경 변수**를 정한다(IMDS hop limit, `AWS_REGION`, 필요 시 명시적 자격 증명은 보안상 최후 수단).  
    - (B)로 완전히 전환하면 시크릿 값은 **호스트 `.env` 파일에 쓰지 않아도** 되므로, 4-3-A에서 쓰던 `env_file` 항목을 제거하거나 DB URL 등 **비시크릿만** 남기도록 정리한다.
  - **5. A → B 전환 시 체크리스트(요약)**  
    1. 컨테이너 내에서 SDK 기본 자격 증명 체인으로 Secrets Manager 호출 성공 여부 확인  
    2. 세 백엔드 서비스 모두 동일 시크릿/동일 설정으로 기동되는지 확인  
    3. 호스트의 시크릿용 `.env` 생성 스크립트 비활성화 또는 제거, compose 정리  
    4. Phase 5 헬스·DB·OAuth 동작 재검증

- **확인 방법(공통)**  
  - EC2에서 `docker compose up -d` 후 `docker compose logs auth-service`(및 member, cook)에서 **시크릿 누락·DB 연결·OAuth 설정** 관련 오류가 없는지 본다.  
  - (A) 추가로: 스크립트 직후 `grep -v '^#' .env.secrets | cut -d= -f1` 등으로 **키 이름만** 확인하고 값은 로그에 남기지 않는다.  
  - 최종 통합 검증은 Phase 5에서 수행한다.

### 완료 기준

- [🟢] 환경변수 목록이 표로 정리되어 있고, Secrets Manager 시크릿 구조가 일관됨.
- [ ] 선택한 주입 방식(스크립트 또는 앱 내부)이 구현되어, (Phase 5 연계) 앱 실행 시 시크릿이 주입됨.
- [🟢] (4-3-A인 경우) EC2에 스크립트 경로·`OUTPUT_FILE` 경로·compose `env_file`이 문서화되어 있고, **Phase 10에 옮길 원격 명령 순서**가 위 4-3-A **5번**과 동일한지 확인함.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 3
- [Secrets-Manager-환경변수-주입-가이드.md](Secrets-Manager-환경변수-주입-가이드.md)
- [환경변수-설정-가이드.md](환경변수-설정-가이드.md)
- Phase 10(백엔드 job)과의 연계: [Phase 10. 자동화 (CI/CD)](#phase-10-자동화-cicd) — 10-3

---

## Phase 5. 백엔드 기동

### 목표

EC2에서 Docker Compose로 auth-service, member-service, cook-service가 기동되고, RDS·S3·OAuth 설정이 반영되어 헬스체크까지 통과하는 상태가 됩니다. (아직 Nginx·systemd 연동 전이라 수동 실행 기준)

### 이 Phase에서 읽는 순서

1. 아래 **5-1 ~ 5-3**은 **선택 2: Docker Registry(ECR/Docker Hub)** 를 기준으로 한 **권장·본문** 흐름입니다. 이미지는 로컬/CI에서 빌드·push하고, EC2에서는 pull 후 Compose로 기동합니다.
2. **EC2에서 직접 `docker build`** 하거나 **`docker save`/`load`로 tar 전달**하는 방식은 문서 하단 **부록: 다른 이미지 전달 방식**에만 정리해 두었습니다. (선택 1, 선택 3)

### 작업 위치(어디서 작업하나?)

- **로컬/CI**: (선택) JAR 빌드, `Dockerfile.backend`로 이미지 빌드, Registry에 `docker push`
- **EC2(SSH)**: Registry `docker login`·`docker pull`, `docker-compose.yml` 배치·환경 설정, `docker compose up`, 로그·헬스체크

---

### 5-1. Registry 기준: 이미지 빌드·전달·EC2 반영 (선택 2)

백엔드 3개 서비스(auth/member/cook)는 `Dockerfile.backend`(멀티스테이지, Gradle)로 한 이미지에 묶는 전제입니다.

#### 5-1-1. 개념 한 줄

로컬 또는 CI에서 이미지를 빌드해 **Registry에 올리고**, EC2에서는 **같은 태그로 pull**한 뒤 `docker-compose.yml`이 참조하는 로컬 이미지 이름(`knusrae-backend:태그` 등)과 맞춥니다.

#### 5-1-2. 이미지 이름·태그(Compose와 맞추기)

- `docker-compose.yml`에서 쓰는 값과 통일하는 것을 권장합니다.
  - `BACKEND_IMAGE_REPO` (기본값 예: `knusrae-backend`)
  - `BACKEND_IMAGE_TAG` (기본값 예: `latest`)
- CI/CD에서는 `BACKEND_IMAGE_TAG`를 커밋 SHA·버전 태그로 두면 롤백·추적이 쉽습니다.

#### 5-1-3. (선택) 로컬/CI에서 JAR만 먼저 빌드

`Dockerfile.backend`가 빌드 스테이지에서 Gradle을 돌리면 **JAR을 따로 만들지 않아도** 됩니다. 캐시·CI 단계 분리 등으로 JAR을 미리 만들고 싶을 때만 아래를 실행합니다.

- **JAR의 역할**: 이미지에 복사되어 컨테이너에서 `java -jar ...`로 실행되는 산출물입니다.

```bash
# 로컬/CI
./gradlew :backend:auth-service:bootJar :backend:member-service:bootJar :backend:cook-service:bootJar --no-daemon -x test
```

#### [🟢] 5-1-4. 로컬/CI: 이미지 빌드 → Registry push

```bash
# 로컬/CI (저장소 루트에서, Dockerfile.backend가 있는 컨텍스트)
# 1) Dockerfile.backend로 현재 디렉터리(.)를 빌드하고, 로컬에 ${BACKEND_IMAGE_REPO}:${BACKEND_IMAGE_TAG} 이름의 이미지를 만듭니다.
docker build -f Dockerfile.backend -t ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest} .
# 2) 위에서 만든 이미지에 Docker Hub에 올릴 전체 이름(<DOCKERHUB_ID>/리포지토리:태그)을 추가 태그로 붙입니다. (같은 이미지 ID, 이름만 Registry 형식으로 맞춤)
# - Docker Hub에 올리려면 계정/리포지토리:태그 형식이어야 해서, 같은 이미지 ID에 Registry용 이름을 하나 더 붙입니다.
docker tag ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest} <DOCKERHUB_ID>/${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
# 3) Docker Hub에 로그인합니다. 이후 push 시 본인 계정 권한으로 업로드됩니다. (CI에서는 토큰·--password-stdin 등으로 비대화형 로그인)
docker login
# 4) 태그된 이미지를 Docker Hub 원격 저장소로 업로드(push)합니다. EC2 등에서는 이후 docker pull로 같은 태그를 받을 수 있습니다.
docker push <DOCKERHUB_ID>/${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
```

- **ECR 사용 시**: Docker Hub 대신 `aws ecr get-login-password ... | docker login ...` 로 Registry에 로그인한 뒤, ECR 리포지토리 URI로 `tag`·`push` 합니다.

#### [🟢] 5-1-5. EC2: pull → 로컬 태그 정렬 → 이미지 확인

Compose가 기대하는 로컬 이름(`knusrae-backend:태그`)과 Registry 경로가 다를 때, pull 후 한 번 `docker tag`로 맞춥니다.

```bash
# EC2
# 1) Docker Hub(또는 사용 중인 Registry)에 로그인합니다. private 이미지 pull·인증된 레지스트리 접근에 필요합니다.
docker login
# 2) Registry에 올려 둔 이미지를 EC2 로컬로 내려받습니다. (<DOCKERHUB_ID>/리포지토리:태그 = push했을 때와 동일한 이름)
docker pull <DOCKERHUB_ID>/${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
# 3) docker-compose.yml의 image 필드가 기대하는 로컬 이름(${BACKEND_IMAGE_REPO}:${BACKEND_IMAGE_TAG})에 맞추기 위해, 방금 받은 이미지에 동일 내용의 추가 태그를 붙입니다.
# - docker-compose.yml의 image:가 knusrae-backend:latest 이름을 쓰고 있기 때문에 같은 이름으로 별칭을 추가한다.
# - pull로 받아온 레포/이미지명의 이미지는 삭제해도 됨. 단, ID는 두 이미지가 동일하므로 REPOSITORY 명으로 rmi 할 것
docker tag <DOCKERHUB_ID>/${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest} ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
# 4) knusrae-backend 관련 이미지가 로컬에 존재하는지 확인합니다. (REPOSITORY·TAG·IMAGE ID 확인)
docker images | grep knusrae-backend

# 권한 없는 경우 login 부터 sudo 사용 (permission denied)
sudo docker login
sudo docker pull <DOCKERHUB_ID>/${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
sudo docker tag <DOCKERHUB_ID>/${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest} ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
sudo docker images | grep knusrae-backend

```

- **ECR**: 위와 동일하게 ECR 로그인·pull 후, Compose가 쓰는 이름으로 `docker tag` 합니다.

---

### 5-2. EC2에서 Docker Compose 설정

- [🟢] **1.** 프로젝트의 `docker-compose.yml`을 EC2 배포 경로(예: `/home/ubuntu/knusrae/app/`)에 둔다. (Registry 방식이면 **소스 전체가 없어도 되고**, compose·env·필요 시 스크립트만 전달하면 된다.)
  - Compose가 `env_file: ./.env.secrets`를 참조하므로, **같은 디렉터리에 `.env.secrets`가 있어야 한다.**
  - **4-3-A를 이미 따른 경우**(호스트에서 `fetch-secrets-to-env.sh`로 EC2에 `.env.secrets` 생성): 이 단계에서는 **`docker-compose.yml`만** 로컬 → EC2로 복사하면 된다. `.env.secrets`는 EC2에 이미 있으므로 **다시 scp하지 않는다**(덮어쓰기·로컬 부재 오류 방지). 배포 디렉터리는 4-3에서 이미 썼다면 `mkdir`도 생략해도 되고, 없을 때만 아래 `mkdir`을 실행하면 된다(idempotent).
  - **시크릿을 로컬 파일로만 두는 경우**(4-3-A 미사용): `docker-compose.yml`과 `.env.secrets`를 함께 scp한다. (Git에 커밋하지 말고, SSM·배포 파이프라인으로 전달해도 된다.)
  - **로컬 PC → EC2 전달 예시** (`<키.pem>`, `<EC2_PUBLIC_DNS>`는 본인 환경에 맞게 바꿈):

```bash
# 프로젝트 루트에서 실행
# 배포 디렉터리가 없을 때만(4-3에서 이미 만들었다면 생략 가능; 다시 실행해도 무방)
ssh -i <키.pem> ubuntu@<EC2_PUBLIC_DNS> "mkdir -p /home/ubuntu/knusrae/app"
# 4-3-A 완료 후: compose 파일만 전달
scp -i <키.pem> docker-compose.yml ubuntu@<EC2_PUBLIC_DNS>:/home/ubuntu/knusrae/app/
# 4-3-A를 쓰지 않고 로컬 .env.secrets를 올리는 경우에만 아래 줄 추가
# scp -i <키.pem> .env.secrets ubuntu@<EC2_PUBLIC_DNS>:/home/ubuntu/knusrae/app/
```

- [🟢] **2.** RDS 사용 시 `postgres-db` 서비스는 비활성화하거나 제거하고, 환경변수에 RDS 엔드포인트·DB 이름·사용자·비밀번호를 넣는다. (Phase 4에서 주입한 시크릿 또는 `env_file` 활용)
  - 저장소 기준 `docker-compose.yml`에서 `postgres-db`는 `profiles: ["local"]`이므로, **`docker compose up -d`만 실행하면 로컬 Postgres 컨테이너는 기본적으로 기동하지 않는다.** RDS 연결 값은 `.env.secrets` 등에 맞춘다.
  - ⭐ EC2 `.env.secrets` 파일에 DB 정보가 등록되어 있다면 `docker-compose.yml`에 DB 정보가 없어도 됨.

- [🟢] **3.** 각 서비스의 `SPRING_PROFILES_ACTIVE`를 운영용 프로파일(`prod`)로 설정한다.
  - Compose에서 이미 `${SPRING_PROFILES_ACTIVE:-prod}` 기본값이 있으면 추가 작업 없이 `prod`가 적용된다. 명시하려면 배포 디렉터리에 `.env` 파일로 `SPRING_PROFILES_ACTIVE=prod`를 두거나(Compose가 로드), 셸에서 `export SPRING_PROFILES_ACTIVE=prod` 후 아래 명령을 실행한다.
  - ⭐ `docker-compose.yml`에 `SPRING_PROFILES_ACTIVE`이 `prod`로 설정되어 있다면 별도 수정 필요없음.

- [🟢] **4.** EC2에서 실행 순서 (구형 CLI는 `docker-compose`로 바꿔서 동일하게 사용 가능):

```bash
cd /home/ubuntu/knusrae/app
ls -la
# docker-compose.yml, .env.secrets 존재 확인
docker compose config
# 오류 시 파일·환경변수·변수 치환 먼저 수정
docker compose up -d
# 또는 docker compose up -d --no-build
docker compose ps
docker compose logs --tail=80 auth-service
docker compose logs --tail=80 member-service
docker compose logs --tail=80 cook-service
# 실시간 확인: docker compose logs -f auth-service

# 권한 없는 경우 sudo 사용 (permission denied)
# 예시
sudo docker compose up -d --no-build
# --no-build는 compose 파일에서 build:가 존재하기 때문

# sudo 없이 사용(권장)
# ubuntu 사용자를 docker 그룹에 추가
sudo usermod -aG docker ubuntu
# 그룹 적용 및 확인
newgrp docker
docker ps

```

- **확인 방법**: `docker compose config`로 설정 검증 후 `docker compose ps`에서 서비스가 `Up` 상태인지 확인한다.

---

### 5-3. 수동 기동 및 연결 확인

아래 명령은 **EC2**에서 실행한다. 배포 디렉터리로 이동한 뒤 진행한다(예: `cd /home/ubuntu/knusrae/app`). `docker` 데몬 권한이 없으면 `docker` → `sudo docker`로 바꾼다.

- [🟢] **1.** `docker compose up -d` (또는 `docker-compose up -d`)로 서비스를 띄운다.

```bash
cd /home/ubuntu/knusrae/app
# compose에 build:가 있고 이미지는 이미 pull·tag 해 둔 경우
docker compose up -d --no-build
# 로컬에서 빌드까지 compose에 맡기는 경우(소스·Dockerfile이 같은 디렉터리에 있을 때)
# docker compose up -d
docker compose ps
```

- [🟢] **2.** DB 연결 성공 여부를 로그에서 확인한다. (`docker compose logs auth-service` 등)

```bash
cd /home/ubuntu/knusrae/app
# auth-service의 로그를 120줄 확인
docker compose logs --tail=120 auth-service
# member-service의 로그를 120줄 확인
docker compose logs --tail=120 member-service
# cook-service의 로그를 120줄 확인
docker compose logs --tail=120 cook-service
# 키워드만 좁혀 볼 때(서비스·로그 문구는 앱 버전에 따라 다를 수 있음)
docker compose logs auth-service 2>&1 | tail -n 200 | grep -iE 'datasource|postgres|jdbc|hibernate|connection'
```

- [🟢] **3.** S3 연결이 필요한 API가 있다면 해당 기능으로 S3 접근이 되는지 확인한다.

```bash
cd /home/ubuntu/knusrae/app
# 기동 직후 S3/스토리지 관련 오류가 있는지 로그에서 확인 (아무것도 안 떠도 정상)
docker compose logs cook-service 2>&1 | tail -n 200 | grep -iE 's3|amazon|storage|aws'
# 실제로는 앱의 업로드·다운로드 API를 호출하거나, 브라우저/클라이언트로 해당 기능을 한 번 실행해 본 뒤 위 로그를 다시 본다.
```

- [🟢] **4.** OAuth(네이버/구글/카카오) 설정이 적용되어 있는지 설정값·리다이렉트 URI 확인한다.

```bash
cd /home/ubuntu/knusrae/app
# 시크릿에 OAuth 관련 키가 들어 있는지(값은 출력하지 않음 — 키 이름만)
grep -v '^\s*#' ./.env.secrets | grep -v '^\s*$' | cut -d= -f1 | grep -iE 'oauth|naver|google|kakao|client' || true
# 각 제공자 개발자 콘솔에서 리다이렉트 URI·Client ID가 운영 도메인과 일치하는지 수동 점검
# (로그인 시도 후) auth 쪽 로그 확인
docker compose logs --tail=100 auth-service
```

- [🟢] **5.** 각 서비스의 헬스체크 엔드포인트를 호출해 200 응답을 확인한다. (예: auth 8081, member 8083, cook 8082)

```bash
# EC2 셸에서(컨테이너가 같은 호스트의 포트를 publish한 경우)
# Knusrae는 Spring Security에서 GET /health 만 permitAll(common-service HealthController).
# spring-boot-starter-actuator 미사용이므로 /actuator/health 는 401이 정상(가이드 구버전 오류).
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8081/health
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8082/health
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8083/health
# 본문까지 보려면(예상: OK)
curl -s http://127.0.0.1:8081/health
```

- **확인 방법**: 위 **5**에서 HTTP 코드가 `200`인지 확인하고, **2**의 로그에 DB 연결 실패 스택이 반복되지 않는지 본다. (S3·OAuth는 **3**·**4**의 로그·콘솔·실제 API 호출로 교차 확인)

---

### 완료 기준

- [🟢] EC2에서 Docker Compose로 3개 백엔드 서비스가 기동됨.
- [🟢] DB 연결, (사용 시) S3 연결, OAuth 설정이 반영됨.
- [🟢] 헬스체크 엔드포인트가 정상 응답함.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 5
- [Docker-점검-및-개선-사항.md](Docker-점검-및-개선-사항.md)

---

### 부록: 다른 이미지 전달 방식

아래는 **선택 2와 동시에 쓰지 말고**, 필요할 때만 참고합니다.

#### 부록 A — 선택 1: EC2에서 직접 `docker build` (흐름 A)

- **언제 쓰나**: Registry 없이 단순히 올리고 싶을 때, 또는 EC2에 리포지토리를 두고 빌드만 하는 경우.
- **EC2에 필요한 것**: Gradle 프로젝트 소스 + `Dockerfile.backend` + `docker-compose.yml` (예: `git clone` 또는 `scp`/`rsync`).
- **`Dockerfile.backend`가 `COPY`하는 경로**가 있어야 빌드됩니다: `gradlew`, `gradle/`, `build.gradle`, `settings.gradle`, `backend/` 등.
- Gradle이 이미지 빌드 단계에서 JAR을 만들므로 **로컬에서 JAR을 따로 만들 필요는 없습니다.**

```bash
# EC2
cd /home/ubuntu/knusrae/app
# (예시) git clone <REPO_URL> .

docker build -f Dockerfile.backend -t ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest} .
docker images | grep knusrae-backend
```

이후 본문 **5-2**, **5-3** 절차와 동일하게 진행합니다.

#### 부록 B — 선택 3: `docker save` / `docker load`로 tar 전달 (흐름 C, 임시)

- **언제 쓰나**: Registry를 당장 쓰기 어렵고, 이미지 파일만 EC2로 옮겨야 할 때(비권장·임시).
- **로컬**: 이미지 빌드 후 tar 저장

```bash
# 로컬
docker build -f Dockerfile.backend -t ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest} .
docker save -o knusrae-backend.tar ${BACKEND_IMAGE_REPO:-knusrae-backend}:${BACKEND_IMAGE_TAG:-latest}
```

- **로컬 → EC2**: 파일 복사 후 로드

```bash
# 로컬 → EC2 (Linux/macOS 예시)
scp -i <pem키경로> knusrae-backend.tar ubuntu@<EC2_IP>:/home/ubuntu/knusrae/app/

# EC2
cd /home/ubuntu/knusrae/app
docker load -i knusrae-backend.tar
docker images | grep knusrae-backend
```

이후 본문 **5-2**, **5-3** 절차와 동일합니다.

---

## Phase 6. 서비스화

### 목표

서버 재부팅 후에도 백엔드가 자동으로 기동되고, Nginx가 리버스 프록시로 외부 요청을 auth/member/cook 서비스로 전달하여, 퍼블릭 IP 또는 도메인으로 API 호출이 가능한 상태가 됩니다.

### 작업 위치(어디서 작업하나?)

- **EC2(SSH)**: systemd 유닛 작성/등록, Nginx 설정/리로드, 재부팅 검증

### 결정 필요

- **▶ 결정 — systemd 단위**: Docker Compose 사용 시 (A) docker-compose 전체를 하나의 systemd 서비스로 등록, (B) 서비스별로 여러 유닛 구성.  
  - 선택지: (A) 1개 유닛으로 docker-compose up (B) 서비스별 유닛  
  - 권장: (A). 한 번에 `docker compose up -d` 실행하는 단일 유닛이 관리하기 쉬움.  
  - 기록: (A)

### 단계별 작업

#### 6-1. systemd 서비스 등록

**어디서 하나?** EC2에 SSH로 접속한 터미널에서 진행한다. (로컬 PC가 아님)

**전제 조건**: `docker-compose.yml`이 있는 디렉터리(이 문서 예시는 `/home/ubuntu/knusrae/app`)에서 **이미** `docker compose up -d`로 컨테이너가 정상 기동되는 것을 확인한 뒤 systemd에 넘긴다. 수동으로도 안 되면 유닛만 등록해도 해결되지 않는다.

**경로 정리**: 아래 예시에서 `WorkingDirectory`는 반드시 **본인 서버에 실제로 `docker-compose.yml`이 있는 절대 경로**로 바꾼다. (앞 단계에서 `git clone`·배포한 위치와 동일해야 함)

---

- [🟢] **1. 디렉터리와 파일 확인**

  ```bash
  # EC2 (SSH 접속 후)
  cd /home/ubuntu/knusrae/app   # 실제 배포 경로로 변경
  ls -la docker-compose.yml .env.secrets   # compose 파일·시크릿 존재 확인
  docker compose ps                          # 수동 기동 상태 점검(선택)
  ```

- [🟢] **2. systemd 유닛 파일 작성**

  - **어디에 쓰나**: `/etc/systemd/system/`은 **EC2 인스턴스(리눅스)의 로컬 경로**다. 아래 `nano`/`vim`·`systemctl`도 **로컬 PC가 아니라 EC2에 SSH로 접속한 셸에서** 실행한다.
  - **파일 위치**: `/etc/systemd/system/` 아래에 `*.service` 이름으로 둔다. (예: `knusrae-backend.service`)
  - **편집 명령**(EC2 SSH 세션에서, 둘 중 하나):

    ```bash
    sudo nano /etc/systemd/system/knusrae-backend.service
    # 또는
    sudo vim /etc/systemd/system/knusrae-backend.service
    ```

  - **내용 예시**(결정 (A): compose 한 번에 기동). `User`/`Group`은 SSH 접속 계정과 맞춘다. Ubuntu AMI는 보통 `ubuntu`, Amazon Linux는 보통 `ec2-user`. 해당 사용자가 `docker` 그룹에 있어야 compose가 동작한다(`groups`로 확인, 없으면 `sudo usermod -aG docker <사용자>` 후 재로그인).

    ```ini
    [Unit]
    Description=Knusrae backend (Docker Compose)
    After=docker.service network-online.target
    Wants=network-online.target

    [Service]
    Type=oneshot
    RemainAfterExit=yes
    WorkingDirectory=/home/ubuntu/knusrae/app
    # compose/.env에서 쓰는 변수가 있다면 선택: EnvironmentFile=-/home/ubuntu/knusrae/app/.env
    ExecStart=/usr/bin/docker compose up -d
    ExecStop=/usr/bin/docker compose down
    User=ubuntu
    Group=ubuntu
    Restart=on-failure
    RestartSec=10

    [Install]
    WantedBy=multi-user.target
    ```

  - **왜 `Type=oneshot`인가?** `docker compose up -d`는 백그라운드로 컨테이너만 띄우고 **명령이 곧 종료**된다. `Type=simple`로 두면 systemd가 “서비스가 끝났다”고 오인할 수 있어, `oneshot` + `RemainAfterExit=yes` 조합이 흔한 패턴이다.
  - **`docker` 경로**: 다른 환경에서는 `which docker compose`로 실제 경로를 확인해 `ExecStart`/`ExecStop`에 맞출 수 있다. Docker 공식 패키지에서는 `docker compose`가 플러그인으로 동작하므로 위처럼 `/usr/bin/docker compose` 한 줄로 쓰는 방식이 많다.
  - **`Restart=always` 대신 `on-failure`**: compose는 시작 직후 프로세스가 종료되므로 `always`는 의미가 어긋날 수 있다. Docker 데몬 장애 후 재시도가 필요하면 `Restart=on-failure` 정도가 무난하다. (컨테이너 자체 재시작은 `docker-compose.yml`의 `restart` 정책이 담당) 문제 없이 동작하면 `[Service]`에서 `Restart`/`RestartSec` 줄을 아예 빼도 된다.
  - **`ExecStop`**: `docker compose down`은 컨테이너를 내리고 네트워크 등을 정리한다. 중지만 하고 제거는 나중에 하고 싶다면 `ExecStop=/usr/bin/docker compose stop`으로 바꿀 수 있다.

- [🟢] **3. 데몬 리로드·등록·기동**

  ```bash
  sudo systemctl daemon-reload
  sudo systemctl enable knusrae-backend.service
  sudo systemctl start knusrae-backend.service
  sudo systemctl status knusrae-backend.service --no-pager
  ```

  - 실패 시: `journalctl -u knusrae-backend.service -e --no-pager`로 에러 메시지를 확인한다. (권한·경로 오타·`User`가 docker를 쓸 수 없음 등이 흔한 원인)

- [🟢] **4. 컨테이너와 유닛 상태 확인**

  ```bash
  docker ps
  docker compose -f /home/ubuntu/knusrae/app/docker-compose.yml ps
  ```

- [🟢] **5. 재부팅 후 자동 기동 검증**

  ```bash
  sudo reboot
  ```

  - SSH가 다시 붙은 뒤:

    ```bash
    sudo systemctl status knusrae-backend.service --no-pager
    docker ps
    ```

- **확인 방법 요약**: `systemctl status knusrae-backend.service`, `journalctl -u knusrae-backend.service -f`(로그 팔로우), 재부팅 후 `docker ps`에 auth/member/cook 컨테이너가 다시 떠 있는지 확인

#### 6-2. Nginx reverse proxy 구성

**어디서 하나?** EC2에 SSH 접속한 터미널. Nginx는 **호스트(OS)** 에 설치되고, Docker 컨테이너는 `localhost:8081` 등으로만 포트를 열어두는 전제(compose의 `ports`)와 맞물린다.

**왜 필요한가?** 외부(브라우저·프론트)는 보통 **80/443 한 포트**만 쓰고, 서버 안에서는 서비스마다 포트가 다르다(예: auth 8081, cook 8082, member 8083). Nginx가 **한 진입점**에서 URL 경로를 보고 적절한 컨테이너로 넘겨준다(리버스 프록시).

**전제 조건**

- `docker compose ps`로 auth/member/cook 컨테이너가 떠 있고, EC2 안에서 **`GET /health`**로 로컬 포트 응답을 먼저 확인한다. (본 문서 **Phase 5 — 항목 5**와 동일. Knusrae는 `common-service` `HealthController`의 `/health`만 헬스용으로 쓰고 **spring-boot-starter-actuator는 쓰지 않으므로 `/actuator/health`는 헬스 확인에 쓰지 않는다**—401이 나올 수 있음.)

  ```bash
  curl -sS -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8081/health
  curl -sS -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8082/health
  curl -sS -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8083/health
  ```
- EC2 **보안 그룹** 인바운드에 **80/tcp**(HTTP)·HTTPS 쓸 때 **443/tcp**가 열려 있어야 외부에서 Nginx에 닿는다. (SSH 22는 관리용으로만 제한하는 것을 권장)

**Knusrae API 경로 분기(현재 코드 기준)** — `location`은 **위에서 아래로** 더 구체적인 경로를 먼저 두는 편이 안전하다.

| 프리픽스 | 백엔드(컨테이너) | 포트 |
|----------|------------------|------|
| `/api/auth/` | auth-service | 8081 |
| `/api/member/` | member-service | 8083 |
| `/api/cook/` (레시피·검색·카테고리·관리자 API 등) | cook-service | 8082 |

엔드포인트를 추가·분리했다면 위 표와 Nginx `location`을 같이 수정한다.

---

- [🟢] **1. Nginx 설치·기동 확인**(미설치인 경우, Ubuntu/Debian 예시)

  - **이미 설치됐는지 먼저 보려면**(EC2 SSH에서):

    ```bash
    nginx -v
    systemctl status nginx --no-pager
    ```

    - `nginx -v`가 버전을 출력하면 실행 파일이 있다. `command not found`면 패키지가 없을 가능성이 크다.
    - `systemctl status nginx`가 `active (running)`이면 서비스까지 떠 있는 상태다. `Unit nginx.service could not be found`면 아직 설치되지 않은 경우가 많다.
    - (선택) Ubuntu/Debian에서 패키지 등록 여부: `dpkg -l nginx | cat`

  - **미설치이거나 기동이 안 되면** 아래로 설치·기동한다.

  ```bash
  sudo apt update
  sudo apt install -y nginx
  sudo systemctl enable nginx
  sudo systemctl start nginx
  sudo systemctl status nginx --no-pager
  ```

  - Amazon Linux 등이면 `yum`/`dnf`로 패키지명을 맞춘다. 설치 여부는 `nginx -v`, `systemctl status nginx`, 또는 `rpm -q nginx` 등으로 본다. 설정 디렉터리는 보통 `/etc/nginx/`이다.

- [🟢] **2. 서버 블록 파일 만들기**

  - **Ubuntu/Debian**: `sites-available`에 두고 `sites-enabled`에 심볼릭 링크하는 방식이 흔하다.
    - 1) 새 설정 파일 생성
      - Nginx는 sites-enabled에 있는 설정만 “활성”으로 취급하는 경우가 많아서, sites-available에 만든 파일을 심볼릭 링크로 sites-enabled에 걸어 이 사이트 설정을 적용합니다.
      - 파일 생성 후 편집기가 열리면, 3번 내용을 입력한다. (현재 문서 963~1023 라인)
    ```bash
    sudo nano /etc/nginx/sites-available/knusrae-api
    ```

    기본 사이트가 충돌하면 비활성화한다.

    - 2) default 사이트 제거/비활성화
      - 기본 default 가 같은 포트(특히 80)를 쓰면 충돌하거나 예상과 다른 응답이 나올 수 있어서, sites-enabled/default 를 지우거나 빼서 기본 가상 호스트와의 충돌을 막는 단계입니다.
    ```bash
    sudo rm /etc/nginx/sites-enabled/default   # 또는 default를 다른 이름으로 이동
    ```

    - 3) 심볼릭 링크(바로가기) 생성
      - sites-available에 있는 knusrae-api 설정을 sites-enabled에 등록해서 Nginx가 이 사이트 설정을 적용하게 한다
    ```bash
    sudo ln -s /etc/nginx/sites-available/knusrae-api /etc/nginx/sites-enabled/knusrae-api
    ```

  - **Amazon Linux / conf.d 스타일**: `/etc/nginx/conf.d/knusrae-api.conf` 한 파일로 동일 내용을 넣어도 된다.

- [🟢] **3. 설정 내용 예시**(HTTP 80만, 도메인·IP는 본인 값으로 변경)

  - 아래는 **요청 URI를 그대로** 백엔드로 넘기는 형태다(`proxy_pass`에 경로를 붙이지 않음).
  - `server_name`에 `_`(와일드카드)·실제 도메인·또는 퍼블릭 IP를 둘 수 있다. 처음엔 IP만으로 테스트해도 된다.
  - 단, **초기 테스트에서 `server_name _;`를 사용했다면**, 운영 도메인/HTTPS 단계(Phase 8-2, Certbot `--nginx`) 전에 반드시 `server_name api.<도메인>;`처럼 **실제 FQDN으로 교체**해야 한다.
    - 교체하지 않으면 Certbot이 `Could not automatically find a matching server block for ...` 오류를 낼 수 있다.
    - 교체 명령어 예시(Ubuntu/Debian, `sites-available` 방식):
      ```bash
      sudo cp /etc/nginx/sites-available/knusrae-api /etc/nginx/sites-available/knusrae-api.bak
      sudo sed -i 's/server_name _;/server_name api.knusrae.com;/' /etc/nginx/sites-available/knusrae-api
      sudo grep -n "server_name" /etc/nginx/sites-available/knusrae-api
      ```
    - 수동 편집으로 교체하려면:
      ```bash
      sudo nano /etc/nginx/sites-available/knusrae-api
      ```
    - 교체 후 검증 예시:
      ```bash
      sudo nginx -t
      sudo systemctl reload nginx
      sudo grep -Rni "server_name" /etc/nginx/sites-available /etc/nginx/sites-enabled
      ```
  - **권장 운영 패턴**: Phase 6(HTTP/IP 기능 확인)에서는 `_` 또는 IP로 빠르게 검증하고, Phase 8(도메인·HTTPS) 진입 시점에 `api.<도메인>`으로 고정한다.
  - 내용 상세
    - 어떤 호스트명·포트(예: api.example.com, 443)로 들어오는지
    - 리버스 프록시: 요청을 실제로 돌아가는 Spring Boot(또는 다른 백엔드) 주소로 넘길지
    - (선택) SSL, 헤더, 업로드 크기 등

    ```nginx
    server {
        listen 80;
        listen [::]:80;
        server_name _;

        client_max_body_size 50M;

        location ^~ /api/auth/ {
            proxy_pass http://127.0.0.1:8081;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location ^~ /api/member/ {
            proxy_pass http://127.0.0.1:8083;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # LB/운영 점검용 공통 헬스체크 엔드포인트
        # 필요 시 8081/8082/8083 중 기준 서비스 포트로 통일해서 사용
        location = /health {
            proxy_pass http://127.0.0.1:8081/health;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location ^~ /api/cook/ {
            proxy_pass http://127.0.0.1:8082;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
    ```

  - **복사 시**: 문서에 보이는 ` ```nginx ` / ` ``` ` 줄은 넣지 않고, `server {`부터 마지막 `}`까지만 붙여 넣는다. 줄 앞 **불필요한 공백**도 제거한다.
  - **HTTPS(443)**: Let’s Encrypt·인증서·리다이렉트는 [backend-https-option-a-nginx-certbot.md](backend-https-option-a-nginx-certbot.md)를 따른다. 443을 쓰면 `X-Forwarded-Proto`가 프록시 뒤 앱에서 스킴 판별에 쓰인다.
  - `/health` 외부 점검이 필요하면 위 `location = /health`를 유지하고, 아래로 검증한다.
    - `curl -I http://api.<도메인>/health`
    - `curl -I https://api.<도메인>/health`

- [🟢] **4. 문법 검사·리로드**

  ```bash
  sudo nginx -t
  sudo systemctl reload nginx
  ```

  - `nginx -t`가 `successful`이 아니면 메시지에 나온 파일·줄 번호를 고친 뒤 다시 실행한다.
    - 결과 참고
  ```
  nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
  nginx: configuration file /etc/nginx/nginx.conf test is successful
  ```

- [🟢] **5. 동작 확인**

  - EC2 **내부**에서 (auth 프록시 확인용. `GET /api/auth/` 루트는 매핑이 없어 500이 날 수 있음):

    ```bash
    curl -sS -o /dev/null -w "%{http_code}\n" "http://127.0.0.1/api/auth/oauth/state?provider=google"
    ```

    정상이면 **200**이 한 줄로 출력된다.

  - **외부**(로컬 PC 등)에서—퍼블릭 IP 또는 도메인, 실제 존재하는 경로로 바꿈:

    ```bash
    curl -sS -D- "http://<EC2_퍼블릭_IP>/api/..." | head -n 20
    ```

  - 브라우저로 API 문서·헬스 URL이 있으면 동일하게 확인한다.

- **확인 방법 요약**: `nginx -t` 통과, `systemctl status nginx`, 외부에서 `http://<IP또는도메인>/api/...` 응답 코드·본문 확인

#### 6-3. 로그 위치 확인

**어디서 보나?** 대부분 EC2 SSH에서 본다. “로그가 어디 있나”만 알아두면 장애 시 순서대로 열어볼 수 있다.

---

- [🟢] **1. systemd(Compose 기동 유닛)**

  - **저장 위치**: 이전 단계에서 만든 유닛(예: `knusrae-backend.service`).
  - **보는 법**:

    ```bash
    journalctl -u knusrae-backend.service -e --no-pager
    journalctl -u knusrae-backend.service -f
    ```

    - **첫 줄**: `-u knusrae-backend.service`로 **그 유닛에 묶인 로그만** 본다. `--no-pager`는 less 같은 **페이저 없이** 터미널에 한 번에 출력한다. `-e`는 페이저로 볼 때 **맨 끝(최신)**으로 이동하는 옵션이라, 무페이저와도 흔히 같이 쓴다. 출력이 매우 길면 `-n 200` 등으로 줄 수를 제한하는 편이 좋다.
    - **둘째 줄**: `-f`(follow)로 **새로 찍히는 로그를 실시간**으로 이어서 본다. 기동·재시작 직후 오류 확인에 쓰고, 종료는 `Ctrl+C`.

  - `docker compose up -d` 실행 실패·권한 오류 등이 여기에 남는 경우가 많다.

- [🟢] **2. Nginx**

  - **Ubuntu/Debian 기본 경로**(배포판·설정에 따라 다를 수 있음):

    - 액세스 로그: `/var/log/nginx/access.log`
    - 에러 로그: `/var/log/nginx/error.log`

  - **빠른 확인**:

    ```bash
    sudo tail -f /var/log/nginx/error.log
    sudo tail -f /var/log/nginx/access.log
    ```

    - **첫 줄**: Nginx **에러 로그**를 **실시간**(`-f`, follow)으로 본다. 업스트림 연결 실패(502)·설정 오류·SSL 문제 등이 여기에 남는 경우가 많다. 종료는 `Ctrl+C`.
    - **둘째 줄**: **액세스 로그**를 실시간으로 본다. 어떤 URL이 들어왔는지, 응답 코드·리퍼러 등 요청 단위 기록이 쌓인다. 종료는 `Ctrl+C`.

- [🟢] **3. Spring Boot(컨테이너)**

  - 기본적으로 로그는 **컨테이너 표준 출력**으로 나간다.

    ```bash
    cd /home/ubuntu/knusrae/app
    docker compose logs -f --tail=200
    docker compose logs -f auth-service
    docker compose logs -f member-service
    docker compose logs -f cook-service
    ```

    - **`cd`**: `docker-compose.yml`이 있는 디렉터리로 이동한다. 경로는 환경에 맞게 바꾼다. 다른 위치에서 실행하려면 `-f`로 compose 파일 경로를 지정해도 된다.
    - **`docker compose logs -f --tail=200`**: 정의된 **모든 서비스** 로그를 **최근 200줄**부터 보여 주고, 이후 **실시간**으로 이어 붙인다(`-f`). 종료는 `Ctrl+C`.
    - **아래 세 줄**: 각각 **auth-service / member-service / cook-service 컨테이너만** 실시간 로그로 본다. 한 서비스만 의심될 때 출력이 적어서 보기 쉽다. 종료는 `Ctrl+C`.

  - 파일로 별도 마운트하지 않았다면 호스트의 `/var/log/...`에 JAR 로그가 자동 생기지는 않는다.

- [🟢] **4. (선택) 정리 표**

  | 구분 | 확인 명령 / 경로 |
  |------|------------------|
  | systemd | `journalctl -u knusrae-backend.service` |
  | Nginx | `/var/log/nginx/access.log`, `error.log` |
  | 애플리케이션 | `docker compose logs` (서비스명 지정 가능) |

- **확인 방법 요약**: 의도적으로 잘못된 URL을 한 번 호출해 Nginx access/error와 `docker compose logs`에 흔적이 남는지 본다.

### 완료 기준

- [🟢] systemd로 서버 재부팅 후 백엔드 자동 기동됨.
- [🟢] Nginx를 통해 퍼블릭 IP(또는 도메인)로 API 호출 가능.
- [🟢] 로그 위치를 알고 있음.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 5-2, 5-3

---

## Phase 7. 프론트엔드 배포

### 목표

Vue SPA 빌드 산출물이 S3에 업로드되고, CloudFront 배포를 통해 사용자가 프론트엔드를 열 수 있으며, API Base URL·SPA 라우팅(새로고침)·로그인 흐름이 동작하는 상태가 됩니다.

### 이 Phase에서 읽는 순서

1. **7-1**에서 로컬/CI로 빌드해 `dist/`를 만든다.
2. **7-2**에서 S3·CloudFront를 준비하고 산출물을 올린 뒤 무효화까지 한다.
3. **7-3**에서 API URL·OAuth·CORS를 맞추고, 필요하면 **7-1~7-2를 다시** 돌린다(환경변수는 빌드 시 박히므로).

### 작업 위치(어디서 작업하나?)

- **로컬/CI**: Vue 빌드(`npm run build`)
- **AWS 콘솔 또는 AWS CLI**: S3 업로드, CloudFront 설정
- **브라우저 테스트**: 도메인 접속, 로그인/새로고침/API 호출 점검

### 결정 필요

- **▶ 결정 — CloudFront 도메인**: 배포 생성 시점에 커스텀 도메인(예: www.knusrae.com)을 연결할지, 나중에 추가할지.  
  - 선택지: (A) 생성 시 도메인 연결 (B) 나중에 추가  
  - 도메인 미보유 시 (B) 선택. 배포 생성 후 기본 CloudFront URL(예: d1234abcd.cloudfront.net)로 먼저 테스트 가능.  
  - 기록: (A) 도메인은 현재 구매가 완료된 상황이야 (www.knusrae.com)

### 단계별 작업

#### 7-1. 프론트 빌드

**어디서 하나?** **로컬 PC** 또는 **CI 러너**(GitHub Actions 등). **EC2가 아님.**

**전제 조건**: Node.js LTS·npm(또는 pnpm 등 팀 표준) 설치. Knusrae 프론트 소스는 저장소 **`frontend/`** 디렉터리에 있다.

**환경변수(Vite)**: 빌드 시점에 `import.meta.env`로 박힌다. 프로덕션용 값은 **`frontend/.env.production`**(저장소에 올리지 않는 경우 로컬만 생성) 또는 CI 시크릿으로 주입한다. 이 프로젝트에서 쓰는 키 예시는 아래와 같다(실제 URL·클라이언트 ID는 본인 값으로).

| 변수 | 의미 |
|------|------|
| `VITE_API_BASE_URL_AUTH` | auth-service 공개 URL(예: `https://api.example.com` 또는 Nginx 단일 도메인이면 동일 베이스) |
| `VITE_API_BASE_URL_MEMBER` | member-service 공개 URL |
| `VITE_API_BASE_URL_COOK` | cook-service 공개 URL |
| `VITE_*_REDIRECT_URI` | OAuth 콜백 URL — **백엔드**가 받는 주소(예: `https://api.../api/auth/google/callback`) |
| `VITE_*_CLIENT_ID` | 각 프로바이더 콘솔의 클라이언트 ID |

Nginx로 **한 도메인에 `/api/auth/` 등만** 올린 경우 세 베이스 URL을 **같은 오리진**(예: `https://api.knusrae.com`)으로 두어도 된다(`constants.ts`가 서비스별로 요청을 나눔).

**프론트 HTTPS + API만 HTTP(mixed content)**:

- CloudFront 기본(`https://xxxx.cloudfront.net`)이나 **HTTPS** 커스텀 도메인으로 프론트를 열면, 브라우저가 **HTTPS 페이지에서 `http://` API로 보내는 XHR/fetch**를 **차단**하는 경우가 많다.
- 그래서 `.env.production`에 **`http://탄력적IP`** 만 넣어 배포하면, **브라우저에서 로그인·API 호출이 실패**할 수 있다(EC2 `curl` 등은 HTTP로도 동작할 수 있음).
- **권장 순서**: 사용자에게 맞출 최종 형태는 **프론트·API 모두 HTTPS**이므로, `VITE_API_BASE_URL_*`·`VITE_*_REDIRECT_URI`에는 가능하면 **`https://` + 도메인**(또는 인증서를 쓴 API 호스트)을 넣는다. 인증서·Nginx는 **Phase 8** 및 [backend-https-option-a-nginx-certbot.md](backend-https-option-a-nginx-certbot.md) 등으로 맞춘 **뒤**(또는 **동시에**) 프로덕션 빌드를 고정하면 된다.
- Phase 8 전에만 잠깐 검증할 때: 프론트를 **HTTP로만** 열 수 있는지(CloudFront 뷰어 프로토콜·접속 URL) 확인하거나, 브라우저 E2E는 API HTTPS 적용 이후로 미룬다.

---

- [🟢] **1. 의존성 설치**

  ```bash
  # 로컬 (저장소 클론 루트에서)
  cd frontend
  npm ci
  ```

  - **`cd frontend`**: Vue·Vite 프로젝트 루트로 이동한다.
  - **`npm ci`**: `package-lock.json` 기준으로 깨끗이 설치한다(CI와 동일 권장). 로컬만 빠르게 쓸 때는 `npm install`도 가능하나 lockfile 불일치에 주의한다. ci는 clean install에 가깝게 동작한다.

- [🟢] **2. 프로덕션 빌드**

  ```bash
  npm run build
  ```

  - **`npm run build`**: `vue-tsc`로 타입 검사 후 `vite build`를 실행한다. 성공 시 산출물은 기본 **`frontend/dist/`** 에 생성된다.

- [🟢] **3. 산출물 확인**

  ```bash
  ls -la dist
  test -f dist/index.html && echo "index ok"
  ```

  - Windows PowerShell이면: `Get-ChildItem dist`, `Test-Path dist\index.html`
    - 그외 참고 명령어
      - -Force는 숨김 항목까지 표시
        - `Get-ChildItem dist -Force`
        - `dir dist -Force`
      - ls -l처럼 자세한 한 줄 목록
        - `Get-ChildItem dist -Force | Format-Table Mode, LastWriteTime, Length, Name`

- **확인 방법**: `dist/index.html` 존재, `dist/assets/` 등에 JS·CSS 번들이 생성됨.

#### 7-2. S3 업로드 및 CloudFront 배포 생성

**어디서 하나?**

| 작업 | 권장 위치 |
|------|-----------|
| S3 버킷·CloudFront 배포 **생성·점검**, OAC·403/404→`index.html`·커스텀 도메인 | **AWS 관리 콘솔**(브라우저). CloudFront **신규 생성** 절차는 [cloudfront-creation-guide.md](cloudfront-creation-guide.md). Knusrae는 배포 **`knusrae-frontend-distribution`** 을 이미 둔 뒤 **7-2**에서 점검·설정을 이어간다. |
| 빌드 산출물 **업로드**·**캐시 무효화** | 콘솔 **또는** **로컬/CI**에서 **AWS CLI**( `aws configure` 또는 IAM 역할로 자격 증명이 잡힌 환경) |

**콘솔로 할 때** — 마법사 단계·OAC 자동 설정 등은 [cloudfront-creation-guide.md](cloudfront-creation-guide.md)와 같이 진행하면 된다. 아래는 **AWS 관리 콘솔**에서 어떤 메뉴로 무엇을 설정하는지만 단계별로 적었다.

- [🟢] **1. S3에 `dist/` 내용 업로드 (버킷 루트)**

  1. 콘솔 검색에서 **S3** → **버킷(Buckets)** → Phase 2에서 만든 **프론트 전용 버킷**을 연다.
  2. **객체(Objects)** 탭 → **업로드(Upload)**.
  3. **파일 추가(Add files)** 또는 **폴더 추가(Add folder)** 로 올릴 대상을 지정한다. 둘 다 사용해도 된다.
     - **피해야 할 것**: **폴더 추가**로 **`dist` 폴더 자체**를 지정해 올리는 방식이다. 이렇게 하면 객체 키가 `dist/index.html`처럼 **`dist/` 접두사**가 붙는 경우가 많아, 버킷 루트의 `index.html`을 기대하는 CloudFront 설정과 어긋난다.
     - **권장**: `dist`를 연 뒤 **`assets` 폴더**는 **폴더 추가**로, 루트에 있는 **`index.html` 등 파일**은 **파일 추가**로 올리거나, 한 번에 하려면 `dist` **안쪽만** 대상이 되도록 선택해 **`index.html`이 버킷 최상위**, `assets/`가 그 아래에 오게 한다. (CLI는 `aws s3 sync ./dist/ s3://버킷/` 처럼 `dist/` 끝 슬래시로 “내용만” 동기화하는 것과 같다.)
  4. 화면 아래쪽 **권한(Permissions)** 등을 지나갈 때, 최근 버킷에서는 **「다른 AWS 계정에 퍼블릭 액세스 및 액세스 권한을 부여합니다」**, **「객체 소유권에 대해 버킷 소유자 적용… 버킷 정책을 사용하여 액세스를 제어」** 같은 **안내문만** 보이고, 예전처럼 **Grant public read access** 체크박스가 없을 수 있다. 이는 **객체 소유권 = 버킷 소유자 강제(Bucket owner enforced)** 로 **ACL이 비활성**인 일반적인 설정이기 때문이다. 이 경우 업로드 단계에서 **별도로 끌 스위치가 없어도 정상**이며, 공개 여부는 **버킷의 퍼블릭 액세스 차단**과 **버킷 정책(CloudFront OAC용)** 으로만 제어된다.
  5. **속성(Properties)**·스토리지 클래스 등은 기본값으로 두어도 된다. 준비가 끝났으면 **업로드(Upload)** 를 누르면 전송이 시작되고, 완료 후 버킷 **객체** 목록으로 돌아온다.
  6. 객체 목록에 `index.html`과 `assets/`(또는 그 안의 번들 파일)이 보이는지 확인한다. ACL이 꺼진 버킷에서는 객체 **권한** 탭에도 예전 방식의 퍼블릭 ACL 목록이 없을 수 있으므로, 대신 버킷 **권한** 탭에서 **퍼블릭 액세스 차단**과 **버킷 정책**(CloudFront 서비스 프린시펄 허용)이 기대대로인지 보면 된다. (구형 설정으로 **ACL이 켜진** 버킷만 업로드 화면에 퍼블릭 읽기 옵션이 남아 있을 수 있으며, 그때는 **퍼블릭 ACL을 부여하지 않는다**.)

- [🟢] **2. CloudFront 기존 배포 점검 (`knusrae-frontend-distribution`) · S3 오리진 · OAC**

  Knusrae 프론트용 CloudFront 배포는 **이미 생성해 둔 상태**로 진행한다. 콘솔에서 배포 **이름(Distribution name)** 이 **`knusrae-frontend-distribution`** 인 항목을 연다(목록 상단 검색창에 이름을 넣어도 된다).

  1. **CloudFront** 콘솔 → **Distributions** → **`knusrae-frontend-distribution`** 클릭.
  2. **General** 탭: **Distribution domain name**(예: `dxxxx.cloudfront.net`)과 **Distribution ID**를 메모한다. 이후 CLI 무효화·스크립트에 쓴다.
  3. **Origins** 탭: 오리진이 **7-2 항목 1**에서 쓰는 **프론트 전용 S3 버킷**을 가리키는지 확인한다. 도메인 형태는 보통 `버킷명.s3.리전.amazonaws.com` 이다.
  4. **OAC 확인 위치(콘솔)**: 같은 배포 화면에서 **Origins** 탭을 연 상태로, 목록의 **S3 오리진** 행을 선택한 뒤 **Edit**(또는 오리진 이름/ID 링크)를 누른다. 편집 패널을 아래로 스크롤하면 **Origin access** 블록이 있다. 여기서 **Origin access control settings (recommended)** 가 선택되어 있고, **Origin access control** 드롭다운에 OAC 이름(예: `xxxx` 또는 자동 생성 이름)이 지정되어 있으면 OAC가 연결된 것이다. **Public** 이거나 **Legacy access identities (OAI)** 만 보이면 OAC가 아니다. OAC로 바꾸는 절차는 [cloudfront-creation-guide.md](cloudfront-creation-guide.md) **2-6절**을 따른다. **버킷 정책**은 **S3** 콘솔 → 해당 **프론트 버킷** → **Permissions(권한)** → **Bucket policy** 에서 확인한다(CloudFront가 `s3:GetObject` 할 수 있는 `Service`: `cloudfront.amazonaws.com` 조건 등이 있는지 본다).
  5. **Behaviors** 탭 → 목록에서 **Default** 로 표시된 캐시 동작을 연 뒤 **Edit**: **Viewer protocol policy** 는 가능하면 **Redirect HTTP to HTTPS** 로 둔다.
  6. **신규로 배포를 만들 경우**에만 [cloudfront-creation-guide.md](cloudfront-creation-guide.md) 마법사를 따른다. 생성 시 **Distribution name** 을 `knusrae-frontend-distribution` 으로 두면 이 문서와 이름이 일치한다.

- [🟢] **3. Default root object = `index.html`** (`knusrae-frontend-distribution`)

  1. **`knusrae-frontend-distribution`** 배포 → **설정**(영문 **General**) → **편집(Edit)**.
  2. **기본 루트 객체(Default root object)** 에 `index.html`만 입력한다(앞에 `/` 없음). **저장(확인) 후 마법사가 닫히는 것만으로는 끝이 아니다** — 변경은 엣지 전 세계로 퍼지며 보통 수 분 걸린다.
     - **「상태」는 어디에 있나**: **배포 목록** 표의 **상태** 열에서만 본다. 배포 한 걸 눌러 들어간 **설정** 화면(이름, 설명, 가격 분류, HTTP 버전, 대체 도메인, 기본 루트 객체 등만 있는 영역)에는 **전파 진행 전용 상태**가 **없는 UI가 많다**. 문서에 적었던 **마지막 수정·배너**도 이 화면에 안 나올 수 있다.
     - **상태** 열 값은 대개 **배포 ON/OFF**(예: **활성화됨**)뿐이라, **저장 직후에도 바뀌지 않는 것이 정상**이다. 전파 완료 여부는 이 열만으로는 알기 어렵다.
     - **실무 확인**: (선택) 상세 화면 **맨 위**에 일시적인 안내 배너가 뜨는 경우는 있으나 없을 수 있다. **배포 목록**으로 돌아가 표를 **새로고침**해 보거나, **2~5분 뒤** 목록의 **Domain name** URL로 접속·새로고침해 **동작**으로 확인하는 것이 가장 확실하다.

- [🟢] **4. SPA용 사용자 지정 오류 응답 (403·404 → `index.html`, 200)** (`knusrae-frontend-distribution`)

  Vue Router **history** 모드에서는 `/recipe/1`처럼 하위 경로로 직접 들어가거나 새로고침할 때 S3가 객체를 찾지 못해 **403** 또는 **404**가 날 수 있다. CloudFront가 그때도 `index.html`을 내려주도록 설정한다(표·의미는 [cloudfront-creation-guide.md](cloudfront-creation-guide.md) **2-7절**).

  1. **`knusrae-frontend-distribution`** 선택 → **Error pages** / **Custom error responses** → **Create custom error response**(또는 동등 메뉴).
  2. **HTTP error code** **403**: **Response page path** `/index.html`, **HTTP Response code** **200**(콘솔에 “Replace with 200” 유형 표기가 있으면 그에 맞춤).
  3. **404**에 대해 동일하게 한 줄 더 추가한다.
  4. 저장·전파 후 **해당 배포의** CloudFront 도메인에서 하위 경로로 새로고침했을 때 앱이 뜨는지 확인한다.

- [🟢] **5. 도메인 · ACM · DNS** (`knusrae-frontend-distribution`)

  - **(B) 커스텀 도메인 없이 먼저 검증**: 위 **4**까지 적용된 뒤 브라우저에서 **`knusrae-frontend-distribution`** 의 **Distribution domain name**( `https://dxxxx.cloudfront.net` )만으로 동작을 확인한다. 이 경로에서는 **ACM·Route 53 설정이 필수는 아니다.**

  - **(A) 자체 도메인(예: `www.knusrae.com`) 연결** — Phase 7 결정에 도메인이 있으면 여기에 맞춘다. **콘솔에서의 상세 절차**(ACM·DNS 검증·CloudFront·Route 53)는 [AWS-ACM-CloudFront-커스텀-도메인-가이드.md](AWS-ACM-CloudFront-커스텀-도메인-가이드.md)를 참고한다.

    1. **Certificate Manager(ACM)** 콘솔에서 **리전을 us-east-1(버지니아 북부)** 로 맞춘다. CloudFront에 연결하는 퍼블릭 인증서는 **이 리전에서만** 선택할 수 있다.
    2. **Request certificate** → 퍼블릭 → 도메인에 `www.knusrae.com` 등 필요한 FQDN 입력 → **DNS validation** → Route 53에 검증용 CNAME을 생성·전파 완료까지 대기.
    3. CloudFront → **`knusrae-frontend-distribution`** → **Edit** → **Alternate domain name (CNAME)** 에 동일한 호스트 이름 추가 → **Custom SSL certificate**에서 위 ACM 인증서 선택 → 저장.
    4. **Route 53**(또는 사용 중인 DNS)에서 `www` 등에 **별칭(Alias) A 레코드**로 **`knusrae-frontend-distribution`** 배포를 지정하거나, 정책에 따라 **CNAME**을 해당 배포의 `dxxxx.cloudfront.net`에 연결한다.
    5. DNS 반영 후 `https://www.knusrae.com` 등으로 접속해 인증서·HTTPS 리다이렉트·SPA 새로고침을 확인한다.

**AWS CLI 예시(로컬 또는 CI)** — 자격 증명이 해당 계정·버킷에 `s3:PutObject` 등 권한이 있어야 한다.

```bash
# 새 빌드 테스트
# 1) 새 빌드(dist/)가 S3(프론트 버킷)에 반영되는지 확인
# 2) 사용자가 브라우저에서 새 버전을 보려면, CloudFront가 예전 파일을 계속 줄 수 있으니 캐시를 비우거나(무효화) TTL이 지나도록 기다린다

# 로컬: frontend/dist 가 이미 있어야 함
cd frontend

# 1) 로컬 dist/ 내용을 버킷 루트와 동기화. --delete 는 S3에만 있고 로컬에 없는 오브젝트를 지워 오래된 해시 파일을 정리한다.
aws s3 sync ./dist/ s3://<프론트버킷이름>/ --delete

# 2) CloudFront 엣지 캐시를 비워 방금 올린 파일이 보이게 한다. <배포ID> 는 콘솔 CloudFront 배포 목록에서 확인.
aws cloudfront create-invalidation --distribution-id <배포ID> --paths "/*"

# AWS CLI 설치되지 않은 경우 (powershell에서 설치)
winget install Amazon.AWSCLI
```

- **`aws s3 sync ./dist/ s3://버킷/`**: `dist` 아래 파일을 S3에 맞춰 올린다. 경로는 **프로젝트의 `frontend`에서** 실행했다는 전제(`./dist/`).
- **`--delete`**: S3에만 남아 있는 예전 빌드 파일을 삭제한다. 잘못된 버킷이면 데이터가 지워지므로 **버킷 이름을 반드시 검증**한다.
- **`create-invalidation --paths "/*"`**: 배포 전체 캐시 무효화. 자주 하면 비용이 붙을 수 있어 운영에서는 경로를 좁히거나 Phase 10에서 정책을 정한다.

- **확인 방법**: 브라우저에서 CloudFront 도메인 또는 커스텀 도메인으로 접속해 Vue 앱이 로드되는지 본다.

#### 7-3. API 주소 및 OAuth·CORS 연결

**어디서 하나?**

| 작업 | 위치 |
|------|------|
| API 베이스 URL·OAuth 클라이언트 ID 등 **프론트 빌드 변수** | **로컬**(`.env.production` 수정) → **`npm run build` 다시** → S3 sync 반복 |
| OAuth **승인 Redirect URI**·JavaScript 출처 | **네이버·구글·카카오 개발자 콘솔**(브라우저) |
| **CORS 허용 Origin** | **백엔드(각 서비스) 환경변수** — EC2에서 `docker compose`가 읽는 `.env` / `.env.secrets` 등. Spring 속성 `app.cors.allowed-origins`(쉼표 구분) |

---

- [🟢] **1. 프론트 API URL 반영**

  - **Knusrae 코드 기준**: `VITE_API_BASE_URL_AUTH`, `VITE_API_BASE_URL_MEMBER`, `VITE_API_BASE_URL_COOK`를 **브라우저가 호출 가능한 공개 URL**(Elastic IP·도메인·Nginx 단일 호스트)로 맞춘다.
  - 값을 바꾼 뒤에는 **반드시 다시 `npm run build`** 하고 **7-2 업로드·무효화**를 반복한다(정적 파일이라 배포 없이 서버만 바꿔서는 반영되지 않음).
  - **적용 확인 체크리스트(권장 순서)**:
    1. 브라우저 개발자도구 **Network** 탭에서 API 요청 대상 호스트가 `localhost`가 아니라 운영 API 호스트인지 확인한다. (나는 브라우저 console에서 메인화면 오류에 나와있는 호출 URL 보고 확인 완료함)
    2. 로그인/회원/요리 API 호출이 200대 응답 또는 기대한 인증 흐름으로 동작하는지 확인한다.
    3. 오류가 나면 먼저 `npm run build`를 다시 수행했는지, 이후 S3 sync와 CloudFront invalidation(`/*`)까지 실행했는지 역순으로 점검한다.
  - **빠른 완료 기준**: 배포 URL에서 새로고침 후에도 API 요청이 운영 호스트로 나가고 CORS 오류가 없으면 반영 완료로 본다.

- [🟢] **2. OAuth Redirect URI 정합**

  - 프론트가 여는 로그인 URL은 브라우저·프로바이더로 가지만, **콜백은 백엔드**가 받는다. 각 콘솔에 등록하는 Redirect URI는 **`https://<백엔드공개호스트>/api/auth/{naver|google|kakao}/callback`** 형태가 코드·`VITE_*_REDIRECT_URI`와 일치해야 한다.
  - 프론트 Origin이 `https://www.knusrae.com`이어도, Redirect URI는 **API 호스트**를 쓰는지(현재 구조) 문서·콘솔을 맞춘다.

- [🟢] **3. 백엔드 CORS**

  - `SecurityConfig`는 `app.cors.allowed-origins`를 읽는다. Docker 환경에서는 예를 들어 다음과 같이 줄 수 있다(실제 도메인으로 치환).

    ```bash
    # EC2: compose/.env 등 (팀 규칙에 맞는 파일). 쉼표로 여러 Origin 허용.
    # 예: APP_CORS_ALLOWED_ORIGINS=https://www.knusrae.com,https://d1234abcd.cloudfront.net
    ```

  - 환경변수 이름은 Spring Boot 완화 바인딩 규칙에 맞춘다(`APP_CORS_ALLOWED_ORIGINS` → `app.cors.allowed-origins`). 변경 후 컨테이너 **재시작**이 필요하다.

- [ ] **4. 브라우저 통합 확인**

  - 로그인 플로우, API 호출, **하위 경로에서 새로고침**(SPA) 시 404 없이 동작하는지 본다.

- **확인 방법**: 개발자 도구 Network 탭에서 API가 **CORS 오류 없이** 200대 응답인지, 로그인 후 쿠키·리다이렉트가 기대대로인지 확인한다.

### 운영 권장 순서 (중요)

- [🟢] **1.** 이 문서 **Phase 4~7** 기준으로 **수동 배포를 1회 성공**시킨다.
- [ ] **2.** 성공한 수동 절차를 그대로 GitHub Actions 등 **Phase 10** 자동화에 옮긴다.
- [ ] **3.** CI/CD 이후에도 장애 시 **EC2·콘솔·CLI 수동 복구** 절차는 유지한다.

### 완료 기준

- [🟢] 프론트 빌드 산출물이 S3에 업로드되고 CloudFront로 서빙됨.
- [🟢] API 주소·OAuth·CORS가 반영되어 로그인 및 API 호출이 동작함.
- [🟢] 직접 URL 접근 및 새로고침 시에도 정상 표시됨.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 6
- [cloudfront-creation-guide.md](cloudfront-creation-guide.md)

---

## Phase 8. 운영 공개 준비 (도메인 / HTTPS)

### 목표

도메인 연결, HTTPS 적용, OAuth Redirect URI·CORS 최종 반영이 되어 운영 도메인에서 소셜 로그인까지 포함해 서비스가 안전하게 공개된 상태가 됩니다.

### 결정 필요

- **▶ 결정 1 — 도메인 보유 여부**: 도메인을 이미 보유했는지, 구매할 예정인지.  
  - 있음 → 서브도메인 전략(www, api) 결정. 없음 → Elastic IP로 1차 테스트 후 도메인 연동.  
  - 기록: 도메인 이미 보유하고 있음. 서브도메인 전략 있음.
    - CNAME 타입 : www / CloudFront Domain name
    - A 타입 : api / EC2 Elastic IP

- **▶ 결정 2 — 백엔드 HTTPS**: (A) Nginx + Certbot(Let's Encrypt), (B) ALB + ACM.  
  - 선택지: (A) Nginx + Certbot (B) ALB + ACM  
  - 권장: (A). 비용·구성 단순함.  
  - 기록: (A)로 진행

### 단계별 작업

#### 8-1. 도메인 연결

- [🟢] **1.** 도메인 구매 또는 보유 도메인 사용. (결정 1)
- [🟢] **2.** DNS에서 프론트(예: www), 백엔드(예: api) 서브도메인을 해당 리소스(CloudFront 배포 도메인, EC2 Elastic IP)로 설정한다. (A 레코드 또는 CNAME)
- **확인 방법**: `dig www.<도메인>` 또는 브라우저에서 해당 도메인 접속 시 프론트/API 연결 확인

#### 8-2. HTTPS 인증서 적용

- [🟢] **1.** 프론트(S3+CloudFront): ACM 인증서 발급 후 CloudFront 배포에 커스텀 도메인·인증서 연결. (Phase 7에서 미연결 시 여기서 진행)
- [ ] **2.** 백엔드: 결정 2에서 (A) 선택 시 Nginx에 Certbot으로 인증서 발급·설정. (B) 선택 시 ALB에 ACM 인증서 연결.
- [ ] **3.** HTTP → HTTPS 리다이렉트 설정. (Nginx 또는 ALB/CloudFront)
- **확인 방법**: `https://www.<도메인>`, `https://api.<도메인>` 접속 시 자물쇠 표시 및 정상 응답

##### 8-2-A. 백엔드 HTTPS 상세 절차 (A안: Nginx + Certbot, 권장)

> 전제: `api.<도메인>` DNS가 EC2 Elastic IP를 가리키고, EC2 Security Group에 `80/443` 인바운드가 허용되어 있어야 한다.

- [🟢] **A-1. 서버 접속 및 사전 점검**
  - 로컬 터미널에서:
    - `ssh -i <키파일.pem> ubuntu@<EC2_PUBLIC_IP>`
  - EC2에서:
    - `sudo nginx -t` (Nginx 설정 문법 확인)
    - `curl -I http://api.<도메인>` (HTTP 응답 확인)
  - 실패 시 우선 확인:
    - Route 53(또는 DNS 제공사)에서 `api` 레코드 대상
    - EC2 Security Group 인바운드 `80`, `443`
    - NACL/방화벽 차단 여부

- [🟢] **A-2. Certbot 설치 (Ubuntu 기준)**
  - `sudo apt update`
  - `sudo apt install -y certbot python3-certbot-nginx`
  - 설치 확인:
    - `certbot --version`

- [🟢] **A-3. 인증서 발급 + Nginx 자동 설정**
  - 실행 전 필수 점검(`server_name` 매칭):
    - `sudo grep -Rni "server_name" /etc/nginx/sites-available /etc/nginx/sites-enabled`
      - Nginx 활성 설정 파일들에서 `server_name` 선언을 모두 찾아, 도메인 매칭 여부를 확인한다.
    - 결과에 `server_name api.<도메인>;`가 없으면 Certbot이 Nginx 서버 블록을 자동 매칭하지 못할 수 있다.
  - `server_name` 미설정 시 선행 조치:
    - `sudo cp /etc/nginx/sites-available/knusrae-api /etc/nginx/sites-available/knusrae-api.bak`
      - 현재 설정을 백업해, 수정 오류 시 즉시 원복할 수 있게 한다.
    - `sudo sed -i 's/server_name _;/server_name api.<도메인>;/' /etc/nginx/sites-available/knusrae-api`
      - `server_name _;`를 실제 도메인 값으로 자동 치환한다.
    - `sudo nginx -t && sudo systemctl reload nginx`
      - 설정 문법 검증 후, 문제 없을 때 Nginx에 변경 사항을 반영한다.
    - `sudo grep -n "server_name" /etc/nginx/sites-available/knusrae-api`
      - 최종 파일에서 `server_name`이 기대 값으로 바뀌었는지 다시 확인한다.
  - 명령 실행:
    - `sudo certbot --nginx -d api.<도메인> --redirect -m <운영메일> --agree-tos --no-eff-email`
      - 인증서 발급/적용을 수행하고 HTTP 요청을 HTTPS로 자동 리다이렉트하도록 설정한다.
      - 명령어 실행 시 선택 문구가 뜨면 1번 선택
        - 1: Attempt to reinstall this existing certificate
          - 기존 인증서를 Nginx에 다시 연결/적용하는 용도
        - 2: Renew & replace the certificate (may be subject to CA rate limits)
          - 아직 갱신 시점이 아닌데 새로 발급 시도라 불필요
          - CA rate limit(발급 제한) 리스크 있음
        - 선택 후 확인
          - `sudo nginx -t && sudo systemctl reload nginx`
            - Certbot이 수정한 Nginx 설정이 정상인지 재검증하고 재적용한다.
          - 실제 HTTPS/리다이렉트 동작 검증은 바로 다음 **A-4 단계**에서 수행한다.
  - 이 명령이 수행하는 작업:
    - 도메인 소유 검증(HTTP-01)
    - 인증서 발급
    - Nginx `443` 서버 블록 설정
    - HTTP(`80`) 요청을 HTTPS로 리다이렉트 설정
  - 적용 확인:
    - `sudo nginx -t && sudo systemctl reload nginx`

- [🟢] **A-4. 동작 확인(서버/브라우저)**
  - 서버에서:
    - `curl -I https://api.<도메인>`
    - `curl -I http://api.<도메인>` (결과가 `301` 또는 `308`이면 정상)
  - 브라우저에서:
    - `https://api.<도메인>/health` 또는 실제 서비스 엔드포인트 접속
    - 참고: 현재 Nginx 예시는 `/api/*` 경로만 프록시하므로, `/health`를 외부에서 직접 확인하려면 Nginx에 `location = /health` 라우팅을 추가하거나 EC2 내부에서 `http://127.0.0.1:8081|8082|8083/health`로 점검한다.
      - EC2 서버의 Nginx 설정 파일에 반영 필요
        - 1) 파일 열기
          - `sudo nano /etc/nginx/sites-available/knusrae-api`
        - 2) 내용 추가(Phase 6 1036~)
        - 3) 검사/반영
          - `sudo nginx -t`
          - `sudo systemctl reload nginx`
        - 4) 브라우저 재접속
          - `https://api.<도메인>/health`
    - 자물쇠 아이콘 및 인증서 발급자(Let's Encrypt) 확인

- [🟢] **A-5. 자동 갱신 점검**
  - dry-run:
    - `sudo certbot renew --dry-run`
  - 타이머 확인:
    - `systemctl list-timers | grep certbot`
  - 로그 확인:
    - `sudo journalctl -u certbot --no-pager | grep -Ei "renew|error|failed"`

##### 8-2-B. 백엔드 HTTPS 상세 절차 (B안: ALB + ACM)

> 전제: EC2는 Private/ Public 어느 쪽이든 ALB Target Group으로 라우팅 가능해야 하며, 도메인 `api.<도메인>`을 ALB DNS로 연결할 수 있어야 한다.

- [ ] **B-1. ACM 인증서 발급 (AWS Console)**
  - 콘솔 경로: `AWS Console -> Certificate Manager(ACM) -> Request`
  - 선택: `Request a public certificate`
  - 도메인 추가: `api.<도메인>`
  - 검증 방식: `DNS validation` (권장)
  - Route 53 사용 시 `Create records in Route 53` 클릭
  - 상태가 `Issued`가 될 때까지 대기

- [ ] **B-2. ALB 생성 및 리스너 구성 (AWS Console)**
  - 콘솔 경로: `EC2 -> Load Balancers -> Create Load Balancer -> Application Load Balancer`
  - 설정:
    - Scheme: `internet-facing`
    - Listener: `HTTP:80`, `HTTPS:443`
    - Security Group: `80`, `443` 허용
    - AZ/Subnet: 최소 2개 AZ 권장
  - Target Group 생성:
    - Target type: `Instances` (또는 `IP`)
    - Protocol/Port: 백엔드 노출 포트(예: `HTTP:8080`)
    - Health check path: `/health` (프로젝트 기준 경로)
  - EC2 인스턴스를 Target Group에 등록

- [ ] **B-3. HTTPS 리스너에 ACM 인증서 연결**
  - 콘솔 경로: `EC2 -> Load Balancers -> <ALB> -> Listeners and rules`
  - `HTTPS:443` 리스너에서 `Forward to <TargetGroup>` 규칙 확인
  - `View/edit certificates`에서 ACM 인증서(`api.<도메인>`) 선택

- [ ] **B-4. HTTP -> HTTPS 리다이렉트**
  - 동일 화면의 `HTTP:80` 리스너 기본 동작을 `Redirect to HTTPS`로 설정
    - Protocol: `HTTPS`
    - Port: `443`
    - Status code: `HTTP_301`

- [ ] **B-5. DNS 연결**
  - Route 53 사용 시:
    - 콘솔 경로: `Route 53 -> Hosted zones -> <도메인>`
    - 레코드 생성: `A (Alias)` / 이름 `api`
    - 대상: 생성한 ALB DNS 이름
  - 외부 DNS 사용 시:
    - `CNAME api -> <ALB DNS name>`

- [ ] **B-6. 동작 확인**
  - `curl -I http://api.<도메인>` -> `301/302`로 HTTPS 이동 확인
  - `curl -I https://api.<도메인>` -> `200` 또는 백엔드 정상 응답 확인
  - ALB Target Group health 상태가 `healthy`인지 확인

##### 8-2-C. 프론트 CloudFront HTTPS 최종 점검 체크리스트

- [🟢] CloudFront 배포의 `Alternate domain name (CNAME)`에 `www.<도메인>` 등록
- [🟢] ACM 인증서가 **us-east-1**에서 발급되었는지 확인(CloudFront 요구사항)
- [🟢] Route 53 `www` 레코드가 CloudFront 도메인을 가리키는지 확인
- [🟢] `http://www.<도메인>` 접속 시 `https://www.<도메인>`으로 리다이렉트되는지 확인

#### 8-3. OAuth·CORS 최종 정리

- [🟢] **1.** 네이버/구글/카카오 개발자 콘솔에서 Redirect URI에 실제 운영 도메인(https)을 등록한다.
- [🟢] **2.** 백엔드 CORS 허용 origin에 운영 프론트 도메인을 추가한다.
- [🟢] **3.** 프론트 환경변수(API URL, OAuth redirect 등)를 운영 도메인 기준으로 수정·재배포한다.
- [ ] **4.** 운영 도메인에서 소셜 로그인부터 API 호출까지 전 흐름을 테스트한다.
- **확인 방법**: 운영 URL에서 로그인 → API 사용 → 로그아웃까지 정상 동작

### 완료 기준

- [🟢] 도메인이 프론트·백엔드에 연결됨.
- [🟢] HTTPS가 프론트·백엔드에 적용됨.
- [ ] OAuth Redirect URI·CORS가 운영 도메인으로 반영되어 소셜 로그인 정상 동작.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 7
- [도메인-및-HTTPS-구성-가이드.md](도메인-및-HTTPS-구성-가이드.md)
- [소셜-로그인-프로세스-정리.md](소셜-로그인-프로세스-정리.md)

---

## Phase 9. 운영 안정화

### 목표

로그·모니터링·알람·백업이 정리되어, 장애 징후를 파악하고 복구 절차를 알 수 있는 상태가 됩니다.

### 결정 필요

- **▶ 결정 — CloudWatch 연동 범위**: (A) 최소(EC2 기본 메트릭만), (B) 상세(CloudWatch Agent, 로그 수집 등).  
  - 선택지: (A) 최소 (B) 상세  
  - 권장: 초기에는 (A), 필요 시 (B) 확장.  
  - 기록: _______________

### 단계별 작업

#### 9-1. 로그 수집

- [🟢] **A안(최소) — 로그 위치 표준화만 수행**
  - **어디서**: EC2 서버(SSH)
  - **용량/비용 메모(중요)**:
    - `/var/log/nginx` 로그는 메모리(RAM)보다 **디스크 용량**에 영향을 준다.
    - 로그는 계속 누적되므로 `logrotate` 동작 여부와 디스크 사용률을 주기적으로 확인한다.
    - 확인 명령어(예): `df -h`, `sudo du -sh /var/log/nginx`, `sudo ls -l /etc/logrotate.d/nginx`
  - **무엇을**: 장애 대응에 필요한 기본 로그 위치를 문서화
    - Nginx: `/var/log/nginx/access.log`, `/var/log/nginx/error.log`
    - systemd 서비스 로그: `journalctl -u <service-name>`
    - Docker 컨테이너 로그(사용 시): `docker logs <container-name>`
  - **명령어(예시)**:
    - `ssh -i <키파일.pem> ubuntu@<EC2_PUBLIC_IP>`: EC2 서버에 SSH로 접속한다.
    - `sudo ls -l /var/log/nginx`: Nginx 로그 파일 존재 여부·권한·최근 수정 시각을 확인한다.
    - `sudo journalctl -u nginx -n 100 --no-pager`: Nginx systemd 로그 최근 100줄을 조회한다.
    - `docker ps`: 실행 중인 컨테이너 목록과 이름을 확인한다.
    - `docker logs --tail 100 <container-name>`: 특정 컨테이너 로그 최근 100줄을 확인한다.
  - **확인 방법**: 장애 상황을 가정했을 때 어떤 로그를 어디서 확인할지 5분 내 설명 가능

- [ ] **B안(상세) — CloudWatch Logs 연동 포함**
  - **어디서**: AWS Console + EC2 서버
  - **AWS Console 경로**: `CloudWatch -> Logs -> Log groups`
  - **용량/비용 메모(중요)**:
    - CloudWatch Logs는 수집량과 보존기간에 비례해 비용이 증가할 수 있다.
    - 초기에는 Log Group 보존기간을 14~30일로 짧게 시작하고, 필요 시 90일 이상으로 확장한다.
  - **사전 조건**:
    - EC2 인스턴스 Role에 `CloudWatchAgentServerPolicy`(또는 동등 권한) 부여
  - **EC2 명령어(CloudWatch Agent 설치/기동, Ubuntu 예시)**:
    - `sudo apt update`: 패키지 인덱스를 최신 상태로 갱신한다.
    - `sudo apt install -y amazon-cloudwatch-agent`: CloudWatch Agent 패키지를 설치한다.
    - `sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-config-wizard`: 수집할 로그/메트릭 설정 파일을 대화형으로 생성한다.
    - `sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s`: 생성한 설정을 적용하고 에이전트를 시작한다.
    - `sudo systemctl status amazon-cloudwatch-agent`: 에이전트 서비스가 정상 실행 중인지 확인한다.
  - **무엇을**:
    - Nginx access/error, 앱 로그 파일, systemd 로그를 Log Group으로 전송
    - Log Group 보존 기간(예: 30일/90일) 설정
  - **확인 방법**:
    - CloudWatch Logs에서 최근 로그 이벤트 유입 확인
    - EC2에서 테스트 로그 1줄 출력 후(예: 애플리케이션 재시작) 콘솔 반영 확인

#### 9-2. 모니터링

- [🟢] **A안(최소) — 기본 메트릭 중심**
  - **어디서**: AWS Console
  - **AWS Console 경로**:
    - EC2: `CloudWatch -> Metrics -> EC2 -> Per-Instance Metrics`
    - RDS: `CloudWatch -> Metrics -> RDS`
  - **무엇을**:
    - EC2 CPUUtilization, NetworkIn/Out 확인
    - RDS CPUUtilization, FreeStorageSpace, DatabaseConnections 확인
    - CloudWatch 대시보드 1개 생성(최소 위젯 4개)
  - **헬스체크(EC2에서 수동/자동)**
    - **수동 1회 점검**:
      - `curl -fsS https://api.<도메인>/health`: 헬스 엔드포인트를 즉시 1회 호출해 성공/실패를 확인한다.
    - **자동 주기 점검(cron, 5분 간격)**
      - `crontab -e`: 현재 사용자 cron 편집기를 열어 스케줄을 등록한다.
      - 등록 라인(예시): `*/5 * * * * curl -fsS https://api.<도메인>/health >/dev/null || echo "[healthcheck failed] $(date)" >> /var/log/healthcheck.log`
      - `crontab -l`: 등록된 cron 항목이 반영되었는지 확인한다.
  - **확인 방법**: 대시보드에서 EC2/RDS 핵심 지표가 동시에 보이고, 헬스체크 성공/실패 확인 가능

- [ ] **B안(상세) — Agent 메트릭 포함**
  - **어디서**: AWS Console + EC2 서버
  - **무엇을**:
    - A안 + CloudWatch Agent로 메모리/디스크/파일시스템 지표 추가
    - 필요 시 커스텀 대시보드(서비스별 위젯 분리) 구성
  - **EC2 명령어(에이전트 상태 확인)**:
    - `sudo systemctl status amazon-cloudwatch-agent`: systemd 관점에서 에이전트 실행 상태를 확인한다.
    - `sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a status`: CloudWatch Agent 자체 상태와 적용된 설정 정보를 확인한다.
  - **확인 방법**: `CloudWatch -> Metrics -> CWAgent` 네임스페이스에 메모리/디스크 지표 확인

#### 9-3. 알람

- [🟢] **A안(최소) — 핵심 알람 2~3개**
  - **어디서**: AWS Console
  - **AWS Console 경로**:
    - `SNS -> Topics -> Create topic` (이메일 구독 추가 후 Confirm)
    - `CloudWatch -> Alarms -> Create alarm`
  - **비용 메모(중요)**:
    - SNS는 Topic 생성 자체보다 **알림 전송 건수/프로토콜**(이메일, SMS 등)에 따라 비용이 달라진다.
    - CloudWatch는 **알람 개수**, 커스텀 메트릭, 로그 수집/보존(CloudWatch Logs) 사용량에 따라 비용이 증가할 수 있다.
    - 초기에는 알람 수를 2~3개로 제한하고, SNS는 이메일 채널부터 시작해 비용을 최소화한다.
  - **무엇을**:
    - EC2 CPU 과다(예: `CPUUtilization > 80%`, 5분 x 2회)
    - RDS 저장공간 부족(예: `FreeStorageSpace` 임계치 이하)
    - (가능 시) 헬스체크 실패 알람 1개
  - **SNS Topic 생성 절차(콘솔 상세)**
    - 콘솔 경로: `SNS -> Topics -> Create topic`
    - 1) **Type**: `Standard` 선택
    - 2) **Name**: `knusrae-alerts` 입력(예시)
    - 3) **Create topic** 클릭
    - 4) 생성된 Topic 상세에서 **Create subscription** 클릭
    - 5) **Protocol**: `Email` 선택
    - 6) **Endpoint**: 알림 수신 이메일 입력
    - 7) **Create subscription** 클릭
    - 8) 수신 메일함에서 AWS 확인 메일의 **Confirm subscription** 링크 클릭(확정 전에는 알림 수신 안 됨)
    - 9) Topic 상세의 Subscriptions 상태가 `Confirmed`인지 확인
  - **CloudWatch 콘솔 UI 순서 기준 매핑(중요)**
    - 1) **Select metric(지표 선택)**
      - EC2 CPU 알람: `EC2 -> Per-Instance Metrics -> CPUUtilization` 선택
      - RDS 저장공간 알람: `RDS -> DBInstanceIdentifier -> FreeStorageSpace` 선택
    - 2) **Specify metric and conditions(지표 및 조건 지정)**
      - Statistic(통계): `Average` (권장)
      - Period(기간): `5 minutes`(5분)
      - Condition(조건):
        - Threshold type(임계값 유형): `Static`(정적)
        - CPU 알람
          - 경보 조건 정의 : `Greater/Equal`(>=, 보다 크거나 같음)
          - 임계값 정의 : `80`
        - RDS 저장공간 알람
          - 경보 조건 정의 : `Lower/Equal`(<=, 보다 작거나 같음)
          - 임계값 정의 : `10737418240`(10 GiB)
      - Additional configuration(추가 구성):
        - 경보를 알릴 데이터 포인트
          - `Evaluation periods = 2`
          - `Datapoints to alarm = 2`
        - 누락된 데이터 처리 : default(누락된 데이터를 누락(으)로 처리)
    - 3) **Configure actions(작업 구성)**
      - **Alarm state trigger(경보 상태 트리거)**:
        - 기본: `In alarm`(경보 상태) 선택 후 아래 SNS Topic 연결
        - 추가 상태 알림이 필요하면 `OK`(정상) 또는 `Insufficient data`(데이터 부족)로 전환해서 각각 별도 알림을 추가
      - **Send a notification to the following SNS topic(다음 SNS 주제에 알림을 보냅니다.)**:
        - `Select an existing SNS topic`(기존 SNS 주제 선택) 선택 후 `knusrae-alerts` 지정
        - Topic이 없으면 `Create new topic`으로 생성 후 사용
      - **Add notification(알림 추가)**:
        - 초기 권장: `In alarm`만 우선 구성
        - 운영 성숙 후 복구/데이터 공백 알림이 필요하면 `OK`, `Insufficient data`에도 `알림 추가`로 SNS 연결
    - 4) **Add name and description(이름 및 설명)**
      - Name 예시:
        - EC2 CPU: `knusrae-ec2-cpu-high`
        - RDS Storage: `knusrae-rds-free-storage-low`
      - Description(선택): 임계치/대상 리소스/담당자 정보를 짧게 기입
    - 5) **Preview and create(검토 및 생성)**
      - 설정값(지표/임계치/SNS Topic)을 마지막으로 검토 후 생성
  - **확인 방법**:
    - `CloudWatch -> Alarms`에서 상태 전환 확인
    - SNS 테스트 메일 수신 확인

- [ ] **B안(상세) — 운영 알람 세분화**
  - **어디서**: AWS Console (+ 선택: Lambda/EventBridge)
  - **비용 메모(중요)**:
    - 지표·알람·로그 연동 범위가 넓어질수록 비용이 증가하므로, 단계적으로 확장하고 월별 비용 추이를 함께 점검한다.
  - **무엇을**:
    - A안 + 에러율/지연시간/메모리/디스크/연결 수 알람 추가
    - 알람 등급 분리(Warning/Critical), 야간 알림 채널 분리
  - **확인 방법**: 테스트 조건(임계치 하향 등)으로 다중 알람 발송 검증

#### 9-4. RDS 백업 정책

- [🟢] **A안(최소) — 자동 백업 + 배포 전 수동 스냅샷**
  - **어디서**: AWS Console
  - **AWS Console 경로**: `RDS -> Databases -> <DB 인스턴스> -> Maintenance & backups(유지 관리 및 백업)`
  - **무엇을**:
    - 자동 백업 활성화 및 보존기간(예: 7~14일) 확인
    - 주요 배포 전 수동 스냅샷 생성 규칙 확정
  - **CLI(선택, 수동 스냅샷)**:
    - `aws rds create-db-snapshot --db-instance-identifier <db-id> --db-snapshot-identifier <snapshot-name>`: 지정한 RDS 인스턴스의 수동 스냅샷을 생성한다.
  - **확인 방법**: `RDS -> Snapshots`에서 생성 내역 및 복구 가능 상태 확인

- [ ] **B안(상세) — 장기 보존/복구 리허설 포함**
  - **어디서**: AWS Console (+ 선택: AWS Backup)
  - **무엇을**:
    - A안 + 월간 장기 보존 스냅샷 정책, 복구 리허설(테스트 인스턴스 복원) 수행
  - **확인 방법**: 복원 테스트 DB 기동 후 애플리케이션 연결/쿼리 정상 확인

#### 9-5. EC2·배포 복구 관점

- [ ] **A안(최소) — 재현 문서 우선**
  - **어디서**: 로컬 저장소 + EC2
  - **사전 준비**:
    - 로컬 저장소에 재현 문서를 둘 위치를 정한다. (예: `infra/README.md`, `infra/deploy/`, 또는 본 운영 문서의 별도 섹션)
    - 민감정보 마스킹 기준을 먼저 정한다. (예: 도메인/경로는 기록, 비밀번호/토큰/실제 시크릿 값은 제외)
    - 확인 대상 범위를 확정한다: Nginx, systemd, compose, 배포 스크립트/작업 디렉터리
  - **수행 순서(권장)**:
    - **1) Nginx 실제 적용 상태 수집**
      - `sudo nginx -T`: 현재 적용 중인 Nginx 전체 설정(포함 파일 포함)을 출력해 백업/검토한다.
      - 문서에 최소 항목을 기록한다:
        - 메인 include 구조(`nginx.conf`에서 어떤 `sites-enabled` 파일을 참조하는지)
        - `server_name`, `listen`, `location /`의 프록시 대상(내부 포트/서비스)
        - TLS 사용 시 인증서 경로(파일명은 가능, 개인키 내용은 절대 기록 금지)
    - **2) 서비스 기동 체계(systemd) 수집**
      - `systemctl list-unit-files | grep -E "nginx|docker|<service-name>"`: 관련 서비스 유닛의 등록/활성화 상태를 확인한다.
      - `systemctl status <service-name> --no-pager`: 서비스 실행 사용자, WorkingDirectory, ExecStart/ExecStop를 확인한다.
      - 문서에 최소 항목을 기록한다:
        - 어떤 유닛이 부팅 시 자동 시작(`enabled`)인지
        - 배포 시 수동으로 재시작해야 하는 유닛 순서
        - 유닛 파일 경로(`/etc/systemd/system/...`)와 의존 관계(`After=`, `Requires=` 등 핵심만)
    - **3) Compose 최종 설정 수집**
      - `docker compose config`: compose 파일을 병합/해석한 최종 설정을 검증한다.
      - 문서에 최소 항목을 기록한다:
        - compose 파일 실제 위치(예: `/home/ubuntu/knusrae/compose/docker-compose.yml`)
        - 서비스명, 이미지 태그 또는 build/pull 방식, 포트 매핑
        - `env_file`/볼륨/네트워크 사용 여부(값 자체는 마스킹)
    - **4) 배포 스크립트 및 실행 경로 고정**
      - 배포 시 사용하는 스크립트 경로와 실행 순서를 고정 기록한다.
      - 예: `scripts/fetch-secrets-to-env.sh` 실행 → `docker compose pull`(해당 시) → `docker compose up -d` → 헬스체크
      - 각 명령의 실행 위치(작업 디렉터리)까지 함께 기록한다.
    - **5) 저장소 반영(민감정보 제외)**
      - 수집 내용을 저장소 `infra/` 또는 운영 문서에 반영한다.
      - 재현에 필요한 파일(예: 예시 Nginx/server 블록, systemd 유닛 템플릿, compose 예시)은 템플릿 형태로 저장한다.
      - 실제 시크릿/키/토큰/내부 민감 도메인 값은 placeholder로 치환한다.
  - **산출물(최소)**:
    - `복구 절차 문서` 1개: 신규 담당자가 순서대로 따라 할 수 있는 단계형 가이드
    - `설정 위치 목록` 1개: Nginx/systemd/compose/스크립트의 실제 경로 및 담당 컴포넌트 매핑
    - `운영 명령어 목록` 1개: 기동/재기동/상태확인/헬스체크 명령
  - **확인 방법**:
    - 신규 담당자(또는 본인)가 문서만 보고 다음을 재현할 수 있으면 완료:
      - Nginx 라우팅 구조 이해 및 점검
      - 서비스(systemd 또는 compose) 기동/재기동
      - 배포 후 헬스체크 성공 확인

- [ ] **B안(상세) — 스크립트 자동 재현**
  - **어디서**: 로컬 저장소(스크립트 작성) + EC2 테스트
  - **무엇을**:
    - A안 + 부트스트랩 스크립트(패키지 설치, Nginx 배치, 서비스 등록, compose 기동) 작성
    - 새 EC2에서 드라이런/실행 검증
  - **확인 방법**: 빈 EC2에서 문서+스크립트로 동일 상태 재현 성공

#### 9-6. S3 백업/수명주기 (필요 시)

- [🟢] **A안(최소) — 정책 확인 및 기본 설정**
  - **어디서**: AWS Console
  - **AWS Console 경로**:
    - Versioning: `S3 -> Buckets -> <업로드 버킷> -> Properties -> Bucket Versioning`
    - Lifecycle: `S3 -> Buckets -> <업로드 버킷> -> Management -> Lifecycle rules`
  - **무엇을**:
    - Versioning 활성화 여부 확인 (Disabled면 `Edit -> Enable`)
      - 데이터/배포 복구 가능성 확보
        - 실수로 파일을 덮어쓰기/삭제해도 이전 버전으로 복구 가능
        - 배포 산출물(프론트 정적 파일 등) 롤백이 필요할 때 안전장치 역할
        - 운영에서 가장 흔한 사고(오업로드, 잘못된 배포)의 피해 방지
    - Lifecycle Rule(예: 비활성/구버전 만료) 1개 이상 정의
      - 비용 폭증/운영 부채 방지
        - Versioning을 켜면 이전 버전이 계속 쌓여서 스토리지 비용이 증가할 수 있음
        - 오래된 버전/불필요 객체를 자동 만료·전환해서 비용과 관리 부담을 통제
        - 수동 정리 없이 정책으로 운영 일관성을 유지 가능
  - **Lifecycle 설정 절차(버킷별 분리, 콘솔 기준)**:
    - **A) 프론트 정적 파일 버킷 (CloudFront 원본)**
      - **1) 규칙 생성 시작**
        - `S3 -> Buckets(버킷) -> <frontend-static-bucket> -> Management(관리) -> Lifecycle rules(수명 주기 규칙) -> Create lifecycle rule(수명 주기 규칙 생성)`
        - Rule name 예시: `frontend-noncurrent-expire-30d`
      - **2) 범위(Scope) 선택**
        - `Choose a rule scope(규칙 범위 선택)`에서 `Apply to all objects in the bucket(버킷의 모든 객체에 적용)` 권장
          - `이 규칙이 버킷의 모든 객체에 적용된다는 데 동의합니다.`에 **동의**
      - **3) 액션 선택**
        - `Lifecycle rule actions(수명 주기 규칙 작업)`에서 아래 항목 선택
        - `Expire current versions of objects(객체의 현재 버전 만료)`: 비활성 (현재 서비스 파일 보존)
        - `Permanently delete noncurrent versions of objects(객체의 이전 버전 영구 삭제)`: 활성화
          - `객체가 최신이 아닌 상태로 전환된 후 경과 일수(Days after objects become noncurrent)`에 `30` 입력
          - `보관할 비최신 버전 수(Number of newer versions to retain)`는 기본 미입력(필요 시에만 사용)
        - `Delete expired object delete markers(만료된 객체 삭제 마커 삭제)`: 활성화
          - 체크하면 `만료된 객체 삭제 마커 또는 완료되지 않은 멀티파트 업로드 삭제` 영역이 펼쳐짐
          - 펼쳐진 영역에서 `만료된 객체 삭제 마커 삭제(Delete expired object delete markers)`를 체크
      - **4) 멀티파트 업로드 정리(권장)**
        - 같은 펼침 영역에서 `완료되지 않은 멀티파트 업로드 삭제(Delete incomplete multipart uploads)` 체크
        - `완료되지 않은 멀티파트 업로드 경과 일수(Days after initiation)`에 `7` 입력
      - **5) 저장 및 적용**
        - `Create rule(규칙 생성)` 후 상태가 `Enabled(활성화)`인지 확인
      - **운영 메모**
        - 프론트 빌드 파일은 해시 파일명(immutable) 유지 권장
        - `index.html` 같은 고정 파일 변경 이력은 noncurrent 30일로 관리
    - **B) 사용자 업로드/이미지 버킷 (레시피 이미지 등)**
      - **1) 규칙 생성 시작**
        - `S3 -> Buckets(버킷) -> <uploads-bucket> -> Management(관리) -> Lifecycle rules(수명 주기 규칙) -> Create lifecycle rule(수명 주기 규칙 생성)`
        - Rule name 예시: `uploads-noncurrent-expire-90d`
      - **2) 범위(Scope) 선택**
        - 기본은 `Choose a rule scope(규칙 범위 선택)`에서 `Apply to all objects in the bucket(버킷의 모든 객체에 적용)`
          - `이 규칙이 버킷의 모든 객체에 적용된다는 데 동의합니다.`에 **동의**
        - 업로드 경로를 분리해뒀다면 prefix(예: `recipes/`, `users/`) 단위 규칙 적용 가능
      - **3) 액션 선택**
        - `Lifecycle rule actions(수명 주기 규칙 작업)`에서 아래 항목 선택
        - `Expire current versions of objects(객체의 현재 버전 만료)`: 기본 비활성 (현재 사용자 파일 보존)
        - `Permanently delete noncurrent versions of objects(객체의 이전 버전 영구 삭제)`: 활성화
          - `객체가 최신이 아닌 상태로 전환된 후 경과 일수(Days after objects become noncurrent)`에 `90` 입력
          - `보관할 비최신 버전 수(Number of newer versions to retain)`는 기본 미입력(필요 시에만 사용)
        - `Delete expired object delete markers(만료된 객체 삭제 마커 삭제)`: 활성화
          - 체크하면 `만료된 객체 삭제 마커 또는 완료되지 않은 멀티파트 업로드 삭제` 영역이 펼쳐짐
          - 펼쳐진 영역에서 `만료된 객체 삭제 마커 삭제(Delete expired object delete markers)`를 체크
      - **4) 멀티파트 업로드 정리(권장)**
        - 같은 펼침 영역에서 `완료되지 않은 멀티파트 업로드 삭제(Delete incomplete multipart uploads)` 체크
        - `완료되지 않은 멀티파트 업로드 경과 일수(Days after initiation)`에 `7` 입력
      - **5) 저장 및 적용**
        - `Create rule(규칙 생성)` 후 상태가 `Enabled(활성화)`인지 확인
      - **운영 메모**
        - 업로드 원본 보존 정책이 필요하면 noncurrent를 `90일`보다 길게 조정
        - 장기 보관이 많아지면 B안에서 IA/Glacier 전환 정책 추가
  - **확인 방법**:
    - `Properties -> Bucket Versioning` 상태가 `Enabled`인지 확인
    - `Management -> Lifecycle rules`에 규칙이 생성/활성화되었는지 확인
    - 규칙 상세에서 noncurrent/multipart 일수가 의도한 값(30/90일, 7일)인지 재확인

- [ ] **B안(상세) — 백업 대상 분리 및 비용 최적화**
  - **어디서**: AWS Console (+ 선택: S3 Replication)
  - **사전 결정(먼저 기록)**:
    - 백업 전략: `A. Cross-Region Replication` 또는 `B. 백업 전용 버킷 분리(동일 리전)` 중 선택
    - 대상 데이터 범위: 전체 버킷 vs prefix 단위(예: `uploads/`, `logs/`)
    - 보존 기간/복구 목표: 운영 RTO·RPO 기준으로 보존일수와 전환 시점 확정
  - **무엇을**:
    - A안 + Cross-Region Replication 또는 백업 전용 버킷 분리 검토
    - 액세스 패턴 기준 스토리지 클래스 전환 정책(Intelligent-Tiering/Glacier) 적용
  - **수행 순서(권장)**:
    - **1) 백업 대상 버킷 준비**
      - 복제 선택 시: 대상 리전에 백업 버킷 생성, 버전 관리 활성화
      - 분리 선택 시: 동일/타 리전에 백업 전용 버킷 생성, 접근 권한 최소화(읽기/복구 담당자 한정)
    - **2) 복제 규칙 구성(Replication 선택 시)**
      - 경로: `S3 -> Buckets -> <원본 버킷> -> Management -> Replication rules -> Create replication rule`
      - Scope: 전체 또는 prefix 지정
      - Destination: 대상 버킷·리전 지정, 필요한 IAM Role 생성/연결
      - Replication Time Control(RTC)는 엄격한 RPO가 필요할 때만 사용(비용 증가)
    - **3) 스토리지 클래스 전환 정책 구성**
      - 경로: `S3 -> Buckets -> <원본 또는 백업 버킷> -> Management -> Lifecycle rules`
      - 권장 시작안:
        - 최근 접근 가능성이 낮은 객체: `90일` 후 `Standard-IA` 또는 `Intelligent-Tiering`
        - 장기 보관 백업: `180일` 후 `Glacier Flexible Retrieval`(또는 요구사항에 맞는 Glacier 계열)
      - 현재 서비스 경로(핫 데이터)는 즉시 Glacier로 내리지 않도록 prefix 분리 적용 권장
    - **4) 복구 절차 문서화**
      - 복구 시 참조할 버킷/경로, 권한, 복원 명령(또는 콘솔 절차)을 운영 문서에 기록
      - Glacier 계열 사용 시 복원 대기 시간(분~시간)과 복원 후 유효 기간도 명시
    - **5) 비용/효과 점검 루틴 설정**
      - Cost Explorer 또는 S3 Storage Lens로 월별 용량/요금 추이 확인
      - 정책 적용 후 1~2개월 동안 전환 효과(비용 절감 vs 복구 지연) 점검 후 값 미세 조정
  - **확인 방법**:
    - `Management -> Replication rules`에서 규칙이 `Enabled`이고 대상 버킷/범위가 의도대로 설정되었는지 확인
    - 샘플 객체 업로드 후 대상 버킷에 복제 객체가 생성되는지 확인(복제 선택 시)
    - `Lifecycle rules` 상세에서 전환/만료 일수와 대상 prefix가 의도와 일치하는지 확인
    - 월 비용 리포트에서 전환 전/후 비용 추세를 비교해 정책 효과를 확인

### 완료 기준

- [🟢] 로그 위치를 알고, (선택) CloudWatch 로그 연동 완료.
- [🟢] EC2·RDS 메트릭을 확인할 수 있음.
- [🟢] 알람이 설정되어 장애 징후를 수신할 수 있음.
- [🟢] RDS 백업 정책이 확인되었고, 복구 방법을 알고 있음.
- [ ] EC2/배포 재현 가능한 문서·스크립트가 있음.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 8, 9, 10

---

## Phase 10. 자동화 (CI/CD)

### 목표

반복 배포가 push 또는 클릭 몇 번으로 가능하고, 배포 후 헬스체크까지 자동화된 상태가 됩니다. (필수는 아니나 수동 실수를 줄임)

**Phase 4 연계(시크릿 주입이 4-3-A인 경우)**: 백엔드 배포 job은 EC2에 접속한 뒤 **Phase 4에서 수동으로 검증한 것과 같은 순서**를 실행하는 것이 기본이다. (1) 배포 산출물·`docker-compose.yml` 반영 (2) `fetch-secrets-to-env.sh`로 `.env.secrets` 갱신 — **AWS 자격 증명은 EC2 인스턴스 Role에 의존** (3) `docker compose up -d` 등 재기동 (4) 헬스체크. CI에 AWS 액세스 키를 넣어 Secrets Manager를 **직접** 호출하는 방식은 가능하나, IAM·노출 면에서 별도 설계가 필요하므로 초기에는 EC2에서 스크립트 실행 방식을 유지하는 것을 권장한다.

### 결정 필요

- **▶ 결정 — CI/CD 트리거**: (A) main(또는 특정 브랜치) push 시 자동 배포, (B) 수동 트리거 또는 다른 브랜치 전략.  
  - 선택지: (A) main push 자동 (B) 수동/다른 브랜치  
  - 권장: 사용자 환경에 맞게 선택.  
  - 기록: (A)

### 단계별 작업

#### 10-0. 사전 준비 (처음 1회)

- [🟢] **1. 배포 계정/권한 정리**
  - **권장 방식 선택**
    - 기본 권장: `OIDC + IAM Role` (장기 Access Key 미사용)
    - 대안: IAM 사용자 Access Key 방식 (초기 구성은 쉽지만 보안/운영 부담 증가)
  - **AWS 콘솔 작업 순서 (OIDC + IAM Role)**
  - **1) OIDC 공급자 확인/생성 (최초 1회)**
    - 이동: `AWS Console -> IAM -> Identity providers -> Add provider`
    - `Provider type`: `OpenID Connect`
    - `Provider URL`: `https://token.actions.githubusercontent.com`
    - `Audience`: `sts.amazonaws.com`
    - 이미 동일 공급자가 있으면 재생성하지 않고 기존 항목을 사용한다.
  - **2) GitHub Actions용 IAM Role 생성**
    - 이동: `AWS Console -> IAM -> Roles -> Create role`
    - **1단계: 신뢰할 수 있는 엔터티 선택**
      - 유형: `Web identity`
      - `ID 제공업체`: `token.actions.githubusercontent.com`
      - `Audience`: `sts.amazonaws.com`
      - `GitHub organization`: `<OWNER>` (개인 계정이면 GitHub 사용자명, 예: `JinseokJang91`)
      - `GitHub repository`: `Knusrae` (권장, `*` 지양)
      - `GitHub branch`: `main` 또는 실제 배포 브랜치 (권장, `*` 지양)
    - **2단계: 권한 추가**
      - 이 단계에서는 권한 정책 JSON을 직접 작성하지 않고, 기존 정책을 **선택/연결(Attach)** 한다.
      - 새 정책이 필요하면 먼저 별도 화면에서 생성한 뒤 이 단계에서 선택한다.
        - 정책 생성 경로: `IAM -> Policies -> Create policy`
      - 프론트 배포 최소 권한: 버킷 ARN `arn:aws:s3:::버킷명`에 `s3:ListBucket`, `s3:GetBucketLocation`; 객체 ARN `arn:aws:s3:::버킷명/*`에 `s3:PutObject`, `s3:DeleteObject`; CloudFront 배포 ARN에 `cloudfront:CreateInvalidation`. (`ListBucket`은 버킷 ARN만—끝에 `/*`를 붙이면 HeadBucket·sync 목록 조회가 403이 날 수 있음)
      - 프론트 배포용 정책 예시(JSON) — `Create policy`에서 아래를 붙여넣고 `<S3_BUCKET_NAME>`, `<AWS_ACCOUNT_ID>`, `<CLOUDFRONT_DISTRIBUTION_ID>`를 실제 값으로 변경:
      - `<S3_BUCKET_NAME>`은 frontend용 버킷의 이름을 넣어야 한다.
      ```json
      {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Sid": "S3ListBucket",
            "Effect": "Allow",
            "Action": [
              "s3:ListBucket",
              "s3:GetBucketLocation"
            ],
            "Resource": "arn:aws:s3:::<S3_BUCKET_NAME>"
          },
          {
            "Sid": "S3ObjectWriteDelete",
            "Effect": "Allow",
            "Action": [
              "s3:PutObject",
              "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::<S3_BUCKET_NAME>/*"
          },
          {
            "Sid": "CloudFrontInvalidation",
            "Effect": "Allow",
            "Action": "cloudfront:CreateInvalidation",
            "Resource": "arn:aws:cloudfront::<AWS_ACCOUNT_ID>:distribution/<CLOUDFRONT_DISTRIBUTION_ID>"
          }
        ]
      }
      ```
      - 위 정책 생성 후 Role 생성의 `2단계: 권한 추가`에서 해당 정책을 검색해 Attach 한다.
      - 백엔드 배포(SSH 방식): AWS API 권한보다 SSH 권한(EC2 접속 사용자 권한/명령 허용 범위)이 핵심
      - 공통 원칙: 과도한 `AdministratorAccess` 금지
    - **3단계: 이름 지정, 검토 및 생성**
      - Role name 예시: `KnusraeGitHubActionsDeployRole`
      - 신뢰 엔터티와 권한 요약을 최종 확인 후 생성한다.
    - 생성 화면의 organization/repository/branch 값은 최종적으로 `sub` 제한 조건에 반영된다.
  - **3) 생성된 Role의 Trust policy 최종 확인/보정**
    - 이동: `IAM -> Roles -> <Role> -> Trust relationships -> Edit trust policy`
    - 참고: 생성 화면에 `sub` 입력란이 직접 보이지 않아도 정상일 수 있다.
    - `Condition`에 아래 2개가 있는지 확인:
      - `token.actions.githubusercontent.com:aud = sts.amazonaws.com`
      - `token.actions.githubusercontent.com:sub = repo:<OWNER>/Knusrae:ref:refs/heads/<BRANCH>`
    - 아래 예시를 기준으로 저장소/브랜치 값을 현재 운영값에 맞게 바꿔 입력한다. (예: `JinseokJang91`, `Knusrae`, `main`)
    ```json
    {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Effect": "Allow",
          "Principal": {
            "Federated": "arn:aws:iam::<AWS_ACCOUNT_ID>:oidc-provider/token.actions.githubusercontent.com"
          },
          "Action": "sts:AssumeRoleWithWebIdentity",
          "Condition": {
            "StringEquals": {
              "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
              "token.actions.githubusercontent.com:sub": "repo:JinseokJang91/Knusrae:ref:refs/heads/main"
            }
          }
        }
      ]
    }
    ```
    - 현재 정책에 `sub` 배열 값이 중복되어 있으면 1개만 남긴다. (동일 값 2개 등록 불필요)
  - **4) Role 권한 정책 재확인(생성 후 점검)**
    - 이동: `IAM -> Roles -> <Role> -> Permissions`
    - 2단계에서 연결한 정책이 의도한 최소 권한 범위인지 다시 확인한다.
- [🟢] **2. GitHub Secrets/Variables 표 작성 후 등록**
  - 경로: `GitHub Repository -> Settings -> Secrets and variables -> Actions`
  - **현재 화면에서 순서대로 입력 (OIDC 권장 기준)**
  - **1) 먼저 저장 방식 구분**
    - 민감정보는 `Secrets`에 저장한다. (예: SSH 개인키)
    - 민감하지 않은 고정값은 `Variables`에 저장한다. (예: 리전, 버킷명)
  - **2) OIDC용 필수 항목 등록**
    - `Secrets`에 추가:
      - `AWS_ROLE_TO_ASSUME` = IAM Role ARN (예: `arn:aws:iam::<ACCOUNT_ID>:role/KnusraeGitHubActionsDeployRole`)
    - `Variables`에 추가:
      - `AWS_REGION` = 배포 리전 (예: `ap-northeast-2`)
  - **3) 프론트 배포 항목 등록**
    - `Variables`에 추가:
      - `FRONTEND_S3_BUCKET` = 프론트 정적 파일 배포 버킷 이름
      - `CLOUDFRONT_DISTRIBUTION_ID` = 프론트 배포용 CloudFront 배포 ID
  - **4) 백엔드(SSH 배포 시) 항목 등록**
    - `Secrets`에 추가:
      - `EC2_HOST`
        - 예: Elastic IP: 3.34.56.78 또는 퍼블릭 DNS: ec2-3-34-56-78.ap-northeast-2.compute.amazonaws.com (도메인을 썼다면 api.example.com 같은 A 레코드가 가리키는 호스트도 가능)
      - `EC2_USER`
        - 예 : Ubuntu AMI면 보통 ubuntu, Amazon Linux면 ec2-user
      - `EC2_SSH_PRIVATE_KEY`
        - 로컬에서 쓰는 것과 동일한 PEM 전체를 한 덩어리로 넣음. - 시작: -----BEGIN RSA PRIVATE KEY----- 또는 -----BEGIN OPENSSH PRIVATE KEY----- - 끝: -----END ...----- GitHub Secrets는 여러 줄 그대로 저장하면 됨. 공개키(.pub)가 아님.
    - 필요 시 `Variables` 또는 `Secrets`에 추가:
      - `BACKEND_DEPLOY_PATH`
        - 예 : 가이드 권장 구조라면: /home/ubuntu/knusrae compose, 스크립트가 하위 폴더를 쓰면: /home/ubuntu/knusrae/app 등 실제 docker compose 실행 디렉터리
        - Secrets으로 두는 경우 : 경로에 민감한 정보가 없으면 Variables로 충분
      - `BACKEND_HEALTHCHECK_URL`
        - 예 : 배포 후 GitHub 러너가 HTTP로 닿을 수 있는 URL - Nginx로 묶었다면: https://api.example.com/actuator/health 또는 https://api.example.com/health - 인증 서비스만 확인: http://<EC2_HOST>:8081/actuator/health (보안 그룹에서 해당 포트가 러너/인터넷에 열려 있어야 함; 보통은 443 + 도메인이 안전)
        - Secrets로 두는 경우 : URL에 토큰·쿼리 시크릿이 들어가면 Secrets
  - **5) Access Key 방식 사용 시(대안)**
    - OIDC를 쓰지 않는 경우에만 `Secrets`에 `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`를 등록한다.
    - OIDC 사용 시에는 위 Access Key 2개를 등록하지 않는다.
  - **입력 체크리스트**
    - 키 이름 오탈자 없이 등록되었는지 확인한다. (`AWS_ROLE_TO_ASSUME` vs `AWS_ROLE_ARN` 혼동 주의)
    - `FRONTEND_S3_BUCKET`은 업로드 버킷이 아니라 프론트 배포 버킷인지 확인한다.
    - 등록 후 워크플로에서 참조 키(`secrets.*`, `vars.*`) 이름과 1:1로 일치하는지 확인한다.
- [🟢] **3. 브랜치 보호/배포 브랜치 규칙 확정**
  - `develop`에서 테스트 후 `main` merge 시 자동 배포할지,
    또는 `workflow_dispatch`(수동 실행)를 병행할지 결정한다.
    - 자동 배포 + 수동 배포 둘 다 사용으로 결정
  - 실서비스라면 `main push` + `workflow_dispatch` 동시 지원을 권장.
- [🟢] **4. 러너 환경 확인**
  - 기본은 `ubuntu-latest` 사용.
  - 백엔드에서 Docker build가 필요하면 Docker 사용 가능 여부를 사전 확인.
- **확인 방법**:
  - Secrets 등록 화면에서 키 이름이 모두 존재하는지 확인
  - 배포 대상(버킷, CloudFront, EC2)에 최소 권한으로 실제 접근 가능한지 사전 테스트

#### 10-1. GitHub Actions 워크플로 작성

이 절에서는 **확인만 하면 되는 항목**과 **직접 수행해야 하는 항목**을 구분하고, Knusrae 레포 구조(`frontend/`, `backend/`, 루트 Gradle)에 맞춘 실행 순서를 정리한다.

##### A. 확인만 하면 되는 항목 (검증·판단)

파일 작성 전·후에 아래를 읽고 대조하면 된다.

| 확인 내용 | 어디서 / 어떻게 |
|-----------|-----------------|
| 배포 브랜치 | 상위 절에서 결정한 브랜치(예: `main`)와 워크플로 `on.push.branches`가 일치하는지 |
| Secrets/Variables 이름 | `GitHub Repository → Settings → Secrets and variables → Actions`에 등록된 이름이 YAML의 `secrets.*`, `vars.*`와 **글자까지 동일**한지 (10절 입력 체크리스트) |
| 프론트 빌드 산출물 | 로컬에서 `frontend`에서 `npm run build` 후 **`dist/`** 가 생기는지(Vite 기본). S3 sync 경로가 `dist/`와 맞는지 |
| 백엔드 빌드 방식 | 루트 `build.gradle` 기준 `./gradlew` 위치·멀티모듈 구조. Docker 배포면 `Dockerfile`/`compose` 경로가 Phase 4와 같은지 |
| 트리거 동작 | Push 후 **Actions**에서 워크플로가 **어느 이벤트**로 돌았는지, **어떤 job**이 스킵/실행됐는지 |
| `paths` 필터 | `frontend/**`만 바꾼 커밋에서 백엔드 워크플로가 안 도는지(또는 그 반대) 등 **의도한 대로만** 실행되는지 |
| concurrency | 같은 브랜치에 연속 push 시 **이전 실행이 취소**되는지(로그에 `Cancelled` 표시) |
| 시크릿 노출 | 실패 로그에 PEM, 토큰, `.env` 내용이 찍히지 않는지 |

##### B. 직접 수행해야 하는 항목 (작업)

| 순서 | 해야 할 일 |
|------|------------|
| 1 | 저장소 루트에 **`.github/workflows/`** 디렉터리 생성 |
| 2 | 전략 결정: 권장은 **`frontend-deploy.yml`** + **`backend-deploy.yml`** 두 파일(실패 시 프론트/백 구분이 쉬움) |
| 3 | 각 YAML에 **`name`**, **`on`**(push 브랜치 + `workflow_dispatch` + 필요 시 `paths`)**, **`concurrency`**, **`jobs`** 작성 |
| 4 | Job 안을 **step 단위**로 분리: checkout → 런타임 설치 → 의존성/테스트/빌드 → 배포(10-2, 10-3) → (선택) 헬스체크 |
| 5 | 브랜치에 **커밋·푸시** 후 Actions에서 **수동 실행(Run workflow)** 과 **main push** 둘 다 한 번씩 검증 |

##### C. 수행 순서 상세 (기존 항목 1~5와 대응)

- [🟢] **C-1. 워크플로 파일 분리 전략**
  - 권장: `.github/workflows/frontend-deploy.yml`, `.github/workflows/backend-deploy.yml`
  - 대안: 한 파일에 `jobs.frontend`, `jobs.backend` + `if` 제어(한 번에 올리고 싶을 때 등)
    - 결과 : frontend와 backend 분리. 권장과 같이 생성 완료.

- [🟢] **C-2. 트리거 정의**
  - `on.push.branches`: `main`(또는 팀이 정한 배포 브랜치)
  - `on.workflow_dispatch`: 수동 배포 버튼
  - 선택: `paths`로 변경 파일이 있을 때만 실행
    - 프론트 예: `frontend/**`
    - 백엔드 예: `backend/**`, `infra/**`, 루트 `build.gradle`·`settings.gradle` 등 빌드에 필요한 경로(루트 Gradle만 바꿔도 배포가 필요하면 포함)
  - `paths`를 쓰면 **해당 경로가 바뀌지 않은 push에서는 워크플로가 실행되지 않음**을 전제로 팀 규칙과 맞는지 A절에서 확인한다.

- [🟢] **C-3. 공통 job 기본 골격**
  1. `actions/checkout@v4`(또는 팀 표준 버전)
  2. 런타임: 프론트는 `actions/setup-node@v4` + 프로젝트와 맞는 Node 버전; 백엔드는 `actions/setup-java@v4` 등(Gradle/JDK 버전은 저장소와 동일하게)
  3. **같은 job 내 순서**: 의존성 설치 → 테스트(선택) → 빌드 → 배포. 배포는 빌드 **뒤**에만 두거나, `needs`로 job을 분리
  - **Knusrae 프론트**: 작업 디렉터리 `frontend/` → `npm ci`(lockfile 기준) → `npm run build` → 이후 10-2에서 `dist/` 검증·S3 등
  - **Knusrae 백엔드**: 저장소 루트에서 `./gradlew test`(또는 팀 표준) → `./gradlew build` 등 → 이후 10-3의 Docker/SSH 단계

- [🟢] **C-4. 동시 실행 제어(concurrency)**
  - 워크플로 최상위에 `concurrency` 설정
  - 예: 프론트 `group: frontend-${{ github.ref }}`, 백엔드 `group: backend-${{ github.ref }}` — **그룹 이름을 프론트/백에서 다르게** 해 서로의 실행을 취소하지 않게 한다.
  - `cancel-in-progress: true`로 같은 브랜치에서 이전 배포 진행 중이면 취소

- [🟢] **C-5. 실패 로그 가시성**
  - 테스트·빌드·배포·헬스체크를 **별도 step**으로 나누고 step `name`을 명확히
  - `echo`로 브랜치, commit SHA, 배포 대상 식별 정보(민감값·전체 시크릿 제외)만 남김

##### D. 10-2·10-3과의 역할 구분

- **10-1**: YAML **뼈대**(트리거, job/step 순서, concurrency, 로그).
- **10-2**: 프론트 job에 AWS OIDC, S3 sync, CloudFront invalidation 등 **채움**.
- **10-3**: 백엔드 job에 Gradle 빌드 산출, SSH, compose, 헬스체크 등 **채움**.

권장 진행: **10-1만 만족하는 최소 버전**(checkout → build까지 Actions에서 성공)으로 먼저 올린 뒤, 10-2·10-3을 단계적으로 붙인다.

##### E. 확인 방법 (통합)

- [ ] `frontend`만 의도적으로 수정한 커밋을 push했을 때 **프론트 워크플로만** 도는지 확인(`paths` 사용 시).
- [🟢] Actions에서 **Run workflow**로 수동 실행이 되는지 확인.
- [ ] 같은 브랜치에 연속 push 후 **첫 실행이 cancelled**되는지 확인(concurrency).
- [ ] 트리거·job 분기·step 순서가 의도와 같은지 Actions 로그로 확인.

#### 10-2. 프론트 자동배포

- [🟢] **1. 빌드 단계 구성**
  - 작업 디렉터리: `frontend/`
  - `npm ci`(또는 프로젝트 표준 설치 명령) → `npm run build`
  - 산출물 디렉터리(`dist/` 등)가 존재하는지 검증 step 추가
- [🟢] **2. AWS 인증 설정**
  - `aws-actions/configure-aws-credentials`로 AWS 자격 증명 주입
  - 리전은 `AWS_REGION`, 버킷은 `FRONTEND_S3_BUCKET`를 사용
- [🟢] **3. S3 업로드 전략 적용**
  - 기본 권장: `aws s3 sync dist/ s3://$FRONTEND_S3_BUCKET --delete`
  - 캐시 전략 권장:
    - `index.html`은 짧은 캐시 또는 no-cache
    - 해시 파일(`assets/*.js`, `assets/*.css`)은 장기 캐시
  - 필요 시 2단계 업로드(정적 자산 먼저, index.html 나중)로 무중단 반영 안정성 향상
  - 캐시 전략 및 2단계 업로드는 지금 꼭 넣을 필요 없음. 배포가 안정된 뒤, 트래픽·비용·무중단 요구가 생기면 그때 넣어도 됨
    - 엣지/브라우저 캐시를 길게 쓰고, 무효화를 줄이거나, 배포 짧은 순간의 불일치를 더 줄이고 싶을 때
- [🟢] **4. CloudFront 무효화**
  - 배포 직후 `aws cloudfront create-invalidation --distribution-id ... --paths "/*"` 실행
  - 대규모 트래픽이면 `/index.html` 중심 최소 invalidation 전략도 검토
- [🟢] **5. 배포 검증 자동화(권장)**
  - invalidation 후 서비스 URL에 `curl` 요청(또는 간단한 상태 점검)으로 200 응답 확인
  - 실패 시 workflow를 `failed`로 종료해 반영 실패를 즉시 인지
- **확인 방법**:
  - push/merge 후 S3 객체 수정 시각이 갱신되었는지 확인
  - CloudFront invalidation 상태가 `Completed`인지 확인
  - 브라우저 강력 새로고침 시 최신 프론트 화면이 표시되는지 확인

#### 10-3. 백엔드 자동배포

- [🟢] **1. 빌드/패키징 단계**
  - 백엔드 프로젝트 빌드(`test` 포함) 후 산출물 생성
    - JAR 방식: `jar` 생성 확인
    - Docker 방식: 이미지 빌드 후 태그 지정(예: commit SHA)
  - 실패 시 배포 단계로 넘어가지 않도록 job 분리(`build` -> `deploy`)
- [🟢] **2. 배포 방식 결정 후 고정**
  - **A. SSH + rsync/scp 전달**: 초기 구축이 단순하고 디버깅이 쉬움
  - **B. Registry pull 방식**: 장기적으로 표준화/확장에 유리
  - 현재 문서 기준(Phase 4 연계)에서는 A안을 기본으로 유지
- [🟢] **3. EC2 접속 시크릿/보안 설정**
  - `EC2_HOST`, `EC2_USER`, `EC2_SSH_PRIVATE_KEY` 등록
    - `EC2_HOST` : api.knsurae.com
    - `EC2_USER` : ubuntu
    - `EC2_SSH_PRIVATE_KEY` : pem key 파일 내용
  - 호스트 키 검증(known_hosts) 설정으로 MITM 위험 완화
    - Variables 추가 등록 완료
      - SSH 공개키 등록
        - EC2에 SSH 접속 후 아래 명령어 실행
          - `sudo ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub -E sha256`
            - `256 SHA256:7SAB1LWeSzN24nAofttK8kPynJ4/0wV9dDm9QuixX38 root@ip-172-31-6-205 (ED25519)`
          - `sudo ssh-keygen -lf /etc/ssh/ssh_host_ecdsa_key.pub -E sha256`
            - `256 SHA256:CrajEWPkaKRIy95fqCStFDQKhYh+p42pr7okZdUkkaw root@ip-172-31-6-205 (ECDSA)`
          - `sudo ssh-keygen -lf /etc/ssh/ssh_host_rsa_key.pub -E sha256`
            - `3072 SHA256:pBAex3FdtVVbenEaTVi1lUyHLMK+/PjnsakyYsAXQfU root@ip-172-31-6-205 (RSA)`
          - 우선순위는 ED25519 > ECDSA > RSA 순서
            - ED25519 입력했는데 안되면 ECDSA.. 순서로 variables 수정
      - `EC2_SSH_HOST_FINGERPRINT` : SHA256:CrajEWPkaKRIy95fqCStFDQKhYh+p42pr7okZdUkkaw
  - SSH 키 권한(읽기 제한)과 로테이션 주기 운영 규칙 문서화
    - 로컬 pem키 권한 설정 변경 완료
      - `icacls "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" /inheritance:r`
        - 상속 제거
      - `icacls "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem" /grant:r "$(whoami):(R)"`
        - whoami 결과로 본인만 읽기 전용 권한 부여
      - `icacls "C:\Users\FORYOUCOM\.ssh\knusrae-server-key-pair.pem"`
        - 출력에 본인 계정만 (R) 이고, 다른 사용자·그룹이 과하게 남아 있지 않으면 완료
- [🟢] **4. SSH 배포 스크립트 표준 순서**
  - (1) 산출물/compose 파일 동기화
  - (2) (Phase 4 — 4-3-A) 시크릿 스크립트 동기화 및 실행
    - **스크립트 동기화**: 저장소에 `infra/scripts/fetch-secrets-to-env.sh`가 있다면 `rsync`/`scp`로 EC2의 고정 경로(예: `/home/ubuntu/knusrae/scripts/`)에 복사한다. 저장소에 없으면 Phase 4에서 정한 **단일 원본**(S3, 별도 ops 레포 등)에서 가져오는 단계를 동일하게 넣는다.
    - **시크릿 파일 갱신**: `compose` 디렉터리에서 `SECRET_ID`·`AWS_REGION`·`OUTPUT_FILE`을 Phase 4와 동일하게 지정하여 스크립트를 실행한다. (또는 EC2에 이미 고정해 둔 환경 변수 사용)
  - (3) 컨테이너 기동: `docker compose pull`(해당 시) -> `docker compose up -d`
  - (4) 불필요 컨테이너/이미지 정리(선택): 디스크 부족 방지용 housekeeping
- [🟢] **5. 헬스체크 게이트**
  - 배포 후 일정 대기(예: 10~30초) -> 헬스 엔드포인트 호출
  - 200 응답 또는 기대 JSON 상태일 때만 성공 처리
  - 실패 시 workflow를 실패 처리하고, 필요하면 자동 롤백(이전 태그 재기동) 단계 추가
- [🟢] **6. 운영 로그/감사성 확보(권장)**
  - 워크플로 로그에 배포 commit SHA, 이미지 태그, EC2 대상 호스트를 남긴다(민감정보 제외)
  - 장애 분석 시점에 "어떤 버전이 언제 배포되었는지" 즉시 추적 가능해야 한다
- **확인 방법**:
  - push 후 EC2에서 새 버전이 기동되고 헬스체크가 성공하는지 확인
  - 필요 시 SSH로 `.env.secrets` 존재·타임스탬프·권한(600)만 확인하고 값은 출력하지 않는다
  - 애플리케이션 로그에서 배포 시각 이후 정상 요청이 처리되는지 확인

### 완료 기준

- [ ] `workflow_dispatch` 또는 push 트리거로 CI/CD를 실행할 수 있음.
- [ ] 프론트 배포가 자동화됨 (build -> S3 -> CloudFront invalidation -> 반영 확인).
- [ ] 백엔드 배포가 자동화됨 (build -> EC2 배포 -> 재시작 -> 헬스체크 게이트).
- [ ] 실패 시 어느 단계에서 실패했는지 Actions 로그만으로 원인 구분이 가능함.
- [ ] 시크릿 값이 로그/출력에 노출되지 않도록 마스킹 및 출력 금지 규칙이 지켜짐.

### 참고

- [knusrae_aws_deployment_plan.md](knusrae_aws_deployment_plan.md) — STEP 11

- Nginx 설정 파일 수정하기
  - 1) SSH 접속
    - `ssh -i <키파일.pem> ubuntu@<EC2_PUBLIC_IP_OR_DOMAIN>`

  - 2) 현재 Nginx 설정 백업
    - `sudo cp /etc/nginx/sites-available/knusrae-api /etc/nginx/sites-available/knusrae-api.bak.$(date +%F-%H%M)`

  - 3) 설정 파일 열기
    - `sudo nano /etc/nginx/sites-available/knusrae-api`
    - 수정 후 저장 시 Ctrl+O -> Enter -> Ctrl+X
  
  - 4) 문법 검사
    - `sudo nginx -t`

  - 5) 반영
    - `sudo systemctl reload nginx`

  - 6) 기타
    - 파일 비우기
      - `sudo truncate -s 0 /etc/nginx/sites-available/knusrae-api`
    - 한 줄씩 삭제
      - Ctrl + K