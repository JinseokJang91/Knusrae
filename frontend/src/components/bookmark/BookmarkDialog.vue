<template>
    <Dialog
        v-model:visible="visible"
        modal
        header="북마크에 저장"
        :style="{ width: '500px' }"
        :draggable="false"
    >
        <div class="bookmark-dialog-content">
            <!-- 로딩 상태 -->
            <div v-if="loading" class="flex justify-center items-center py-8">
                <i class="pi pi-spin pi-spinner text-4xl text-primary"></i>
            </div>

            <!-- 폴더 목록 -->
            <template v-else>
                <div class="mb-4">
                    <p class="text-gray-600 text-sm mb-3">레시피를 저장할 폴더를 선택하세요</p>
                    
                    <!-- 폴더가 없을 때 -->
                    <div v-if="folders.length === 0" class="text-center py-6">
                        <i class="pi pi-bookmark text-4xl text-gray-300 mb-3"></i>
                        <p class="text-gray-500 mb-4">아직 폴더가 없습니다</p>
                        <Button
                            label="첫 번째 폴더 만들기"
                            icon="pi pi-plus"
                            @click="openFolderDialog()"
                            outlined
                        />
                    </div>

                    <!-- 폴더 목록 -->
                    <div v-else class="folder-list">
                        <div
                            v-for="folder in folders"
                            :key="folder.id"
                            class="folder-item"
                            :class="{ 'folder-item-selected': selectedFolderIds.includes(folder.id) }"
                            @click="toggleFolder(folder.id)"
                        >
                            <div class="flex items-center gap-3 flex-1">
                                <i
                                    class="pi pi-bookmark text-2xl"
                                    :style="{ color: getFolderColorHex(folder.color) }"
                                ></i>
                                <div class="flex-1">
                                    <div class="font-medium">{{ folder.name }}</div>
                                    <div class="text-sm text-gray-500">{{ folder.bookmarkCount }}개 레시피</div>
                                </div>
                            </div>
                            <i
                                v-if="selectedFolderIds.includes(folder.id)"
                                class="pi pi-check text-xl text-primary"
                            ></i>
                        </div>
                    </div>
                </div>

                <!-- 새 폴더 만들기 버튼 -->
                <Button
                    label="새 폴더 만들기"
                    icon="pi pi-plus"
                    @click="openFolderDialog()"
                    outlined
                    class="w-full"
                />
            </template>
        </div>

        <template #footer>
            <Button label="취소" text @click="closeDialog" />
            <Button
                label="저장"
                @click="saveBookmarks"
                :disabled="loading || !hasChanges"
            />
        </template>
    </Dialog>

    <!-- 폴더 생성 Dialog -->
    <FolderFormDialog
        v-model:visible="folderDialogVisible"
        @folder-created="onFolderCreated"
    />
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import FolderFormDialog from './FolderFormDialog.vue';
import { getFolders, addBookmark, removeBookmark, checkBookmark } from '@/api/bookmarkApi';
import { getFolderColorHex } from '@/types/bookmark';
import type { BookmarkFolder } from '@/types/bookmark';
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
const folders = ref<BookmarkFolder[]>([]);
const selectedFolderIds = ref<number[]>([]);
const initialFolderIds = ref<number[]>([]);
const folderDialogVisible = ref(false);

/** 선택이 초기 상태와 달라졌을 때만 저장 가능 (전체 해제 후 저장 포함) */
const hasChanges = computed(() => {
    const add = selectedFolderIds.value.filter(id => !initialFolderIds.value.includes(id));
    const remove = initialFolderIds.value.filter(id => !selectedFolderIds.value.includes(id));
    return add.length > 0 || remove.length > 0;
});

// visible prop 변경 감지
watch(() => props.visible, (newVal) => {
    visible.value = newVal;
    if (newVal && props.recipeId) {
        loadData();
    }
});

// visible 변경 시 emit
watch(visible, (newVal) => {
    emit('update:visible', newVal);
});

/**
 * 데이터 로드
 */
const loadData = async () => {
    if (!props.recipeId) return;

    try {
        loading.value = true;

        // 폴더 목록 조회
        folders.value = await getFolders();

        // 현재 레시피가 저장된 폴더 확인
        const bookmarkStatus = await checkBookmark(props.recipeId);
        selectedFolderIds.value = [...bookmarkStatus.folders];
        initialFolderIds.value = [...bookmarkStatus.folders];
    } catch (error) {
        console.error('데이터 로드 실패:', error);
        showError('폴더 목록을 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

/**
 * 폴더 선택/해제 토글
 */
const toggleFolder = (folderId: number) => {
    const index = selectedFolderIds.value.indexOf(folderId);
    if (index > -1) {
        selectedFolderIds.value.splice(index, 1);
    } else {
        selectedFolderIds.value.push(folderId);
    }
};

/**
 * 북마크 저장
 */
const saveBookmarks = async () => {
    if (!props.recipeId) return;

    try {
        loading.value = true;

        // 추가할 폴더 (새로 선택된 폴더)
        const foldersToAdd = selectedFolderIds.value.filter(
            id => !initialFolderIds.value.includes(id)
        );

        // 제거할 폴더 (선택 해제된 폴더)
        const foldersToRemove = initialFolderIds.value.filter(
            id => !selectedFolderIds.value.includes(id)
        );

        // 북마크 추가
        for (const folderId of foldersToAdd) {
            await addBookmark(folderId, props.recipeId);
        }

        // 북마크 제거
        for (const folderId of foldersToRemove) {
            await removeBookmark(folderId, props.recipeId);
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

/**
 * 폴더 생성 Dialog 열기
 */
const openFolderDialog = () => {
    folderDialogVisible.value = true;
};

/**
 * 폴더 생성 완료 처리
 */
const onFolderCreated = async (folder: BookmarkFolder) => {
    // 폴더 목록 새로고침
    folders.value = await getFolders();
    
    // 새로 생성된 폴더 자동 선택
    if (!selectedFolderIds.value.includes(folder.id)) {
        selectedFolderIds.value.push(folder.id);
    }
};

/**
 * Dialog 닫기
 */
const closeDialog = () => {
    visible.value = false;
    selectedFolderIds.value = [];
    initialFolderIds.value = [];
};
</script>

<style scoped lang="scss">
.bookmark-dialog-content {
    min-height: 200px;
}

.folder-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    max-height: 400px;
    overflow-y: auto;
}

.folder-item {
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

    &.folder-item-selected {
        background-color: var(--primary-50);
        border-color: var(--primary-color);
    }
}
</style>
