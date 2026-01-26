<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getCategoryRecipes } from '@/api/categoryApi';
import RecipeCard from '@/components/recipe/RecipeCard.vue';
import Button from 'primevue/button';
import Badge from 'primevue/badge';
import type { CategoryInfo } from '@/types/category';
import type { Recipe } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';

const props = defineProps<{
    category: CategoryInfo;
}>();

const { showError } = useAppToast();
const router = useRouter();

const recipes = ref<Recipe[]>([]);
const loading = ref(false);

// 카테고리별 아이콘 매핑
const categoryIcon = computed(() => {
    const icons: Record<string, string> = {
        'PASTA': 'pi pi-star',
        'KOREAN': 'pi pi-heart',
        'DESSERT': 'pi pi-gift',
        'CHINESE': 'pi pi-sparkles',
        'JAPANESE': 'pi pi-sun',
        'WESTERN': 'pi pi-crown',
    };
    return icons[props.category.detailCodeId] || 'pi pi-bookmark';
});

// 레시피 로드
const loadRecipes = async () => {
    loading.value = true;
    try {
        const response = await getCategoryRecipes(
            props.category.codeId,
            props.category.detailCodeId,
            12,
            'mixed'
        );
        recipes.value = response.recipes;
    } catch (error) {
        console.error('카테고리 레시피 로딩 실패:', error);
        showError('카테고리 레시피를 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

// 카테고리 페이지로 이동
const goToCategory = () => {
    router.push(`/recipes/category/${props.category.codeId}/${props.category.detailCodeId}`);
};

onMounted(() => {
    loadRecipes();
});
</script>

<template>
    <div class="category-section">
        <div class="section-header">
            <div class="category-title-area">
                <i :class="categoryIcon" class="category-icon"></i>
                <div>
                    <h2 class="section-title">{{ category.detailName }}</h2>
                    <p class="section-subtitle">
                        <Badge v-if="category.reason" :value="category.reason" />
                        {{ category.recipeCount ? `${category.recipeCount}개의 레시피` : '' }}
                    </p>
                </div>
            </div>
            <Button 
                label="카테고리 페이지" 
                icon="pi pi-arrow-right"
                icon-pos="right"
                @click="goToCategory"
                text
            />
        </div>
        
        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
            <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
        </div>
        
        <!-- 레시피 그리드 -->
        <div v-else-if="recipes.length > 0" class="recipes-grid">
            <RecipeCard
                v-for="recipe in recipes"
                :key="recipe.id"
                :recipe="recipe"
                :show-stats="true"
            />
        </div>
        
        <!-- 데이터 없음 -->
        <div v-else class="text-center py-10">
            <i class="pi pi-inbox text-4xl text-gray-300 mb-2"></i>
            <p class="text-gray-500">레시피가 없습니다.</p>
        </div>
    </div>
</template>

<style scoped>
.category-section {
    padding: 0;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.category-title-area {
    display: flex;
    align-items: center;
    gap: 12px;
}

.category-icon {
    font-size: 32px;
    color: var(--primary-color);
}

.section-title {
    font-size: 24px;
    font-weight: 700;
    margin: 0;
    color: var(--text-color);
}

.section-subtitle {
    font-size: 14px;
    color: var(--text-color-secondary);
    margin: 4px 0 0 0;
    display: flex;
    align-items: center;
    gap: 8px;
}

.recipes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

@media (max-width: 1024px) {
    .recipes-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 16px;
    }
}

@media (max-width: 768px) {
    .category-section {
        padding: 1rem 0;
    }
    
    .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }
    
    .section-title {
        font-size: 20px;
    }
    
    .recipes-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 12px;
    }
}

@media (max-width: 480px) {
    .recipes-grid {
        display: flex;
        overflow-x: auto;
        gap: 12px;
        scroll-snap-type: x mandatory;
        padding-bottom: 8px;
    }
    
    .recipes-grid > * {
        flex: 0 0 240px;
        scroll-snap-align: start;
    }
}
</style>
