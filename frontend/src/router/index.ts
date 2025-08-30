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

export default router;
