# API 목록

**작성 기준**: 백엔드 Controller 코드 분석 (2026-02-20)

본 애플리케이션에 구현된 REST API Endpoint를 정리한 목록입니다.

---

## 1. API 목록 테이블

| No | 서비스 | HTTP Method | 요청 경로 | Controller | 비고 |
|----|--------|-------------|-----------|------------|------|
| 1 | auth | GET | /api/auth/naver/callback | AuthController | 네이버 로그인 콜백 |
| 2 | auth | GET | /api/auth/google/callback | AuthController | 구글 로그인 콜백 |
| 3 | auth | GET | /api/auth/kakao/callback | AuthController | 카카오 로그인 콜백 |
| 4 | auth | POST | /api/auth/logout | AuthController | 로그아웃 |
| 5 | auth | POST | /api/auth/refresh | AuthController | 토큰 갱신 |
| 6 | auth | GET | /api/auth/jwt/token | AuthController | JWT 토큰 조회 |
| 7 | auth | POST | /api/auth/test/login | AuthController | 테스트 로그인 |
| 8 | auth | GET | /api/auth/test/accounts | AuthController | 테스트 계정 목록 |
| 9 | common | GET | /api/common-codes | CommonCodeController | 공통코드 목록 조회 |
| 10 | common | GET | /api/admin/common-codes | AdminCommonCodeController | 공통코드 목록(관리자) |
| 11 | common | GET | /api/admin/common-codes/{codeId} | AdminCommonCodeController | 공통코드 상세(관리자) |
| 12 | common | POST | /api/admin/common-codes | AdminCommonCodeController | 공통코드 등록 |
| 13 | common | PUT | /api/admin/common-codes/{codeId} | AdminCommonCodeController | 공통코드 수정 |
| 14 | common | DELETE | /api/admin/common-codes/{codeId} | AdminCommonCodeController | 공통코드 삭제 |
| 15 | common | POST | /api/admin/common-codes/{codeId}/details | AdminCommonCodeController | 상세코드 등록 |
| 16 | common | PUT | /api/admin/common-codes/{codeId}/details/{detailCodeId} | AdminCommonCodeController | 상세코드 수정 |
| 17 | common | DELETE | /api/admin/common-codes/{codeId}/details/{detailCodeId} | AdminCommonCodeController | 상세코드 삭제 |
| 18 | member | GET | /api/member/me | MemberController | 내 정보 조회 |
| 19 | member | GET | /api/member/{memberId} | MemberController | 회원 정보 조회 |
| 20 | member | PUT | /api/member/profile | MemberController | 프로필 수정(멀티파트) |
| 21 | member | POST | /api/follows/{memberId} | FollowController | 팔로우 |
| 22 | member | DELETE | /api/follows/{memberId} | FollowController | 언팔로우 |
| 23 | member | GET | /api/follows/{memberId}/check | FollowController | 팔로우 여부 확인 |
| 24 | member | GET | /api/members/{memberId}/followers | FollowController | 팔로워 목록 |
| 25 | member | GET | /api/members/{memberId}/followings | FollowController | 팔로잉 목록 |
| 26 | member | POST | /api/inquiries | InquiryController | 문의 등록(멀티파트) |
| 27 | member | GET | /api/inquiries/my | InquiryController | 내 문의 목록 |
| 28 | member | GET | /api/inquiries/{id} | InquiryController | 문의 상세 |
| 29 | member | PUT | /api/inquiries/{id} | InquiryController | 문의 수정(멀티파트) |
| 30 | member | DELETE | /api/inquiries/{id} | InquiryController | 문의 삭제 |
| 31 | member | GET | /api/inquiries/admin | InquiryController | 문의 목록(관리자) |
| 32 | member | GET | /api/inquiries/admin/{id} | InquiryController | 문의 상세(관리자) |
| 33 | member | POST | /api/inquiries/{id}/reply | InquiryController | 문의 답변 등록 |
| 34 | cook | POST | /api/recipe | RecipeController | 레시피 등록(멀티파트) |
| 35 | cook | GET | /api/recipe/list/member/{memberId} | RecipeController | 회원별 레시피 목록 |
| 36 | cook | GET | /api/recipe/list/all | RecipeController | 레시피 전체 목록 |
| 37 | cook | GET | /api/recipe/{id} | RecipeController | 레시피 상세 |
| 38 | cook | PUT | /api/recipe/{id} | RecipeController | 레시피 수정(멀티파트) |
| 39 | cook | DELETE | /api/recipe/{id} | RecipeController | 레시피 삭제 |
| 40 | cook | GET | /api/recipe/popular | RecipeController | 인기 레시피 목록 |
| 41 | cook | GET | /api/recipe/following-feed | RecipeController | 팔로잉 피드 |
| 42 | cook | POST | /api/recipe/comments/{recipeId} | RecipeCommentController | 댓글 등록 |
| 43 | cook | POST | /api/recipe/comments/{recipeId}/with-image | RecipeCommentController | 댓글 등록(이미지) |
| 44 | cook | GET | /api/recipe/comments/member/{memberId} | RecipeCommentController | 회원별 댓글 목록 |
| 45 | cook | GET | /api/recipe/comments/{recipeId} | RecipeCommentController | 레시피 댓글 목록 |
| 46 | cook | GET | /api/recipe/comments/{recipeId}/page | RecipeCommentController | 레시피 댓글 페이징 |
| 47 | cook | PUT | /api/recipe/comments/{commentId} | RecipeCommentController | 댓글 수정 |
| 48 | cook | PUT | /api/recipe/comments/{commentId}/with-image | RecipeCommentController | 댓글 수정(이미지) |
| 49 | cook | DELETE | /api/recipe/comments/{commentId} | RecipeCommentController | 댓글 삭제 |
| 50 | cook | GET | /api/recipe/comments/{recipeId}/count | RecipeCommentController | 댓글 개수 |
| 51 | cook | GET | /api/recipe/bookmarks/recipe-books/{recipeBookId}/bookmarks | RecipeBookmarkController | 레시피북별 북마크 조회 |
| 52 | cook | POST | /api/recipe/bookmarks | RecipeBookmarkController | 북마크 추가 |
| 53 | cook | DELETE | /api/recipe/bookmarks | RecipeBookmarkController | 북마크 제거(Query: recipeBookId, recipeId) |
| 54 | cook | GET | /api/recipe/bookmarks/check/{recipeId} | RecipeBookmarkController | 북마크 여부 확인 |
| 55 | cook | PUT | /api/recipe/bookmarks/{bookmarkId}/move | RecipeBookmarkController | 북마크 이동 |
| 56 | cook | PUT | /api/recipe/bookmarks/{bookmarkId}/memo | RecipeBookmarkController | 북마크 메모 수정 |
| 57 | cook | GET | /api/recipe/bookmarks/recipe-books | RecipeBookController | 레시피북 목록 |
| 58 | cook | POST | /api/recipe/bookmarks/recipe-books | RecipeBookController | 레시피북 생성 |
| 59 | cook | PUT | /api/recipe/bookmarks/recipe-books/{recipeBookId} | RecipeBookController | 레시피북 수정 |
| 60 | cook | DELETE | /api/recipe/bookmarks/recipe-books/{recipeBookId} | RecipeBookController | 레시피북 삭제 |
| 61 | cook | PUT | /api/recipe/bookmarks/recipe-books/reorder | RecipeBookController | 레시피북 순서 변경 |
| 62 | cook | POST | /api/recipe/bookmarks/recipe-books/default | RecipeBookController | 기본 레시피북 생성 |
| 63 | cook | POST | /api/recipes/{recipeId}/view | RecipeViewController | 레시피 조회 기록 등록 |
| 64 | cook | GET | /api/members/{memberId}/recent-views | RecipeViewController | 최근 조회 목록 |
| 65 | cook | DELETE | /api/members/{memberId}/recent-views | RecipeViewController | 최근 조회 삭제 |
| 66 | cook | GET | /api/recipe/favorites/{memberId} | RecipeFavoriteController | 회원별 즐겨찾기 목록 |
| 67 | cook | POST | /api/recipe/favorites | RecipeFavoriteController | 즐겨찾기 추가 |
| 68 | cook | DELETE | /api/recipe/favorites | RecipeFavoriteController | 즐겨찾기 제거 |
| 69 | cook | PUT | /api/recipe/favorites/toggle | RecipeFavoriteController | 즐겨찾기 토글 |
| 70 | cook | GET | /api/recipe/favorites/check | RecipeFavoriteController | 즐겨찾기 여부 확인 |
| 71 | cook | GET | /api/recipe/favorites/count/{recipeId} | RecipeFavoriteController | 레시피별 즐겨찾기 수 |
| 72 | cook | GET | /api/categories/trending | CategoryController | 트렌딩 카테고리 |
| 73 | cook | GET | /api/categories/{codeId}/{detailCodeId}/recipes | CategoryController | 카테고리별 레시피 |
| 74 | cook | GET | /api/themes/active | ThemeCollectionController | 활성 테마 목록 |
| 75 | cook | GET | /api/themes/{themeId}/recipes | ThemeCollectionController | 테마별 레시피 |
| 76 | cook | POST | /api/admin/themes | AdminThemeCollectionController | 테마 등록 |
| 77 | cook | POST | /api/admin/themes/{themeId}/recipes | AdminThemeCollectionController | 테마에 레시피 추가 |
| 78 | cook | DELETE | /api/admin/themes/{themeId}/recipes/{recipeId} | AdminThemeCollectionController | 테마에서 레시피 제거 |
| 79 | cook | GET | /api/creators/recommended | CreatorController | 추천 크리에이터 |
| 80 | cook | GET | /api/search/recipes | SearchController | 레시피 검색 |
| 81 | cook | GET | /api/recipes/recommendations/today | RecommendationController | 오늘의 추천 레시피 |
| 82 | cook | GET | /api/ingredients/groups | IngredientController | 재료 그룹 목록 |
| 83 | cook | GET | /api/ingredients | IngredientController | 재료 목록 |
| 84 | cook | GET | /api/ingredients/{ingredientId}/storage | IngredientController | 보관법 목록 |
| 85 | cook | GET | /api/ingredients/{ingredientId}/preparation | IngredientController | 손질법 목록 |
| 86 | cook | POST | /api/ingredients/requests | IngredientRequestController | 재료 요청 등록 |
| 87 | cook | GET | /api/ingredients/requests/my | IngredientRequestController | 내 재료 요청 목록 |
| 88 | cook | GET | /api/ingredients/requests/admin | IngredientRequestController | 재료 요청 목록(관리자) |
| 89 | cook | PUT | /api/ingredients/requests/{id}/status | IngredientRequestController | 재료 요청 상태 변경 |
| 90 | cook | POST | /api/admin/ingredients/groups | AdminIngredientController | 재료 그룹 등록 |
| 91 | cook | POST | /api/admin/ingredients | AdminIngredientController | 재료 등록 |
| 92 | cook | PUT | /api/admin/ingredients/groups/{id} | AdminIngredientController | 재료 그룹 수정 |
| 93 | cook | DELETE | /api/admin/ingredients/groups/{id} | AdminIngredientController | 재료 그룹 삭제 |
| 94 | cook | PUT | /api/admin/ingredients/{id} | AdminIngredientController | 재료 수정 |
| 95 | cook | DELETE | /api/admin/ingredients/{id} | AdminIngredientController | 재료 삭제 |
| 96 | cook | POST | /api/admin/ingredients/storage | AdminIngredientController | 보관법 등록 |
| 97 | cook | POST | /api/admin/ingredients/preparation | AdminIngredientController | 손질법 등록 |
| 98 | cook | PUT | /api/admin/ingredients/storage/{id} | AdminIngredientController | 보관법 수정 |
| 99 | cook | PUT | /api/admin/ingredients/preparation/{id} | AdminIngredientController | 손질법 수정 |
| 100 | cook | POST | /api/admin/ingredients/upload-image | AdminIngredientController | 재료 이미지 업로드 |
| 101 | cook | DELETE | /api/admin/ingredients/storage/{id} | AdminIngredientController | 보관법 삭제 |
| 102 | cook | DELETE | /api/admin/ingredients/preparation/{id} | AdminIngredientController | 손질법 삭제 |

---

## 2. 서비스별 요약

| 서비스 | 엔드포인트 수 | 설명 |
|--------|----------------|------|
| auth | 8 | 인증(소셜 로그인, 로그아웃, 토큰 갱신, 테스트) |
| common | 9 | 공통코드 조회/관리 |
| member | 16 | 회원, 팔로우, 문의 |
| cook | 69 | 레시피, 댓글, 북마크, 조회기록, 즐겨찾기, 카테고리, 테마, 크리에이터, 검색, 추천, 재료, 관리자 재료 |

**총 102개 엔드포인트**

---

## 3. 참고

- 상세 요청/응답 스키마: [api-specification.md](./api-specification.md)
- 프로그램·화면 매핑: [05-program-list/프로그램목록.md](../05-program-list/프로그램목록.md)
