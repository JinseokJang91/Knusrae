import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

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
            // 1-2. 검색 결과
            {
                path: '/recipe/search',
                name: 'searchResult',
                component: () => import('@/views/pages/recipe/SearchResult.vue')
            },
            // 2. 내 레시피 (로그인 필요) — 목록은 마이페이지 탭에서 표시
            {
                path: '/my/recipes',
                name: 'myRecipes',
                redirect: '/my?tab=recipes',
                meta: { requiresAuth: true }
            },
            {
                path: '/my/recipes/new',
                name: 'myRecipeCreate',
                component: () => import('@/views/pages/my/RecipeCreate.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: '/my/recipes/:id/edit',
                name: 'myRecipeEdit',
                component: () => import('@/views/pages/my/RecipeEdit.vue'),
                meta: { requiresAuth: true }
            },
            // 3. 프로필 / 마이페이지 (정책: 나의 것은 모두 /my 아래, 로그인 필요)
            {
                path: '/my',
                name: 'mypage',
                component: () => import('@/views/pages/my/MyPage.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: '/my/profile',
                redirect: '/my?tab=profile',
                meta: { requiresAuth: true }
            },
            {
                path: '/my/comments',
                redirect: '/my?tab=comments',
                meta: { requiresAuth: true }
            },
            {
                path: '/my/inquiries/:id',
                redirect: () => ({ path: '/my', query: { tab: 'inquiries' } }),
                meta: { requiresAuth: true }
            },
            {
                path: '/my/inquiries',
                redirect: '/my?tab=inquiries',
                meta: { requiresAuth: true }
            },
            {
                path: '/my/favorites',
                redirect: '/my?tab=favorites',
                meta: { requiresAuth: true }
            },
            // 3-1. 관리자 페이지 (관리자 전용)
            {
                path: '/admin',
                name: 'admin',
                component: () => import('@/views/pages/admin/Admin.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/ingredient-groups',
                name: 'adminIngredientGroupManagement',
                component: () => import('@/views/pages/admin/IngredientGroupManagement.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/ingredient-group/register',
                name: 'adminIngredientGroupRegister',
                component: () => import('@/views/pages/admin/IngredientGroupRegister.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/ingredient/register',
                name: 'adminIngredientRegister',
                component: () => import('@/views/pages/admin/IngredientRegister.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/ingredient-management/register',
                name: 'adminIngredientManagementRegister',
                component: () => import('@/views/pages/admin/IngredientManagementRegister.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/ingredient-requests',
                name: 'adminIngredientRequestList',
                component: () => import('@/views/pages/admin/IngredientRequestList.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/inquiries',
                name: 'adminInquiryList',
                component: () => import('@/views/pages/admin/AdminInquiryList.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: '/admin/common-codes',
                name: 'adminCommonCodeManagement',
                component: () => import('@/views/pages/admin/CommonCodeManagement.vue'),
                meta: { requiresAdmin: true }
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
                component: () => import('@/views/pages/admin/IngredientManagementRegister.vue'),
                meta: { requiresAdmin: true }
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
            // 4-4. 고객지원 (FAQ 포함)
            {
                path: '/support',
                name: 'customerSupport',
                component: () => import('@/views/pages/community/CustomerSupport.vue')
            },
            {
                path: '/community/faq',
                redirect: '/support'
            },
            // 5. 팔로우 관련
            // 5-1. 다른 회원 프로필
            {
                path: '/member/:id',
                name: 'memberProfile',
                component: () => import('@/views/pages/member/MemberProfile.vue')
            },
            // 5-2. 팔로잉 피드 (로그인 필요)
            {
                path: '/feed/following',
                name: 'followingFeed',
                component: () => import('@/views/pages/feed/FollowingFeed.vue'),
                meta: { requiresAuth: true }
            }
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
    // 알 수 없는 경로 → 404 (반드시 마지막에 등록)
    {
        path: '/:pathMatch(.*)*',
        name: 'notFound',
        component: () => import('@/views/pages/error/NotFound.vue')
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach(async (to) => {
    const authStore = useAuthStore();
    if (!authStore.isInitialized) {
        await authStore.checkAuth();
    }
    const requiresAuth = to.meta.requiresAuth === true;
    const requiresAdmin = to.meta.requiresAdmin === true;
    if (requiresAuth || requiresAdmin) {
        if (!authStore.isLoggedIn) {
            return {
                path: '/auth/login',
                query: to.path !== '/auth/login' ? { redirect: to.fullPath } : undefined
            };
        }
    }
    if (requiresAdmin && !authStore.isAdmin) {
        return { path: '/error/access' };
    }
});

export default router;
