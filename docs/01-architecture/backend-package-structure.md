# Backend 패키지 및 파일 구조도

**작성일**: 2026-04-14  
**대상**: `backend/` (Spring Boot 기반 멀티 모듈)

---

## 1. 루트 디렉터리 개요 (현행)

```text
backend/
├── auth-service/         # 인증 (JWT, OAuth 소셜 로그인)
├── common-service/       # 공통 도메인/보안/유틸/스토리지
├── cook-service/         # 레시피/재료/테마/검색/추천/인기 레시피
├── member-service/       # 회원/팔로우/문의
└── database-migrations/  # DB 마이그레이션(SQL)
```

- 각 서비스는 Gradle 기반이며 `build.gradle`, `src/main/java`, `src/main/resources`, `src/test` 구조를 사용합니다.
- `database-migrations`는 애플리케이션 서비스와 별도로 SQL 마이그레이션을 관리합니다.

---

## 2. auth-service

**기능**: 로그인/로그아웃, JWT 발급/갱신, OAuth 로그인(Google/Kakao/Naver), OAuth state 처리

```text
auth-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/auth/
│   ├── AuthServiceApplication.java
│   └── api/
│       ├── domain/
│       │   ├── entity/
│       │   │   ├── RefreshToken.java
│       │   │   └── TokenBlacklist.java
│       │   ├── repository/
│       │   │   ├── RefreshTokenRepository.java
│       │   │   └── TokenBlacklistRepository.java
│       │   └── service/
│       │       ├── GoogleAuthService.java
│       │       ├── KakaoAuthService.java
│       │       ├── NaverAuthService.java
│       │       ├── OAuthStateService.java
│       │       ├── TokenService.java
│       │       └── TokenBlacklistCheckerImpl.java
│       ├── dto/
│       │   ├── GoogleUserDTO.java
│       │   ├── KakaoUserDTO.java
│       │   └── NaverUserDTO.java
│       ├── utils/
│       │   ├── CookieUtils.java
│       │   └── LoginFormatter.java
│       └── web/
│           ├── AuthController.java
│           ├── request/
│           │   └── TestLoginRequest.java
│           └── response/
│               └── TokenResponse.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
└── src/test/java/com/knusrae/auth/
    └── AuthServiceApplicationTests.java
```

---

## 3. common-service

**기능**: 공통 엔티티/레포지토리, 공통코드, 보안 필터/설정, 전역 예외, 마스킹 유틸, 스토리지(Local/S3), 헬스 체크

```text
common-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/common/
│   ├── custom/
│   │   ├── config/
│   │   │   └── WebConfig.java
│   │   └── storage/
│   │       ├── ImageStorage.java
│   │       ├── LocalImageStorage.java
│   │       ├── S3Config.java
│   │       ├── S3ImageStorage.java
│   │       └── StorageKeyUtils.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── CommonCode.java
│   │   │   ├── CommonCodeDetail.java
│   │   │   ├── CommonCodeDetailId.java
│   │   │   ├── Follow.java
│   │   │   └── Member.java
│   │   ├── enums/
│   │   │   ├── Active.java
│   │   │   ├── Gender.java
│   │   │   └── SocialRole.java
│   │   ├── repository/
│   │   │   ├── CommonCodeDetailRepository.java
│   │   │   ├── CommonCodeRepository.java
│   │   │   ├── FollowRepository.java
│   │   │   └── MemberRepository.java
│   │   └── service/
│   │       └── CommonCodeService.java
│   ├── dto/
│   │   ├── AdminCommonCodeDetailDto.java
│   │   ├── AdminCommonCodeResponse.java
│   │   ├── CommonCodeCreateRequest.java
│   │   ├── CommonCodeDetailCreateRequest.java
│   │   ├── CommonCodeDetailResponse.java
│   │   ├── CommonCodeDetailUpdateRequest.java
│   │   ├── CommonCodeListDto.java
│   │   ├── CommonCodeResponse.java
│   │   ├── CommonCodeUpdateRequest.java
│   │   └── ErrorResponse.java
│   ├── exception/
│   │   └── ResourceNotFoundException.java
│   ├── handler/
│   │   └── GlobalExceptionHandler.java
│   ├── health/
│   │   └── HealthController.java
│   ├── security/
│   │   ├── TokenBlacklistChecker.java
│   │   ├── config/
│   │   │   ├── QueryDslConfig.java
│   │   │   ├── RestTemplateConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── filter/
│   │   │   ├── CorsFilter.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── handler/
│   │   │   └── SecurityHandlers.java
│   │   └── provider/
│   │       └── JwtTokenProvider.java
│   └── utils/
│       ├── AuthenticationUtils.java
│       ├── PiiMaskUtils.java
│       ├── QuantityParser.java
│       └── constants/
│           └── CommonConstants.java
├── src/main/resources/
│   └── application-common.yml
└── src/test/ (필요 시)
```

---

## 4. cook-service

**기능**: 레시피/재료/테마/카테고리/검색/추천/인기 레시피, 관리자 기능

```text
cook-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/cook/
│   ├── CookServiceApplication.java
│   └── api/
│       ├── admin/
│       │   ├── domain/service/
│       │   │   └── AdminIngredientService.java
│       │   └── web/
│       │       ├── AdminCommonCodeController.java
│       │       ├── AdminIngredientController.java
│       │       └── AdminThemeCollectionController.java
│       ├── category/
│       │   ├── domain/service/CategoryService.java
│       │   ├── dto/TrendingCategoryDto.java
│       │   └── web/CategoryController.java
│       ├── creator/
│       │   ├── domain/service/CreatorRecommendationService.java
│       │   ├── dto/CreatorDto.java
│       │   └── web/CreatorController.java
│       ├── ingredient/
│       │   ├── domain/entity/
│       │   │   ├── Ingredient.java
│       │   │   ├── IngredientGroup.java
│       │   │   ├── IngredientPreparation.java
│       │   │   ├── IngredientRequest.java
│       │   │   └── IngredientStorage.java
│       │   ├── domain/repository/(Ingredient*Repository 계열)
│       │   ├── domain/service/
│       │   │   ├── IngredientService.java
│       │   │   └── IngredientRequestService.java
│       │   ├── dto/(IngredientDto, IngredientGroupDto 등)
│       │   └── web/
│       │       ├── IngredientController.java
│       │       └── IngredientRequestController.java
│       ├── popular/
│       │   ├── config/BatchSchedulerConfig.java
│       │   ├── domain/service/
│       │   │   ├── PopularRecipeService.java
│       │   │   └── PopularityCalculationService.java
│       │   └── dto/
│       │       ├── PopularRecipeDto.java
│       │       └── PopularityStatsDto.java
│       ├── recipe/
│       │   ├── domain/constants/RecipeConstants.java
│       │   ├── domain/entity/
│       │   │   ├── Recipe.java
│       │   │   ├── RecipeBook.java
│       │   │   ├── RecipeCategory.java
│       │   │   ├── RecipeCategoryId.java
│       │   │   ├── RecipeComment.java
│       │   │   ├── RecipeDetail.java
│       │   │   ├── RecipeFavorite.java
│       │   │   ├── RecipeImage.java
│       │   │   ├── RecipeIngredientGroup.java
│       │   │   ├── RecipePopularity.java
│       │   │   ├── RecipePopularityHistory.java
│       │   │   └── RecipeView.java
│       │   ├── domain/enums/(Status, Visibility)
│       │   ├── domain/repository/(Recipe*Repository 계열)
│       │   ├── domain/service/
│       │   │   ├── RecipeService.java
│       │   │   ├── RecipeBookService.java
│       │   │   ├── RecipeBookmarkService.java
│       │   │   ├── RecipeCommentService.java
│       │   │   ├── RecipeFavoriteService.java
│       │   │   └── RecipeViewService.java
│       │   ├── dto/(RecipeDto, RecipeDetailDto, RecipeStatsDto 등)
│       │   └── web/
│       │       ├── RecipeController.java
│       │       ├── RecipeBookController.java
│       │       ├── RecipeBookmarkController.java
│       │       ├── RecipeCommentController.java
│       │       ├── RecipeFavoriteController.java
│       │       └── RecipeViewController.java
│       ├── recommendation/
│       │   ├── domain/service/RecommendationService.java
│       │   ├── dto/(RecommendedRecipeDto, TodayRecommendationDto)
│       │   └── web/RecommendationController.java
│       ├── search/
│       │   ├── domain/service/SearchService.java
│       │   └── web/SearchController.java
│       └── theme/
│           ├── domain/entity/(ThemeCollection, ThemeCollectionRecipe)
│           ├── domain/repository/(ThemeCollection*Repository)
│           ├── domain/service/ThemeCollectionService.java
│           ├── dto/(CreateThemeRequest, ThemeCollectionDto 등)
│           └── web/ThemeCollectionController.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
└── src/test/java/com/knusrae/cook/
    └── CookServiceApplicationTests.java
```

---

## 5. member-service

**기능**: 회원 정보, 팔로우/팔로워, 1:1 문의

```text
member-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/member/
│   ├── MemberServiceApplication.java
│   └── api/
│       ├── follow/
│       │   ├── domain/service/FollowService.java
│       │   ├── dto/(FollowDto, FollowerDto, FollowingDto)
│       │   └── web/FollowController.java
│       ├── inquiry/
│       │   ├── domain/entity/(Inquiry, InquiryImage, InquiryReply)
│       │   ├── domain/repository/
│       │   │   ├── InquiryRepository.java
│       │   │   ├── InquiryImageRepository.java
│       │   │   └── InquiryReplyRepository.java
│       │   ├── domain/service/InquiryService.java
│       │   ├── dto/
│       │   │   ├── InquiryDetailDto.java
│       │   │   ├── InquiryListItemDto.java
│       │   │   ├── InquiryListResponse.java
│       │   │   └── InquiryReplyDto.java
│       │   └── web/InquiryController.java
│       └── member/
│           ├── domain/service/MemberService.java
│           ├── dto/MemberDto.java
│           └── web/MemberController.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
└── src/test/
    ├── java/com/knusrae/member/MemberServiceApplicationTests.java
    └── resources/application-test.yml
```

---

## 6. database-migrations

**기능**: DB 스키마 버전 관리 및 마이그레이션 SQL 관리

```text
database-migrations/
├── build.gradle
└── src/main/resources/db/migration/
    └── V1__baseline_schema.sql
```