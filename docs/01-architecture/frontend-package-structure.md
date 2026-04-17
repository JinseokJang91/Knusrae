# Frontend 패키지 및 파일 구조도

**작성일**: 2026-04-14  
**대상**: `frontend/` (Vue 3 + TypeScript + Vite + PrimeVue)

---

## 1. 루트 디렉터리 개요 (현행)

```text
frontend/
├── src/                         # 소스 코드 (아래 상세)
├── index.html
├── package.json / package-lock.json
├── vite.config.ts
├── tsconfig.json / tsconfig.node.json
├── tailwind.config.js
├── postcss.config.js
├── .eslintrc.cjs / eslint.config.cjs
├── .prettierrc.json / .editorconfig / .gitignore / .npmrc
├── components.d.ts              # 컴포넌트 자동 임포트 타입
├── vercel.json
├── Dockerfile / nginx.conf      # 컨테이너/서버 배포 설정
└── README.md / LICENSE.md
```

---

## 2. src 디렉터리 구조 (패키지 및 파일)

```text
src/
├── main.ts                      # 앱 진입점
├── App.vue                      # 루트 컴포넌트
├── env.d.ts                     # Vite 환경 변수 타입
│
├── api/                         # 도메인별 백엔드 API 호출 모듈
│   ├── authApi.ts
│   ├── bookmarkApi.ts
│   ├── categoryApi.ts
│   ├── commonCodeApi.ts
│   ├── creatorApi.ts
│   ├── followApi.ts
│   ├── ingredientApi.ts
│   ├── inquiryApi.ts
│   ├── memberApi.ts
│   ├── recipeApi.ts
│   ├── recipeViewApi.ts
│   ├── recommendationApi.ts
│   ├── searchApi.ts
│   └── themeApi.ts
│
├── types/                       # TypeScript 타입/인터페이스
│   ├── auth.ts
│   ├── bookmark.ts
│   ├── category.ts
│   ├── common.ts
│   ├── creator.ts
│   ├── faq.ts
│   ├── follow.ts
│   ├── ingredient.ts
│   ├── inquiry.ts
│   ├── profile.ts
│   ├── recipe.ts
│   ├── recipeCategory.ts
│   ├── recipeForm.ts
│   ├── router.d.ts
│   └── theme.ts
│
├── stores/                      # Pinia 스토어
│   └── authStore.ts
│
├── router/
│   └── index.ts                 # Vue Router 설정
│
├── utils/                       # 공통 유틸리티
│   ├── auth.ts
│   ├── constants.ts
│   ├── errorHandler.ts
│   ├── globalErrorHandler.ts
│   ├── http.ts                  # HTTP 클라이언트
│   ├── mask.ts
│   ├── oauth.ts
│   ├── search.ts
│   └── toast.ts
│
├── layout/                      # 레이아웃 컴포넌트
│   ├── AppLayout.vue
│   ├── AppTopbar.vue
│   └── AppFooter.vue
│
├── views/                       # 페이지 단위 뷰
│   ├── Dashboard.vue
│   └── pages/
│       ├── admin/
│       │   ├── Admin.vue
│       │   ├── AdminInquiryList.vue
│       │   ├── CommonCodeManagement.vue
│       │   ├── IngredientGroupManagement.vue
│       │   ├── IngredientGroupRegister.vue
│       │   ├── IngredientManagementRegister.vue
│       │   ├── IngredientRegister.vue
│       │   └── IngredientRequestList.vue
│       ├── auth/
│       │   ├── Login.vue
│       │   ├── OAuthCallback.vue
│       │   ├── GoogleCallback.vue
│       │   ├── KakaoCallback.vue
│       │   └── NaverCallback.vue
│       ├── community/
│       │   └── CustomerSupport.vue
│       ├── error/
│       │   ├── Access.vue
│       │   ├── Error.vue
│       │   └── NotFound.vue
│       ├── feed/
│       │   └── FollowingFeed.vue
│       ├── ingredient/
│       │   ├── IngredientDetail.vue
│       │   └── IngredientManagement.vue
│       ├── member/
│       │   └── MemberProfile.vue
│       ├── my/
│       │   ├── MyPage.vue
│       │   ├── Profile.vue
│       │   ├── Bookmarks.vue
│       │   ├── Comments.vue
│       │   ├── Favorites.vue
│       │   ├── Inquiries.vue
│       │   ├── RecipeCreate.vue
│       │   ├── RecipeEdit.vue
│       │   └── Recipes.vue
│       ├── ranking/
│       │   └── Ranking.vue
│       └── recipe/
│           ├── Category.vue
│           ├── RecipeDetail.vue
│           └── SearchResult.vue
│
├── components/                  # 재사용 컴포넌트 (도메인별)
│   ├── ScrollToTop.vue
│   ├── SocialLoginButtons.vue
│   ├── bookmark/
│   │   ├── BookFrame.vue
│   │   ├── BookmarkDialog.vue
│   │   ├── BookmarkMemoDialog.vue
│   │   ├── BookPage.vue
│   │   ├── OpenBookView.vue
│   │   └── RecipeBookFormDialog.vue
│   ├── common/
│   │   ├── AppToast.vue
│   │   └── PageStateBlock.vue
│   ├── community/
│   │   └── FAQ.vue
│   ├── dashboard/
│   │   ├── CategorySection.vue
│   │   ├── CategorySections.vue
│   │   ├── PopularRecipes.vue
│   │   ├── RecentViews.vue
│   │   ├── RecommendedCreators.vue
│   │   ├── ThemeCollections.vue
│   │   └── TodayRecommendations.vue
│   ├── editor/
│   │   ├── ToastUiEditor.vue
│   │   └── ToastUiViewer.vue
│   ├── follow/
│   │   └── FollowListDialog.vue
│   ├── ingredient/
│   │   ├── IngredientCard.vue
│   │   ├── IngredientGrid.vue
│   │   ├── IngredientGroupSelector.vue
│   │   └── IngredientList.vue
│   ├── inquiry/
│   │   ├── InquiryDetailDialog.vue
│   │   └── InquiryFormDialog.vue
│   └── recipe/
│       ├── RecipeCard.vue
│       ├── RecipeComments.vue
│       ├── RecipeDetailGallery.vue
│       ├── RecipeDetailHeader.vue
│       ├── RecipeDetailIngredients.vue
│       ├── RecipeDetailSteps.vue
│       ├── RecipeGridCard.vue
│       ├── RecipeListItem.vue
│       └── form/
│           ├── RecipeFormBasicInfo.vue
│           ├── RecipeFormClassification.vue
│           ├── RecipeFormIngredients.vue
│           └── RecipeFormSteps.vue
│
├── data/                        # 정적/로컬 데이터
│   ├── faqData.ts
│   └── recipeCategoryData.ts
│
└── assets/                      # 스타일·이미지 등 리소스
    ├── styles.scss
    ├── tailwind.css
    ├── images/
    │   └── badges/
    │       ├── badge_appstore.svg
    │       └── badge_googleplay.svg
    └── layout/
        ├── layout.scss
        ├── _core.scss
        ├── _footer.scss
        ├── _main.scss
        ├── _mixins.scss
        ├── _page-container.scss
        ├── _recipe-card-list.scss
        ├── _recipe-sort-buttons.scss
        ├── _responsive.scss
        ├── _topbar.scss
        ├── _typography.scss
        ├── _utils.scss
        └── variables/
            └── _common.scss
```

---

## 3. 패키지(폴더) 역할 요약 (현행)

| 폴더 | 역할 |
|------|------|
| `api/` | 도메인별 REST API 호출 함수 (`auth`, `recipe`, `member`, `theme` 등) |
| `types/` | API/폼/화면에서 사용하는 타입 및 선언 파일 (`router.d.ts` 포함) |
| `stores/` | 전역 상태 저장소 (현재 `authStore` 중심) |
| `router/` | 라우트 정의 및 가드 |
| `utils/` | HTTP 클라이언트, 인증/OAuth, 에러 처리, 마스킹, 토스트 유틸 |
| `layout/` | 상단/하단/전체 레이아웃 컴포넌트 |
| `views/` | 라우트 단위 페이지 (`admin`, `auth`, `my`, `recipe` 등) |
| `components/` | 재사용 컴포넌트 (`bookmark`, `dashboard`, `recipe`, `ingredient` 등) |
| `data/` | FAQ, 레시피 카테고리 등 정적 데이터 |
| `assets/` | SCSS, 이미지 배지, 레이아웃 스타일 리소스 |

---
