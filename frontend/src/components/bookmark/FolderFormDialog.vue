<template>
    <Dialog
        v-model:visible="visible"
        modal
        :header="isEditMode ? '폴더 수정' : '새 폴더 만들기'"
        :style="{ width: '450px' }"
        :draggable="false"
    >
        <div class="folder-form">
            <!-- 폴더 이름 -->
            <div class="mb-4">
                <label class="block mb-2 font-medium">폴더 이름</label>
                <InputText
                    v-model="folderName"
                    placeholder="예: 한식 레시피, 간식 모음"
                    class="w-full"
                    :maxlength="50"
                    @keyup.enter="saveFolder"
                />
                <small class="text-gray-500">{{ folderName.length }}/50</small>
            </div>

            <!-- 폴더 색상 -->
            <div class="mb-4">
                <label class="block mb-2 font-medium">폴더 색상</label>
                <div class="color-grid">
                    <div
                        v-for="color in FOLDER_COLORS"
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
                        :style="{ color: getFolderColorHex(selectedColor) }"
                    ></i>
                    <div class="ml-3">
                        <div class="font-medium text-lg">
                            {{ folderName || '폴더 이름' }}
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
                @click="saveFolder"
                :disabled="!folderName.trim() || loading"
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
import { createFolder, updateFolder } from '@/api/bookmarkApi';
import { FOLDER_COLORS, getFolderColorHex } from '@/types/bookmark';
import type { BookmarkFolder } from '@/types/bookmark';
import { useAppToast } from '@/utils/toast';

const props = defineProps<{
    visible: boolean;
    folder?: BookmarkFolder | null;
}>();

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'folder-created', folder: BookmarkFolder): void;
    (e: 'folder-updated', folder: BookmarkFolder): void;
}>();

const { showError } = useAppToast();

const visible = ref(props.visible);
const loading = ref(false);
const folderName = ref('');
const selectedColor = ref('blue');
const isEditMode = ref(false);

// visible prop 변경 감지
watch(() => props.visible, (newVal) => {
    visible.value = newVal;
    if (newVal) {
        initForm();
    }
});

// visible 변경 시 emit
watch(visible, (newVal) => {
    emit('update:visible', newVal);
});

/**
 * 폼 초기화
 */
const initForm = () => {
    if (props.folder) {
        // 수정 모드
        isEditMode.value = true;
        folderName.value = props.folder.name;
        selectedColor.value = props.folder.color;
    } else {
        // 생성 모드
        isEditMode.value = false;
        folderName.value = '';
        selectedColor.value = 'blue';
    }
};

/**
 * 폴더 저장 (생성 또는 수정)
 */
const saveFolder = async () => {
    const trimmedName = folderName.value.trim();
    
    if (!trimmedName) {
        showError('폴더 이름을 입력해주세요.');
        return;
    }

    try {
        loading.value = true;

        let savedFolder: BookmarkFolder;

        if (isEditMode.value && props.folder) {
            // 폴더 수정
            savedFolder = await updateFolder(props.folder.id, trimmedName, selectedColor.value);
            emit('folder-updated', savedFolder);
        } else {
            // 폴더 생성
            savedFolder = await createFolder(trimmedName, selectedColor.value);
            emit('folder-created', savedFolder);
        }

        closeDialog();
    } catch (error: any) {
        console.error('폴더 저장 실패:', error);
        
        if (error.message && error.message.includes('409')) {
            showError('이미 존재하는 폴더 이름입니다.');
        } else {
            showError(
                isEditMode.value
                    ? '폴더 수정에 실패했습니다.'
                    : '폴더 생성에 실패했습니다.'
            );
        }
    } finally {
        loading.value = false;
    }
};

/**
 * Dialog 닫기
 */
const closeDialog = () => {
    visible.value = false;
    folderName.value = '';
    selectedColor.value = 'blue';
    isEditMode.value = false;
};
</script>

<style scoped lang="scss">
.folder-form {
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
