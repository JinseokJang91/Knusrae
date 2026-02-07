<template>
    <div class="mypage-container">
        <div class="card">
            <div class="mypage-header">
                <h1 class="text-3xl font-bold m-0">마이페이지</h1>
                <Button
                    label="내 레시피 관리"
                    icon="pi pi-book"
                    severity="secondary"
                    outlined
                    @click="goToMyRecipes"
                />
            </div>

            <Tabs v-model:value="activeTab">
                <TabList>
                    <Tab value="profile">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-id-card"></i>
                            <span>내 정보 수정</span>
                        </div>
                    </Tab>
                    <Tab value="comments">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-comments"></i>
                            <span>댓글 관리</span>
                        </div>
                    </Tab>
                    <Tab value="inquiries">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-inbox"></i>
                            <span>1:1 문의 내역</span>
                        </div>
                    </Tab>
                    <Tab value="favorites">
                        <div class="flex items-center gap-2">
                            <i class="pi pi-heart"></i>
                            <span>찜 목록</span>
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
                </TabPanels>
            </Tabs>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import Button from 'primevue/button';
import Profile from './Profile.vue';
import Comments from './Comments.vue';
import Inquiries from './Inquiries.vue';
import Favorites from './Favorites.vue';

const route = useRoute();
const router = useRouter();
const activeTab = ref('profile');

// URL 쿼리 파라미터에서 탭 정보 읽기
onMounted(() => {
    const tab = route.query.tab as string;
    if (tab && ['profile', 'comments', 'inquiries', 'favorites'].includes(tab)) {
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

const goToMyRecipes = () => {
    router.push('/my/recipes');
};
</script>

<style lang="scss" scoped>
.mypage-container {
    padding: 1rem;
    max-width: 1400px;
    margin: 0 auto;

    .mypage-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 1.5rem;
        flex-wrap: wrap;

        h1 {
            color: var(--primary-color);
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

                &[data-p-active="true"] {
                    color: var(--primary-color);
                    border-bottom-color: var(--primary-color);
                }
            }
        }

        .p-tabpanels {
            padding: 2rem 0;
            background: transparent;
        }
    }
}

@media (max-width: 768px) {
    .mypage-container {
        padding: 0.5rem;

        :deep(.p-tabs) {
            .p-tablist {
                flex-wrap: wrap;
                gap: 0.25rem;
                
                .p-tab {
                    padding: 0.75rem 1rem;
                    font-size: 0.9rem;
                }
            }

            .p-tabpanels {
                padding: 1rem 0;
            }
        }
    }
}
</style>

