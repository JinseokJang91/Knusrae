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
                path: '/my/profile',
                name: 'myProfile',
                component: () => import('@/views/pages/my/Profile.vue')
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
                path: '/my/comments',
                name: 'myComments',
                component: () => import('@/views/pages/my/Comments.vue')
            },
            {
                path: '/my/reviews',
                name: 'myReviews',
                component: () => import('@/views/pages/my/Reviews.vue')
            },
            {
                path: '/my/inquiries',
                name: 'myInquiries',
                component: () => import('@/views/pages/my/Inquiries.vue')
            },
            // 추천 메뉴
            {
                path: '/recommend/today',
                name: 'todayRecipe',
                component: () => import('@/views/pages/recommend/TodayRecipe.vue')
            },
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
                path: '/recipe/favorites',
                name: 'favorites',
                component: () => import('@/views/pages/recipe/Favorites.vue')
            },
            {
                path: '/recipe/category',
                name: 'category',
                component: () => import('@/views/pages/recipe/Category.vue')
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
            // 소통 메뉴
            {
                path: '/community/reviews',
                name: 'reviews',
                component: () => import('@/views/pages/community/Reviews.vue')
            },
            {
                path: '/community/board',
                name: 'board',
                component: () => import('@/views/pages/community/Board.vue')
            },
            {
                path: '/community/events',
                name: 'events',
                component: () => import('@/views/pages/community/Events.vue')
            },
            {
                path: '/community/qna',
                name: 'qna',
                component: () => import('@/views/pages/community/QnA.vue')
            },
            {
                path: '/uikit/formlayout',
                name: 'formlayout',
                component: () => import('@/views/uikit/FormLayout.vue')
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

// 로그인 필요 경로 가드: /my/** 경로는 비로그인 시 로그인 페이지로 리다이렉트
router.beforeEach((to, _from, next) => {
    try {
        const requiresAuth = to.path.startsWith('/my');
        // 동적 임포트 없이 로컬 스토리지로 간단 체크 (utils/auth를 직접 임포트하지 않음)
        const token = localStorage.getItem('accessToken');

        if (requiresAuth && !token) {
            next({
                path: '/auth/login',
                query: { redirect: to.fullPath }
            });
            return;
        }
    } catch (e) {
        // 문제가 있어도 네비게이션은 진행
    }
    next();
});

export default router;
