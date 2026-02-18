<template>
    <Dialog
        v-model:visible="visible"
        modal
        :header="isEditMode ? '레시피북 수정' : '새 레시피북 만들기'"
        :style="{ width: '450px' }"
        :draggable="false"
    >
        <div class="recipe-book-form">
            <!-- 레시피북 이름 -->
            <div class="mb-4">
                <label class="block mb-2 font-medium">레시피북 이름</label>
                <InputText
                    v-model="recipeBookName"
                    placeholder="예: 한식 레시피, 간식 모음"
                    class="w-full"
                    :maxlength="50"
                    @keyup.enter="saveRecipeBook"
                />
                <small class="text-gray-500">{{ recipeBookName.length }}/50</small>
            </div>

            <!-- 레시피북 색상 -->
            <div class="mb-4">
                <label class="block mb-2 font-medium">레시피북 색상</label>
                <div class="color-grid">
                    <div
                        v-for="color in RECIPE_BOOK_COLORS"
                        :key="color.name"
                        class="color-option"
                        :class="{ 'color-option-selected': selectedColor === color.name }"
                        @click="selectedColor = color.name"
                    >
                        <div
                            class="color-circle"
                            :style="{ backgroundColor: color.hex }"
                        ></div>
                        <span class="color-label">{{ color.label }}</span>
                        <i
                            v-if="selectedColor === color.name"
                            class="pi pi-check text-primary"
                        ></i>
                    </div>
                </div>
            </div>

            <!-- 미리보기 -->
            <div class="preview-box">
                <div class="preview-label">미리보기</div>
                <div class="preview-content">
                    <i
                        class="pi pi-bookmark text-3xl"
                        :style="{ color: getRecipeBookColorHex(selectedColor) }"
                    ></i>
                    <div class="ml-3">
                        <div class="font-medium text-lg">
                            {{ recipeBookName || '레시피북 이름' }}
                        </div>
                        <div class="text-sm text-gray-500">0개 레시피</div>
                    </div>
                </div>
            </div>
        </div>

        <template #footer>
            <Button label="취소" text @click="closeDialog" />
            <Button
                :label="isEditMode ? '수정' : '만들기'"
                @click="saveRecipeBook"
                :disabled="!recipeBookName.trim() || loading"
                :loading="loading"
            />
        </template>
    </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import { createRecipeBook, updateRecipeBook } from '@/api/bookmarkApi';
import { RECIPE_BOOK_COLORS, getRecipeBookColorHex } from '@/types/bookmark';
import type { RecipeBook } from '@/types/bookmark';
import { useAppToast } from '@/utils/toast';

const props = defineProps<{
    visible: boolean;
    recipeBook?: RecipeBook | null;
}>();

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'recipe-book-created', recipeBook: RecipeBook): void;
    (e: 'recipe-book-updated', recipeBook: RecipeBook): void;
}>();

const { showError } = useAppToast();

const visible = ref(props.visible);
const loading = ref(false);
const recipeBookName = ref('');
const selectedColor = ref('blue');
const isEditMode = ref(false);

watch(() => props.visible, (newVal) => {
    visible.value = newVal;
    if (newVal) {
        initForm();
    }
});

watch(visible, (newVal) => {
    emit('update:visible', newVal);
});

const initForm = () => {
    if (props.recipeBook) {
        isEditMode.value = true;
        recipeBookName.value = props.recipeBook.name;
        selectedColor.value = props.recipeBook.color;
    } else {
        isEditMode.value = false;
        recipeBookName.value = '';
        selectedColor.value = 'blue';
    }
};

const saveRecipeBook = async () => {
    const trimmedName = recipeBookName.value.trim();
    if (!trimmedName) {
        showError('레시피북 이름을 입력해주세요.');
        return;
    }
    try {
        loading.value = true;
        let saved: RecipeBook;
        if (isEditMode.value && props.recipeBook) {
            saved = await updateRecipeBook(props.recipeBook.id, trimmedName, selectedColor.value);
            emit('recipe-book-updated', saved);
        } else {
            saved = await createRecipeBook(trimmedName, selectedColor.value);
            emit('recipe-book-created', saved);
        }
        closeDialog();
    } catch (error: any) {
        console.error('레시피북 저장 실패:', error);
        if (error.message && error.message.includes('409')) {
            showError('이미 존재하는 레시피북 이름입니다.');
        } else {
            showError(
                isEditMode.value
                    ? '레시피북 수정에 실패했습니다.'
                    : '레시피북 생성에 실패했습니다.'
            );
        }
    } finally {
        loading.value = false;
    }
};

const closeDialog = () => {
    visible.value = false;
    recipeBookName.value = '';
    selectedColor.value = 'blue';
    isEditMode.value = false;
};
</script>

<style scoped lang="scss">
.recipe-book-form {
    padding: 0.5rem 0;
}

.color-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
}

.color-option {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem;
    border: 2px solid var(--surface-border);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background-color: var(--surface-hover);
        border-color: var(--primary-color);
    }

    &.color-option-selected {
        background-color: var(--primary-50);
        border-color: var(--primary-color);
    }
}

.color-circle {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    flex-shrink: 0;
}

.color-label {
    flex: 1;
    font-size: 0.9rem;
}

.preview-box {
    margin-top: 1.5rem;
    padding: 1rem;
    background-color: var(--surface-50);
    border-radius: 8px;
}

.preview-label {
    font-size: 0.85rem;
    color: var(--text-color-secondary);
    margin-bottom: 0.75rem;
}

.preview-content {
    display: flex;
    align-items: center;
}
</style>
