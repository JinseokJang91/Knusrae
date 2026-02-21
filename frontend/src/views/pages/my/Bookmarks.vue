<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import OpenBookView from '@/components/bookmark/OpenBookView.vue';
import RecipeBookFormDialog from '@/components/bookmark/RecipeBookFormDialog.vue';
import { getRecipeBooks, getBookmarksByRecipeBook, deleteRecipeBook, reorderRecipeBooks } from '@/api/bookmarkApi';
import { getRecipeBookColorHex } from '@/types/bookmark';
import type { RecipeBook, RecipeBookmark } from '@/types/bookmark';
import type { Recipe, RecipeCookingTip, RecipeGridItem, RecipeCategory } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';

const router = useRouter();
const { showError } = useAppToast();

const loading = ref(false);
const error = ref<string | null>(null);
const recipeBooks = ref<RecipeBook[]>([]);
const selectedRecipeBook = ref<RecipeBook | null>(null);

const bookmarksLoading = ref(false);
const bookmarks = ref<RecipeBookmark[]>([]);

const recipeBookDialogVisible = ref(false);
const editingRecipeBook = ref<RecipeBook | null>(null);

const draggedRecipeBookId = ref<number | null>(null);
const dragOverRecipeBookId = ref<number | null>(null);

const loadRecipeBooks = async () => {
    try {
        loading.value = true;
        error.value = null;
        recipeBooks.value = await getRecipeBooks();
        if (recipeBooks.value.length > 0 && !selectedRecipeBook.value) {
            selectRecipeBook(recipeBooks.value[0]);
        }
    } catch (err) {
        console.error('레시피북 목록 로드 실패:', err);
        error.value = '레시피북 목록을 불러오는데 실패했습니다.';
    } finally {
        loading.value = false;
    }
};

const selectRecipeBook = async (recipeBook: RecipeBook) => {
    selectedRecipeBook.value = recipeBook;
    await loadBookmarks(recipeBook.id);
};

const loadBookmarks = async (recipeBookId: number) => {
    try {
        bookmarksLoading.value = true;
        bookmarks.value = await getBookmarksByRecipeBook(recipeBookId);
    } catch (err) {
        console.error('북마크 로드 실패:', err);
        showError('북마크를 불러오는데 실패했습니다.');
        bookmarks.value = [];
    } finally {
        bookmarksLoading.value = false;
    }
};

/**
 * 레시피북 생성 Dialog 열기
 */
const openRecipeBookDialog = (recipeBook?: RecipeBook) => {
    editingRecipeBook.value = recipeBook || null;
    recipeBookDialogVisible.value = true;
};

const onRecipeBookCreated = async (recipeBook: RecipeBook) => {
    await loadRecipeBooks();
    selectRecipeBook(recipeBook);
};

const onRecipeBookUpdated = async () => {
    await loadRecipeBooks();
};

const onRecipeBookDragStart = (_event: DragEvent, recipeBook: RecipeBook) => {
    draggedRecipeBookId.value = recipeBook.id;
    if (_event.dataTransfer) {
        _event.dataTransfer.effectAllowed = 'move';
        _event.dataTransfer.setData('text/plain', String(recipeBook.id));
    }
};

const onRecipeBookDragEnd = () => {
    draggedRecipeBookId.value = null;
    dragOverRecipeBookId.value = null;
};

const onRecipeBookDragOver = (_event: DragEvent, recipeBook: RecipeBook) => {
    if (draggedRecipeBookId.value === null || draggedRecipeBookId.value === recipeBook.id) return;
    dragOverRecipeBookId.value = recipeBook.id;
};

const onRecipeBookDragLeave = (recipeBook: RecipeBook) => {
    if (dragOverRecipeBookId.value === recipeBook.id) dragOverRecipeBookId.value = null;
};

const onRecipeBookDrop = async (_event: DragEvent, targetRecipeBook: RecipeBook) => {
    const sourceId = draggedRecipeBookId.value;
    dragOverRecipeBookId.value = null;
    draggedRecipeBookId.value = null;
    if (sourceId == null || sourceId === targetRecipeBook.id) return;

    const currentOrder = recipeBooks.value.map((rb) => rb.id);
    const fromIndex = currentOrder.indexOf(sourceId);
    const toIndex = currentOrder.indexOf(targetRecipeBook.id);
    if (fromIndex === -1 || toIndex === -1) return;

    const newOrder = [...currentOrder];
    newOrder.splice(fromIndex, 1);
    newOrder.splice(toIndex, 0, sourceId);

    try {
        const updated = await reorderRecipeBooks(newOrder);
        recipeBooks.value = updated;
    } catch (err) {
        console.error('레시피북 순서 변경 실패:', err);
        showError('레시피북 순서 변경에 실패했습니다.');
    }
};

const handleDeleteRecipeBook = async (recipeBook: RecipeBook) => {
    try {
        await deleteRecipeBook(recipeBook.id);
        if (selectedRecipeBook.value?.id === recipeBook.id) {
            selectedRecipeBook.value = null;
            bookmarks.value = [];
        }
        await loadRecipeBooks();
    } catch (err) {
        console.error('레시피북 삭제 실패:', err);
        showError('레시피북 삭제에 실패했습니다.');
    }
};

/**
 * 레시피 그리드 아이템 변환
 */
const getRecipeGridItem = (bookmark: RecipeBookmark): RecipeGridItem => ({
    id: bookmark.recipe.id,
    title: bookmark.recipe.title,
    thumbnail: bookmark.recipe.thumbnail,
    cookingTime: getCookingTime(bookmark.recipe) ?? undefined,
    servings: getServings(bookmark.recipe) ?? undefined,
    hits: bookmark.recipe.hits,
    isFavorite: false,
    memberNickname: bookmark.recipe.memberNickname,
    memberName: bookmark.recipe.memberName,
    memberProfileImage: bookmark.recipe.memberProfileImage
});

/**
 * 요리 시간 추출
 */
const getCookingTime = (recipe: Recipe): string | null => {
    if (!recipe.cookingTips || !Array.isArray(recipe.cookingTips)) return null;
    const cookingTimeTip = recipe.cookingTips.find((tip: RecipeCookingTip) => tip.codeId === 'COOKING_TIME');
    return cookingTimeTip?.detailName || null;
};

/**
 * 인분 수 추출
 */
const getServings = (recipe: Recipe): string | null => {
    if (!recipe.cookingTips || !Array.isArray(recipe.cookingTips)) return null;
    const servingTip = recipe.cookingTips.find((tip: RecipeCookingTip) => tip.codeId === 'SERVINGS');
    return servingTip?.detailName || null;
};

/**
 * 카테고리 이름 추출
 */
const getCategoryName = (recipe: Recipe): string | null => {
    if (!recipe.categories || !Array.isArray(recipe.categories) || recipe.categories.length === 0) return null;
    const keywordCategory = recipe.categories.find((cat: RecipeCategory) => cat.codeId === 'COOKING_KEYWORD');
    const target = keywordCategory || recipe.categories[0];
    return target?.detailName || target?.codeName || null;
};

/**
 * 날짜 포맷
 */
const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
};

/**
 * 레시피 상세 보기
 */
const viewRecipe = (recipeId: number): void => {
    router.push(`/recipe/${recipeId}`);
};

const onMemoUpdated = async (): Promise<void> => {
    if (selectedRecipeBook.value) {
        await loadBookmarks(selectedRecipeBook.value.id);
    }
};

/**
 * 레시피 둘러보기
 */
const browseRecipes = (): void => {
    router.push('/recipe/category');
};

onMounted(() => {
    loadRecipeBooks();
});
</script>

<template>
    <div class="bookmarks-content">
        <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
            <p class="text-gray-700 italic">레시피북을 선택해서 북마크한 레시피를 확인하세요. 레시피북은 드래그하여 순서를 변경할 수 있어요.</p>
        </div>

        <!-- 로딩 상태 -->
        <PageStateBlock v-if="loading" state="loading" loading-message="북마크를 불러오는 중..." />

        <!-- 에러 상태 -->
        <PageStateBlock v-else-if="error" state="error" error-title="북마크를 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadRecipeBooks" />

        <!-- 메인 컨텐츠 -->
        <div v-else class="bookmarks-layout">
            <!-- 좌측: 레시피북 목록 -->
            <div class="recipe-book-sidebar">
                <div class="recipe-book-header">
                    <h3 class="text-lg font-semibold m-0">내 레시피북</h3>
                    <Button icon="pi pi-plus" label="새 레시피북" @click="openRecipeBookDialog()" size="small" outlined />
                </div>

                <!-- 레시피북이 없을 때 -->
                <div v-if="recipeBooks.length === 0" class="empty-recipe-books">
                    <i class="pi pi-bookmark text-5xl text-gray-300 mb-3"></i>
                    <p class="text-gray-500 mb-4">아직 레시피북이 없습니다</p>
                    <Button label="첫 번째 레시피북 만들기" icon="pi pi-plus" @click="openRecipeBookDialog()" outlined />
                </div>

                <!-- 레시피북 목록 (드래그로 순서 변경) -->
                <div v-else class="recipe-book-list">
                    <div
                        v-for="recipeBook in recipeBooks"
                        :key="recipeBook.id"
                        class="recipe-book-item"
                        :class="{
                            'recipe-book-item-active': selectedRecipeBook?.id === recipeBook.id,
                            'recipe-book-item-dragging': draggedRecipeBookId === recipeBook.id,
                            'recipe-book-item-drag-over': dragOverRecipeBookId === recipeBook.id
                        }"
                        @click="selectRecipeBook(recipeBook)"
                        @dragover.prevent="onRecipeBookDragOver($event, recipeBook)"
                        @dragleave="onRecipeBookDragLeave(recipeBook)"
                        @drop.prevent="onRecipeBookDrop($event, recipeBook)"
                    >
                        <div class="recipe-book-drag-handle" draggable="true" @dragstart="onRecipeBookDragStart($event, recipeBook)" @dragend="onRecipeBookDragEnd" title="드래그하여 순서 변경">
                            <i class="pi pi-bars"></i>
                        </div>
                        <div class="flex items-center gap-3 flex-1 min-w-0">
                            <i class="pi pi-bookmark text-2xl flex-shrink-0" :style="{ color: getRecipeBookColorHex(recipeBook.color) }"></i>
                            <div class="flex-1 min-w-0">
                                <div class="recipe-book-name">{{ recipeBook.name }}</div>
                                <div class="recipe-book-count">{{ recipeBook.bookmarkCount }}개</div>
                            </div>
                        </div>

                        <div class="recipe-book-actions" @click.stop>
                            <Button icon="pi pi-pencil" text rounded size="small" @click="openRecipeBookDialog(recipeBook)" v-tooltip.top="'수정'" />
                            <Button icon="pi pi-trash" text rounded size="small" severity="danger" @click="handleDeleteRecipeBook(recipeBook)" v-tooltip.top="'삭제'" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 우측: 북마크된 레시피 목록 -->
            <div class="bookmarks-main">
                <!-- 레시피북 선택 안 됨 -->
                <div v-if="!selectedRecipeBook" class="empty-state">
                    <i class="pi pi-bookmark text-6xl text-gray-300 mb-4"></i>
                    <h3 class="text-xl font-semibold text-gray-700 mb-2">레시피북을 선택하세요</h3>
                    <p class="text-gray-500">좌측에서 레시피북을 선택하면 저장된 레시피를 볼 수 있습니다</p>
                </div>

                <!-- 레시피북 선택됨 -->
                <template v-else>
                    <!-- 북마크 로딩 -->
                    <PageStateBlock v-if="bookmarksLoading" state="loading" loading-message="레시피를 불러오는 중..." />

                    <!-- 북마크가 없을 때 -->
                    <PageStateBlock
                        v-else-if="bookmarks.length === 0"
                        state="empty"
                        empty-icon="pi pi-bookmark"
                        empty-title="저장된 레시피가 없습니다"
                        empty-message="레시피 카드나 상세 페이지에서 북마크 버튼을 눌러 저장해보세요"
                        empty-button-label="레시피 둘러보기"
                        @empty-action="browseRecipes"
                    />

                    <!-- 열린 책 뷰 -->
                    <OpenBookView
                        v-else
                        :recipe-book="selectedRecipeBook"
                        :bookmarks="bookmarks"
                        :get-recipe-grid-item="getRecipeGridItem"
                        :get-category-name="getCategoryName"
                        :format-date="formatDate"
                        @recipe-click="viewRecipe"
                        @memo-updated="onMemoUpdated"
                    />
                </template>
            </div>
        </div>

        <!-- 레시피북 생성/수정 Dialog -->
        <RecipeBookFormDialog v-model:visible="recipeBookDialogVisible" :recipe-book="editingRecipeBook" @recipe-book-created="onRecipeBookCreated" @recipe-book-updated="onRecipeBookUpdated" />
    </div>
</template>

<style scoped lang="scss">
.bookmarks-layout {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 2rem;
    min-height: 500px;

    @media (max-width: 768px) {
        grid-template-columns: 1fr;
    }
}

.recipe-book-sidebar {
    border-right: 1px solid var(--surface-border);
    padding-right: 2rem;

    @media (max-width: 768px) {
        border-right: none;
        border-bottom: 1px solid var(--surface-border);
        padding-right: 0;
        padding-bottom: 2rem;
    }
}

.recipe-book-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.5rem;
}

.empty-recipe-books {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 1rem;
    text-align: center;
}

.recipe-book-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.recipe-book-drag-handle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    margin-right: 0.25rem;
    border-radius: 6px;
    color: var(--text-color-secondary);
    cursor: grab;
    flex-shrink: 0;

    &:active {
        cursor: grabbing;
    }

    &:hover {
        color: var(--primary-color);
        background-color: var(--surface-hover);
    }
}

.recipe-book-item {
    display: flex;
    align-items: center;
    padding: 1rem;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    border: 2px solid transparent;

    &:hover {
        background-color: var(--surface-hover);
        border-color: var(--primary-color);
    }

    &.recipe-book-item-active {
        background-color: var(--primary-50);
        border-color: var(--primary-color);
    }

    &.recipe-book-item-dragging {
        opacity: 0.6;
    }

    &.recipe-book-item-drag-over {
        border-color: var(--primary-color);
        background-color: var(--primary-50);
    }
}

.recipe-book-name {
    font-weight: 500;
    margin-bottom: 0.25rem;
}

.recipe-book-count {
    font-size: 0.85rem;
    color: var(--text-color-secondary);
}

.recipe-book-actions {
    display: flex;
    gap: 0.25rem;
    opacity: 0;
    transition: opacity 0.2s;

    .recipe-book-item:hover & {
        opacity: 1;
    }
}

.bookmarks-main {
    flex: 1;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    text-align: center;
}
</style>
