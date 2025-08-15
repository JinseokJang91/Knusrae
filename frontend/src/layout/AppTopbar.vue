<script setup>
import { useLayout } from '@/layout/composables/layout';
import { ref } from 'vue';
import AppConfigurator from './AppConfigurator.vue';

const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();

const searchQuery = ref('');

const handleSearch = () => {
    if (searchQuery.value.trim()) {
        // 검색 로직 구현
        console.log('검색어:', searchQuery.value);
        // 여기에 실제 검색 기능을 추가할 수 있습니다
    }
};

const clearSearch = () => {
    searchQuery.value = '';
};
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/" class="layout-topbar-logo">
                <img src="/images/logo-text.png" alt="너에게 스며드는 레시피" class="logo-text-image" />
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
            <div class="layout-config-menu">
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
            </div>

            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <button type="button" class="layout-topbar-action">
                        <i class="pi pi-user"></i>
                        <span>Profile</span>
                    </button>
                    <router-link to="/auth/login" class="layout-topbar-action">
                        <i class="pi pi-sign-in"></i>
                        <span>Sign In</span>
                    </router-link>
                </div>
            </div>
        </div>
    </div>
</template>
