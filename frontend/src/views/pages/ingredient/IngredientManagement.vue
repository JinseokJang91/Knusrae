<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import IngredientList from '@/components/ingredient/IngredientList.vue';
import type { ComponentPublicInstance } from 'vue';

const route = useRoute();
const router = useRouter();

const activeTab = ref<'storage' | 'preparation'>('storage');
const selectedGroupId = ref<number | null>(null);
const searchQuery = ref('');
const storageListRef = ref<ComponentPublicInstance | null>(null);
const preparationListRef = ref<ComponentPublicInstance | null>(null);

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

const openRequestDialog = () => {
    const listRef = activeTab.value === 'storage' ? storageListRef.value : preparationListRef.value;
    (listRef as { openRequestDialog?: () => void })?.openRequestDialog?.();
};

// URL 쿼리 변경 감지
watch(
    () => route.query,
    (newQuery) => {
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
    },
    { immediate: true }
);
</script>

<template>
    <div class="page-container page-container--card">
        <div class="ingredient-panel">
            <!-- 1. 검색창(좌) + 재료 정보 요청(우) -->
            <div class="top-row">
                <span class="p-input-icon-left search-bar">
                    <InputText v-model="searchQuery" placeholder="재료명을 검색하세요...(예: 감자, 계란)" class="w-full" @input="updateQuery()" />
                </span>
                <Button label="재료 정보 요청하기" icon="pi pi-send" class="request-btn" size="small" raised @click="openRequestDialog" />
            </div>

            <!-- 검색 영역 하단 구분선 (Category.vue .category-tabs-panel과 동일) -->
            <div class="tab-panel-wrap">
                <Tabs v-model:value="activeTab" class="ingredient-tabs">
                    <TabList>
                        <Tab value="storage">재료 보관법</Tab>
                        <Tab value="preparation">재료 손질법</Tab>
                    </TabList>
                    <TabPanels>
                        <TabPanel value="storage">
                            <IngredientList ref="storageListRef" type="storage" :selected-group-id="selectedGroupId" :search-query="searchQuery" @group-selected="handleGroupSelected" />
                        </TabPanel>
                        <TabPanel value="preparation">
                            <IngredientList ref="preparationListRef" type="preparation" :selected-group-id="selectedGroupId" :search-query="searchQuery" @group-selected="handleGroupSelected" />
                        </TabPanel>
                    </TabPanels>
                </Tabs>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 배경 단순화: 페이지 배경(오렌지) → 하나의 흰 카드. 테두리는 Category.vue .category-selector와 동일 */
.ingredient-panel {
    background: var(--surface-card);
    border: 1px solid #fed7aa;
    border-radius: 12px;
    padding: 0;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
}

/* 검색 영역: Category.vue .category-selector와 동일한 패딩(1.5rem)으로 위치 맞춤 */
.top-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1.5rem;
    background: #fff7ed;
    border-bottom: 1px solid #fed7aa;
    flex-shrink: 0;
}

/* Category.vue 검색창과 동일한 길이 */
.search-bar {
    flex: 0 1 400px;
    max-width: 400px;
    min-width: 0;
}

.request-btn {
    flex-shrink: 0;
    margin-left: auto;
}

/* 탭/리스트 영역: 같은 카드 안에 이어지도록 테두리만 구분 (별도 흰 박스 제거) */
.tab-panel-wrap {
    padding: 0.5rem 0.5rem 0.5rem;
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

/* PrimeVue Tabs: 내부 박스 스타일 제거 → 카드와 한 블록으로 보이게 */
.ingredient-tabs {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    border: none;
    border-radius: 0;
    overflow: hidden;
    background: transparent;
}

.ingredient-tabs :deep(.p-tablist) {
    flex-shrink: 0;
    padding-left: 1rem;
    padding-right: 1rem;
    padding-top: 0.5rem;
}

.ingredient-tabs :deep(.p-tabpanels) {
    flex: 1;
    min-height: 0;
    padding-left: 1rem;
    padding-right: 1rem;
}

.ingredient-tabs :deep(.p-tabpanel) {
    min-height: 0;
    padding-top: 1rem;
}

@media (max-width: 768px) {
    .top-row {
        padding: 1rem; /* Category.vue .category-selector 반응형과 동일 */
    }

    .tab-panel-wrap {
        padding: 1rem;
    }

    .search-bar {
        max-width: 100%;
    }
}
</style>
