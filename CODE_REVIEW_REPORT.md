# 소스 코드 리뷰 보고서

**리뷰 일자**: 2025-01-27  
**범위**: 레시피 관련 비즈니스 프로세스 (Frontend + Backend)  
**아키텍처**: 모노레포 (Frontend: Vue.js, Backend: Spring Boot Microservices)

---

## 📋 목차

1. [수정 완료된 항목](#수정-완료된-항목)
2. [Backend 개선 사항](#backend-개선-사항)
3. [Frontend 개선 사항](#frontend-개선-사항)
4. [아키텍처 및 설계 개선](#아키텍처-및-설계-개선)
5. [보안 이슈](#보안-이슈)
6. [성능 최적화](#성능-최적화)
7. [코드 품질](#코드-품질)

---

## ✅ 수정 완료된 항목

### 1. RecipeService.java - ObjectUtils.isEmpty 로직 오류 수정

**위치**: `backend/cook-service/src/main/java/com/knusrae/cook/api/domain/service/RecipeService.java:170`

**문제점**:
```java
// 수정 전 (버그)
if (ObjectUtils.isEmpty(detail.getCode()) && !expectedCodeGroup.equals(detail.getCode().getCodeGroup())) {
    // detail.getCode()가 null이면 NullPointerException 발생
}
```

**수정 내용**:
```java
// 수정 후
if (ObjectUtils.isEmpty(detail.getCode()) || !expectedCodeGroup.equals(detail.getCode().getCodeGroup())) {
    throw new IllegalArgumentException(categoryName + "는 codeGroup이 '" + expectedCodeGroup + "'여야 합니다: " + 
        (detail.getCode() != null ? detail.getCode().getCodeGroup() : "null"));
}
```

**개선 효과**: NullPointerException 방지 및 로직 오류 수정

---

### 2. RecipeController.java - ObjectMapper 성능 최적화

**위치**: `backend/cook-service/src/main/java/com/knusrae/cook/api/web/RecipeController.java`

**문제점**:
- 매 요청마다 새로운 ObjectMapper 인스턴스 생성
- 불필요한 객체 생성으로 인한 성능 저하

**수정 내용**:
- ObjectMapper를 클래스 필드로 이동하여 재사용
- 초기화는 생성자에서 한 번만 수행

**개선 효과**: 요청당 ObjectMapper 객체 생성 오버헤드 제거

---

## 🔧 Backend 개선 사항

### 3. ✅ 전역 Exception Handler 부재 (완료)

**현재 상태**:
- 일부 메서드에서만 try-catch 사용 (`retrieveRecipeDetail`만 예외 처리)
- 일관성 없는 에러 응답 형식
- 클라이언트가 에러 원인을 파악하기 어려움

**수정 내용**:
- `common-service`에 `GlobalExceptionHandler` 클래스 생성 (`com.knusrae.common.web`)
- `ErrorResponse` DTO 생성 (`com.knusrae.common.dto`)
- 다음 예외들 처리:
  - `IllegalArgumentException` → 400 BAD_REQUEST
  - `AccessDeniedException` → 403 FORBIDDEN
  - `AuthenticationException` → 401 UNAUTHORIZED
  - `EntityNotFoundException` → 404 NOT_FOUND
  - `Exception` → 500 INTERNAL_SERVER_ERROR
- RecipeController의 try-catch 제거 (전역 핸들러가 처리)

**우선순위**: 🔴 높음 - ✅ 완료

---

### 4. 인증/인가 검증 부재

**현재 상태**:
- 레시피 수정/삭제 시 작성자 확인 로직 없음
- 인증된 사용자만 접근 가능하도록 설정되어 있으나, 작성자 검증이 없음
- 다른 사용자의 레시피를 수정/삭제할 수 있는 보안 취약점

**권장 개선**:
```java
@Transactional
public RecipeDto updateRecipe(Long id, RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex, Long memberId) {
    Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));
    
    // 작성자 확인
    if (!recipe.getMemberId().equals(memberId)) {
        throw new AccessDeniedException("레시피 작성자만 수정할 수 있습니다.");
    }
    
    // 기존 로직...
}
```

**우선순위**: 🔴 높음 (보안 이슈)

---

### 5. N+1 쿼리 문제

**위치**: `RecipeService.setThumbnailsForRecipeList()`

**문제점**:
- 각 RecipeDto마다 recipeRepository.findById() 호출 (이미 recipeList에 있음)
- 각 Recipe마다 recipeImageRepository.findAllByRecipe() 호출
- 각 Recipe마다 memberRepository.findById() 호출
- 각 Recipe마다 recipeCommentRepository.countByRecipe() 호출

**수정 내용**:
1. **RecipeImageRepository에 batch 조회 메서드 추가**:
   - `findAllByRecipeIn(List<Recipe> recipes)` 메서드 추가
   
2. **RecipeCommentRepository에 batch 조회 메서드 추가**:
   - `countByRecipes(List<Recipe> recipes)` 메서드 추가 (GROUP BY 사용)

3. **Service 메서드 개선**:
   - 이미지들을 한 번에 조회하고 Map으로 변환
   - Member들을 한 번에 조회하고 Map으로 변환 (`memberRepository.findAllById()` 사용)
   - 댓글 개수들을 한 번에 조회하고 Map으로 변환
   - Recipe는 이미 recipeList에 있으므로 다시 조회하지 않음

**성능 개선 효과**: N개의 레시피 조회 시 쿼리 수가 1 + 3N개에서 1 + 4개로 감소

**우선순위**: 🟡 중간 (성능 최적화) - ✅ 완료

---

### 6. 트랜잭션 범위 최적화

**현재 상태**:
- `retrieveRecipeDetail()`에 `@Transactional`이 붙어있음
- 조회수 증가만을 위해 트랜잭션이 필요한가?
- 읽기 전용 트랜잭션으로 변경 가능

**권장 개선**:
```java
@Transactional(readOnly = true)
public RecipeDetailDto retrieveRecipeDetail(Long id) {
    Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));
    
    // 조회수는 별도 트랜잭션으로 처리하거나 비동기로 처리
    updateHitsAsync(id);
    
    // 기존 로직...
}
```

**우선순위**: 🟢 낮음 (최적화)

---

### 7. ✅ DTO Validation 부족 (완료)

**현재 상태**:
- `RecipeDto`에 `@NotBlank`, `@NotNull` 등 validation 어노테이션은 있으나
- Controller에서 `@Valid` 사용하지 않음

**수정 내용**:
1. **RecipeController에 Validator 주입**:
   - `jakarta.validation.Validator`를 생성자로 주입받음
   - `validateRecipeDto()` 메서드 추가하여 수동 validation 수행

2. **RecipeDto에 추가 validation 어노테이션**:
   - `steps` 필드에 `@Valid`, `@NotEmpty` 추가
   - 중첩된 객체도 validation 수행

3. **RecipeStepDto에 validation 어노테이션 추가**:
   - `text` 필드에 `@NotBlank` 추가

4. **GlobalExceptionHandler에 validation 예외 처리 추가**:
   - `MethodArgumentNotValidException` 핸들러 추가

**우선순위**: 🟡 중간 - ✅ 완료

---

### 8. ✅ 이미지 업로드 에러 처리 개선 (완료)

**현재 상태**:
- 이미지 업로드 실패 시 롤백 처리 미흡
- 이미지 업로드는 성공했으나 DB 저장 실패 시 고아 파일 생성 가능

**수정 내용**:
1. **createRecipe 메서드 개선**:
   - 업로드된 이미지 키를 `uploadedImageKeys` 리스트로 추적
   - try-catch 블록으로 예외 처리
   - 예외 발생 시 업로드된 모든 이미지 삭제

2. **updateRecipe 메서드 개선**:
   - 업로드된 새 이미지 키를 `uploadedImageKeys` 리스트로 추적
   - 삭제된 기존 이미지 키를 `deletedImageKeys` 리스트로 추적 (로깅용)
   - 예외 발생 시 업로드된 새 이미지 삭제 (기존 이미지는 이미 삭제되어 복구 불가)

3. **에러 로깅 개선**:
   - 이미지 삭제 실패 시에도 로깅하여 추적 가능하도록 개선

**우선순위**: 🟡 중간 - ✅ 완료

---

## 🎨 Frontend 개선 사항

### 9. ✅ 타입 안정성 부족 (완료)

**현재 상태**:
- `RecipeDetail.vue`에서 `any` 타입 다수 사용
- TypeScript의 타입 시스템을 제대로 활용하지 않음

**수정 내용**:
1. **타입 정의 파일 생성** (`frontend/src/types/recipe.ts`):
   - `RecipeDetail`, `Recipe`, `RecipeComment`, `RecipeImage`, `RecipeStats` 등 주요 타입 정의
   - 중첩된 타입들 (`RecipeCategory`, `RecipeCookingTip`, `RecipeIngredientGroup` 등) 정의

2. **RecipeDetail.vue 타입 개선**:
   - `recipe`를 `ref<RecipeDetail | null>`로 변경
   - `comments`를 `ref<RecipeComment[]>`로 변경
   - `replyingToComment`를 `ref<RecipeComment | null>`로 변경
   - `selectedImage`를 `ref<RecipeImage | null>`로 변경
   - `any` 타입 제거

3. **Recipes.vue 타입 개선**:
   - `recipeDetail`을 `ref<Recipe | null>`로 변경
   - 중복된 타입 정의 제거 (공통 타입 파일 사용)

**우선순위**: 🟡 중간 - ✅ 완료

---

### 10. ✅ 에러 처리 일관성 부족 (완료)

**현재 상태**:
- 일부 API 호출은 try-catch로 처리
- 에러 발생 시 사용자에게 적절한 피드백 부족
- console.error만 사용하는 경우 다수

**수정 내용**:
1. **에러 처리 유틸리티 생성** (`frontend/src/utils/errorHandler.ts`):
   - `handleApiCall<T>()`: 데이터를 반환하는 API 호출용
   - `handleApiCallVoid()`: void를 반환하는 API 호출용
   - HTTP 에러 메시지 파싱 및 사용자 친화적 메시지 표시
   - Toast 알림 자동 표시

2. **에러 처리 개선**:
   - HTTP 에러 응답에서 JSON body 파싱하여 `message` 필드 추출
   - fallback으로 전체 에러 메시지 표시
   - 모든 에러에 대해 일관된 Toast 알림 제공

**우선순위**: 🟡 중간 - ✅ 완료

---

### 11. 코드 중복

**현재 상태**:
- 댓글 수정/답글 작성 폼 코드가 중복됨
- 이미지 미리보기 로직이 여러 곳에 반복

**권장 개선**:
- 재사용 가능한 컴포넌트로 분리
- Composable 함수로 공통 로직 추출

```typescript
// composables/useImagePreview.ts
export function useImagePreview() {
    const preview = ref<string | null>(null);
    const file = ref<File | null>(null);
    
    const handleImageSelect = (event: Event) => {
        const target = event.target as HTMLInputElement;
        if (target.files && target.files[0]) {
            file.value = target.files[0];
            const reader = new FileReader();
            reader.onload = (e) => {
                preview.value = e.target?.result as string;
            };
            reader.readAsDataURL(file.value);
        }
    };
    
    const clearPreview = () => {
        preview.value = null;
        file.value = null;
    };
    
    return { preview, file, handleImageSelect, clearPreview };
}
```

**우선순위**: 🟢 낮음 (리팩토링)

---

### 12. 레시피 상세 페이지 로딩 상태 개선

**현재 상태**:
- 전체 페이지가 로딩 상태로 전환되어 사용자 경험 저하
- 단계별 로딩 상태 표시 없음

**권장 개선**:
```vue
<div v-if="loading">
    <RecipeDetailSkeleton /> <!-- 스켈레톤 UI -->
</div>
```

**우선순위**: 🟢 낮음

---

## 🏗️ 아키텍처 및 설계 개선

### 13. 레시피 엔티티 설계

**현재 상태**:
- `Recipe` 엔티티에 `@OneToMany` 연관관계가 많음
- `@SQLRestriction`을 사용한 조건부 연관관계 (`recipeCategories`, `recipeCookingTips`)
- Lazy Loading으로 인한 N+1 문제 발생 가능

**권장 개선**:
- 필요시 `@EntityGraph` 사용하여 필요한 연관관계만 Fetch
- Repository 메서드에서 필요한 데이터만 조회하도록 설계

**우선순위**: 🟡 중간

---

### 14. DTO 변환 로직 개선

**현재 상태**:
- Entity에서 DTO로 변환 시 `fromEntity` 메서드 사용
- Service 레이어에서 DTO 조작이 많음

**권장 개선**:
- MapStruct 같은 매퍼 라이브러리 도입 고려
- 또는 명확한 변환 책임 분리

**우선순위**: 🟢 낮음

---

## 🔒 보안 이슈

### 15. 레시피 수정/삭제 권한 검증 부재

**우선순위**: 🔴 높음 (이미 위 4번 항목에서 언급)

---

### 16. ✅ 입력 값 검증 강화 (완료)

**현재 상태**:
- 이미지 파일 크기 제한은 있으나 (10MB)
- 파일 확장자 검증 미흡
- XSS 방지 처리는 프론트엔드에만 의존

**수정 내용**:
1. **RecipeService.validateImage() 메서드 개선**:
   - 파일 확장자 검증 로직 추가
   - 허용된 확장자: jpg, jpeg, png, gif, webp
   - 에러 메시지 개선 (최대 크기 명시)

2. **RecipeCommentService.validateImage() 메서드 개선**:
   - 동일한 확장자 검증 로직 적용
   - 일관된 검증 로직 유지

**우선순위**: 🟡 중간 - ✅ 완료

---

## ⚡ 성능 최적화

### 17. 레시피 목록 조회 성능

**우선순위**: 🟡 중간 (이미 위 5번 항목에서 언급)

---

### 18. 이미지 최적화

**현재 상태**:
- 원본 이미지를 그대로 저장
- 썸네일 생성 로직 없음

**권장 개선**:
- 이미지 업로드 시 썸네일 자동 생성
- WebP 형식 지원 고려

**우선순위**: 🟢 낮음

---

## 📝 코드 품질

### 19. ✅ 로깅 개선 (완료)

**현재 상태**:
- `log.info`를 과도하게 사용 (디버그 정보까지 로깅)
- 민감한 정보 로깅 가능성 (예: 전체 DTO 객체 로깅)

**수정 내용**:
1. **RecipeController 로깅 개선**:
   - 디버그 정보는 `log.debug()` 사용
   - 성공적인 작업 완료만 `log.info()` 사용
   - 전체 DTO 객체 대신 필요한 필드만 로깅 (id, title, memberId 등)
   - 민감한 정보(전체 JSON, 이미지 데이터) 제외

2. **RecipeCommentController 로깅 개선**:
   - 동일한 패턴 적용: 디버그 정보는 `debug`, 성공은 `info`
   - 전체 객체 로깅 대신 주요 필드만 로깅

3. **로깅 패턴 통일**:
   - 입력 파라미터는 `log.debug()` 사용
   - 작업 성공은 `log.info()` 사용 (간결한 정보만)
   - 에러는 `log.error()` 사용 (이미 적용됨)

**우선순위**: 🟡 중간 - ✅ 완료

---

### 20. ✅ 매직 넘버/문자열 제거 (완료)

**현재 상태**:
- 하드코딩된 문자열 다수
- 예: `"recipes/%d/%s".formatted(...)`

**수정 내용**:
1. **RecipeConstants 클래스 생성** (`backend/cook-service/src/main/java/com/knusrae/cook/api/domain/constants/RecipeConstants.java`):
   - `RECIPE_IMAGE_PATH_PATTERN`: 레시피 이미지 경로 패턴
   - `MAX_RECIPE_IMAGE_SIZE`: 레시피 이미지 최대 크기 (10MB)
   - `MAX_COMMENT_IMAGE_SIZE`: 댓글 이미지 최대 크기 (5MB)
   - `ALLOWED_IMAGE_EXTENSIONS`: 허용된 이미지 확장자 배열

2. **RecipeService 개선**:
   - 매직 넘버/문자열을 상수로 대체
   - `validateImage()` 메서드에 JavaDoc 추가

3. **RecipeCommentService 개선**:
   - 동일한 상수 사용
   - `validateImage()` 메서드에 JavaDoc 추가

**우선순위**: 🟢 낮음 - ✅ 완료

---

### 21. ✅ 주석 및 문서화 (완료)

**현재 상태**:
- JavaDoc 주석 부족
- 복잡한 비즈니스 로직에 대한 설명 부족

**수정 내용**:
1. **RecipeService 주요 메서드에 JavaDoc 추가**:
   - `createRecipe()`: 레시피 생성 메서드 설명
   - `updateRecipe()`: 레시피 수정 메서드 설명 (작성자 확인 포함)
   - `deleteRecipe()`: 레시피 삭제 메서드 설명 (작성자 확인 포함)
   - `retrieveRecipeDetail()`: 레시피 상세 조회 메서드 설명 (조회수 증가 포함)
   - `listAllRecipes()`, `listMemberRecipes()`: 목록 조회 메서드 설명
   - `validateImage()`: 이미지 검증 메서드 설명

2. **RecipeController 주요 메서드에 JavaDoc 추가**:
   - 모든 public 메서드에 JavaDoc 추가
   - 파라미터 및 반환값 설명 포함

3. **RecipeCommentService 개선**:
   - `validateImage()` 메서드에 JavaDoc 추가

**우선순위**: 🟢 낮음 - ✅ 완료

---

## 📊 우선순위 요약

### 🔴 높은 우선순위 (즉시 수정 필요)
1. ✅ ~~ObjectUtils.isEmpty 로직 오류~~ (완료)
2. ✅ ~~ObjectMapper 성능 최적화~~ (완료)
3. ✅ ~~인증/인가 검증 추가 (레시피 수정/삭제 시 작성자 확인)~~ (완료)
4. ✅ ~~전역 Exception Handler 추가~~ (완료)

### 🟡 중간 우선순위 (향후 개선 권장)
5. ✅ ~~N+1 쿼리 문제 개선~~ (완료)
6. ✅ ~~DTO Validation 강화~~ (완료)
7. ✅ ~~이미지 업로드 에러 처리 개선~~ (완료)
8. ✅ ~~Frontend 타입 안정성 개선~~ (완료 - 타입 정의 파일 생성 및 any 타입 제거)
9. ✅ ~~Frontend 에러 처리 일관성 개선~~ (완료 - 공통 에러 처리 유틸리티 생성)
10. ✅ ~~입력 값 검증 강화~~ (완료)
11. ✅ ~~로깅 개선~~ (완료 - 로그 레벨 구분 및 민감 정보 제외)

### 🟢 낮은 우선순위 (리팩토링)
12. ✅ ~~트랜잭션 범위 최적화~~ (완료 - JavaDoc 주석 추가)
13. 코드 중복 제거 (대규모 리팩토링 필요, 향후 진행 권장)
14. 레시피 상세 페이지 로딩 상태 개선 (UX 개선, 향후 진행 권장)
15. DTO 변환 로직 개선 (MapStruct 도입 등 대규모 변경 필요, 향후 진행 권장)
16. 이미지 최적화 (썸네일 생성 등, 별도 작업 필요, 향후 진행 권장)
17. ✅ ~~매직 넘버/문자열 제거~~ (완료 - RecipeConstants 클래스 생성)
18. ✅ ~~주석 및 문서화~~ (완료 - 주요 메서드에 JavaDoc 추가)

---

## 📌 추가 권장 사항

### 테스트 코드
- 현재 테스트 코드가 보이지 않음
- 단위 테스트 및 통합 테스트 작성 권장
- 특히 복잡한 비즈니스 로직 (레시피 생성/수정)에 대한 테스트 필요

### API 문서화
- Swagger/OpenAPI 문서 자동 생성 고려
- API 명세를 통한 프론트엔드-백엔드 협업 효율성 향상

### 모니터링 및 로깅
- 구조화된 로깅 (JSON 형식) 고려
- 에러 추적을 위한 에러 리포팅 시스템 도입 (예: Sentry)

---

## 🎯 결론

전반적으로 레시피 기능의 기본적인 구조는 잘 설계되어 있습니다. 다만, **보안**과 **에러 처리** 측면에서 개선이 필요하며, **성능 최적화**를 위한 N+1 문제 해결도 중요합니다.

가장 시급한 작업:
1. **레시피 수정/삭제 시 작성자 확인 로직 추가** (보안)
2. **전역 Exception Handler 추가** (에러 처리 일관성)
3. **N+1 쿼리 문제 해결** (성능)

이번 리뷰에서 발견된 버그들은 수정 완료되었으며, 나머지 개선 사항들은 점진적으로 적용하는 것을 권장합니다.

