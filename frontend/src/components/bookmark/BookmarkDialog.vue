<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import RecipeBookFormDialog from './RecipeBookFormDialog.vue';
import { getRecipeBooks, addBookmark, removeBookmark, checkBookmark } from '@/api/bookmarkApi';
import { getRecipeBookColorHex } from '@/types/bookmark';
import type { RecipeBook } from '@/types/bookmark';
import { useAppToast } from '@/utils/toast';

const props = defineProps<{
    visible: boolean;
    recipeId: number | null;
}>();

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'bookmarked'): void;
}>();

const { showError } = useAppToast();

const visible = ref(props.visible);
const loading = ref(false);
const recipeBooks = ref<RecipeBook[]>([]);
const selectedRecipeBookIds = ref<number[]>([]);
const initialRecipeBookIds = ref<number[]>([]);
const recipeBookDialogVisible = ref(false);

const hasChanges = computed(() => {
    const add = selectedRecipeBookIds.value.filter((id) => !initialRecipeBookIds.value.includes(id));
    const remove = initialRecipeBookIds.value.filter((id) => !selectedRecipeBookIds.value.includes(id));
    return add.length > 0 || remove.length > 0;
});

watch(
    () => props.visible,
    (newVal) => {
        visible.value = newVal;
        if (newVal && props.recipeId) {
            loadData();
        }
    }
);

watch(visible, (newVal) => {
    emit('update:visible', newVal);
});

const loadData = async () => {
    if (!props.recipeId) return;
    try {
        loading.value = true;
        recipeBooks.value = await getRecipeBooks();
        const bookmarkStatus = await checkBookmark(props.recipeId);
        selectedRecipeBookIds.value = [...bookmarkStatus.recipeBooks];
        initialRecipeBookIds.value = [...bookmarkStatus.recipeBooks];
    } catch (error) {
        console.error('데이터 로드 실패:', error);
        showError('레시피북 목록을 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

const toggleRecipeBook = (recipeBookId: number) => {
    const index = selectedRecipeBookIds.value.indexOf(recipeBookId);
    if (index > -1) {
        selectedRecipeBookIds.value.splice(index, 1);
    } else {
        selectedRecipeBookIds.value.push(recipeBookId);
    }
};

const saveBookmarks = async () => {
    if (!props.recipeId) return;
    try {
        loading.value = true;
        const toAdd = selectedRecipeBookIds.value.filter((id) => !initialRecipeBookIds.value.includes(id));
        const toRemove = initialRecipeBookIds.value.filter((id) => !selectedRecipeBookIds.value.includes(id));
        for (const recipeBookId of toAdd) {
            await addBookmark(recipeBookId, props.recipeId);
        }
        for (const recipeBookId of toRemove) {
            await removeBookmark(recipeBookId, props.recipeId);
        }
        emit('bookmarked');
        closeDialog();
    } catch (error) {
        console.error('북마크 저장 실패:', error);
        showError('북마크 저장에 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

const openRecipeBookDialog = () => {
    recipeBookDialogVisible.value = true;
};

const onRecipeBookCreated = async (recipeBook: RecipeBook) => {
    recipeBooks.value = await getRecipeBooks();
    if (!selectedRecipeBookIds.value.includes(recipeBook.id)) {
        selectedRecipeBookIds.value.push(recipeBook.id);
    }
};

const closeDialog = () => {
    visible.value = false;
    selectedRecipeBookIds.value = [];
    initialRecipeBookIds.value = [];
};
</script>

<template>
    <Dialog v-model:visible="visible" modal header="북마크에 저장" :style="{ width: '500px' }" :draggable="false">
        <div class="bookmark-dialog-content">
            <div v-if="loading" class="flex justify-center items-center py-8">
                <i class="pi pi-spin pi-spinner text-4xl text-primary"></i>
            </div>

            <template v-else>
                <div class="mb-4">
                    <p class="text-gray-600 text-sm mb-3">레시피를 저장할 레시피북을 선택하세요</p>

                    <div v-if="recipeBooks.length === 0" class="text-center py-6">
                        <i class="pi pi-bookmark text-4xl text-gray-300 mb-3"></i>
                        <p class="text-gray-500 mb-4">아직 레시피북이 없습니다</p>
                        <Button label="첫 번째 레시피북 만들기" icon="pi pi-plus" @click="openRecipeBookDialog()" outlined />
                    </div>

                    <div v-else class="recipe-book-list">
                        <div v-for="recipeBook in recipeBooks" :key="recipeBook.id" class="recipe-book-item" :class="{ 'recipe-book-item-selected': selectedRecipeBookIds.includes(recipeBook.id) }" @click="toggleRecipeBook(recipeBook.id)">
                            <div class="flex items-center gap-3 flex-1">
                                <i class="pi pi-bookmark text-2xl" :style="{ color: getRecipeBookColorHex(recipeBook.color) }"></i>
                                <div class="flex-1">
                                    <div class="font-medium">{{ recipeBook.name }}</div>
                                    <div class="text-sm text-gray-500">{{ recipeBook.bookmarkCount }}개 레시피</div>
                                </div>
                            </div>
                            <i v-if="selectedRecipeBookIds.includes(recipeBook.id)" class="pi pi-check text-xl text-primary"></i>
                        </div>
                    </div>
                </div>

                <Button label="새 레시피북 만들기" icon="pi pi-plus" @click="openRecipeBookDialog()" outlined class="w-full" />
            </template>
        </div>

        <template #footer>
            <Button label="취소" text @click="closeDialog" />
            <Button label="저장" @click="saveBookmarks" :disabled="loading || !hasChanges" />
        </template>
    </Dialog>

    <RecipeBookFormDialog v-model:visible="recipeBookDialogVisible" @recipe-book-created="onRecipeBookCreated" />
</template>

<style scoped lang="scss">
.bookmark-dialog-content {
    min-height: 200px;
}

.recipe-book-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    max-height: 400px;
    overflow-y: auto;
}

.recipe-book-item {
    display: flex;
    align-items: center;
    padding: 1rem;
    border: 2px solid var(--surface-border);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background-color: var(--surface-hover);
        border-color: var(--primary-color);
    }

    &.recipe-book-item-selected {
        background-color: var(--primary-50);
        border-color: var(--primary-color);
    }
}
</style>
