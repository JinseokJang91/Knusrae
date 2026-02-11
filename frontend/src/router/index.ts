import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: AppLayout,
        children: [
            // 1. 메인 화면
            // 1-1. 대시보드
            {
                path: '/',
                name: 'dashboard',
                component: () => import('@/views/Dashboard.vue')
            },
            // 1-2. 오늘의 레시피 추천
            {
                path: '/recommend/today',
                name: 'todayRecipe',
                component: () => import('@/views/pages/recommend/TodayRecipe.vue')
            },
            // 1-3. 검색 결과
            {
                path: '/recipe/search',
                name: 'searchResult',
                component: () => import('@/views/pages/recipe/SearchResult.vue')
            },
            // 2. 내 레시피
            {
                path: '/my/recipes',
                name: 'myRecipes',
                component: () => import('@/views/pages/my/Recipes.vue')
            },
            {
                path: '/my/recipes/new',
                name: 'myRecipeCreate',
                component: () => import('@/views/pages/my/RecipeCreate.vue')
            },
            {
                path: '/my/recipes/:id/edit',
                name: 'myRecipeEdit',
                component: () => import('@/views/pages/my/RecipeEdit.vue')
            },
            // 3. 프로필 / 마이페이지 (정책: 나의 것은 모두 /my 아래)
            {
                path: '/my',
                name: 'mypage',
                component: () => import('@/views/pages/my/MyPage.vue')
            },
            {
                path: '/mypage',
                redirect: (to) => ({ path: '/my', query: to.query })
            },
            {
                path: '/my/profile',
                redirect: '/my?tab=profile'
            },
            {
                path: '/my/comments',
                redirect: '/my?tab=comments'
            },
            {
                path: '/my/inquiries/:id',
                name: 'inquiryDetail',
                component: () => import('@/views/pages/my/InquiryDetail.vue')
            },
            {
                path: '/my/inquiries',
                redirect: '/my?tab=inquiries'
            },
            {
                path: '/my/favorites',
                redirect: '/my?tab=favorites'
            },
            // 3-1. 관리자 페이지
            {
                path: '/admin',
                name: 'admin',
                component: () => import('@/views/pages/admin/Admin.vue')
            },
            {
                path: '/admin/ingredient-groups',
                name: 'adminIngredientGroupManagement',
                component: () => import('@/views/pages/admin/IngredientGroupManagement.vue')
            },
            {
                path: '/admin/ingredient-group/register',
                name: 'adminIngredientGroupRegister',
                component: () => import('@/views/pages/admin/IngredientGroupRegister.vue')
            },
            {
                path: '/admin/ingredient/register',
                name: 'adminIngredientRegister',
                component: () => import('@/views/pages/admin/IngredientRegister.vue')
            },
            {
                path: '/admin/ingredient-management/register',
                name: 'adminIngredientManagementRegister',
                component: () => import('@/views/pages/admin/IngredientManagementRegister.vue')
            },
            {
                path: '/admin/ingredient-requests',
                name: 'adminIngredientRequestList',
                component: () => import('@/views/pages/admin/IngredientRequestList.vue')
            },
            {
                path: '/admin/inquiries',
                name: 'adminInquiryList',
                component: () => import('@/views/pages/admin/AdminInquiryList.vue')
            },
            {
                path: '/admin/common-codes',
                name: 'adminCommonCodeManagement',
                component: () => import('@/views/pages/admin/CommonCodeManagement.vue')
            },
            // 4. 메인 메뉴
            // 4-1. 전체 레시피
            {
                path: '/recipe/category',
                name: 'category',
                component: () => import('@/views/pages/recipe/Category.vue')
            },
            {
                path: '/recipe/:id',
                name: 'recipeDetail',
                component: () => import('@/views/pages/recipe/RecipeDetail.vue')
            },
            // 4-2. 재료 관리
            {
                path: '/ingredient/management',
                name: 'ingredientManagement',
                component: () => import('@/views/pages/ingredient/IngredientManagement.vue')
            },
            {
                path: '/ingredient/management/register',
                name: 'ingredientRegister',
                component: () => import('@/views/pages/admin/IngredientManagementRegister.vue')
            },
            {
                path: '/ingredient/management/:id',
                name: 'ingredientDetail',
                component: () => import('@/views/pages/ingredient/IngredientDetail.vue')
            },
            // 4-3. 랭킹 (단일 페이지 + 탭, 기간별 경로는 /ranking으로 리다이렉트)
            {
                path: '/ranking',
                name: 'ranking',
                component: () => import('@/views/pages/ranking/Ranking.vue')
            },
            {
                path: '/ranking/weekly',
                name: 'weeklyRanking',
                redirect: { path: '/ranking', query: { period: '7d' } }
            },
            {
                path: '/ranking/monthly',
                name: 'monthlyRanking',
                redirect: { path: '/ranking', query: { period: '30d' } }
            },
            // 4-4. FAQ
            {
                path: '/community/faq',
                name: 'faq',
                component: () => import('@/views/pages/community/FAQ.vue')
            },
        ]
    },
    {
        path: '/auth/login',
        name: 'login',
        component: () => import('@/views/pages/auth/Login.vue')
    },
    {
        path: '/auth/naver/callback',
        name: 'naverCallback',
        component: () => import('@/views/pages/auth/NaverCallback.vue')
    },
    {
        path: '/auth/google/callback',
        name: 'googleCallback',
        component: () => import('@/views/pages/auth/GoogleCallback.vue')
    },
    {
        path: '/auth/kakao/callback',
        name: 'kakaoCallback',
        component: () => import('@/views/pages/auth/KakaoCallback.vue')
    },
    {
        path: '/error/notfound',
        name: 'notfound',
        component: () => import('@/views/pages/error/NotFound.vue')
    },
    {
        path: '/error/access',
        name: 'accessDenied',
        component: () => import('@/views/pages/error/Access.vue')
    },
    {
        path: '/error/error',
        name: 'error',
        component: () => import('@/views/pages/error/Error.vue')
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
