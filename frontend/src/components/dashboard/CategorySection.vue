<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getCategoryRecipes } from '@/api/categoryApi';
import RecipeCard from '@/components/recipe/RecipeCard.vue';
import Badge from 'primevue/badge';
import type { CategoryInfo } from '@/types/category';
import type { Recipe } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';
import { isEmptyDataError } from '@/utils/errorHandler';

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
        PASTA: 'pi pi-star',
        KOREAN: 'pi pi-heart',
        DESSERT: 'pi pi-gift',
        CHINESE: 'pi pi-sparkles',
        JAPANESE: 'pi pi-sun',
        WESTERN: 'pi pi-crown'
    };
    return icons[props.category.detailCodeId] || 'pi pi-bookmark';
});

// 레시피 로드
const loadRecipes = async () => {
    loading.value = true;
    try {
        const response = await getCategoryRecipes(props.category.codeId, props.category.detailCodeId, 12, 'mixed');
        recipes.value = response.recipes;
    } catch (error) {
        if (isEmptyDataError(error)) {
            recipes.value = [];
            return;
        }
        console.error('카테고리 레시피 로딩 실패:', error);
        showError('카테고리 레시피를 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

// 카테고리 페이지로 이동
const goToCategory = () => {
    router.push(`/recipe/category/${props.category.codeId}/${props.category.detailCodeId}`);
};

onMounted(() => {
    loadRecipes();
});
</script>

<template>
    <div class="category-section">
        <div class="section-header">
            <div class="category-title-area">
                <i :class="categoryIcon" class="category-icon" aria-hidden="true"></i>
                <div class="category-main">
                    <div class="category-title-row">
                        <h2 class="section-title">{{ category.detailName }}</h2>
                        <button type="button" class="category-page-link" @click="goToCategory">
                            <span class="category-page-link__label">카테고리 페이지</span>
                            <i class="pi pi-arrow-right category-page-link__icon" aria-hidden="true"></i>
                        </button>
                    </div>
                    <p class="section-subtitle">
                        <Badge v-if="category.reason" :value="category.reason" />
                        {{ category.recipeCount ? `${category.recipeCount}개의 레시피` : '' }}
                    </p>
                </div>
            </div>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
            <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
        </div>

        <!-- 레시피 그리드 -->
        <div v-else-if="recipes.length > 0" class="recipes-grid">
            <RecipeCard v-for="recipe in recipes" :key="recipe.id" :recipe="recipe" :show-stats="true" />
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
    margin-bottom: 24px;
}

.category-title-area {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.category-main {
    flex: 1;
    min-width: 0;
}

.category-title-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 0.5rem;
}

.category-page-link {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    margin: 0;
    padding: 0;
    border: none;
    background: none;
    cursor: pointer;
    font-family: inherit;
    font-size: 0.75rem;
    font-weight: 500;
    line-height: 1.3;
    color: var(--primary-color, #f97316);
    white-space: nowrap;
    -webkit-tap-highlight-color: transparent;
}

.category-page-link:hover {
    text-decoration: underline;
}

.category-page-link:focus-visible {
    outline: 2px solid var(--primary-color, #f97316);
    outline-offset: 2px;
    border-radius: 4px;
}

.category-page-link__icon {
    font-size: 0.65rem;
    opacity: 0.9;
}

@media (min-width: 769px) {
    .category-page-link {
        font-size: 0.8125rem;
    }

    .category-page-link__icon {
        font-size: 0.7rem;
    }
}

.category-icon {
    font-size: 32px;
    color: var(--primary-color);
}

.section-title {
    flex: 1;
    min-width: 0;
    font-size: 1.5rem;
    font-weight: 700;
    line-height: 1.3;
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
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
}

@media (max-width: 1024px) {
    .recipes-grid {
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
        gap: 16px;
    }

    /* 대시보드 다른 섹션 헤더(1.125rem)와 동일 트랙 */
    .section-title {
        font-size: 1.125rem;
        line-height: 1.35;
    }

    .category-icon {
        font-size: 1.375rem;
    }
}

@media (max-width: 768px) {
    .category-section {
        padding: 1rem 0;
    }

    .section-title {
        font-size: 1.0625rem;
        line-height: 1.35;
    }

    .section-subtitle {
        font-size: 0.8125rem;
    }

    .category-icon {
        font-size: 1.25rem;
    }

    .category-title-area {
        gap: 0.625rem;
    }

    .category-page-link {
        font-size: 0.6875rem;
        margin-top: 0.125rem;
    }

    .category-page-link__icon {
        font-size: 0.6rem;
    }

    /* 360~430px에서 2열은 가능할 때만(auto-fill), 좁으면 1열 */
    .recipes-grid {
        grid-template-columns: repeat(auto-fill, minmax(158px, 1fr));
        gap: 0.875rem;
    }
}

@media (max-width: 480px) {
    .recipes-grid {
        display: flex;
        overflow-x: auto;
        gap: 0.875rem;
        scroll-snap-type: x mandatory;
        scroll-padding-inline: 0;
        padding-bottom: 8px;
        overscroll-behavior-x: contain;
        -webkit-overflow-scrolling: touch;
    }

    .recipes-grid > * {
        flex: 0 0 min(240px, calc(100vw - 3.25rem));
        scroll-snap-align: start;
    }
}
</style>
