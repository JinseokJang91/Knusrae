<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getPopularRecipes } from '@/api/recipeApi';
import RecipeListItem from '@/components/recipe/RecipeListItem.vue';
import type { PopularRecipeItem } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';

const route = useRoute();
const { showError } = useAppToast();

const RANKING_LIMIT = 50;
type PeriodValue = '24h' | '7d' | '30d';

const popularRecipes = ref<PopularRecipeItem[]>([]);
const loading = ref(false);
const selectedPeriod = ref<PeriodValue>('24h');
const calculatedAt = ref<string | null>(null);

const periodOptions: { label: string; value: PeriodValue }[] = [
    { label: '일간', value: '24h' },
    { label: '주간', value: '7d' },
    { label: '월간', value: '30d' }
];

function formatCalculatedAt(isoString?: string): string {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

const displayCalculatedAt = computed(() => formatCalculatedAt(calculatedAt.value ?? undefined));

async function loadPopularRecipes() {
    loading.value = true;
    try {
        const recipes = await getPopularRecipes(RANKING_LIMIT, selectedPeriod.value);
        popularRecipes.value = recipes;
        const first = recipes[0];
        calculatedAt.value = first?.calculatedAt ?? null;
    } catch (error) {
        console.error('Failed to load ranking:', error);
        showError('랭킹을 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
}

function changePeriod(period: PeriodValue) {
    selectedPeriod.value = period;
    loadPopularRecipes();
}

function getTrendIcon(status: string): string {
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
}

function getTrendLabel(status: string): string {
    switch (status) {
        case 'UP':
            return '순위 상승';
        case 'DOWN':
            return '순위 하락';
        case 'NEW':
            return '신규 진입';
        default:
            return '유지';
    }
}

function initPeriodFromQuery() {
    const period = route.query.period as string | undefined;
    if (period === '24h' || period === '7d' || period === '30d') {
        selectedPeriod.value = period;
    }
}

onMounted(() => {
    initPeriodFromQuery();
    loadPopularRecipes();
});

watch(
    () => route.query.period,
    () => {
        initPeriodFromQuery();
        loadPopularRecipes();
    }
);
</script>

<template>
    <div class="ranking-page">
        <section class="ranking-header">
            <h1 class="ranking-title">랭킹</h1>
            <p class="ranking-subtitle">지금 많은 사람들이 보고 있는 레시피를 기간별로 확인하세요.</p>

            <div class="ranking-tabs">
                <button v-for="option in periodOptions" :key="option.value" type="button" :class="['tab-button', selectedPeriod === option.value ? 'tab-button-active' : 'tab-button-inactive']" @click="changePeriod(option.value)">
                    {{ option.label }}
                </button>
            </div>

            <p v-if="displayCalculatedAt" class="calculated-at">기준: {{ displayCalculatedAt }} 갱신</p>
        </section>

        <section class="ranking-content">
            <div v-if="loading" class="loading-state">
                <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
                <p class="mt-4 text-gray-600">랭킹을 불러오는 중...</p>
            </div>

            <div v-else-if="popularRecipes.length > 0" class="ranking-list">
                <div v-for="item in popularRecipes" :key="item.recipe.id" class="ranking-row">
                    <div class="rank-cell">
                        <span :class="['rank-badge', item.rank === 1 ? 'rank-1' : item.rank === 2 ? 'rank-2' : item.rank === 3 ? 'rank-3' : 'rank-other']"> {{ item.rank }}위 </span>
                        <span v-if="item.trendStatus !== 'SAME'" class="trend-badge" :title="getTrendLabel(item.trendStatus)">
                            <i :class="['pi', getTrendIcon(item.trendStatus)]"></i>
                        </span>
                    </div>
                    <div class="recipe-cell">
                        <RecipeListItem :recipe="item.recipe" :show-stats="true" :show-author="true" />
                    </div>
                </div>
            </div>

            <div v-else class="empty-state">
                <i class="pi pi-inbox text-6xl text-gray-300 mb-4"></i>
                <p class="text-gray-500 text-lg">아직 랭킹 데이터가 없습니다.</p>
            </div>
        </section>

        <section class="ranking-footer">
            <router-link to="/" class="link-to-main"> 메인 인기 TOP 보기 </router-link>
        </section>
    </div>
</template>

<style scoped>
.ranking-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 16px;
}

.ranking-header {
    margin-bottom: 32px;
}

.ranking-title {
    font-size: 2rem;
    font-weight: 700;
    color: var(--text-color);
    margin-bottom: 8px;
}

.ranking-subtitle {
    font-size: 1rem;
    color: var(--text-color-secondary);
    margin-bottom: 24px;
}

.ranking-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
}

.tab-button {
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 600;
    transition:
        background-color 0.2s,
        color 0.2s;
    border: none;
    cursor: pointer;
}

.tab-button-active {
    background: var(--primary-500);
    color: white;
}

.tab-button-inactive {
    background: var(--surface-100);
    color: var(--text-color-secondary);
}

.tab-button-inactive:hover {
    background: var(--surface-200);
    color: var(--text-color);
}

.calculated-at {
    font-size: 0.875rem;
    color: var(--text-color-secondary);
    margin: 0;
}

.ranking-content {
    min-height: 200px;
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 24px;
}

.ranking-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.ranking-row {
    display: flex;
    align-items: flex-start;
    gap: 16px;
}

.rank-cell {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 80px;
    padding-top: 8px;
}

.rank-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 48px;
    height: 32px;
    padding: 0 8px;
    border-radius: 8px;
    font-weight: 700;
    font-size: 0.95rem;
    color: white;
}

.rank-1 {
    background: linear-gradient(135deg, #eab308, #ca8a04);
}

.rank-2 {
    background: linear-gradient(135deg, #6b7280, #4b5563);
}

.rank-3 {
    background: linear-gradient(135deg, #ea580c, #c2410c);
}

.rank-other {
    background: var(--surface-300);
    color: var(--text-color);
}

.trend-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: var(--surface-100);
}

.recipe-cell {
    flex: 1;
    min-width: 0;
}

.empty-state {
    text-align: center;
    padding: 48px 24px;
}

.ranking-footer {
    margin-top: 40px;
    padding-top: 24px;
    border-top: 1px solid var(--surface-border);
    text-align: center;
}

.link-to-main {
    color: var(--primary-500);
    font-weight: 600;
    text-decoration: none;
}

.link-to-main:hover {
    text-decoration: underline;
}

@media (max-width: 768px) {
    .ranking-row {
        flex-direction: column;
        gap: 12px;
    }

    .rank-cell {
        min-width: auto;
        padding-top: 0;
    }
}
</style>
