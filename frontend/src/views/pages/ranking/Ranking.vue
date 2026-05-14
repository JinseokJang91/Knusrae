<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import { getPopularRecipes, toggleFavorite as toggleFavoriteApi } from '@/api/recipeApi';
import { getRecipeBooks, getBookmarksByRecipeBook } from '@/api/bookmarkApi';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import BookmarkDialog from '@/components/bookmark/BookmarkDialog.vue';
import { useAuthStore } from '@/stores/authStore';
import type { PopularRecipeItem, RecipeCookingTip } from '@/types/recipe';
import type { RecipeGridItem } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';

/** cookingTips에서 요리 시간/인분 추출 (Category와 동일) */
function extractCookingTime(cookingTips: RecipeCookingTip[] | undefined): string | null {
    if (!cookingTips || !Array.isArray(cookingTips)) return null;
    const tip = cookingTips.find((t) => t.codeId === 'COOKING_TIME');
    return tip?.detailName ?? null;
}
function extractServings(cookingTips: RecipeCookingTip[] | undefined): string | null {
    if (!cookingTips || !Array.isArray(cookingTips)) return null;
    const tip = cookingTips.find((t) => t.codeId === 'SERVINGS');
    return tip?.detailName ?? null;
}

/** RecipeGridCard에 넘길 레시피 (cookingTime/servings 포함, 전체 레시피 화면과 동일 형태) */
function getCardRecipe(item: PopularRecipeItem): RecipeGridItem {
    const r = item.recipe;
    return {
        ...r,
        cookingTime: r.cookingTime ?? extractCookingTime(r.cookingTips) ?? undefined,
        servings: r.servings ?? extractServings(r.cookingTips) ?? undefined
    };
}

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { showError, showWarn } = useAppToast();

const RANKING_LIMIT = 50;
const INITIAL_DISPLAY_COUNT = 12; // 1열 기준 첫 화면 분량
const LOAD_MORE_STEP = 12;
type PeriodValue = '24h' | '7d' | '30d';

const popularRecipes = ref<PopularRecipeItem[]>([]);
const displayCount = ref(INITIAL_DISPLAY_COUNT);
const loading = ref(false);
const loadError = ref(false);
const selectedPeriod = ref<PeriodValue>('24h');
const calculatedAt = ref<string | null>(null);
const skipLoadFromWatch = ref(false);

const displayedRecipes = computed(() => popularRecipes.value.slice(0, displayCount.value));
const hasMoreRecipes = computed(() => displayCount.value < popularRecipes.value.length);

// 북마크 Dialog 및 카드 상태 (Category와 동일)
const bookmarkDialogVisible = ref(false);
const bookmarkRecipeId = ref<number | null>(null);
const bookmarkedRecipeIds = ref<Set<number>>(new Set());

const periodOptions: { label: string; value: PeriodValue }[] = [
    { label: '일간 랭킹', value: '24h' },
    { label: '주간 랭킹', value: '7d' },
    { label: '월간 랭킹', value: '30d' }
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
    loadError.value = false;
    displayCount.value = INITIAL_DISPLAY_COUNT;
    try {
        const recipes = await getPopularRecipes(RANKING_LIMIT, selectedPeriod.value);
        popularRecipes.value = recipes;
        const first = recipes[0];
        calculatedAt.value = first?.calculatedAt ?? null;
    } catch (error) {
        console.error('Failed to load ranking:', error);
        loadError.value = true;
        showError('랭킹을 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
}

function loadMoreRecipes() {
    displayCount.value = Math.min(displayCount.value + LOAD_MORE_STEP, popularRecipes.value.length);
}

// 기간(탭) 변경 시 데이터 로드 및 URL 쿼리 동기화 (쿼리 복원으로 인한 변경 시에는 onMounted/route watch에서 이미 로드하므로 스킵)
// 같은 path에서 query만 바꿀 때: path 명시 + 새 query 객체 + nextTick으로 URL이 갱신되도록 함 (Vue Router 동작)
watch(selectedPeriod, (period) => {
    if (skipLoadFromWatch.value) {
        skipLoadFromWatch.value = false;
        return;
    }
    loadPopularRecipes();
    const nextQuery = { ...route.query, period };
    nextTick(() => {
        router.replace({ path: '/ranking', query: nextQuery });
    });
});

/** 1~3위 메달 이모지 (금·은·동) */
function getRankMedal(rank: number): string {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return '';
}

/** 카드용 대표 카테고리 라벨 */
function getCategoryLabel(item: PopularRecipeItem): string | null {
    const first = item.recipe.categories?.[0];
    return first?.detailName ?? first?.codeName ?? null;
}

function viewRecipe(recipeId: number): void {
    router.push(`/recipe/${recipeId}`);
}

function scrollToComments(recipeId: number): void {
    router.push(`/recipe/${recipeId}#comments`);
}

async function toggleFavorite(recipeId: number): Promise<void> {
    if (!authStore.memberInfo?.id) return;
    const item = popularRecipes.value.find((r) => r.recipe.id === recipeId);
    if (!item) return;
    try {
        const response = await toggleFavoriteApi(authStore.memberInfo.id, recipeId);
        item.recipe.isFavorite = response.isFavorite;
    } catch (err) {
        console.error('찜 토글 실패:', err);
    }
}

function bookmarkRecipe(recipeId: number): void {
    if (!authStore.memberInfo?.id) {
        showWarn('로그인이 필요한 기능입니다.');
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } });
        return;
    }
    bookmarkRecipeId.value = recipeId;
    bookmarkDialogVisible.value = true;
}

async function onBookmarked(): Promise<void> {
    await loadBookmarkedRecipeIds();
}

async function loadBookmarkedRecipeIds(): Promise<void> {
    if (!authStore.memberInfo?.id) {
        bookmarkedRecipeIds.value = new Set();
        return;
    }
    try {
        const recipeBooks = await getRecipeBooks();
        const ids = new Set<number>();
        for (const recipeBook of recipeBooks) {
            const bookmarks = await getBookmarksByRecipeBook(recipeBook.id);
            bookmarks.forEach((b) => ids.add(b.recipeId));
        }
        bookmarkedRecipeIds.value = ids;
    } catch {
        bookmarkedRecipeIds.value = new Set();
    }
}

function initPeriodFromQuery() {
    const period = route.query.period as string | undefined;
    if (period === '24h' || period === '7d' || period === '30d') {
        skipLoadFromWatch.value = true;
        selectedPeriod.value = period;
    }
}

onMounted(() => {
    initPeriodFromQuery();
    loadPopularRecipes();
    loadBookmarkedRecipeIds();
});

// URL 쿼리 변경 시(뒤로가기 등) 기간 복원 후, 실제로 기간이 바뀐 경우에만 로드 (탭 클릭으로 인한 URL 변경 시 이중 로드 방지)
watch(
    () => route.query.period,
    () => {
        const prev = selectedPeriod.value;
        initPeriodFromQuery();
        if (selectedPeriod.value !== prev) {
            loadPopularRecipes();
        }
        // 다음 탭 클릭에서 URL 갱신이 스킵되지 않도록 플래그 해제 (initPeriodFromQuery에서 true로 설정됨)
        nextTick(() => {
            skipLoadFromWatch.value = false;
        });
    }
);
</script>

<template>
    <div class="page-container page-container--card">
        <div class="ranking-page">
            <section class="ranking-header">
                <div class="ranking-subtitle">
                    <i class="fa-solid fa-ranking-star ranking-subtitle__icon" aria-hidden="true"></i>
                    <span class="ranking-subtitle__text">지금 많은 사람들이 보고 있는 레시피를 기간별로 확인하세요.</span>
                </div>
                <div class="ranking-tabs-wrap">
                    <Tabs v-model:value="selectedPeriod" class="ranking-tabs">
                        <TabList>
                            <Tab v-for="option in periodOptions" :key="option.value" :value="option.value">
                                {{ option.label }}
                            </Tab>
                        </TabList>
                        <p v-if="displayCalculatedAt" class="ranking-calculated-at">기준: {{ displayCalculatedAt }} 갱신</p>
                        <TabPanels>
                            <TabPanel v-for="option in periodOptions" :key="option.value" :value="option.value">
                                <div class="ranking-content">
                                    <div v-if="loading" class="loading-state">
                                        <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
                                        <p class="mt-4 text-gray-600">랭킹을 불러오는 중...</p>
                                    </div>

                                    <div v-else-if="loadError" class="error-state">
                                        <i class="pi pi-exclamation-triangle text-6xl text-amber-500 mb-4"></i>
                                        <p class="text-gray-700 text-lg mb-4">랭킹을 불러오는데 실패했습니다.</p>
                                        <button type="button" class="retry-button" @click="loadPopularRecipes">다시 시도</button>
                                    </div>

                                    <div v-else-if="popularRecipes.length > 0" class="ranking-list-wrap">
                                        <div class="recipe-grid ranking-grid">
                                            <div v-for="item in displayedRecipes" :key="item.recipe.id" class="ranking-card-wrap">
                                                <span :class="['rank-badge rank-badge--card', item.rank === 1 ? 'rank-1' : item.rank === 2 ? 'rank-2' : item.rank === 3 ? 'rank-3' : 'rank-other']">
                                                    <span v-if="getRankMedal(item.rank)" class="rank-medal-emoji" aria-hidden="true">{{ getRankMedal(item.rank) }}</span>
                                                    <span v-else class="rank-medal-emoji" aria-hidden="true">🏅</span>
                                                    <span class="rank-text">{{ item.rank }}위</span>
                                                </span>
                                                <RecipeGridCard
                                                    :recipe="getCardRecipe(item)"
                                                    :category-label="getCategoryLabel(item)"
                                                    :is-bookmarked="bookmarkedRecipeIds.has(item.recipe.id)"
                                                    show-bookmark
                                                    show-comment-count
                                                    @click="viewRecipe"
                                                    @favorite="toggleFavorite"
                                                    @bookmark="bookmarkRecipe"
                                                    @scroll-to-comments="scrollToComments"
                                                />
                                            </div>
                                        </div>
                                        <button v-if="hasMoreRecipes" type="button" class="ranking-btn-more" @click="loadMoreRecipes">더보기</button>
                                    </div>

                                    <div v-else class="empty-state">
                                        <i class="pi pi-inbox text-6xl text-gray-300 mb-4"></i>
                                        <p class="text-gray-500 text-lg mb-2">아직 랭킹 데이터가 없습니다.</p>
                                        <p class="text-gray-400 text-sm">다른 기간(일간/주간/월간)을 선택해 보세요.</p>
                                    </div>
                                </div>
                            </TabPanel>
                        </TabPanels>
                    </Tabs>
                </div>
            </section>
        </div>
        <BookmarkDialog v-model:visible="bookmarkDialogVisible" :recipe-id="bookmarkRecipeId" @bookmarked="onBookmarked" />
    </div>
</template>

<style scoped>
.ranking-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 16px;
}

@media (max-width: 768px) {
    .ranking-page {
        padding: 16px 12px;
    }
}

.ranking-header {
    margin-bottom: 32px;
}

@media (max-width: 768px) {
    .ranking-header {
        margin-bottom: 20px;
    }
}

.ranking-subtitle {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin: 0 0 16px 0;
    padding: 12px 14px;
    background: linear-gradient(135deg, var(--primary-50, #f0f9ff) 0%, var(--primary-100, #e0f2fe) 100%);
    border: 1px solid var(--primary-200, #bae6fd);
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

:root.dark .ranking-subtitle,
:global(.dark) .ranking-subtitle {
    background: linear-gradient(135deg, color-mix(in srgb, var(--primary-500) 15%, transparent) 0%, color-mix(in srgb, var(--primary-500) 8%, transparent) 100%);
    border-color: var(--primary-700, rgba(255, 255, 255, 0.1));
    box-shadow: none;
}

.ranking-subtitle__icon {
    flex-shrink: 0;
    font-size: 1.1rem;
    color: var(--primary-600, var(--primary-color));
    margin-top: 0.1rem;
}

.ranking-subtitle__text {
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--primary-900, var(--text-color));
    letter-spacing: -0.02em;
    line-height: 1.45;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-width: 0;
    flex: 1;
}

@media (min-width: 769px) {
    .ranking-subtitle {
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;
        padding: 14px 18px;
    }

    .ranking-subtitle__icon {
        font-size: 1.25rem;
        margin-top: 0;
    }

    .ranking-subtitle__text {
        font-size: 1rem;
        -webkit-line-clamp: unset;
        line-clamp: unset;
        display: block;
        overflow: visible;
    }
}

.ranking-tabs-wrap {
    margin-bottom: 0;
}

.ranking-tabs {
    border: none;
    border-radius: 0;
    background: transparent;
}

/* 탭: 리스트 전체 너비에 3등분 + 인디케이터(ink bar)가 각 탭 영역과 정렬되도록 */
.ranking-tabs :deep(.p-tablist) {
    width: 100%;
    margin-bottom: 0;
}

.ranking-tabs :deep(.p-tablist-content) {
    width: 100%;
    overflow: hidden;
}

.ranking-tabs :deep(.p-tablist-tab-list) {
    display: flex;
    width: 100%;
    align-items: stretch;
    position: relative;
}

.ranking-tabs :deep(.p-tab) {
    flex: 1 1 0;
    min-width: 0;
    justify-content: center;
    text-align: center;
    font-size: 0.8125rem;
    padding: 0.65rem 0.35rem;
}

@media (min-width: 480px) {
    .ranking-tabs :deep(.p-tab) {
        font-size: 0.875rem;
        padding: 0.75rem 0.5rem;
    }
}

.ranking-tabs :deep(.p-tabpanel) {
    padding: 0;
}

.ranking-calculated-at {
    font-size: 0.7rem;
    color: var(--text-color-secondary);
    margin: 6px 0 10px 0;
    text-align: right;
    line-height: 1.35;
}

@media (min-width: 640px) {
    .ranking-calculated-at {
        font-size: 0.75rem;
        margin-bottom: 12px;
    }
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

.ranking-list-wrap {
    margin-top: 0;
}

/* 1열·큰 썸네일: _recipe-card-list.scss 의 .recipe-grid.ranking-grid 와 함께 사용 */
.ranking-grid {
    margin-top: 0.35rem;
}

.ranking-btn-more {
    display: block;
    width: 100%;
    max-width: 320px;
    margin: 28px auto 0;
    padding: 14px 24px;
    background: var(--surface-card);
    color: var(--text-color);
    font-size: 1rem;
    font-weight: 600;
    border: 2px solid var(--surface-border);
    border-radius: 12px;
    cursor: pointer;
    transition:
        background 0.2s,
        border-color 0.2s,
        transform 0.15s;
}

.ranking-btn-more:hover {
    background: var(--primary-50, #f0f9ff);
    border-color: var(--primary-300, #7dd3fc);
    transform: translateY(-1px);
}

.ranking-card-wrap {
    position: relative;
    container-type: inline-size;
    container-name: recipe-card;
}

.rank-badge.rank-badge--card {
    position: absolute;
    top: 12px;
    left: 12px;
    z-index: 2;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 48px;
    height: 32px;
    padding: 0 10px;
    border-radius: 10px;
    font-weight: 700;
    font-size: 0.95rem;
    color: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    pointer-events: none;
    overflow: hidden;
}

.rank-badge.rank-badge--card .pi {
    font-size: 0.85rem;
}

.rank-badge.rank-badge--card .rank-medal-emoji {
    margin-right: 4px;
    font-size: 1.1em;
    line-height: 1;
}

/* 1~3위: 금·은·동 메달 톤 + 은은한 반짝임 */
.rank-1,
.rank-2,
.rank-3 {
    position: relative;
}

.rank-1::after,
.rank-2::after,
.rank-3::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent 0%, transparent 40%, rgba(255, 255, 255, 0.25) 50%, transparent 60%, transparent 100%);
    background-size: 200% 100%;
    animation: rank-shine 3.5s ease-in-out infinite;
    pointer-events: none;
}

@keyframes rank-shine {
    0% {
        background-position: 100% 0;
    }
    28% {
        background-position: -100% 0;
    }

    28.01%,
    100% {
        background-position: -100% 0;
    }
}

.rank-1 {
    background: linear-gradient(135deg, #fde047, #d4af37);
}

.rank-2 {
    background: linear-gradient(135deg, #d1d5db, #6b7280);
}

.rank-3 {
    background: linear-gradient(135deg, #d97706, #b45309);
}

/* 4위~ : 앱 톤에 맞춘 밝은 오렌지 */
.rank-other {
    background: linear-gradient(135deg, #f97316, #ea580c);
    color: white;
}

.error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 48px 24px;
}

.retry-button {
    padding: 10px 24px;
    border-radius: 8px;
    font-weight: 600;
    background: var(--primary-500);
    color: white;
    border: none;
    cursor: pointer;
    transition: background-color 0.2s;
}

.retry-button:hover {
    background: var(--primary-600);
}

.empty-state {
    text-align: center;
    padding: 48px 24px;
}

@media (max-width: 768px) {
    .rank-badge.rank-badge--card {
        top: 8px;
        left: 8px;
        min-width: 42px;
        height: 28px;
        font-size: 0.875rem;
    }
}
</style>
