# UI 목록

**정의**: 독립된 화면(라우트 단위)으로 기능을 수행하는 화면 목록  
**기준**: `frontend/src/router/index.ts` 및 View 컴포넌트 분석 (2026-02-20)

---

## 1. 메인·탐색

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 1 | / | dashboard | Dashboard.vue | 메인 대시보드. 인기 레시피, 테마 컬렉션, 트렌딩 카테고리, 최근 본 레시피, 오늘의 추천, 추천 크리에이터 등 배치 |
| 2 | /recipe/search | searchResult | SearchResult.vue | 레시피 검색 결과 목록 |
| 3 | /recipe/category | category | Category.vue | 카테고리별 레시피 목록(전체 레시피) |
| 4 | /recipe/:id | recipeDetail | RecipeDetail.vue | 레시피 상세 조회(헤더·갤러리·재료·조리순서·댓글·북마크 등) |

---

## 2. 내 레시피

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 5 | /my/recipes | myRecipes | Recipes.vue | 내가 작성한 레시피 목록 |
| 6 | /my/recipes/new | myRecipeCreate | RecipeCreate.vue | 새 레시피 등록 폼 |
| 7 | /my/recipes/:id/edit | myRecipeEdit | RecipeEdit.vue | 기존 레시피 수정 폼 |

---

## 3. 마이페이지·프로필

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 8 | /my | mypage | MyPage.vue | 마이페이지. 탭: 내 정보 수정(Profile), 내 댓글(Comments), 1:1 문의(Inquiries), 찜 목록(Favorites), 북마크 관리(Bookmarks) |
| 9 | /my/inquiries/:id | inquiryDetail | InquiryDetail.vue | 1:1 문의 상세 조회·답변 확인 |

---

## 4. 관리자

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 10 | /admin | admin | Admin.vue | 관리자 메인(메뉴 진입) |
| 11 | /admin/ingredient-groups | adminIngredientGroupManagement | IngredientGroupManagement.vue | 재료 그룹 목록·관리 |
| 12 | /admin/ingredient-group/register | adminIngredientGroupRegister | IngredientGroupRegister.vue | 재료 그룹 등록 |
| 13 | /admin/ingredient/register | adminIngredientRegister | IngredientRegister.vue | 재료 등록(관리자) |
| 14 | /admin/ingredient-management/register | adminIngredientManagementRegister | IngredientManagementRegister.vue | 재료 관리용 등록(저장법·손질법 등) |
| 15 | /admin/ingredient-requests | adminIngredientRequestList | IngredientRequestList.vue | 재료 추가 요청 목록·상태 처리 |
| 16 | /admin/inquiries | adminInquiryList | AdminInquiryList.vue | 1:1 문의 목록·답변 처리 |
| 17 | /admin/common-codes | adminCommonCodeManagement | CommonCodeManagement.vue | 공통코드 관리 |

---

## 5. 재료(사용자)

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 18 | /ingredient/management | ingredientManagement | IngredientManagement.vue | 재료 관리 목록(그룹·재료 조회) |
| 19 | /ingredient/management/register | ingredientRegister | IngredientManagementRegister.vue | 재료 관리용 등록(사용자 경로) |
| 20 | /ingredient/management/:id | ingredientDetail | IngredientDetail.vue | 재료 상세(저장법·손질법 등) |

---

## 6. 랭킹·커뮤니티

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 21 | /ranking | ranking | Ranking.vue | 레시피 랭킹(기간별 탭) |
| 22 | /community/faq | faq | FAQ.vue | 자주 묻는 질문(FAQ) |

---

## 7. 회원·피드

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 23 | /member/:id | memberProfile | MemberProfile.vue | 다른 회원 프로필·레시피 목록·팔로우 |
| 24 | /feed/following | followingFeed | FollowingFeed.vue | 팔로잉 피드(팔로우한 회원 레시피) |

---

## 8. 인증

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 25 | /auth/login | login | Login.vue | 로그인(소셜·테스트 계정) |
| 26 | /auth/naver/callback | naverCallback | NaverCallback.vue | 네이버 OAuth 콜백 처리 |
| 27 | /auth/google/callback | googleCallback | GoogleCallback.vue | 구글 OAuth 콜백 처리 |
| 28 | /auth/kakao/callback | kakaoCallback | KakaoCallback.vue | 카카오 OAuth 콜백 처리 |

---

## 9. 에러

| No | 경로 | 라우트 name | View 컴포넌트 | 설명·기능 |
|----|------|-------------|---------------|-----------|
| 29 | /error/notfound | notfound | NotFound.vue | 404 페이지 없음 |
| 30 | /error/access | accessDenied | Access.vue | 접근 거부(403) |
| 31 | /error/error | error | Error.vue | 일반 오류 안내 |

---

## 비고

- `/my/profile`, `/my/comments`, `/my/inquiries`, `/my/favorites` 는 `/my?tab=...` 로 리다이렉트되며, 실제 콘텐츠는 MyPage.vue 의 탭(Profile, Comments, Inquiries, Favorites)으로 표시됨.
- `/ranking/weekly`, `/ranking/monthly` 는 `/ranking?period=...` 로 리다이렉트됨.
- AppLayout 하위 라우트(1~24)는 공통 레이아웃(헤더·네비게이션 등)을 사용함.
