<script setup lang="ts">
import logoText from '@/assets/images/logo-text.png';
import { useLayout } from '@/layout/composables/layout';
import { useAuthStore } from '@/stores/authStore';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';

const router = useRouter();
const confirm = useConfirm();
const { toggleMenu } = useLayout();
const authStore = useAuthStore();

const searchQuery = ref('');

const handleSearch = () => {
    if (searchQuery.value.trim()) {
        console.log('검색어:', searchQuery.value);
    }
};

const clearSearch = () => {
    searchQuery.value = '';
};

const handleMyMenuClick = (path: string, event: Event) => {
    event.preventDefault();
    
    if (!authStore.isLoggedIn) {
        confirm.require({
            message: '로그인 후 이용 가능합니다.',
            header: '안내',
            icon: 'pi pi-info-circle',
            rejectProps: {
                label: '취소',
                severity: 'secondary',
                outlined: true
            },
            acceptProps: {
                label: '로그인'
            },
            accept: () => {
                router.push({
                    path: '/auth/login',
                    query: { redirect: path }
                });
            },
            reject: () => {
                // 취소 시 아무것도 하지 않음
            }
        });
    } else {
        router.push(path);
    }
};

const handleLogout = async () => {
    confirm.require({
        message: '로그아웃 하시겠습니까?',
        header: '안내',
        icon: 'pi pi-info-circle',
        rejectProps: {
            label: '취소',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: '확인'
        },
        accept: async () => {
            await authStore.logout();
            router.push('/');
        },
        reject: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
};

onMounted(() => {
    // OAuth 로그인 성공 시 메시지 수신하여 로그인 상태 업데이트
    window.addEventListener('message', async (event) => {
        if (event.data && (
            event.data.type === 'NAVER_LOGIN_SUCCESS' ||
            event.data.type === 'GOOGLE_LOGIN_SUCCESS' ||
            event.data.type === 'KAKAO_LOGIN_SUCCESS'
        )) {
            // OAuth 로그인 성공 시 로그인 상태 업데이트
            await authStore.login();
            
            // redirect 경로가 있으면 해당 경로로 이동
            const redirectPath = localStorage.getItem('oauth_redirect');
            if (redirectPath) {
                localStorage.removeItem('oauth_redirect');
                router.push(redirectPath);
            }
        } else if (event.data && (
            event.data.type === 'NAVER_LOGIN_ERROR' ||
            event.data.type === 'GOOGLE_LOGIN_ERROR' ||
            event.data.type === 'KAKAO_LOGIN_ERROR'
        )) {
            // 로그인 실패 시 상태 초기화
            authStore.reset();
            localStorage.removeItem('oauth_redirect'); // 에러 시에도 삭제
        }
    });
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
            <div class="layout-config-menu" v-if="authStore.isLoggedIn">
                <span class="layout-topbar-welcome">{{ authStore.memberName }}님 환영합니다.</span>
            </div>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <div class="relative">
                        <button
                            type="button"
                            class="layout-topbar-action profile-button"
                            v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        >
                            <img 
                                v-if="authStore.memberProfileImage" 
                                :src="authStore.memberProfileImage" 
                                alt="프로필" 
                                class="profile-image"
                            />
                            <i v-else class="pi pi-user"></i>
                            <span class="hidden">Profile</span>
                        </button>
                        <div class="hidden absolute right-0 mt-2 w-56 card p-2 z-50">
                            <a href="/my/profile" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/profile', $event)">
                                <i class="pi pi-id-card"></i>
                                <span>내 정보 수정</span>
                            </a>
                            <a href="/my/recipes" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/recipes', $event)">
                                <i class="pi pi-book"></i>
                                <span>레시피 관리</span>
                            </a>
                            <a href="/my/comments" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/comments', $event)">
                                <i class="pi pi-comments"></i>
                                <span>댓글 관리</span>
                            </a>
                            <a href="/my/reviews" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/reviews', $event)">
                                <i class="pi pi-star"></i>
                                <span>후기 관리</span>
                            </a>
                            <a href="/my/inquiries" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/inquiries', $event)">
                                <i class="pi pi-inbox"></i>
                                <span>1:1 문의 내역</span>
                            </a>
                            <a href="/my/favorites" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/favorites', $event)">
                                <i class="pi pi-heart"></i>
                                <span>찜 목록</span>
                            </a>
                            <div class="my-2 border-t"></div>
                            <router-link v-if="!authStore.isLoggedIn" to="/auth/login" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded">
                                <i class="pi pi-sign-in"></i>
                                <span>로그인</span>
                            </router-link>
                            <button v-else type="button" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded w-full text-left" @click="handleLogout">
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
