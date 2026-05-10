<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import AppFooter from './AppFooter.vue';
import AppTopbar from './AppTopbar.vue';
import ScrollToTop from '@/components/ScrollToTop.vue';

type NavItem = {
    key: string;
    label: string;
    icon: string;
    to: string;
    requiresAuth?: boolean;
};

const containerClass = computed(() => {
    return {
        'layout-wrapper': true,
        'layout-horizontal': true
    };
});

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const mobileNavItems = computed<NavItem[]>(() => {
    const items: NavItem[] = [
        { key: 'home', label: '홈', icon: 'pi pi-home', to: '/' },
        { key: 'category', label: '카테고리', icon: 'pi pi-th-large', to: '/recipe/category' },
        { key: 'ranking', label: '랭킹', icon: 'pi pi-chart-bar', to: '/ranking' }
    ];

    if (authStore.isAdmin) {
        items.push({ key: 'admin', label: '관리자', icon: 'pi pi-cog', to: '/admin', requiresAuth: true });
    } else {
        items.push({ key: 'my-recipes', label: '내 레시피', icon: 'pi pi-book', to: '/my/recipes', requiresAuth: true });
        items.push({ key: 'my', label: '마이', icon: 'pi pi-user', to: '/my', requiresAuth: true });
    }

    if (authStore.isAdmin) {
        items.push({ key: 'my', label: '마이', icon: 'pi pi-user', to: '/my', requiresAuth: true });
    }

    return items;
});

const isActiveMobileNav = (item: NavItem): boolean => {
    if (item.to === '/') return route.path === '/';
    return route.path === item.to || route.path.startsWith(`${item.to}/`);
};

const handleMobileNavClick = (item: NavItem) => {
    if (item.requiresAuth && !authStore.isLoggedIn) {
        router.push({
            path: '/auth/login',
            query: { redirect: item.to }
        });
        return;
    }

    router.push(item.to);
};
</script>

<template>
    <div :class="containerClass">
        <app-topbar></app-topbar>
        <div class="layout-main-container">
            <div class="layout-main">
                <router-view></router-view>
            </div>
            <app-footer></app-footer>
        </div>
        <nav class="mobile-bottom-nav" aria-label="모바일 하단 내비게이션">
            <button v-for="item in mobileNavItems" :key="item.key" type="button" class="mobile-bottom-nav__item" :class="{ 'is-active': isActiveMobileNav(item) }" @click="handleMobileNavClick(item)">
                <i :class="item.icon" aria-hidden="true"></i>
                <span>{{ item.label }}</span>
            </button>
        </nav>
    </div>
    <ScrollToTop />
</template>

<style scoped>
.mobile-bottom-nav {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 998;
    display: none;
    align-items: center;
    justify-content: space-around;
    height: calc(var(--mobile-bottom-nav-height) + env(safe-area-inset-bottom));
    padding: 0.375rem 0.25rem calc(0.375rem + env(safe-area-inset-bottom));
    border-top: 1px solid rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.96);
    backdrop-filter: blur(8px);
}

.mobile-bottom-nav__item {
    flex: 1 1 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.1875rem;
    min-height: 44px;
    border: none;
    background: transparent;
    color: #6b7280;
    font-size: 0.6875rem;
    cursor: pointer;
}

.mobile-bottom-nav__item i {
    font-size: 1.0625rem;
}

.mobile-bottom-nav__item.is-active {
    color: var(--primary-color, #f97316);
    font-weight: 600;
}

@media (max-width: 768px) {
    .mobile-bottom-nav {
        display: flex;
    }
}
</style>
