# Backend 패키지 및 파일 구조도

**작성일**: 2026-02-20  
**대상**: `backend/` (Spring Boot, 멀티 모듈·서비스)

---

## 1. 루트 디렉터리 개요

```
backend/
├── auth-service/      # 인증 (JWT, OAuth 소셜 로그인)
├── common-service/    # 공통 도메인·보안·공통코드 (Member, Follow, CommonCode, JWT 필터 등)
├── cook-service/      # 레시피·재료·테마·카테고리·검색·추천 등 요리 도메인
└── member-service/    # 회원·팔로우·문의 (Member API, Follow, Inquiry)
```

각 서비스는 **Gradle** 기반이며, `build.gradle`, `gradle/wrapper/`, `src/main/java`, `src/main/resources`, `src/test/java` 구조를 가집니다.

---

## 2. auth-service

**기능**: 로그인/로그아웃, JWT 발급·갱신, 소셜 로그인(Google, Kakao, Naver)

```
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
│       │       └── TokenService.java
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
│   └── application-prod.yml
└── src/test/java/com/knusrae/auth/
    └── AuthServiceApplicationTests.java
```

---

## 3. common-service

**기능**: 공통 엔티티(Member, Follow), 공통코드(CommonCode), JWT/보안 설정, CORS, 이미지 저장, 전역 예외 처리

```
common-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/common/
│   ├── custom/
│   │   ├── config/
│   │   │   └── WebConfig.java
│   │   └── storage/
│   │       ├── ImageStorage.java
│   │       └── LocalImageStorage.java
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
│   ├── handler/
│   │   └── GlobalExceptionHandler.java
│   ├── security/
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
│   ├── utils/
│   │   ├── AuthenticationUtils.java
│   │   ├── QuantityParser.java
│   │   └── constants/
│   │       └── CommonConstants.java
│   └── web/
│       ├── AdminCommonCodeController.java
│       └── CommonCodeController.java
├── src/main/resources/
│   └── application-common.yml
└── src/test/ (해당 시 존재 시)
```

---

## 4. cook-service

**기능**: 레시피 CRUD·댓글·즐겨찾기·북마크·레시피북·조회수, 재료·재료그룹·재료신청, 카테고리, 테마 컬렉션, 추천(오늘의 레시피·추천 크리에이터), 검색, 관리자 재료 관리

```
cook-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/cook/
│   ├── CookServiceApplication.java
│   └── api/
│       ├── admin/                          # 관리자용 재료 관리
│       │   ├── domain/service/
│       │   │   └── AdminIngredientService.java
│       │   └── web/
│       │       └── AdminIngredientController.java
│       │
│       ├── category/                       # 트렌딩 카테고리
│       │   ├── domain/service/
│       │   │   └── CategoryService.java
│       │   ├── dto/
│       │   │   └── TrendingCategoryDto.java
│       │   └── web/
│       │       └── CategoryController.java
│       │
│       ├── creator/                        # 추천 크리에이터
│       │   ├── domain/service/
│       │   │   └── CreatorRecommendationService.java
│       │   ├── dto/
│       │   │   └── CreatorDto.java
│       │   └── web/
│       │       └── CreatorController.java
│       │
│       ├── ingredient/                     # 재료·재료그룹·재료신청
│       │   ├── domain/entity/
│       │   │   ├── Ingredient.java
│       │   │   ├── IngredientGroup.java
│       │   │   ├── IngredientPreparation.java
│       │   │   ├── IngredientRequest.java
│       │   │   └── IngredientStorage.java
│       │   ├── domain/repository/
│       │   │   └── (Ingredient*, IngredientGroup* 등)
│       │   ├── dto/
│       │   │   ├── IngredientGroupDto.java
│       │   │   ├── IngredientListResponseDto.java
│       │   │   ├── IngredientStorageDto.java
│       │   │   └── (기타)
│       │   └── web/
│       │       └── IngredientController.java
│       │
│       ├── recipe/                         # 레시피 전반
│       │   ├── domain/entity/
│       │   │   ├── Recipe.java
│       │   │   ├── RecipeBook.java
│       │   │   ├── RecipeBookmark.java
│       │   │   ├── RecipeComment.java
│       │   │   ├── RecipeFavorite.java
│       │   │   ├── RecipeImage.java
│       │   │   ├── RecipeIngredient.java
│       │   │   ├── RecipeStep.java
│       │   │   ├── RecipeView.java
│       │   │   └── (기타)
│       │   ├── domain/repository/
│       │   │   ├── RecipeRepository.java
│       │   │   ├── RecipeRepositoryCustom.java
│       │   │   ├── RecipeRepositoryImpl.java
│       │   │   ├── RecipeBookRepository.java
│       │   │   ├── RecipeBookmarkRepository.java
│       │   │   ├── RecipeCommentRepository.java
│       │   │   ├── RecipeFavoriteRepository.java
│       │   │   ├── RecipeViewRepository.java
│       │   │   ├── RecipeStepRepository.java
│       │   │   └── (기타)
│       │   ├── domain/service/
│       │   │   ├── RecipeService.java
│       │   │   ├── RecipeBookService.java
│       │   │   ├── RecipeBookmarkService.java
│       │   │   ├── RecipeCommentService.java
│       │   │   ├── RecipeFavoriteService.java
│       │   │   └── RecipeViewService.java
│       │   ├── domain/constants/
│       │   │   └── RecipeConstants.java
│       │   ├── dto/
│       │   │   ├── RecipeBookDto.java
│       │   │   ├── RecipeBookmarkDto.java
│       │   │   ├── RecipeCategoryDto.java
│       │   │   ├── RecipeCommentDto.java
│       │   │   ├── RecipeCookingTipDto.java
│       │   │   ├── RecipeDetailDto.java
│       │   │   ├── RecipeDto.java
│       │   │   ├── RecipeFavoriteDto.java
│       │   │   ├── RecipeImageDto.java
│       │   │   ├── RecipeIngredientGroupDto.java
│       │   │   ├── RecipeIngredientItemDto.java
│       │   │   ├── RecipeSimpleDto.java
│       │   │   ├── RecipeStatsDto.java
│       │   │   ├── RecipeStepDto.java
│       │   │   ├── RecipeStepDetailDto.java
│       │   │   ├── RecipeViewDto.java
│       │   │   ├── MemberCommentItemDto.java
│       │   │   └── (기타 request/response DTO)
│       │   └── web/
│       │       ├── RecipeController.java
│       │       ├── RecipeBookController.java
│       │       ├── RecipeBookmarkController.java
│       │       ├── RecipeCommentController.java
│       │       ├── RecipeFavoriteController.java
│       │       └── RecipeViewController.java
│       │
│       ├── recommendation/                  # 오늘의 레시피 추천
│       │   ├── domain/service/
│       │   │   └── RecommendationService.java
│       │   ├── dto/
│       │   │   ├── RecommendedRecipeDto.java
│       │   │   └── TodayRecommendationDto.java
│       │   └── web/
│       │       └── RecommendationController.java
│       │
│       ├── search/                          # 통합 검색
│       │   ├── domain/service/
│       │   │   └── SearchService.java
│       │   └── web/
│       │       └── SearchController.java
│       │
│       └── theme/                           # 테마 컬렉션
│           ├── domain/entity/
│           │   ├── ThemeCollection.java
│           │   └── ThemeCollectionRecipe.java
│           ├── domain/repository/
│           │   ├── ThemeCollectionRepository.java
│           │   └── ThemeCollectionRecipeRepository.java
│           ├── domain/service/
│           │   └── ThemeCollectionService.java
│           ├── dto/
│           │   ├── CreateThemeRequest.java
│           │   ├── ThemeCollectionDto.java
│           │   ├── ThemeCollectionDetailDto.java
│           │   └── ThemeRecipeItemDto.java
│           └── web/
│               ├── ThemeCollectionController.java
│               └── AdminThemeCollectionController.java
│
├── src/main/resources/
│   └── application.yml
└── src/test/java/com/knusrae/cook/
    └── CookServiceApplicationTests.java
```

---

## 5. member-service

**기능**: 회원 프로필/정보, 팔로우/팔로워, 1:1 문의(Inquiry) 및 답변

```
member-service/
├── build.gradle
├── gradle/wrapper/
├── src/main/java/com/knusrae/member/
│   ├── MemberServiceApplication.java
│   └── api/
│       ├── domain/
│       │   ├── entity/
│       │   │   ├── Inquiry.java
│       │   │   ├── InquiryImage.java
│       │   │   └── InquiryReply.java
│       │   ├── repository/
│       │   │   ├── InquiryImageRepository.java
│       │   │   ├── InquiryReplyRepository.java
│       │   │   └── InquiryRepository.java
│       │   └── service/
│       │       ├── FollowService.java
│       │       ├── InquiryService.java
│       │       └── MemberService.java
│       ├── dto/
│       │   ├── FollowDto.java
│       │   ├── FollowerDto.java
│       │   ├── FollowingDto.java
│       │   ├── InquiryDetailDto.java
│       │   ├── InquiryListItemDto.java
│       │   ├── InquiryListResponse.java
│       │   ├── InquiryReplyDto.java
│       │   └── MemberDto.java
│       └── web/
│           ├── FollowController.java
│           ├── InquiryController.java
│           └── MemberController.java
├── src/main/resources/
│   └── application.yml
└── src/test/java/com/knusrae/member/
    └── MemberServiceApplicationTests.java
```

---

## 6. 서비스별 패키지 규칙 요약

| 서비스 | 공통 패턴 | 비고 |
|--------|-----------|------|
| **auth-service** | `api/domain`(entity, repository, service), `api/dto`, `api/web`, `api/utils` | OAuth·JWT 전담 |
| **common-service** | `domain`, `dto`, `web`, `security`, `custom`, `handler`, `utils` | Member/Follow/CommonCode·보안·전역 예외 |
| **cook-service** | `api/{도메인}/domain`(entity, repository, service), `api/{도메인}/dto`, `api/{도메인}/web` | admin, category, creator, ingredient, recipe, recommendation, search, theme |
| **member-service** | `api/domain`, `api/dto`, `api/web` | Member, Follow, Inquiry |

---

## 7. 참고

- **빌드**: 각 서비스 디렉터리에서 `./gradlew build` (또는 프로젝트 루트에서 멀티모듈 빌드)
- **실행**: 각 서비스별 `Application` main 실행 또는 프로필별 `application-*.yml` 사용
- **문서 갱신**: 패키지·클래스 추가·이동 시 이 문서를 함께 수정할 것을 권장합니다.
