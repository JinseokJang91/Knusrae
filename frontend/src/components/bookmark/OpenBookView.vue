<template>
    <div class="open-book-view">
        <div class="bookmarks-header">
            <div>
                <h2 class="text-2xl font-semibold m-0 flex items-center gap-3">
                    <i
                        class="pi pi-bookmark"
                        :style="{ color: getRecipeBookColorHex(recipeBook.color) }"
                    ></i>
                    {{ recipeBook.name }}
                </h2>
                <p class="text-gray-600 mt-1">{{ recipeBook.bookmarkCount }}개의 레시피</p>
            </div>
        </div>

        <div class="open-book-view__book">
            <BookFrame>
                <template #left>
                    <div class="open-book-view__page-content">
                        <template v-if="leftBookmark">
                            <RecipeGridCard
                                :recipe="getRecipeGridItem(leftBookmark)"
                                :category-label="getCategoryName(leftBookmark.recipe)"
                                :date-text="formatDate(leftBookmark.createdAt)"
                                :show-favorite="false"
                                :show-bookmark="false"
                                :show-comment-count="false"
                                :show-author="false"
                                @click="onRecipeClick"
                            />
                            <div class="open-book-view__memo" @click.stop>
                                <button
                                    type="button"
                                    class="open-book-view__memo-trigger"
                                    @click="openMemoDialog(leftBookmark)"
                                >
                                    <i class="pi pi-pencil text-sm"></i>
                                    <span v-if="leftBookmark.memo" class="open-book-view__memo-text">{{ leftBookmark.memo }}</span>
                                    <span v-else class="open-book-view__memo-placeholder">메모 추가</span>
                                </button>
                            </div>
                        </template>
                        <div v-else class="open-book-view__empty-page">
                            <i class="pi pi-book text-4xl text-gray-300 mb-2"></i>
                            <p class="text-gray-500 text-sm">이 페이지는 비어 있어요</p>
                        </div>
                    </div>
                </template>
                <template #right>
                    <div class="open-book-view__page-content">
                        <template v-if="rightBookmark">
                            <RecipeGridCard
                                :recipe="getRecipeGridItem(rightBookmark)"
                                :category-label="getCategoryName(rightBookmark.recipe)"
                                :date-text="formatDate(rightBookmark.createdAt)"
                                :show-favorite="false"
                                :show-bookmark="false"
                                :show-comment-count="false"
                                :show-author="false"
                                @click="onRecipeClick"
                            />
                            <div class="open-book-view__memo" @click.stop>
                                <button
                                    type="button"
                                    class="open-book-view__memo-trigger"
                                    @click="openMemoDialog(rightBookmark)"
                                >
                                    <i class="pi pi-pencil text-sm"></i>
                                    <span v-if="rightBookmark.memo" class="open-book-view__memo-text">{{ rightBookmark.memo }}</span>
                                    <span v-else class="open-book-view__memo-placeholder">메모 추가</span>
                                </button>
                            </div>
                        </template>
                        <div v-else class="open-book-view__empty-page">
                            <i class="pi pi-book text-4xl text-gray-300 mb-2"></i>
                            <p class="text-gray-500 text-sm">이 페이지는 비어 있어요</p>
                        </div>
                    </div>
                </template>
            </BookFrame>
        </div>

        <BookmarkMemoDialog
            v-model:visible="memoDialogVisible"
            :bookmark-id="memoDialogBookmarkId"
            :recipe-title="memoDialogRecipeTitle"
            :memo="memoDialogMemo"
            :save-async="onMemoSave"
        />

        <div v-if="spreads.length > 1" class="open-book-view__nav">
            <Button
                icon="pi pi-chevron-left"
                label="이전"
                outlined
                size="small"
                :disabled="currentSpreadIndex <= 0"
                @click="prevSpread"
            />
            <span class="open-book-view__nav-info">
                {{ currentSpreadIndex + 1 }} / {{ spreads.length }}
            </span>
            <Button
                icon="pi pi-chevron-right"
                icon-pos="right"
                label="다음"
                outlined
                size="small"
                :disabled="currentSpreadIndex >= spreads.length - 1"
                @click="nextSpread"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Button from 'primevue/button';
import BookFrame from './BookFrame.vue';
import BookmarkMemoDialog from './BookmarkMemoDialog.vue';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import { updateBookmarkMemo } from '@/api/bookmarkApi';
import { getRecipeBookColorHex } from '@/types/bookmark';
import { useAppToast } from '@/utils/toast';
import type { RecipeBook, RecipeBookmark } from '@/types/bookmark';
import type { Recipe } from '@/types/recipe';
import type { RecipeGridItem } from '@/types/recipe';

const props = defineProps<{
    recipeBook: RecipeBook;
    bookmarks: RecipeBookmark[];
    getRecipeGridItem: (bookmark: RecipeBookmark) => RecipeGridItem;
    getCategoryName: (recipe: Recipe) => string | null;
    formatDate: (dateString: string) => string;
}>();

const emit = defineEmits<{
    'recipe-click': [recipeId: number];
    'memo-updated': [];
}>();

const { showError } = useAppToast();
const currentSpreadIndex = ref(0);

const memoDialogVisible = ref(false);
const memoDialogBookmarkId = ref(0);
const memoDialogRecipeTitle = ref<string | null>(null);
const memoDialogMemo = ref<string | null>(null);

function openMemoDialog(bookmark: RecipeBookmark) {
    memoDialogBookmarkId.value = bookmark.id;
    memoDialogRecipeTitle.value = bookmark.recipe?.title ?? null;
    memoDialogMemo.value = bookmark.memo ?? null;
    memoDialogVisible.value = true;
}

async function onMemoSave(payload: { bookmarkId: number; memo: string | null }): Promise<void> {
    try {
        await updateBookmarkMemo(payload.bookmarkId, payload.memo);
        emit('memo-updated');
    } catch (err) {
        console.error('메모 저장 실패:', err);
        showError('메모 저장에 실패했습니다.');
        throw err;
    }
}

/** 스프레드: [왼쪽 북마크, 오른쪽 북마크][] */
const spreads = computed(() => {
    const list = props.bookmarks;
    if (list.length === 0) return [[null, null] as [RecipeBookmark | null, RecipeBookmark | null]];
    const result: [RecipeBookmark | null, RecipeBookmark | null][] = [];
    for (let i = 0; i < list.length; i += 2) {
        result.push([list[i] ?? null, list[i + 1] ?? null]);
    }
    return result;
});

const leftBookmark = computed(
    () => spreads.value[currentSpreadIndex.value]?.[0] ?? null
);
const rightBookmark = computed(
    () => spreads.value[currentSpreadIndex.value]?.[1] ?? null
);

watch(
    () => props.bookmarks,
    () => {
        currentSpreadIndex.value = 0;
    },
    { deep: true }
);

watch(
    () => currentSpreadIndex.value,
    (val) => {
        if (val < 0 || val >= spreads.value.length) {
            currentSpreadIndex.value = Math.max(0, Math.min(val, spreads.value.length - 1));
        }
    }
);

function prevSpread() {
    if (currentSpreadIndex.value > 0) {
        currentSpreadIndex.value--;
    }
}

function nextSpread() {
    if (currentSpreadIndex.value < spreads.value.length - 1) {
        currentSpreadIndex.value++;
    }
}

function onRecipeClick(recipeId: number) {
    emit('recipe-click', recipeId);
}
</script>

<style scoped lang="scss">
.open-book-view {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.bookmarks-header {
    margin-bottom: 0;
}

.open-book-view__book {
    width: 100%;
}

.open-book-view__page-content {
    height: 100%;
    min-height: 280px;
    display: flex;
    flex-direction: column;

    :deep(.recipe-card-wrapper) {
        flex: 1;
    }

    :deep(.recipe-card-wrapper .recipe-card) {
        height: 100%;
    }
}

.open-book-view__empty-page {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 280px;
    text-align: center;
}

.open-book-view__nav {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    padding: 0.5rem 0;
}

.open-book-view__nav-info {
    font-size: 0.9rem;
    color: var(--text-color-secondary);
    min-width: 4ch;
    text-align: center;
}

.open-book-view__memo {
    margin-top: 0.75rem;
    padding-top: 0.5rem;
    border-top: 1px solid var(--surface-border);
}

.open-book-view__memo-trigger {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    width: 100%;
    padding: 0.35rem 0.5rem;
    border-radius: 6px;
    border: none;
    background: transparent;
    color: var(--text-color-secondary);
    font-size: 0.875rem;
    text-align: left;
    cursor: pointer;
    transition: background-color 0.2s, color 0.2s;
}

.open-book-view__memo-trigger:hover {
    background: var(--surface-hover);
    color: var(--primary-color);
}

.open-book-view__memo-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.open-book-view__memo-placeholder {
    flex: 1;
    font-style: italic;
    color: var(--text-color-secondary);
}
</style>
