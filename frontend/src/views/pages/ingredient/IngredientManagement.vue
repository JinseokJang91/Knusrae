<template>
    <div class="ingredient-management">
        <div class="page-header mb-4">
            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-3xl font-bold text-gray-900">재료 관리</h1>
                    <p class="text-gray-600 mt-2">재료의 보관법과 손질법을 확인하세요</p>
                </div>
                <Button 
                    v-if="authStore.isAdmin"
                    label="등록하기"
                    icon="pi pi-plus"
                    @click="goToRegister"
                    class="ml-4"
                />
            </div>
        </div>

        <Tabs v-model:value="activeTab" class="ingredient-tabs">
            <TabList>
                <Tab value="storage">
                    <div class="flex items-center gap-2">
                        <i class="pi pi-box"></i>
                        <span>재료 보관법</span>
                    </div>
                </Tab>
                <Tab value="preparation">
                    <div class="flex items-center gap-2">
                        <i class="pi pi-cut"></i>
                        <span>재료 손질법</span>
                    </div>
                </Tab>
            </TabList>

            <TabPanels>
                <TabPanel value="storage">
                    <IngredientList 
                        type="storage"
                        :selected-group-id="selectedGroupId"
                        :search-query="searchQuery"
                        @group-selected="handleGroupSelected"
                        @search-changed="handleSearchChanged"
                    />
                </TabPanel>
                
                <TabPanel value="preparation">
                    <IngredientList 
                        type="preparation"
                        :selected-group-id="selectedGroupId"
                        :search-query="searchQuery"
                        @group-selected="handleGroupSelected"
                        @search-changed="handleSearchChanged"
                    />
                </TabPanel>
            </TabPanels>
        </Tabs>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import Button from 'primevue/button';
import IngredientList from '@/components/ingredient/IngredientList.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref<'storage' | 'preparation'>('storage');
const selectedGroupId = ref<number | null>(null);
const searchQuery = ref('');

const goToRegister = () => {
    router.push('/ingredient/management/register');
};

// URL 쿼리 파라미터로 탭 상태 복원
onMounted(() => {
    const type = route.query.type as string;
    if (type === 'preparation') {
        activeTab.value = 'preparation';
    } else {
        activeTab.value = 'storage';
    }
    
    const groupId = route.query.groupId as string;
    if (groupId) {
        selectedGroupId.value = parseInt(groupId);
    }
    
    const search = route.query.search as string;
    if (search) {
        searchQuery.value = search;
    }
});

// 탭 변경 시 URL 쿼리 파라미터 업데이트
watch(activeTab, () => {
    updateQuery();
});

const handleGroupSelected = (groupId: number | null) => {
    selectedGroupId.value = groupId;
    updateQuery();
};

const handleSearchChanged = (query: string) => {
    searchQuery.value = query;
    updateQuery();
};

const updateQuery = () => {
    const query: Record<string, string> = { type: activeTab.value };
    
    if (selectedGroupId.value) {
        query.groupId = selectedGroupId.value.toString();
    }
    
    if (searchQuery.value) {
        query.search = searchQuery.value;
    }
    
    router.replace({ query });
};

// URL 쿼리 변경 감지
watch(() => route.query, (newQuery) => {
    if (newQuery.type === 'preparation' && activeTab.value !== 'preparation') {
        activeTab.value = 'preparation';
    } else if (newQuery.type !== 'preparation' && activeTab.value !== 'storage') {
        activeTab.value = 'storage';
    }
    
    if (newQuery.groupId) {
        selectedGroupId.value = parseInt(newQuery.groupId as string);
    } else {
        selectedGroupId.value = null;
    }
    
    if (newQuery.search) {
        searchQuery.value = newQuery.search as string;
    } else {
        searchQuery.value = '';
    }
}, { immediate: true });
</script>

<style scoped>
.ingredient-management {
    padding: 24px;
    max-width: 1400px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 24px;
}

.ingredient-tabs {
    margin-top: 24px;
}

::deep(.p-tabs) {
    .p-tablist {
        background: transparent;
        border-bottom: 2px solid var(--surface-border);
        margin-bottom: 24px;
    }

    .p-tab {
        padding: 16px 24px;
        font-size: 16px;
        font-weight: 600;
    }

    .p-tabpanels {
        padding: 24px 0;
    }
}
</style>

