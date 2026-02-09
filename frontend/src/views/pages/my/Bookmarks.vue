<template>
    <div class="bookmarks-content">
        <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
            <p class="text-gray-700 italic">
                폴더를 선택해서 북마크한 레시피를 확인하세요. 폴더는 드래그하여 순서를 변경할 수 있어요.
            </p>
        </div>

        <!-- 로딩 상태 -->
        <PageStateBlock
            v-if="loading"
            state="loading"
            loading-message="북마크를 불러오는 중..."
        />

        <!-- 에러 상태 -->
        <PageStateBlock
            v-else-if="error"
            state="error"
            error-title="북마크를 불러올 수 없습니다"
            :error-message="error"
            retry-label="다시 시도"
            @retry="loadFolders"
        />

        <!-- 메인 컨텐츠 -->
        <div v-else class="bookmarks-layout">
            <!-- 좌측: 폴더 목록 -->
            <div class="folder-sidebar">
                <div class="folder-header">
                    <h3 class="text-lg font-semibold m-0">내 폴더</h3>
                    <Button
                        icon="pi pi-plus"
                        label="새 폴더"
                        @click="openFolderDialog()"
                        size="small"
                        outlined
                    />
                </div>

                <!-- 폴더가 없을 때 -->
                <div v-if="folders.length === 0" class="empty-folders">
                    <i class="pi pi-bookmark text-5xl text-gray-300 mb-3"></i>
                    <p class="text-gray-500 mb-4">아직 폴더가 없습니다</p>
                    <Button
                        label="첫 번째 폴더 만들기"
                        icon="pi pi-plus"
                        @click="openFolderDialog()"
                        outlined
                    />
                </div>

                <!-- 폴더 목록 (드래그로 순서 변경) -->
                <div v-else class="folder-list">
                    <div
                        v-for="folder in folders"
                        :key="folder.id"
                        class="folder-item"
                        :class="{
                            'folder-item-active': selectedFolder?.id === folder.id,
                            'folder-item-dragging': draggedFolderId === folder.id,
                            'folder-item-drag-over': dragOverFolderId === folder.id
                        }"
                        @click="selectFolder(folder)"
                        @dragover.prevent="onFolderDragOver($event, folder)"
                        @dragleave="onFolderDragLeave(folder)"
                        @drop.prevent="onFolderDrop($event, folder)"
                    >
                        <div
                            class="folder-drag-handle"
                            draggable="true"
                            @dragstart="onFolderDragStart($event, folder)"
                            @dragend="onFolderDragEnd"
                            title="드래그하여 순서 변경"
                        >
                            <i class="pi pi-bars"></i>
                        </div>
                        <div class="flex items-center gap-3 flex-1 min-w-0">
                            <i
                                class="pi pi-bookmark text-2xl flex-shrink-0"
                                :style="{ color: getFolderColorHex(folder.color) }"
                            ></i>
                            <div class="flex-1 min-w-0">
                                <div class="folder-name">{{ folder.name }}</div>
                                <div class="folder-count">{{ folder.bookmarkCount }}개</div>
                            </div>
                        </div>
                        
                        <div class="folder-actions" @click.stop>
                            <Button
                                icon="pi pi-pencil"
                                text
                                rounded
                                size="small"
                                @click="openFolderDialog(folder)"
                                v-tooltip.top="'수정'"
                            />
                            <Button
                                icon="pi pi-trash"
                                text
                                rounded
                                size="small"
                                severity="danger"
                                @click="handleDeleteFolder(folder)"
                                v-tooltip.top="'삭제'"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 우측: 북마크된 레시피 목록 -->
            <div class="bookmarks-main">
                <!-- 폴더 선택 안 됨 -->
                <div v-if="!selectedFolder" class="empty-state">
                    <i class="pi pi-bookmark text-6xl text-gray-300 mb-4"></i>
                    <h3 class="text-xl font-semibold text-gray-700 mb-2">폴더를 선택하세요</h3>
                    <p class="text-gray-500">좌측에서 폴더를 선택하면 저장된 레시피를 볼 수 있습니다</p>
                </div>

                <!-- 폴더 선택됨 -->
                <template v-else>
                    <div class="bookmarks-header">
                        <div>
                            <h2 class="text-2xl font-semibold m-0 flex items-center gap-3">
                                <i
                                    class="pi pi-bookmark"
                                    :style="{ color: getFolderColorHex(selectedFolder.color) }"
                                ></i>
                                {{ selectedFolder.name }}
                            </h2>
                            <p class="text-gray-600 mt-1">{{ selectedFolder.bookmarkCount }}개의 레시피</p>
                        </div>
                    </div>

                    <!-- 북마크 로딩 -->
                    <PageStateBlock
                        v-if="bookmarksLoading"
                        state="loading"
                        loading-message="레시피를 불러오는 중..."
                    />

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

                    <!-- 북마크 목록 -->
                    <div v-else class="recipe-grid">
                        <RecipeGridCard
                            v-for="bookmark in bookmarks"
                            :key="bookmark.id"
                            :recipe="getRecipeGridItem(bookmark)"
                            :category-label="getCategoryName(bookmark.recipe)"
                            :date-text="formatDate(bookmark.createdAt)"
                            :show-favorite="false"
                            :show-bookmark="false"
                            :show-comment-count="false"
                            :show-author="false"
                            @click="viewRecipe"
                        />
                    </div>
                </template>
            </div>
        </div>

        <!-- 폴더 생성/수정 Dialog -->
        <FolderFormDialog
            v-model:visible="folderDialogVisible"
            :folder="editingFolder"
            @folder-created="onFolderCreated"
            @folder-updated="onFolderUpdated"
        />

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import FolderFormDialog from '@/components/bookmark/FolderFormDialog.vue';
import { getFolders, getBookmarksByFolder, deleteFolder, reorderFolders } from '@/api/bookmarkApi';
import { getFolderColorHex } from '@/types/bookmark';
import type { BookmarkFolder, RecipeBookmark } from '@/types/bookmark';
import type { Recipe, RecipeCookingTip, RecipeGridItem, RecipeCategory } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';

const router = useRouter();
const { showError } = useAppToast();

const loading = ref(false);
const error = ref<string | null>(null);
const folders = ref<BookmarkFolder[]>([]);
const selectedFolder = ref<BookmarkFolder | null>(null);

const bookmarksLoading = ref(false);
const bookmarks = ref<RecipeBookmark[]>([]);

const folderDialogVisible = ref(false);
const editingFolder = ref<BookmarkFolder | null>(null);

/** 드래그 앤 드롭: 드래그 중인 폴더 id */
const draggedFolderId = ref<number | null>(null);
/** 드래그 앤 드롭: 드롭 대상 위에 있는 폴더 id */
const dragOverFolderId = ref<number | null>(null);

/**
 * 폴더 목록 로드
 */
const loadFolders = async () => {
    try {
        loading.value = true;
        error.value = null;
        folders.value = await getFolders();

        // 첫 번째 폴더 자동 선택
        if (folders.value.length > 0 && !selectedFolder.value) {
            selectFolder(folders.value[0]);
        }
    } catch (err) {
        console.error('폴더 목록 로드 실패:', err);
        error.value = '폴더 목록을 불러오는데 실패했습니다.';
    } finally {
        loading.value = false;
    }
};

/**
 * 폴더 선택
 */
const selectFolder = async (folder: BookmarkFolder) => {
    selectedFolder.value = folder;
    await loadBookmarks(folder.id);
};

/**
 * 북마크 목록 로드
 */
const loadBookmarks = async (folderId: number) => {
    try {
        bookmarksLoading.value = true;
        bookmarks.value = await getBookmarksByFolder(folderId);
    } catch (err) {
        console.error('북마크 로드 실패:', err);
        showError('북마크를 불러오는데 실패했습니다.');
        bookmarks.value = [];
    } finally {
        bookmarksLoading.value = false;
    }
};

/**
 * 폴더 생성 Dialog 열기
 */
const openFolderDialog = (folder?: BookmarkFolder) => {
    editingFolder.value = folder || null;
    folderDialogVisible.value = true;
};

/**
 * 폴더 생성 완료
 */
const onFolderCreated = async (folder: BookmarkFolder) => {
    await loadFolders();
    selectFolder(folder);
};

/**
 * 폴더 수정 완료
 */
const onFolderUpdated = async () => {
    await loadFolders();
};

/**
 * 폴더 드래그 시작
 */
const onFolderDragStart = (_event: DragEvent, folder: BookmarkFolder) => {
    draggedFolderId.value = folder.id;
    if (_event.dataTransfer) {
        _event.dataTransfer.effectAllowed = 'move';
        _event.dataTransfer.setData('text/plain', String(folder.id));
    }
};

/**
 * 폴더 드래그 종료
 */
const onFolderDragEnd = () => {
    draggedFolderId.value = null;
    dragOverFolderId.value = null;
};

/**
 * 폴더 위로 드래그 오버
 */
const onFolderDragOver = (_event: DragEvent, folder: BookmarkFolder) => {
    if (draggedFolderId.value === null || draggedFolderId.value === folder.id) return;
    dragOverFolderId.value = folder.id;
};

/**
 * 폴더에서 드래그 리브
 */
const onFolderDragLeave = (folder: BookmarkFolder) => {
    if (dragOverFolderId.value === folder.id) dragOverFolderId.value = null;
};

/**
 * 폴더 드롭 시 순서 변경
 */
const onFolderDrop = async (_event: DragEvent, targetFolder: BookmarkFolder) => {
    const sourceId = draggedFolderId.value;
    dragOverFolderId.value = null;
    draggedFolderId.value = null;
    if (sourceId == null || sourceId === targetFolder.id) return;

    const currentOrder = folders.value.map((f) => f.id);
    const fromIndex = currentOrder.indexOf(sourceId);
    const toIndex = currentOrder.indexOf(targetFolder.id);
    if (fromIndex === -1 || toIndex === -1) return;

    const newOrder = [...currentOrder];
    newOrder.splice(fromIndex, 1);
    newOrder.splice(toIndex, 0, sourceId);

    try {
        const updated = await reorderFolders(newOrder);
        folders.value = updated;
    } catch (err) {
        console.error('폴더 순서 변경 실패:', err);
        showError('폴더 순서 변경에 실패했습니다.');
    }
};

/**
 * 폴더 삭제
 */
const handleDeleteFolder = async (folder: BookmarkFolder) => {
    try {
        await deleteFolder(folder.id);

        // 삭제된 폴더가 선택된 폴더였다면 선택 해제
        if (selectedFolder.value?.id === folder.id) {
            selectedFolder.value = null;
            bookmarks.value = [];
        }
        
        await loadFolders();
    } catch (err) {
        console.error('폴더 삭제 실패:', err);
        showError('폴더 삭제에 실패했습니다.');
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

/**
 * 레시피 둘러보기
 */
const browseRecipes = (): void => {
    router.push('/recipe/category');
};

onMounted(() => {
    loadFolders();
});
</script>

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

.folder-sidebar {
    border-right: 1px solid var(--surface-border);
    padding-right: 2rem;

    @media (max-width: 768px) {
        border-right: none;
        border-bottom: 1px solid var(--surface-border);
        padding-right: 0;
        padding-bottom: 2rem;
    }
}

.folder-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.5rem;
}

.empty-folders {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 1rem;
    text-align: center;
}

.folder-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.folder-drag-handle {
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

.folder-item {
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

    &.folder-item-active {
        background-color: var(--primary-50);
        border-color: var(--primary-color);
    }

    &.folder-item-dragging {
        opacity: 0.6;
    }

    &.folder-item-drag-over {
        border-color: var(--primary-color);
        background-color: var(--primary-50);
    }
}

.folder-name {
    font-weight: 500;
    margin-bottom: 0.25rem;
}

.folder-count {
    font-size: 0.85rem;
    color: var(--text-color-secondary);
}

.folder-actions {
    display: flex;
    gap: 0.25rem;
    opacity: 0;
    transition: opacity 0.2s;

    .folder-item:hover & {
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

.bookmarks-header {
    margin-bottom: 2rem;
}
</style>
