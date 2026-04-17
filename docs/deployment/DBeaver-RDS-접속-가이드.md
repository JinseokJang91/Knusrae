# DBeaver로 RDS 접속 가이드 (보안 고려)

개발자 PC에서 DBeaver로 RDS 데이터를 자주 조회해야 할 때, **보안을 유지하면서** 접속하는 방법을 설명합니다.

> **핵심:** SSH 터널 사용 시 **메인 탭 호스트**에는 `localhost`가 아니라 **RDS 엔드포인트**를 넣어야 정상 접속됩니다. (localhost로 하면 연결 실패·EOFException 발생)

---

## 권장 방식: SSH 터널(포트 포워딩)

### 왜 이 방식을 쓰는가?

| 방식 | 보안 | 유지보수 | 비고 |
|------|------|----------|------|
| **RDS 퍼블릭 + 보안 그룹에 내 IP 추가** | △ IP 변경 시마다 규칙 수정 필요, RDS가 인터넷에 노출 | IP 바뀔 때마다 콘솔 수정 | 재택/이동 시 불편 |
| **SSH 터널(EC2 경유)** ✅ | ◎ RDS는 비공개 유지, EC2 SSH만 제어 | EC2 보안 그룹(SSH 22)만 관리 | **권장** |

- **RDS**는 **퍼블릭 액세스: 아니오**로 두고, 보안 그룹은 **EC2 보안 그룹**만 허용합니다.  
  → RDS는 인터넷에 전혀 노출되지 않습니다.
- **EC2**는 이미 SSH(22)로 접속하고 있으므로, 같은 경로로 **SSH 터널**만 열어서 EC2를 경유해 RDS에 접속합니다.
- 따라서 **RDS 보안 그룹에 개인 IP를 추가할 필요가 없고**, IP가 바뀌어도 EC2 SSH만 접속 가능하면 DBeaver 연결이 유지됩니다.

### 동작 흐름

```
[개발자 PC - DBeaver]
    → SSH(22)로 EC2 접속 (기존 키 페어 사용)
    → DBeaver가 메인 탭에 입력한 "RDS 엔드포인트:5432"로 접속 요청
    → 이 요청이 SSH 터널을 타고 EC2에서 실행됨 → EC2가 같은 VPC 내 RDS에 연결
[결과] RDS에 정상 접속 (메인 탭 호스트는 반드시 RDS 엔드포인트)
```

---

## 사전 조건

- RDS: **퍼블릭 액세스: 아니오**, 보안 그룹에 **EC2 보안 그룹**만 5432 허용 (기존 [AWS RDS 구축 가이드](./AWS-RDS-구축-가이드.md)와 동일)
- EC2: SSH(22) 접속 가능 (보안 그룹에 **내 IP**만 허용 권장)
- 로컬에 **EC2 키 페어(.pem)** 보관됨
- [DBeaver](https://dbeaver.io/) 설치됨

---

## DBeaver에서 SSH 터널 설정

### 1. 새 연결 생성

1. DBeaver 실행 → **데이터베이스** → **새 데이터베이스 연결** (또는 `Ctrl+Shift+N`)
2. **PostgreSQL** 선택 → **다음**

### 2. SSH 터널 설정

1. 왼쪽에서 **SSH** 탭 선택
2. **SSH 터널 사용** 체크
3. 다음을 입력:

| 항목 | 값 |
|------|-----|
| **호스트명** | EC2 퍼블릭 IP 또는 퍼블릭 DNS (예: `ec2-xx-xx-xx-xx.ap-northeast-2.compute.amazonaws.com`) |
| **포트** | 22 |
| **사용자 이름** | `ubuntu` (Ubuntu AMI) 또는 `ec2-user` (Amazon Linux) |
| **인증 방법** | Public Key |
| **Private Key** | EC2 키 페어 `.pem` 파일 경로 (예: `C:\Users\사용자\.ssh\knusrae-key.pem`) |
| **Passphrase** | 키에 설정한 경우만 입력 |

4. **SSH 터널 테스트**로 연결 확인 (선택)

### 3. PostgreSQL 연결 정보 (메인 탭)

> **중요:** SSH 터널을 쓰면 DB 연결이 **EC2를 경유**해 나가므로, **호스트에는 `localhost`가 아니라 RDS 엔드포인트**를 넣어야 합니다.  
> `localhost`로 두면 EC2 자신의 5432로 접속을 시도해 연결이 안 되거나 EOFException이 발생합니다.

1. **메인** 탭에서 아래처럼 입력:

| 항목 | 값 |
|------|-----|
| **호스트** | **RDS 엔드포인트** (예: `knusrae-db.xxxxxxxxx.ap-northeast-2.rds.amazonaws.com`) — RDS 콘솔 "연결 및 보안"에서 확인 |
| **포트** | 5432 |
| **데이터베이스** | `knusrae_db` (RDS 생성 시 지정한 DB명) |
| **사용자 이름** | RDS 마스터 사용자 (예: `knusrae_user`) |
| **비밀번호** | RDS 마스터 비밀번호 |

2. **테스트 연결** 클릭 → 성공 시 **완료**

**정리:** SSH 탭 = EC2 주소·키, 메인 탭 = **호스트 RDS 엔드포인트**, 포트 5432.

### 4. 요약
- SSH 터널이 “로컬 5432 → **EC2에서 보는 RDS 엔드포인트:5432**”로 포워딩되도록 해야 합니다.

**DBeaver 버전에 따른 설정:**

- **드라이버 속성** 또는 **SSH 터널** 쪽에 **원격 호스트/포트**를 넣는 옵션이 있다면:
  - **Local port**: 5432 (또는 비어 두면 자동)
  - **Remote host**: RDS 엔드포인트 (예: `knusrae-db.xxxxxxxxx.ap-northeast-2.rds.amazonaws.com`)
  - **Remote port**: 5432  

- **일반적인 경우**: DBeaver는 “SSH 터널 사용 + 메인 호스트 localhost:5432”로 설정하면, 터널이 **SSH 서버(EC2) 기준**에서 `localhost`로 접속합니다.  
  EC2에서 RDS는 **RDS 엔드포인트**로만 접근 가능하므로, 이 방식이면 **호스트를 RDS 엔드포인트로** 넣어야 합니다.

**사용할 설정 (정상 동작 확인됨):**

#### 메인 탭 호스트 = RDS 엔드포인트

- **SSH 탭**: EC2 정보 (호스트=EC2 IP, 포트=22, 키 등)
- **메인 탭**:  
  - **호스트**: RDS 엔드포인트 (예: `knusrae-db.xxxxxxxxx.ap-northeast-2.rds.amazonaws.com`)  
  - **포트**: 5432  

이렇게 하면 DBeaver가 SSH 터널을 통해 EC2에 접속한 뒤, **EC2 입장에서** RDS 엔드포인트:5432로 연결합니다. (EC2와 RDS가 같은 VPC이므로 동작합니다.)

#### 사용하지 말 것: 메인 호스트에 localhost

- localhost로 설정하면 연결 실패 또는 EOFException 발생. 메인 탭 호스트는 **RDS 엔드포인트**만 사용.  

DBeaver/드라이버에 “원격 호스트” 필드가 있으면 이렇게 설정하고, 없으면 위 **메인 탭 호스트 = RDS 엔드포인트** 설정을 사용하세요.

**요약:** SSH 탭 = EC2, 메인 탭 호스트 = RDS 엔드포인트 (localhost 아님).

---

## 보안 체크리스트

- [ ] RDS: **퍼블릭 액세스: 아니오** 유지
- [ ] RDS 보안 그룹: 5432는 **EC2 보안 그룹**만 허용 (개인 IP 추가하지 않음)
- [ ] EC2 보안 그룹: SSH(22)는 **내 IP**만 허용 (가능하면)
- [ ] `.pem` 키는 로컬에만 보관, Git/공유 금지

---

## 문제 해결

- **Connection attempt timed out**  
  - EC2 SSH(22)가 보안 그룹/방화벽에서 허용되는지 확인  
  - 메인 탭 호스트가 RDS 엔드포인트인지, SSH 탭이 EC2인지 다시 확인  

- **Authentication failed (RDS)**  
  - RDS 사용자명/비밀번호, DB 이름 확인  

- **SSH 터널 실패**  
  - EC2 퍼블릭 IP/DNS, 사용자(ubuntu/ec2-user), `.pem` 경로와 권한 확인  

- **EOFException** (Test Connection 시)  
  - **메인 탭 호스트**: `localhost`가 아니라 **RDS 엔드포인트**를 넣었는지 확인. SSH 터널 사용 시 실제 접속은 EC2에서 이루어지므로, 호스트는 RDS 주소여야 합니다.  
  - **SSL**: 연결 설정 → **드라이버 속성** 또는 **SSL** 탭에서 `sslmode`를 `disable` 또는 `allow`로 시도. (RDS는 기본적으로 SSL 없이도 접속 가능하므로, SSL 협상 오류 시 EOF가 날 수 있음)  
  - **SSH 먼저 확인**: 터미널에서 `ssh -i "키경로.pem" ubuntu@EC2퍼블릭IP` 로 EC2 접속이 되는지 확인.  
  - **방화벽/보안 그룹**: RDS 보안 그룹에서 5432가 **EC2 보안 그룹**에서 오는 트래픽만 허용되는지 확인.  

---

## 참고 문서

- [AWS RDS 구축 가이드](./AWS-RDS-구축-가이드.md) — RDS 생성 및 보안 그룹
- [AWS EC2 배포 가이드](./AWS-EC2-배포-가이드.md) — EC2 생성 및 SSH 접속
