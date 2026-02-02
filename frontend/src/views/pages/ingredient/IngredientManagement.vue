<template>
    <div class="ingredient-panel">
        <div class="tab-headers">
            <button
                type="button"
                class="tab-header"
                :class="{ 'tab-header--active': activeTab === 'storage' }"
                :aria-pressed="activeTab === 'storage'"
                :aria-selected="activeTab === 'storage'"
                @click="activeTab = 'storage'"
            >
                <span>재료 보관법</span>
            </button>
            <button
                type="button"
                class="tab-header"
                :class="{ 'tab-header--active': activeTab === 'preparation' }"
                :aria-pressed="activeTab === 'preparation'"
                :aria-selected="activeTab === 'preparation'"
                @click="activeTab = 'preparation'"
            >
                <i class="pi pi-cut" aria-hidden="true"></i>
                <span>재료 손질법</span>
            </button>
        </div>

        <div class="tab-content">
            <section
                v-show="activeTab === 'storage'"
                class="tab-panel"
                aria-label="재료 보관법"
                :aria-hidden="activeTab !== 'storage'"
            >
                <IngredientList
                    type="storage"
                    :selected-group-id="selectedGroupId"
                    :search-query="searchQuery"
                    @group-selected="handleGroupSelected"
                    @search-changed="handleSearchChanged"
                />
            </section>
            <section
                v-show="activeTab === 'preparation'"
                class="tab-panel"
                aria-label="재료 손질법"
                :aria-hidden="activeTab !== 'preparation'"
            >
                <IngredientList
                    type="preparation"
                    :selected-group-id="selectedGroupId"
                    :search-query="searchQuery"
                    @group-selected="handleGroupSelected"
                    @search-changed="handleSearchChanged"
                />
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import IngredientList from '@/components/ingredient/IngredientList.vue';

const route = useRoute();
const router = useRouter();

const activeTab = ref<'storage' | 'preparation'>('storage');
const selectedGroupId = ref<number | null>(null);
const searchQuery = ref('');

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
/* Category.vue 카테고리 영역(.category-selector)과 동일한 테두리/radius */
.ingredient-panel {
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    padding: 1.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: column;
    min-height: 0;
}

.tab-headers {
    display: flex;
    gap: 0;
    border-bottom: 2px solid var(--surface-border);
    margin-bottom: 1rem;
    flex-shrink: 0;
}

.tab-header {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    padding: 1rem 1.5rem;
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-color-secondary);
    background: transparent;
    border: none;
    border-bottom: 3px solid transparent;
    cursor: pointer;
    transition: color 0.2s, background 0.2s, border-color 0.2s;
}

.tab-header--active {
    color: #fff;
    border-bottom-color: #ea580c;
    background: #f97316;
}

.tab-header__badge {
    font-size: 0.75rem;
    font-weight: 500;
    padding: 0.2rem 0.5rem;
    border-radius: 9999px;
    background: var(--primary-color);
    color: var(--primary-color-text);
}

.tab-content {
    flex: 1;
    min-height: 0;
}

.tab-panel {
    min-height: 0;
    padding: 1rem 0 0;
}

@media (max-width: 768px) {
    .ingredient-panel {
        padding: 1rem;
    }

    .tab-header {
        padding: 0.75rem 1rem;
        font-size: 0.9rem;
    }
}
</style>

