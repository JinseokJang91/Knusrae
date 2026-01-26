<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getTrendingCategories } from '@/api/categoryApi';
import CategorySection from './CategorySection.vue';
import type { CategoryInfo } from '@/types/category';
import { useAppToast } from '@/utils/toast';

const { showError } = useAppToast();

const trendingCategories = ref<CategoryInfo[]>([]);
const loading = ref(false);

const loadTrendingCategories = async () => {
    loading.value = true;
    try {
        const response = await getTrendingCategories(2, '30d');
        trendingCategories.value = response.categories;
    } catch (error) {
        console.error('트렌딩 카테고리 로딩 실패:', error);
        showError('카테고리를 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadTrendingCategories();
});
</script>

<template>
    <div class="category-sections">
        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
            <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
        </div>
        
        <!-- 카테고리 섹션들 -->
        <template v-else>
            <CategorySection
                v-for="category in trendingCategories"
                :key="`${category.codeId}-${category.detailCodeId}`"
                :category="category"
            />
        </template>
        
        <!-- 데이터 없음 -->
        <div v-if="!loading && trendingCategories.length === 0" class="text-center py-20">
            <i class="pi pi-inbox text-6xl text-gray-300 mb-4"></i>
            <p class="text-gray-500 text-lg">카테고리가 없습니다.</p>
        </div>
    </div>
</template>

<style scoped>
.category-sections {
    display: flex;
    flex-direction: column;
    gap: 40px;
}

@media (max-width: 768px) {
    .category-sections {
        gap: 28px;
    }
}
</style>
