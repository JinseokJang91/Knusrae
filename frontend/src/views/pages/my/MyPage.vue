<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import Select from 'primevue/select';
import Profile from './Profile.vue';
import Comments from './Comments.vue';
import Inquiries from './Inquiries.vue';
import Favorites from './Favorites.vue';
import Bookmarks from './Bookmarks.vue';
import Recipes from './Recipes.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref('profile');

const mobileMenuItems = [
    { key: 'profile', icon: 'pi pi-id-card', label: '내 정보' },
    { key: 'comments', icon: 'pi pi-comments', label: '내 댓글' },
    { key: 'inquiries', icon: 'pi pi-inbox', label: '1:1 문의내역' },
    { key: 'favorites', icon: 'pi pi-heart', label: '찜 목록' },
    { key: 'bookmarks', icon: 'pi pi-bookmark', label: '북마크 관리' },
    { key: 'recipes', icon: 'pi pi-book', label: '레시피 관리' }
];

// URL 쿼리 파라미터에서 탭 정보 읽기
onMounted(() => {
    const tab = route.query.tab as string;
    if (tab && ['profile', 'comments', 'inquiries', 'favorites', 'bookmarks', 'recipes'].includes(tab)) {
        activeTab.value = tab;
    }
});

// 탭 변경 시 URL 쿼리 파라미터 업데이트
watch(activeTab, (newTab) => {
    router.replace({
        path: '/my',
        query: { tab: newTab }
    });
});
</script>

<template>
    <div class="page-container page-container--card page-container--wide">
        <div class="mypage-container">
            <div class="mypage-header">
                <div class="mypage-header__title-row">
                    <h1 class="mypage-title m-0">마이페이지</h1>
                    <RouterLink v-if="authStore.isAdmin" to="/admin" class="mypage-admin-link">관리자 메뉴</RouterLink>
                </div>
            </div>

            <div class="mypage-mobile-nav">
                <Select v-model="activeTab" :options="mobileMenuItems" option-label="label" option-value="key" class="mypage-mobile-nav__select">
                    <template #value="slotProps">
                        <div v-if="slotProps.value" class="mypage-mobile-nav__selected">
                            <i :class="[mobileMenuItems.find((item) => item.key === slotProps.value)?.icon || 'pi pi-list', 'mypage-mobile-nav__option-icon']"></i>
                            <span class="mypage-mobile-nav__option-label">{{ mobileMenuItems.find((item) => item.key === slotProps.value)?.label || '' }}</span>
                        </div>
                        <span v-else>메뉴 선택</span>
                    </template>
                    <template #option="slotProps">
                        <div class="mypage-mobile-nav__option">
                            <i :class="[slotProps.option.icon, 'mypage-mobile-nav__option-icon']"></i>
                            <span class="mypage-mobile-nav__option-label">{{ slotProps.option.label }}</span>
                        </div>
                    </template>
                </Select>
            </div>

            <div class="mypage-mobile-content">
                <Profile v-if="activeTab === 'profile'" />
                <Comments v-else-if="activeTab === 'comments'" />
                <Inquiries v-else-if="activeTab === 'inquiries'" />
                <Favorites v-else-if="activeTab === 'favorites'" />
                <Bookmarks v-else-if="activeTab === 'bookmarks'" />
                <Recipes v-else />
            </div>

            <Tabs v-model:value="activeTab" class="mypage-desktop-tabs">
                <TabList>
                    <Tab value="profile">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-id-card"></i>
                            <span>내 정보</span>
                        </div>
                    </Tab>
                    <Tab value="comments">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-comments"></i>
                            <span>내 댓글</span>
                        </div>
                    </Tab>
                    <Tab value="inquiries">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-inbox"></i>
                            <span>1:1 문의내역</span>
                        </div>
                    </Tab>
                    <Tab value="favorites">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-heart"></i>
                            <span>찜 목록</span>
                        </div>
                    </Tab>
                    <Tab value="bookmarks">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-bookmark"></i>
                            <span>북마크 관리</span>
                        </div>
                    </Tab>
                    <Tab value="recipes">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-book"></i>
                            <span>레시피 관리</span>
                        </div>
                    </Tab>
                </TabList>

                <TabPanels>
                    <TabPanel value="profile">
                        <Profile />
                    </TabPanel>
                    <TabPanel value="comments">
                        <Comments />
                    </TabPanel>
                    <TabPanel value="inquiries">
                        <Inquiries />
                    </TabPanel>
                    <TabPanel value="favorites">
                        <Favorites />
                    </TabPanel>
                    <TabPanel value="bookmarks">
                        <Bookmarks />
                    </TabPanel>
                    <TabPanel value="recipes">
                        <Recipes />
                    </TabPanel>
                </TabPanels>
            </Tabs>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.mypage-container {
    padding: 0;

    .mypage-mobile-nav,
    .mypage-mobile-content {
        display: none;
    }

    .mypage-header {
        margin-bottom: 1.5rem;

        &__title-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 0.75rem 1rem;
            flex-wrap: wrap;
        }

        h1.mypage-title {
            margin: 0;
            font-size: 1.1875rem;
            font-weight: 700;
            line-height: 1.35;
            color: var(--primary-color);
        }
    }

    .mypage-admin-link {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0.45rem 0.85rem;
        border-radius: var(--border-radius, 6px);
        border: 1px solid var(--primary-color);
        color: var(--primary-color);
        font-size: 0.8125rem;
        font-weight: 600;
        text-decoration: none;
        transition:
            background-color 0.15s,
            color 0.15s;

        &:hover {
            background: var(--primary-color);
            color: var(--primary-color-text, #fff);
        }
    }

    :deep(.p-tabs) {
        .p-tablist {
            background: transparent;
            border-bottom: 2px solid var(--surface-border);
            gap: 0.5rem;

            .p-tab {
                padding: 1rem 1.5rem;
                font-weight: 500;
                color: var(--text-color-secondary);
                transition: all 0.2s;
                background: transparent;
                border: none;
                border-bottom: 2px solid transparent;
                margin-bottom: -2px;

                &:hover {
                    background: var(--surface-hover);
                    color: var(--text-color);
                }

                &[data-p-active='true'] {
                    color: var(--primary-color);
                    border-bottom-color: var(--primary-color);
                }
            }
        }

        .p-tabpanels {
            padding: 1rem 0;
            background: transparent;
            min-height: 0;
        }
    }
}

@media (min-width: 768px) {
    .mypage-container .mypage-header h1.mypage-title {
        font-size: 1.875rem;
        line-height: 1.25;
    }
}

@media (max-width: 767px) {
    .mypage-container {
        padding: 0;

        .mypage-header {
            margin-bottom: 1rem;
        }

        .mypage-admin-link {
            padding: 0.35rem 0.55rem;
            font-size: 0.75rem;
        }

        .mypage-desktop-tabs {
            display: none;
        }

        .mypage-mobile-nav {
            display: block;
            margin-bottom: 0.875rem;
        }

        .mypage-mobile-nav__select {
            width: 100%;
        }

        .mypage-mobile-nav__selected,
        .mypage-mobile-nav__option {
            display: inline-flex;
            align-items: center;
            gap: 0.625rem;
            min-width: 0;
        }

        .mypage-mobile-nav__selected i,
        .mypage-mobile-nav__option i {
            color: var(--primary-color);
            flex-shrink: 0;
        }

        .mypage-mobile-nav__selected span,
        .mypage-mobile-nav__option span {
            font-size: 0.9rem;
            color: var(--text-color);
        }

        :deep(.mypage-mobile-nav__select .p-select-label) {
            padding-top: 0.7rem;
            padding-bottom: 0.7rem;
        }

        .mypage-mobile-content {
            display: block;
        }
    }
}

@media (max-width: 480px) {
    .mypage-container {
        .mypage-header {
            margin-bottom: 1rem;
        }

        .mypage-header__title-row {
            align-items: flex-start;
        }

        .mypage-header h1.mypage-title {
            font-size: 1.0625rem;
        }

        .mypage-admin-link {
            padding: 0.3rem 0.5rem;
            font-size: 0.6875rem;
        }

        .mypage-mobile-nav__selected span,
        .mypage-mobile-nav__option span {
            font-size: 0.85rem;
        }
    }
}
</style>

<style lang="scss">
/* 전역 typography h1(2.5rem) 덮어쓰기 — scoped와 동일한 선택자 깊이 */
.mypage-container .mypage-header h1.mypage-title {
    margin: 0;
    font-size: 1.1875rem;
    line-height: 1.35;
    font-weight: 700;
    color: var(--primary-color);
}

@media (min-width: 768px) {
    .mypage-container .mypage-header h1.mypage-title {
        font-size: 1.875rem;
        line-height: 1.25;
    }
}

@media (max-width: 480px) {
    .mypage-container .mypage-header h1.mypage-title {
        font-size: 1.0625rem;
    }
}

@media (max-width: 768px) {
    .p-select-list-container {
        max-height: 16rem;
    }

    .p-select-option {
        padding: 0.625rem 0.75rem;
    }

    .p-select-option .mypage-mobile-nav__option {
        display: flex;
        width: 100%;
        align-items: center;
        gap: 0.875rem;
    }

    .p-select-option .mypage-mobile-nav__option-icon {
        width: 1.125rem;
        min-width: 1.125rem;
        text-align: center;
        margin-right: 0;
        color: var(--primary-color);
    }

    .p-select-option .mypage-mobile-nav__option-label {
        line-height: 1.3;
    }
}
</style>
