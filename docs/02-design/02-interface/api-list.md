# API 목록

**작성 기준**: 백엔드 Controller 코드 분석 (2026-04-17)

본 애플리케이션에 구현된 REST API Endpoint를 정리한 목록입니다.

---

## 1. API 목록 테이블

| No | 서비스 | HTTP Method | 요청 경로 | Controller | 비고 |
|----|--------|-------------|-----------|------------|------|
| 1 | auth | GET | /api/auth/oauth/state | AuthController | OAuth state 발급 |
| 2 | auth | GET | /api/auth/naver/callback | AuthController | 네이버 로그인 콜백 |
| 3 | auth | GET | /api/auth/google/callback | AuthController | 구글 로그인 콜백 |
| 4 | auth | GET | /api/auth/kakao/callback | AuthController | 카카오 로그인 콜백 |
| 5 | auth | POST | /api/auth/logout | AuthController | 로그아웃 |
| 6 | auth | POST | /api/auth/refresh | AuthController | 토큰 갱신 |
| 7 | auth | GET | /api/auth/jwt/token | AuthController | JWT 토큰 조회 |
| 8 | auth | POST | /api/auth/test/login | AuthController | 테스트 로그인 |
| 9 | auth | GET | /api/auth/test/accounts | AuthController | 테스트 계정 목록 |
| 10 | common | GET | /api/cook/common-codes | CommonCodeController | 공통코드 목록 조회 |
| 11 | common | GET | /api/cook/admin/common-codes | AdminCommonCodeController | 공통코드 목록(관리자) |
| 12 | common | GET | /api/cook/admin/common-codes/{codeId} | AdminCommonCodeController | 공통코드 상세(관리자) |
| 13 | common | POST | /api/cook/admin/common-codes | AdminCommonCodeController | 공통코드 등록 |
| 14 | common | PUT | /api/cook/admin/common-codes/{codeId} | AdminCommonCodeController | 공통코드 수정 |
| 15 | common | DELETE | /api/cook/admin/common-codes/{codeId} | AdminCommonCodeController | 공통코드 삭제 |
| 16 | common | POST | /api/cook/admin/common-codes/{codeId}/details | AdminCommonCodeController | 상세코드 등록 |
| 17 | common | PUT | /api/cook/admin/common-codes/{codeId}/details/{detailCodeId} | AdminCommonCodeController | 상세코드 수정 |
| 18 | common | DELETE | /api/cook/admin/common-codes/{codeId}/details/{detailCodeId} | AdminCommonCodeController | 상세코드 삭제 |
| 19 | member | GET | /api/member/me | MemberController | 내 정보 조회 |
| 20 | member | GET | /api/member/{memberId} | MemberController | 회원 정보 조회 |
| 21 | member | PUT | /api/member/profile | MemberController | 프로필 수정(멀티파트) |
| 22 | member | POST | /api/member/follows/{memberId} | FollowController | 팔로우 |
| 23 | member | DELETE | /api/member/follows/{memberId} | FollowController | 언팔로우 |
| 24 | member | GET | /api/member/follows/{memberId}/check | FollowController | 팔로우 여부 확인 |
| 25 | member | GET | /api/member/{memberId}/followers | FollowController | 팔로워 목록 |
| 26 | member | GET | /api/member/{memberId}/followings | FollowController | 팔로잉 목록 |
| 27 | member | POST | /api/member/inquiries | InquiryController | 문의 등록(멀티파트) |
| 28 | member | GET | /api/member/inquiries/my | InquiryController | 내 문의 목록 |
| 29 | member | GET | /api/member/inquiries/{id} | InquiryController | 문의 상세 |
| 30 | member | PUT | /api/member/inquiries/{id} | InquiryController | 문의 수정(멀티파트) |
| 31 | member | DELETE | /api/member/inquiries/{id} | InquiryController | 문의 삭제 |
| 32 | member | GET | /api/member/inquiries/admin | InquiryController | 문의 목록(관리자) |
| 33 | member | GET | /api/member/inquiries/admin/{id} | InquiryController | 문의 상세(관리자) |
| 34 | member | POST | /api/member/inquiries/{id}/reply | InquiryController | 문의 답변 등록 |
| 35 | cook | POST | /api/cook/recipe | RecipeController | 레시피 등록(멀티파트) |
| 36 | cook | GET | /api/cook/recipe/list/member/{memberId} | RecipeController | 회원별 레시피 목록 |
| 37 | cook | GET | /api/cook/recipe/list/all | RecipeController | 레시피 전체 목록 |
| 38 | cook | GET | /api/cook/recipe/{id} | RecipeController | 레시피 상세 |
| 39 | cook | PUT | /api/cook/recipe/{id} | RecipeController | 레시피 수정(멀티파트) |
| 40 | cook | DELETE | /api/cook/recipe/{id} | RecipeController | 레시피 삭제 |
| 41 | cook | GET | /api/cook/recipe/popular | RecipeController | 인기 레시피 목록 |
| 42 | cook | GET | /api/cook/recipe/following-feed | RecipeController | 팔로잉 피드 |
| 43 | cook | POST | /api/cook/recipe/comments/{recipeId} | RecipeCommentController | 댓글 등록 |
| 44 | cook | POST | /api/cook/recipe/comments/{recipeId}/with-image | RecipeCommentController | 댓글 등록(이미지) |
| 45 | cook | GET | /api/cook/recipe/comments/member/{memberId} | RecipeCommentController | 회원별 댓글 목록 |
| 46 | cook | GET | /api/cook/recipe/comments/{recipeId} | RecipeCommentController | 레시피 댓글 목록 |
| 47 | cook | GET | /api/cook/recipe/comments/{recipeId}/page | RecipeCommentController | 레시피 댓글 페이징 |
| 48 | cook | PUT | /api/cook/recipe/comments/{commentId} | RecipeCommentController | 댓글 수정 |
| 49 | cook | PUT | /api/cook/recipe/comments/{commentId}/with-image | RecipeCommentController | 댓글 수정(이미지) |
| 50 | cook | DELETE | /api/cook/recipe/comments/{commentId} | RecipeCommentController | 댓글 삭제 |
| 51 | cook | GET | /api/cook/recipe/comments/{recipeId}/count | RecipeCommentController | 댓글 개수 |
| 52 | cook | GET | /api/cook/recipe/bookmarks/recipe-books/{recipeBookId}/bookmarks | RecipeBookmarkController | 레시피북별 북마크 조회 |
| 53 | cook | POST | /api/cook/recipe/bookmarks | RecipeBookmarkController | 북마크 추가 |
| 54 | cook | DELETE | /api/cook/recipe/bookmarks | RecipeBookmarkController | 북마크 제거(Query: recipeBookId, recipeId) |
| 55 | cook | GET | /api/cook/recipe/bookmarks/check/{recipeId} | RecipeBookmarkController | 북마크 여부 확인 |
| 56 | cook | PUT | /api/cook/recipe/bookmarks/{bookmarkId}/move | RecipeBookmarkController | 북마크 이동 |
| 57 | cook | PUT | /api/cook/recipe/bookmarks/{bookmarkId}/memo | RecipeBookmarkController | 북마크 메모 수정 |
| 58 | cook | GET | /api/cook/recipe/bookmarks/recipe-books | RecipeBookController | 레시피북 목록 |
| 59 | cook | POST | /api/cook/recipe/bookmarks/recipe-books | RecipeBookController | 레시피북 생성 |
| 60 | cook | PUT | /api/cook/recipe/bookmarks/recipe-books/{recipeBookId} | RecipeBookController | 레시피북 수정 |
| 61 | cook | DELETE | /api/cook/recipe/bookmarks/recipe-books/{recipeBookId} | RecipeBookController | 레시피북 삭제 |
| 62 | cook | PUT | /api/cook/recipe/bookmarks/recipe-books/reorder | RecipeBookController | 레시피북 순서 변경 |
| 63 | cook | POST | /api/cook/recipe/bookmarks/recipe-books/default | RecipeBookController | 기본 레시피북 생성 |
| 64 | cook | POST | /api/cook/recipes/{recipeId}/view | RecipeViewController | 레시피 조회 기록 등록 |
| 65 | cook | GET | /api/cook/members/{memberId}/recent-views | RecipeViewController | 최근 조회 목록 |
| 66 | cook | DELETE | /api/cook/members/{memberId}/recent-views | RecipeViewController | 최근 조회 삭제 |
| 67 | cook | GET | /api/cook/recipe/favorites/{memberId} | RecipeFavoriteController | 회원별 즐겨찾기 목록 |
| 68 | cook | POST | /api/cook/recipe/favorites | RecipeFavoriteController | 즐겨찾기 추가 |
| 69 | cook | DELETE | /api/cook/recipe/favorites | RecipeFavoriteController | 즐겨찾기 제거 |
| 70 | cook | PUT | /api/cook/recipe/favorites/toggle | RecipeFavoriteController | 즐겨찾기 토글 |
| 71 | cook | GET | /api/cook/recipe/favorites/check | RecipeFavoriteController | 즐겨찾기 여부 확인 |
| 72 | cook | GET | /api/cook/recipe/favorites/count/{recipeId} | RecipeFavoriteController | 레시피별 즐겨찾기 수 |
| 73 | cook | GET | /api/cook/categories/trending | CategoryController | 트렌딩 카테고리 |
| 74 | cook | GET | /api/cook/categories/{codeId}/{detailCodeId}/recipes | CategoryController | 카테고리별 레시피 |
| 75 | cook | GET | /api/cook/themes/active | ThemeCollectionController | 활성 테마 목록 |
| 76 | cook | GET | /api/cook/themes/{themeId}/recipes | ThemeCollectionController | 테마별 레시피 |
| 77 | cook | POST | /api/cook/admin/themes | AdminThemeCollectionController | 테마 등록 |
| 78 | cook | POST | /api/cook/admin/themes/{themeId}/recipes | AdminThemeCollectionController | 테마에 레시피 추가 |
| 79 | cook | DELETE | /api/cook/admin/themes/{themeId}/recipes/{recipeId} | AdminThemeCollectionController | 테마에서 레시피 제거 |
| 80 | cook | GET | /api/cook/creators/recommended | CreatorController | 추천 크리에이터 |
| 81 | cook | GET | /api/cook/search/recipes | SearchController | 레시피 검색 |
| 82 | cook | GET | /api/cook/recipes/recommendations/today | RecommendationController | 오늘의 추천 레시피 |
| 83 | cook | GET | /api/cook/ingredients/groups | IngredientController | 재료 그룹 목록 |
| 84 | cook | GET | /api/cook/ingredients | IngredientController | 재료 목록 |
| 85 | cook | GET | /api/cook/ingredients/{ingredientId}/storage | IngredientController | 보관법 목록 |
| 86 | cook | GET | /api/cook/ingredients/{ingredientId}/preparation | IngredientController | 손질법 목록 |
| 87 | cook | POST | /api/cook/ingredients/requests | IngredientRequestController | 재료 요청 등록 |
| 88 | cook | GET | /api/cook/ingredients/requests/my | IngredientRequestController | 내 재료 요청 목록 |
| 89 | cook | GET | /api/cook/ingredients/requests/admin | IngredientRequestController | 재료 요청 목록(관리자) |
| 90 | cook | PUT | /api/cook/ingredients/requests/{id}/status | IngredientRequestController | 재료 요청 상태 변경 |
| 91 | cook | POST | /api/cook/admin/ingredients/groups | AdminIngredientController | 재료 그룹 등록 |
| 92 | cook | POST | /api/cook/admin/ingredients | AdminIngredientController | 재료 등록 |
| 93 | cook | PUT | /api/cook/admin/ingredients/groups/{id} | AdminIngredientController | 재료 그룹 수정 |
| 94 | cook | DELETE | /api/cook/admin/ingredients/groups/{id} | AdminIngredientController | 재료 그룹 삭제 |
| 95 | cook | PUT | /api/cook/admin/ingredients/{id} | AdminIngredientController | 재료 수정 |
| 96 | cook | DELETE | /api/cook/admin/ingredients/{id} | AdminIngredientController | 재료 삭제 |
| 97 | cook | POST | /api/cook/admin/ingredients/storage | AdminIngredientController | 보관법 등록 |
| 98 | cook | POST | /api/cook/admin/ingredients/preparation | AdminIngredientController | 손질법 등록 |
| 99 | cook | PUT | /api/cook/admin/ingredients/storage/{id} | AdminIngredientController | 보관법 수정 |
| 100 | cook | PUT | /api/cook/admin/ingredients/preparation/{id} | AdminIngredientController | 손질법 수정 |
| 101 | cook | POST | /api/cook/admin/ingredients/upload-image | AdminIngredientController | 재료 이미지 업로드 |
| 102 | cook | DELETE | /api/cook/admin/ingredients/storage/{id} | AdminIngredientController | 보관법 삭제 |
| 103 | cook | DELETE | /api/cook/admin/ingredients/preparation/{id} | AdminIngredientController | 손질법 삭제 |

---

## 2. 서비스별 요약

| 서비스 | 엔드포인트 수 | 설명 |
|--------|----------------|------|
| auth | 9 | 인증(OAuth state, 소셜 로그인, 로그아웃, 토큰 갱신, 테스트) |
| common | 9 | 공통코드 조회/관리 |
| member | 16 | 회원, 팔로우, 문의 |
| cook | 69 | 레시피, 댓글, 북마크, 조회기록, 즐겨찾기, 카테고리, 테마, 크리에이터, 검색, 추천, 재료, 관리자 재료 |

**총 103개 엔드포인트**

---

## 3. 참고

- 프로그램·화면 매핑: [05-program-list/프로그램목록.md](../05-program-list/프로그램목록.md)
