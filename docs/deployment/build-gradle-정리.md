# build.gradle 모노레포 정리 요약

## 목적
- 루트에서 한 번만 적용해도 되는 의존성/설정 통합
- 플러그인·버전 중복 제거
- EC2 등 단일 디렉터리 배포 시 빌드 일관성 유지

---

## 1. 역할 분담

| 위치 | 역할 |
|------|------|
| **settings.gradle** | 플러그인 버전 일원화 (`pluginManagement`) |
| **루트 build.gradle** | 그룹/버전, 저장소, 공통 Java 설정, **공통 의존성**, BOM, 테스트 의존성 |
| **backend/*-service/build.gradle** | 실행 가능한 서비스만: Spring Boot 플러그인, mainClass, common-service 의존성, 서비스별 추가 의존성만 |
| **backend/common-service/build.gradle** | 라이브러리용: java-library, jjwt, AWS S3 (버전은 루트 BOM 사용). 루트 subprojects 공통 의존성도 그대로 상속받음. |

---

## 2. 루트에서 한 번만 적용되는 항목 (root build.gradle)

- **플러그인**: `org.springframework.boot`, `io.spring.dependency-management` (버전은 settings.gradle `pluginManagement`에서 관리)
- **ext**: `javaVersion`, `springBootVersion`, `jjwtVersion`
- **allprojects**: `group`, `version`, `repositories` (mavenCentral)
- **subprojects**:
  - Java 21, UTF-8 인코딩, JUnit Platform
  - **dependencyManagement**: Spring Boot BOM, AWS SDK BOM (`2.20.26`)
  - **공통 implementation**: PostgreSQL, commons-lang3, Lombok, Spring (web, data-jpa, security, validation), QueryDSL
  - **공통 test**: `spring-boot-starter-test`, `spring-security-test`

이제 각 서비스에서는 위 의존성을 다시 선언하지 않아도 됨.

---

## 3. settings.gradle 변경

- **pluginManagement**에 Spring Boot / dependency-management / Kotlin 버전 추가
- 서브프로젝트는 `id 'org.springframework.boot'` 등만 쓰고 **버전 생략** (한 곳에서만 버전 관리)

---

## 4. 서비스별 build.gradle (auth, cook, member)

**제거한 것**
- 플러그인 버전 (3.5.3, 1.1.7) → settings.gradle에서 관리
- `repositories { mavenCentral() }` → 루트 allprojects에 있음
- `implementation 'spring-boot-starter-web'`, `spring-boot-starter-validation'` → 루트 subprojects에 있음
- `testImplementation 'spring-boot-starter-test'` → 루트 subprojects에 있음

**유지한 것**
- `plugins { id 'org.springframework.boot'`, `id 'io.spring.dependency-management' }` (및 auth만 Kotlin)
- `springBoot { mainClass = '...' }`
- `implementation project(':backend:common-service')`
- auth 전용: Kotlin, `bootJar`/`jar` 설정, **jjwt 의존성** (버전은 루트 `ext.jjwtVersion` 사용)

**auth-service에서 jjwt를 유지하는 이유**  
Gradle에서 `implementation`으로 선언한 의존성은 **소비자(consumer)의 컴파일 클래스패스에 노출되지 않습니다.** common-service에 jjwt를 `implementation`으로 넣어도, auth-service의 `TokenService` 등이 `io.jsonwebtoken`을 **컴파일 시점**에 쓰려면 auth-service가 직접 jjwt를 선언해야 합니다. 따라서 jjwt는 common-service(공통 필터/유틸용)와 auth-service(토큰 발급·검증 코드용) 양쪽에 두고, 버전만 루트 `ext.jjwtVersion`으로 통일합니다.

---

## 5. common-service build.gradle

- **제거**: `implementation platform('software.amazon.awssdk:bom:2.27.21')`, S3 버전 `2.20.26`
- **이유**: 루트 `dependencyManagement`에서 이미 `software.amazon.awssdk:bom:2.20.26` 사용
- **유지**: `implementation 'software.amazon.awssdk:s3'` (버전 없음 → BOM에서 관리), jjwt (루트 `ext.jjwtVersion` 사용)
- **참고**: common-service도 subproject이므로 루트의 공통 의존성(PostgreSQL, Spring Web/JPA/Security, QueryDSL 등)을 모두 상속받음. 서비스 전용으로 따로 넣은 것은 jjwt·S3뿐.

---

## 6. 의존성 흐름

- **공통 라이브러리/버전**: 루트 build.gradle (subprojects)
- **공통 모듈**: `backend:common-service` (jjwt, S3 등) → auth/cook/member가 `implementation project(':backend:common-service')`로 사용
- **jjwt**: common-service(공통용)와 **auth-service(토큰 코드 컴파일용)** 둘 다 선언. `implementation`은 전이되지 않으므로 auth가 직접 필요.
- **버전**: Spring Boot / BOM / jjwt → 루트 `ext` + `dependencyManagement`에서만 관리

---

## 7. 빌드 검증

- `./gradlew :backend:common-service:compileJava`
- `./gradlew :backend:cook-service:compileJava`
- `./gradlew :backend:auth-service:compileKotlin`

위 태스크로 설정이 정상 동작하는지 확인 가능.  
전체 `clean build` 시 `backend/build` 등 디렉터리가 다른 프로세스에 의해 잠겨 있으면 clean 실패할 수 있음 (Gradle 설정과 무관).
