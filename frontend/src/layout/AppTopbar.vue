<script setup>
import logoText from '@/assets/images/logo-text.png';
import { useLayout } from '@/layout/composables/layout';
import { getCurrentUser, isLoggedIn } from '@/utils/auth';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();

const searchQuery = ref('');
const isLoggedInState = ref(isLoggedIn());
const userName = ref('');

const handleSearch = () => {
    if (searchQuery.value.trim()) {
        console.log('검색어:', searchQuery.value);
    }
};

const clearSearch = () => {
    searchQuery.value = '';
};

const updateLoginState = () => {
    isLoggedInState.value = isLoggedIn();
    if (isLoggedInState.value) {
        const user = getCurrentUser();
        userName.value = user?.name || '사용자';
    } else {
        userName.value = '';
    }
};

const handleLogout = () => {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('accessToken');
        updateLoginState();
        router.push('/');
    }
};

onMounted(() => {
    updateLoginState();
    window.addEventListener('storage', updateLoginState);
    window.addEventListener('message', (event) => {
        if (event.data && (event.data.type === 'NAVER_LOGIN_SUCCESS' || event.data.type === 'NAVER_LOGIN_ERROR')) {
            updateLoginState();
        }
    });
});

onBeforeUnmount(() => {
    window.removeEventListener('storage', updateLoginState);
});
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/" class="layout-topbar-logo">
                <img :src="logoText" alt="너에게 스며드는 레시피" class="logo-text-image" />
            </router-link>
        </div>

        <div class="layout-topbar-search">
            <div class="search-container">
                <i class="pi pi-search search-icon"></i>
                <input type="text" placeholder="레시피를 검색해보세요..." class="search-input" v-model="searchQuery" @keyup.enter="handleSearch" />
                <button v-if="searchQuery" class="search-clear-btn" @click="clearSearch">
                    <i class="pi pi-times"></i>
                </button>
            </div>
        </div>

        <div class="layout-topbar-actions">
            <!-- <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <div class="relative">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action layout-topbar-action-highlight"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div> -->
            <div class="layout-config-menu" v-if="isLoggedInState">
                <span class="layout-topbar-welcome">{{ userName }}님 환영합니다.</span>
            </div>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <div class="relative">
                        <button
                            type="button"
                            class="layout-topbar-action"
                            v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        >
                            <i class="pi pi-user"></i>
                            <span>Profile</span>
                        </button>
                        <div class="hidden absolute right-0 mt-2 w-56 card p-2 z-50">
                            <router-link to="/my/profile" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-id-card"></i>
                                <span>내 정보 수정</span>
                            </router-link>
                            <router-link to="/my/recipes" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-book"></i>
                                <span>레시피 관리</span>
                            </router-link>
                            <router-link to="/my/comments" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-comments"></i>
                                <span>댓글 관리</span>
                            </router-link>
                            <router-link to="/my/reviews" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-star"></i>
                                <span>후기 관리</span>
                            </router-link>
                            <router-link to="/my/inquiries" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-inbox"></i>
                                <span>1:1 문의 내역</span>
                            </router-link>
                            <router-link to="/recipe/favorites" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-heart"></i>
                                <span>찜 목록</span>
                            </router-link>
                            <div class="my-2 border-t"></div>
                            <router-link v-if="!isLoggedInState" to="/auth/login" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded">
                                <i class="pi pi-sign-in"></i>
                                <span>로그인</span>
                            </router-link>
                            <button v-else type="button" class="flex items-center gap-2 px-3 py-2 hover:bg-surface-100 rounded w-full text-left" @click="handleLogout">
                                <i class="pi pi-sign-out"></i>
                                <span>로그아웃</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
