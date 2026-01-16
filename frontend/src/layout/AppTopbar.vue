<script setup lang="ts">
import logoText from '@/assets/images/logo-text.png';
import { useAuthStore } from '@/stores/authStore';
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { getRecentSearchKeywords, deleteRecentSearchKeyword, deleteAllRecentSearchKeywords, saveRecentSearchKeyword, type RecentSearchKeyword } from '@/utils/search';

const router = useRouter();
const confirm = useConfirm();
const toast = useToast();
const authStore = useAuthStore();

const searchQuery = ref('');
const profileMenuRef = ref<HTMLElement | null>(null);
const recentKeywords = ref<RecentSearchKeyword[]>([]);
const showRecentKeywords = ref(false);
const recentKeywordsLoading = ref(false);

// 자동저장 설정 (localStorage에 저장 - 계정별로 분리)
const getAutoSaveKey = (memberId?: number): string => {
    if (memberId) {
        return `recent_search_auto_save_${memberId}`;
    }
    return 'recent_search_auto_save_guest';
};

const getInitialAutoSaveValue = (): boolean => {
    const memberId = authStore.memberInfo?.id;
    const key = getAutoSaveKey(memberId);
    const saved = localStorage.getItem(key);
    return saved === null ? true : saved === 'true'; // 기본값은 true
};
const isAutoSaveEnabled = ref<boolean>(getInitialAutoSaveValue());

const handleSearch = () => {
    const keyword = searchQuery.value.trim();
    
    if (keyword) {
        // 자동저장이 켜져 있고 로그인 상태일 때만 검색어 저장
        if (authStore.isLoggedIn && isAutoSaveEnabled.value) {
            try {
                const memberId = authStore.memberInfo?.id;
                saveRecentSearchKeyword(keyword, memberId);
                // 저장 후 목록 새로고침
                loadRecentKeywords();
            } catch (error) {
                console.error('검색어 저장 실패:', error);
                // 저장 실패해도 검색은 진행
            }
        }
        
        // 검색어가 있으면 검색 결과 페이지로 이동
        router.push({
            path: '/recipe/search',
            query: { keyword: keyword }
        });
        // 최근 검색어 목록 닫기
        showRecentKeywords.value = false;
    } else {
        // 검색어가 없으면 안내 메시지 표시
        toast.add({
            severity: 'warn',
            summary: '검색어를 입력해주세요',
            detail: '레시피를 검색하려면 검색어를 입력해주세요.',
            life: 3000
        });
    }
};

const clearSearch = () => {
    searchQuery.value = '';
    showRecentKeywords.value = false;
};

// 최근 검색어 목록 로드
const loadRecentKeywords = () => {
    // 로그인 상태가 아니면 목록을 로드하지 않음
    if (!authStore.isLoggedIn) {
        recentKeywords.value = [];
        return;
    }

    // 자동저장 off 상태에서도 목록은 로드 (단지 새 검색어는 저장하지 않음)
    try {
        recentKeywordsLoading.value = true;
        const memberId = authStore.memberInfo?.id;
        const keywords = getRecentSearchKeywords(memberId);
        recentKeywords.value = keywords;
    } catch (error) {
        console.error('최근 검색어 로드 실패:', error);
        recentKeywords.value = [];
    } finally {
        recentKeywordsLoading.value = false;
    }
};

// 검색어 클릭 시 검색 실행
const selectRecentKeyword = (keyword: string) => {
    searchQuery.value = keyword;
    showRecentKeywords.value = false; // 드롭다운 닫기
    
    // 자동저장이 켜져 있고 로그인 상태일 때만 검색어 저장
    if (authStore.isLoggedIn && isAutoSaveEnabled.value) {
        try {
            const memberId = authStore.memberInfo?.id;
            saveRecentSearchKeyword(keyword, memberId);
            // 저장 후 목록 새로고침
            loadRecentKeywords();
        } catch (error) {
            console.error('검색어 저장 실패:', error);
            // 저장 실패해도 검색은 진행
        }
    }
    
    handleSearch();
};

// 최근 검색어 삭제
const handleDeleteKeyword = (keywordId: number, event: Event) => {
    event.stopPropagation(); // 이벤트 전파 방지
    
    try {
        const memberId = authStore.memberInfo?.id;
        deleteRecentSearchKeyword(keywordId, memberId);
        // 목록에서 제거
        recentKeywords.value = recentKeywords.value.filter(k => k.id !== keywordId);
        toast.add({
            severity: 'success',
            summary: '삭제 완료',
            detail: '검색어가 삭제되었습니다.',
            life: 2000
        });
    } catch (error) {
        console.error('검색어 삭제 실패:', error);
        toast.add({
            severity: 'error',
            summary: '삭제 실패',
            detail: '검색어 삭제 중 오류가 발생했습니다.',
            life: 3000
        });
    }
};

// 자동저장 끄기/켜기 (ToggleSwitch의 v-model이 값 변경을 처리)
const toggleAutoSave = (newValue: boolean) => {
    const memberId = authStore.memberInfo?.id;
    const key = getAutoSaveKey(memberId);
    localStorage.setItem(key, String(newValue));
    
    // 자동저장 off로 변경해도 목록은 유지 (단지 새 검색어는 저장하지 않음)
    if (!newValue) {
        toast.add({
            severity: 'info',
            summary: '자동저장 비활성화',
            detail: '이제부터 새로운 검색어는 저장되지 않습니다.',
            life: 2000
        });
    } else {
        toast.add({
            severity: 'success',
            summary: '자동저장 활성화',
            detail: '이제부터 검색한 검색어가 자동으로 저장됩니다.',
            life: 2000
        });
    }
};

// 전체 검색어 삭제
const handleDeleteAllKeywords = (event: Event) => {
    event.stopPropagation(); // 이벤트 전파 방지
    
    confirm.require({
        message: '모든 최근 검색어를 삭제하시겠습니까?',
        header: '전체 삭제',
        icon: 'pi pi-exclamation-triangle',
        rejectProps: {
            label: '취소',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: '삭제',
            severity: 'danger'
        },
        accept: () => {
            try {
                const memberId = authStore.memberInfo?.id;
                deleteAllRecentSearchKeywords(memberId);
                recentKeywords.value = [];
                toast.add({
                    severity: 'success',
                    summary: '삭제 완료',
                    detail: '모든 최근 검색어가 삭제되었습니다.',
                    life: 2000
                });
            } catch (error) {
                console.error('전체 검색어 삭제 실패:', error);
                toast.add({
                    severity: 'error',
                    summary: '삭제 실패',
                    detail: '검색어 삭제 중 오류가 발생했습니다.',
                    life: 3000
                });
            }
        },
        reject: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
};

// 검색창 포커스 시 최근 검색어 표시 (검색창이 비어있을 때만)
const handleSearchFocus = () => {
    // 검색창이 비어있을 때만 최근 검색어 표시 (자동저장 off 상태에서도 표시)
    if (authStore.isLoggedIn && searchQuery.value.trim() === '') {
        loadRecentKeywords();
        showRecentKeywords.value = true;
    } else {
        showRecentKeywords.value = false;
    }
};

// 검색창 블러 시 최근 검색어 숨김 (약간의 지연을 두어 클릭 이벤트 처리)
const handleSearchBlur = (event: FocusEvent) => {
    // 드롭다운 내부를 클릭한 경우는 닫지 않음
    const relatedTarget = event.relatedTarget as HTMLElement;
    if (relatedTarget && relatedTarget.closest('.recent-keywords-dropdown')) {
        return;
    }
    
    setTimeout(() => {
        showRecentKeywords.value = false;
    }, 200);
};

// 드롭다운 외부 클릭 시 닫기
const handleClickOutside = (event: MouseEvent) => {
    const target = event.target as HTMLElement;
    const searchWrapper = target.closest('.search-wrapper');
    const dropdown = target.closest('.recent-keywords-dropdown');
    
    // 검색 래퍼나 드롭다운 외부를 클릭한 경우에만 닫기
    if (!searchWrapper && !dropdown) {
        showRecentKeywords.value = false;
    }
};

const closeProfileMenu = () => {
    if (profileMenuRef.value) {
        profileMenuRef.value.classList.add('hidden');
    }
};

const handleMyRecipesClick = (event: Event) => {
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
                    query: { redirect: '/my/recipes' }
                });
            },
            reject: () => {
                // 취소 시 아무것도 하지 않음
            }
        });
    } else {
        router.push('/my/recipes');
    }
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
                closeProfileMenu();
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
        closeProfileMenu();
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

// 로그인 상태 변경 감시하여 최근 검색어 로드 및 자동저장 설정 업데이트
watch(() => authStore.isLoggedIn, (isLoggedIn) => {
    if (isLoggedIn && showRecentKeywords.value) {
        // 자동저장 off 상태에서도 목록 로드
        loadRecentKeywords();
        // 계정 변경 시 해당 계정의 자동저장 설정으로 업데이트
        isAutoSaveEnabled.value = getInitialAutoSaveValue();
    } else if (!isLoggedIn) {
        recentKeywords.value = [];
        showRecentKeywords.value = false;
        // 로그아웃 시 기본값으로 재설정
        isAutoSaveEnabled.value = getInitialAutoSaveValue();
    }
});

// 사용자 ID 변경 감시 (계정 변경 시 자동저장 설정 업데이트)
watch(() => authStore.memberInfo?.id, (newId, oldId) => {
    // 사용자 ID가 변경되었을 때 (계정 변경)
    if (newId !== oldId && authStore.isLoggedIn) {
        isAutoSaveEnabled.value = getInitialAutoSaveValue();
        // 최근 검색어 목록도 새로 로드
        if (showRecentKeywords.value) {
            loadRecentKeywords();
        }
    }
});

// 검색어 입력 감시 - 값이 입력되면 최근 검색어 목록 숨김
watch(searchQuery, (newValue) => {
    if (newValue.trim() !== '') {
        showRecentKeywords.value = false;
    }
});

onMounted(() => {
    // 외부 클릭 감지
    document.addEventListener('click', handleClickOutside);
    
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
    <div class="layout-topbar-horizontal">
        <!-- 좌측 영역: 로고 + 앱명 + 메뉴 -->
        <div class="topbar-left">
            <!-- 로고와 앱명 -->
            <router-link to="/" class="logo-section">
                <img :src="logoText" alt="너에게 스며드는 레시피" class="logo-image" />
            </router-link>

            <!-- 메뉴 -->
            <nav class="main-menu">
                <router-link to="/recipe/category" class="menu-item">
                    <i class="fa-solid fa-utensils"></i>
                    <span>전체 레시피</span>
                </router-link>
                <router-link to="/ingredient/management" class="menu-item">
                    <i class="fa-solid fa-boxes-packing"></i>
                    <span>재료 관리</span>
                </router-link>
                <router-link to="/ranking" class="menu-item">
                    <i class="fa-solid fa-ranking-star"></i>
                    <span>랭킹</span>
                </router-link>
                <router-link to="/recipe/category" class="menu-item">
                    <i class="fa-solid fa-bars-staggered"></i>
                    <span>카테고리</span>
                </router-link>
                <router-link to="/faq" class="menu-item">
                    <i class="fa-solid fa-circle-question"></i>
                    <span>FAQ</span>
                </router-link>
            </nav>
        </div>

        <!-- 중앙 영역: 검색창 -->
        <div class="topbar-center">
            <div class="search-wrapper">
                <div class="search-container">
                    <input 
                        type="text" 
                        placeholder="레시피를 검색해보세요..." 
                        class="search-input" 
                        v-model="searchQuery" 
                        @keyup.enter="handleSearch"
                        @focus="handleSearchFocus"
                        @blur="handleSearchBlur"
                    />
                    <button v-if="searchQuery" class="search-clear-btn" @click="clearSearch">
                        <i class="pi pi-times"></i>
                    </button>
                    <button class="search-submit-btn" @click="handleSearch" type="button">
                        <i class="pi pi-search"></i>
                    </button>
                </div>
                
                <!-- 최근 검색어 드롭다운 -->
                <div 
                    v-if="showRecentKeywords && authStore.isLoggedIn" 
                    class="recent-keywords-dropdown"
                    @mousedown.prevent
                >
                    <div class="recent-keywords-header">
                        <span class="text-sm font-semibold text-gray-700">최근 검색어</span>
                        <div class="recent-keywords-header-actions">
                            <button 
                                v-if="recentKeywords.length > 0"
                                class="auto-save-toggle-btn text-xs text-gray-600 hover:text-gray-800 whitespace-nowrap"
                                @click="handleDeleteAllKeywords"
                                @mousedown.stop
                                type="button"
                            >
                                전체 삭제
                            </button>
                            <div class="flex items-center gap-2" @mousedown.stop>
                                <span class="text-xs text-gray-600 whitespace-nowrap">자동저장</span>
                                <ToggleSwitch 
                                    v-model="isAutoSaveEnabled"
                                    @update:modelValue="(value: boolean) => toggleAutoSave(value)"
                                />
                            </div>
                        </div>
                    </div>
                    <div v-if="recentKeywordsLoading" class="recent-keywords-loading">
                        <i class="pi pi-spin pi-spinner"></i>
                        <span class="ml-2">로딩 중...</span>
                    </div>
                    <div v-else-if="recentKeywords.length === 0" class="recent-keywords-empty">
                        <span class="text-sm text-gray-500">최근 검색어가 없습니다.</span>
                    </div>
                    <div v-else class="recent-keywords-list">
                        <div 
                            v-for="keyword in recentKeywords" 
                            :key="keyword.id"
                            class="recent-keyword-item"
                            @mousedown.prevent
                            @click="selectRecentKeyword(keyword.keyword)"
                        >
                            <i class="pi pi-clock text-gray-400"></i>
                            <span class="recent-keyword-text">{{ keyword.keyword }}</span>
                            <button 
                                class="recent-keyword-delete"
                                @mousedown.stop
                                @click.stop="handleDeleteKeyword(keyword.id, $event)"
                                type="button"
                            >
                                <i class="pi pi-times"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 우측 영역: 내 레시피 + 프로필/로그인/회원가입 -->
        <div class="topbar-right">
            <button class="menu-item my-recipes-btn" @click="handleMyRecipesClick">
                <i class="pi pi-book"></i>
                <span>내 레시피</span>
            </button>

            <!-- 로그인 상태일 때 -->
            <div v-if="authStore.isLoggedIn" class="profile-section">
                <div class="relative">
                    <button
                        type="button"
                        class="profile-button"
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                    >
                        <img 
                            v-if="authStore.memberProfileImage" 
                            :src="authStore.memberProfileImage" 
                            alt="프로필" 
                            class="profile-image"
                        />
                        <i v-else class="pi pi-user"></i>
                        <span class="ml-2">{{ authStore.memberName }}님</span>
                    </button>
                    <div ref="profileMenuRef" class="hidden absolute right-0 mt-2 w-56 card p-2 z-50">
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
                        <a href="/my/inquiries" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/inquiries', $event)">
                            <i class="pi pi-inbox"></i>
                            <span>1:1 문의 내역</span>
                        </a>
                        <a href="/my/favorites" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded cursor-pointer" @click="handleMyMenuClick('/my/favorites', $event)">
                            <i class="pi pi-heart"></i>
                            <span>찜 목록</span>
                        </a>
                        <div class="my-2 border-t"></div>
                        <button type="button" class="flex items-center gap-2 px-3 py-2 hover:bg-gray-100 rounded w-full text-left" @click="handleLogout(); closeProfileMenu();">
                            <i class="pi pi-sign-out"></i>
                            <span>로그아웃</span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- 비로그인 상태일 때 -->
            <div v-else class="auth-buttons">
                <Button label="로그인" @click="router.push('/auth/login')" outlined size="small" />
                <Button label="회원가입" @click="router.push('/auth/signup')" size="small" />
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped></style>
