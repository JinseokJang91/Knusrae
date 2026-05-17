<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getPopularRecipes } from '@/api/recipeApi';
import RecipeListItem from '@/components/recipe/RecipeListItem.vue';
import type { PopularRecipeItem } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';
import { isEmptyDataError } from '@/utils/errorHandler';

const router = useRouter();

const { showError } = useAppToast();

// 상태
const popularRecipes = ref<PopularRecipeItem[]>([]);
const loading = ref(false);
const selectedPeriod = ref<'24h' | '7d' | '30d'>('24h');

// 기간 옵션 (랭킹 페이지와 동일한 라벨 사용)
const periodOptions = [
    { label: '일간', value: '24h' },
    { label: '주간', value: '7d' },
    { label: '월간', value: '30d' }
];

// 인기 레시피 로드
const loadPopularRecipes = async () => {
    loading.value = true;
    try {
        const recipes = await getPopularRecipes(10, selectedPeriod.value);
        popularRecipes.value = recipes;
    } catch (error) {
        if (isEmptyDataError(error)) {
            popularRecipes.value = [];
            return;
        }
        console.error('Failed to load popular recipes:', error);
        showError('인기 레시피를 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

// 기간 변경 핸들러
const changePeriod = (period: '24h' | '7d' | '30d') => {
    selectedPeriod.value = period;
    loadPopularRecipes();
};

// 전체 랭킹 페이지로 이동 (현재 선택 기간 유지)
function goToFullRanking() {
    router.push({ path: '/ranking', query: { period: selectedPeriod.value } });
}

// TOP 3 레시피 (순위 표시용)
const topThreeRecipes = computed(() => {
    return popularRecipes.value.slice(0, 3);
});

// 나머지 레시피
const remainingRecipes = computed(() => {
    return popularRecipes.value.slice(3);
});

// 트렌드 아이콘 가져오기
const getTrendIcon = (status: string) => {
    switch (status) {
        case 'UP':
            return 'pi-arrow-up text-orange-500';
        case 'DOWN':
            return 'pi-arrow-down text-red-500';
        case 'NEW':
            return 'pi-star text-yellow-500';
        default:
            return 'pi-minus text-gray-400';
    }
};

// 마운트 시 로드
onMounted(() => {
    loadPopularRecipes();
});
</script>

<template>
    <div class="popular-recipes-section">
        <!-- 헤더 -->
        <header class="popular-header">
            <div class="popular-header__intro">
                <h2 class="popular-title">지금 인기 레시피 TOP 🔥</h2>
                <p class="popular-desc">지금 가장 많은 사람들이 관심을 갖는 레시피를 확인해보세요</p>
            </div>

            <div class="popular-header__actions">
                <button type="button" class="btn-ranking" @click="goToFullRanking">전체 랭킹 보기</button>
                <div class="period-segment" role="tablist" aria-label="랭킹 기간">
                    <button
                        v-for="option in periodOptions"
                        :key="option.value"
                        type="button"
                        role="tab"
                        :aria-selected="selectedPeriod === option.value"
                        :class="['period-segment__btn', { 'is-active': selectedPeriod === option.value }]"
                        @click="changePeriod(option.value as '24h' | '7d' | '30d')"
                    >
                        {{ option.label }}
                    </button>
                </div>
            </div>
        </header>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
            <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
        </div>

        <!-- TOP 3 레시피 (큰 카드) -->
        <div v-else-if="topThreeRecipes.length > 0">
            <div class="top-three-grid">
                <div v-for="item in topThreeRecipes" :key="item.recipe.id" class="relative">
                    <!-- 순위 배지 -->
                    <div :class="['rank-badge', item.rank === 1 ? 'rank-badge--gold' : item.rank === 2 ? 'rank-badge--silver' : 'rank-badge--bronze']">
                        {{ item.rank }}
                    </div>

                    <!-- 트렌드 배지 -->
                    <div v-if="item.trendStatus !== 'SAME'" class="absolute top-4 right-4 z-10 bg-white rounded-full p-2 shadow-md">
                        <i :class="['pi', getTrendIcon(item.trendStatus)]"></i>
                    </div>

                    <!-- 레시피 카드 -->
                    <RecipeListItem :recipe="item.recipe" :show-stats="true" :show-author="true" />
                </div>
            </div>

            <!-- 나머지 레시피 (작은 리스트) -->
            <div v-if="remainingRecipes.length > 0" class="remaining-block">
                <h3 class="remaining-title">다른 인기 레시피</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <div v-for="item in remainingRecipes" :key="item.recipe.id" class="relative">
                        <!-- 순위 표시 -->
                        <div class="absolute top-2 left-2 z-10 bg-gray-800 bg-opacity-75 text-white rounded-full w-8 h-8 flex items-center justify-center text-sm font-bold">
                            {{ item.rank }}
                        </div>

                        <RecipeListItem :recipe="item.recipe" :show-stats="true" :show-author="false" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 데이터 없음 -->
        <div v-else class="popular-empty">
            <i class="pi pi-inbox popular-empty__icon"></i>
            <p class="popular-empty__msg">아직 인기 레시피가 없습니다.</p>
        </div>
    </div>
</template>

<style scoped lang="scss">
.popular-recipes-section {
    padding: 0;
}

.popular-header {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: 0.875rem;
    margin-bottom: 1.25rem;
}

.popular-header__intro {
    min-width: 0;
}

.popular-title {
    font-size: 1.5rem;
    font-weight: 700;
    line-height: 1.25;
    margin: 0 0 0.375rem;
    color: var(--text-color);
}

.popular-desc {
    margin: 0;
    font-size: 0.9375rem;
    line-height: 1.45;
    color: var(--text-color-secondary);
}

.popular-header__actions {
    display: flex;
    flex-direction: column;
    gap: 0.625rem;
    width: 100%;
}

.btn-ranking {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    padding: 0.5rem 0.75rem;
    font-size: 0.8125rem;
    font-weight: 600;
    line-height: 1.3;
    border-radius: 0.5rem;
    border: 1px solid var(--primary-color, #f97316);
    color: var(--primary-color, #f97316);
    background: transparent;
    cursor: pointer;
    transition:
        background 0.15s ease,
        color 0.15s ease;

    &:hover {
        background: rgba(249, 115, 22, 0.08);
    }
}

.period-segment {
    display: flex;
    gap: 0.375rem;
    width: 100%;
}

.period-segment__btn {
    flex: 1 1 0;
    min-height: 2.25rem;
    padding: 0.375rem 0.5rem;
    font-size: 0.8125rem;
    font-weight: 600;
    line-height: 1.2;
    border-radius: 0.5rem;
    border: none;
    cursor: pointer;
    background: var(--surface-100, #f3f4f6);
    color: var(--text-color-secondary);
    transition:
        background 0.15s ease,
        color 0.15s ease;

    &:hover {
        background: var(--surface-200, #e5e7eb);
    }

    &.is-active {
        background: var(--primary-color, #f97316);
        color: #fff;
    }
}

.top-three-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 1rem;
    margin-bottom: 1.5rem;
}

@media (min-width: 768px) {
    .top-three-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 1.5rem;
        margin-bottom: 2rem;
    }

    .popular-header {
        flex-direction: row;
        align-items: flex-start;
        justify-content: space-between;
        gap: 1rem 1.5rem;
        margin-bottom: 1.5rem;
    }

    .popular-title {
        font-size: 1.625rem;
    }

    .popular-desc {
        font-size: 1rem;
    }

    .popular-header__actions {
        flex-direction: row;
        flex-wrap: wrap;
        align-items: center;
        justify-content: flex-end;
        width: auto;
        max-width: 100%;
    }

    .btn-ranking {
        width: auto;
        padding: 0.5rem 1rem;
        font-size: 0.875rem;
    }

    .period-segment {
        width: auto;
        flex: 0 0 auto;
    }

    .period-segment__btn {
        flex: 0 1 auto;
        min-width: 3.25rem;
        padding: 0.5rem 0.875rem;
        font-size: 0.875rem;
    }
}

.rank-badge {
    position: absolute;
    top: 0.75rem;
    left: 0.75rem;
    z-index: 10;
    width: 2.5rem;
    height: 2.5rem;
    border-radius: 9999px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    font-weight: 700;
    color: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

    &--gold {
        background: #eab308;
    }

    &--silver {
        background: #9ca3af;
    }

    &--bronze {
        background: #ea580c;
    }
}

@media (min-width: 768px) {
    .rank-badge {
        top: 1rem;
        left: 1rem;
        width: 3rem;
        height: 3rem;
        font-size: 1.125rem;
    }
}

.remaining-block {
    margin-top: 1.5rem;
}

.remaining-title {
    font-size: 1rem;
    font-weight: 700;
    margin: 0 0 0.75rem;
    color: var(--text-color);
}

@media (min-width: 768px) {
    .remaining-block {
        margin-top: 2rem;
    }

    .remaining-title {
        font-size: 1.125rem;
        margin-bottom: 1rem;
    }
}

.popular-empty {
    text-align: center;
    padding: 3rem 1rem;

    &__icon {
        font-size: 3rem;
        color: var(--surface-300, #d1d5db);
        margin-bottom: 0.75rem;
    }

    &__msg {
        margin: 0;
        font-size: 0.875rem;
        color: var(--text-color-secondary);
    }
}

@media (max-width: 767px) {
    .popular-title {
        font-size: 1.125rem;
    }

    .popular-desc {
        font-size: 0.8125rem;
    }
}
</style>
