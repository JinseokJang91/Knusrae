# Frontend 패키지 및 파일 구조도

**작성일**: 2026-02-20  
**대상**: `frontend/` (Vue 3 + TypeScript + Vite + PrimeVue)

---

## 1. 루트 디렉터리 개요

```
frontend/
├── public/                 # 정적 자산 (빌드 시 그대로 복사)
│   └── guide/              # 가이드 이미지 (Guide_01.png ~ Guide_05.png)
├── src/                    # 소스 코드 (아래 상세)
├── index.html
├── package.json            # 의존성: Vue 3, Vue Router, Pinia, PrimeVue, Tailwind, Toast UI Editor
├── vite.config.ts
├── tsconfig.json / tsconfig.node.json
├── tailwind.config.js
├── postcss.config.js
├── .eslintrc.cjs / .prettierrc.json / .editorconfig / .gitignore / .npmrc
├── components.d.ts         # PrimeVue 자동 임포트 타입
├── vercel.json
├── env.d.ts                # (src 내 env.d.ts 참고)
├── README.md / LICENSE.md
└── .env / .env.local       # 환경 변수 (비공개)
```

---

## 2. src 디렉터리 구조 (패키지 및 파일)

```
src/
├── main.ts                 # 앱 진입점
├── App.vue                 # 루트 컴포넌트
├── env.d.ts                # 환경 변수 타입 선언
│
├── api/                    # 백엔드 API 호출 모듈
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
├── types/                  # TypeScript 타입/인터페이스
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
│   └── theme.ts
│
├── stores/                 # Pinia 스토어
│   └── authStore.ts
│
├── router/
│   └── index.ts            # Vue Router 설정
│
├── utils/                  # 공통 유틸리티
│   ├── auth.ts
│   ├── constants.ts
│   ├── errorHandler.ts
│   ├── http.ts             # HTTP 클라이언트 (axios 래퍼)
│   ├── oauth.ts
│   ├── search.ts
│   ├── toast.ts
│   └── (기타)
│
├── layout/                 # 레이아웃 컴포넌트
│   ├── AppLayout.vue
│   ├── AppTopbar.vue
│   └── AppFooter.vue
│
├── views/                  # 페이지 단위 뷰 (라우트와 1:1 대응)
│   ├── Dashboard.vue
│   └── pages/
│       ├── admin/          # 관리자
│       │   ├── Admin.vue
│       │   ├── AdminInquiryList.vue
│       │   ├── CommonCodeManagement.vue
│       │   ├── IngredientGroupManagement.vue
│       │   ├── IngredientGroupRegister.vue
│       │   ├── IngredientManagementRegister.vue
│       │   ├── IngredientRegister.vue
│       │   ├── IngredientRequestList.vue
│       │   └── (기타)
│       ├── auth/           # 인증
│       │   ├── Login.vue
│       │   ├── OAuthCallback.vue
│       │   ├── GoogleCallback.vue
│       │   ├── KakaoCallback.vue
│       │   └── NaverCallback.vue
│       ├── community/
│       │   └── FAQ.vue
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
│       ├── my/             # 마이페이지
│       │   ├── MyPage.vue
│       │   ├── Profile.vue
│       │   ├── Bookmarks.vue
│       │   ├── Comments.vue
│       │   ├── Favorites.vue
│       │   ├── Inquiries.vue
│       │   ├── InquiryDetail.vue
│       │   ├── Recipes.vue
│       │   ├── RecipeCreate.vue
│       │   ├── RecipeEdit.vue
│       │   └── (기타)
│       ├── ranking/
│       │   └── Ranking.vue
│       ├── recipe/
│       │   ├── Category.vue
│       │   ├── RecipeDetail.vue
│       │   └── SearchResult.vue
│       └── recommend/
│           └── TodayRecipe.vue
│
├── components/             # 재사용 컴포넌트 (도메인별)
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
├── data/                   # 정적/로컬 데이터
│   ├── faqData.ts
│   └── recipeCategoryData.ts
│
└── assets/                 # 스타일·이미지 등 리소스
    ├── styles.scss
    ├── tailwind.css
    ├── images/
    │   ├── badges/         # 앱스토어, SNS 배지 등
    │   ├── logo/
    │   └── social/         # Google, Kakao, Naver 아이콘
    └── layout/
        ├── layout.scss
        ├── _core.scss
        ├── _footer.scss
        ├── _main.scss
        ├── _mixins.scss
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

## 3. 패키지(폴더) 역할 요약

| 폴더 | 역할 |
|------|------|
| `api/` | 도메인별 REST API 호출 함수 (auth, recipe, member, theme 등) |
| `types/` | API·폼·화면에서 사용하는 공통 타입/인터페이스 |
| `stores/` | 전역 상태 (현재 인증 `authStore` 위주) |
| `router/` | 라우트 정의 및 가드 |
| `utils/` | HTTP 클라이언트, 인증/oauth, 토스트, 검색 등 공용 유틸 |
| `layout/` | 상단/하단/전체 레이아웃 컴포넌트 |
| `views/` | 라우트 단위 페이지 (admin, auth, my, recipe 등) |
| `components/` | 도메인별 재사용 컴포넌트 (bookmark, dashboard, recipe, ingredient 등) |
| `data/` | FAQ, 레시피 카테고리 등 정적 데이터 |
| `assets/` | SCSS, Tailwind, 이미지, 레이아웃 변수 |

---

## 4. 참고

- **빌드/실행**: `npm run dev`, `npm run build`
- **문서 갱신**: 디렉터리·파일 추가·이동 시 이 문서를 함께 수정할 것을 권장합니다.
