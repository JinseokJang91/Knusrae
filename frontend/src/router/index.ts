import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        component: AppLayout,
        children: [
            {
                path: '/',
                name: 'dashboard',
                component: () => import('@/views/Dashboard.vue')
            },
            // 마이페이지 메뉴
            {
                path: '/mypage',
                name: 'mypage',
                component: () => import('@/views/pages/my/MyPage.vue')
            },
            {
                path: '/my/profile',
                redirect: '/mypage?tab=profile'
            },
            {
                path: '/my/comments',
                redirect: '/mypage?tab=comments'
            },
            {
                path: '/my/inquiries',
                redirect: '/mypage?tab=inquiries'
            },
            {
                path: '/my/favorites',
                redirect: '/mypage?tab=favorites'
            },
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
            // 메인 메뉴
            {
                path: '/recommend/today',
                name: 'todayRecipe',
                component: () => import('@/views/pages/recommend/TodayRecipe.vue')
            },
            {
                path: '/ranking',
                name: 'ranking',
                component: () => import('@/views/pages/ranking/Ranking.vue')
            },
            {
                path: '/recipe/category',
                name: 'category',
                component: () => import('@/views/pages/recipe/Category.vue')
            },
            {
                path: '/ingredient/management',
                name: 'ingredientManagement',
                component: () => import('@/views/pages/ingredient/Management.vue')
            },
            {
                path: '/faq',
                name: 'faq',
                component: () => import('@/views/pages/faq/FAQ.vue')
            },
            // 기존 경로 (하위 호환성 유지)
            {
                path: '/recommend/weekly',
                name: 'weeklyRanking',
                component: () => import('@/views/pages/recommend/WeeklyRanking.vue')
            },
            {
                path: '/recommend/monthly',
                name: 'monthlyRanking',
                component: () => import('@/views/pages/recommend/MonthlyRanking.vue')
            },
            // 레시피 메뉴
            {
                path: '/recipe/search',
                name: 'searchResult',
                component: () => import('@/views/pages/recipe/SearchResult.vue')
            },
            {
                path: '/recipe/:id',
                name: 'recipeDetail',
                component: () => import('@/views/pages/recipe/RecipeDetail.vue')
            },
            {
                path: '/recipe/storage',
                name: 'storage',
                component: () => import('@/views/pages/recipe/Storage.vue')
            },
            {
                path: '/recipe/preparation',
                name: 'preparation',
                component: () => import('@/views/pages/recipe/Preparation.vue')
            },
            // 소통 메뉴 (하위 호환성 유지)
            {
                path: '/community/qna',
                name: 'qna',
                component: () => import('@/views/pages/community/QnA.vue')
            },
            {
                path: '/pages/empty',
                name: 'empty',
                component: () => import('@/views/pages/Empty.vue')
            }
        ]
    },
    {
        path: '/landing',
        name: 'landing',
        component: () => import('@/views/pages/Landing.vue')
    },
    {
        path: '/pages/notfound',
        name: 'notfound',
        component: () => import('@/views/pages/NotFound.vue')
    },
    {
        path: '/auth/login',
        name: 'login',
        component: () => import('@/views/pages/auth/Login.vue')
    },
    {
        path: '/auth/access',
        name: 'accessDenied',
        component: () => import('@/views/pages/auth/Access.vue')
    },
    {
        path: '/auth/error',
        name: 'error',
        component: () => import('@/views/pages/auth/Error.vue')
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
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
