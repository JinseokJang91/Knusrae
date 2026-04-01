<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Dialog from 'primevue/dialog';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import InquiryFormDialog from '@/components/inquiry/InquiryFormDialog.vue';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import ConfirmDialog from 'primevue/confirmdialog';
import { useConfirm } from 'primevue/useconfirm';
import { getInquiryDetail, deleteInquiry } from '@/api/inquiryApi';
import { getInquiryTypeLabel } from '@/types/inquiry';
import type { InquiryDetail as InquiryDetailType } from '@/types/inquiry';
import { useAppToast } from '@/utils/toast';

const props = defineProps<{
    visible: boolean;
    inquiryId: number | null;
}>();

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'deleted'): void;
}>();

const confirm = useConfirm();
const { showSuccess, showError } = useAppToast();

const detail = ref<InquiryDetailType | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const formDialogVisible = ref(false);
const editingInquiryId = ref<number | null>(null);

async function loadDetail() {
    const id = props.inquiryId;
    if (!id) return;
    try {
        loading.value = true;
        error.value = null;
        detail.value = await getInquiryDetail(id);
    } catch (err: unknown) {
        console.error('문의 상세 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '문의를 불러오는데 실패했습니다.';
        detail.value = null;
    } finally {
        loading.value = false;
    }
}

function close() {
    emit('update:visible', false);
}

function formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function openEditDialog() {
    if (detail.value?.reply) return;
    editingInquiryId.value = detail.value?.id ?? null;
    formDialogVisible.value = true;
}

function confirmDelete() {
    if (!detail.value) return;
    confirm.require({
        group: 'inquiry-detail-delete',
        header: '문의 삭제',
        message: `"${detail.value.title}" 문의를 삭제하시겠습니까?`,
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        rejectClass: 'p-button-secondary',
        acceptLabel: '확인',
        acceptClass: 'p-button-danger',
        accept: async () => {
            try {
                await deleteInquiry(detail.value!.id);
                showSuccess('문의가 삭제되었습니다.');
                emit('deleted');
                close();
            } catch (err: unknown) {
                showError((err instanceof Error ? err.message : null) || '삭제에 실패했습니다.');
            }
        }
    });
}

function onInquirySaved() {
    formDialogVisible.value = false;
    editingInquiryId.value = null;
    loadDetail();
}

function onFormClosed() {
    editingInquiryId.value = null;
}

const visibleModel = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
});

watch(
    () => [props.visible, props.inquiryId] as const,
    ([visible, id]) => {
        if (visible && id) {
            detail.value = null;
            error.value = null;
            loadDetail();
        } else if (!visible) {
            detail.value = null;
            error.value = null;
        }
    }
);
</script>

<template>
    <Dialog
        v-model:visible="visibleModel"
        header="문의 상세"
        :modal="true"
        :style="{ width: '90vw', maxWidth: '560px' }"
        :closable="true"
        class="inquiry-detail-dialog"
        @hide="close"
    >
        <div class="inquiry-detail-dialog__body">
            <PageStateBlock v-if="loading" state="loading" loading-message="문의를 불러오는 중..." />
            <PageStateBlock v-else-if="error" state="error" error-title="문의를 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadDetail" />
            <PageStateBlock v-else-if="!detail" state="empty" empty-icon="pi pi-inbox" empty-title="문의를 찾을 수 없습니다" empty-message="권한이 없거나 삭제된 문의일 수 있습니다." empty-button-label="닫기" @empty-action="close" />

            <template v-else>
                <div class="detail-body">
                    <div class="detail-meta mb-4">
                        <span class="inquiry-type-badge">{{ getInquiryTypeLabel(detail.inquiryType) }}</span>
                        <span class="text-color-secondary text-sm ml-2">{{ formatDate(detail.createdAt) }}</span>
                    </div>
                    <h2 class="detail-title">{{ detail.title }}</h2>
                    <div class="detail-inquiry-area">
                        <div class="detail-content">{{ detail.content }}</div>
                        <div v-if="detail.imageUrls && detail.imageUrls.length > 0" class="detail-images">
                            <img v-for="(url, i) in detail.imageUrls" :key="i" :src="url" alt="첨부 이미지" class="detail-image" />
                        </div>
                    </div>

                    <Divider />

                    <div class="reply-section">
                        <h3 class="reply-heading mb-2">관리자 답변</h3>
                        <div v-if="detail.reply" class="reply-content">
                            <p class="m-0">{{ detail.reply.content }}</p>
                            <small class="text-color-secondary">{{ formatDate(detail.reply.repliedAt) }}</small>
                        </div>
                        <p v-else class="text-color-secondary m-0">답변 대기 중입니다.</p>
                    </div>
                </div>

                <div class="flex justify-end gap-2 mt-4 flex-wrap">
                    <Button v-if="!detail.reply" label="수정" severity="secondary" outlined @click="openEditDialog" />
                    <Button label="삭제" severity="danger" outlined @click="confirmDelete" />
                </div>
            </template>
        </div>

        <InquiryFormDialog v-model:visible="formDialogVisible" :inquiry-id="editingInquiryId" @saved="onInquirySaved" @closed="onFormClosed" />

        <ConfirmDialog group="inquiry-detail-delete" />
    </Dialog>
</template>

<style scoped>
.inquiry-detail-dialog__body {
    min-height: 420px;
    display: flex;
    flex-direction: column;
}

.detail-body {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.detail-meta {
    display: flex;
    align-items: center;
}

.detail-inquiry-area {
    min-height: 160px;
    max-height: 240px;
    overflow-y: auto;
    margin-bottom: 1rem;
    padding: 0.875rem 1rem;
    background: var(--p-surface-100, #f3f4f6);
    border-radius: 8px;
    border: 1px solid var(--p-surface-200, #e5e7eb);
}

.inquiry-type-badge {
    display: inline-block;
    padding: 0.25rem 0.5rem;
    background: #374151;
    color: white;
    border-radius: 6px;
    font-size: 0.875rem;
}

.detail-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0 0 1rem 0;
}

.detail-content {
    white-space: pre-wrap;
    line-height: 1.6;
    padding-right: 0.25rem;
    color: var(--p-text-color, #374151);
}

.detail-images {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-top: 0.75rem;
}

.detail-image {
    max-width: 200px;
    max-height: 200px;
    object-fit: cover;
    border-radius: 8px;
    border: 1px solid var(--p-surface-300);
}

.reply-section {
    padding-top: 1rem;
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;
}

.reply-heading {
    font-size: 0.9375rem;
    font-weight: 600;
    color: var(--p-text-color-secondary, #6b7280);
    margin: 0 0 0.5rem 0;
}

.reply-content {
    padding: 0.875rem 1rem;
    background: var(--p-surface-100, #f3f4f6);
    border-radius: 8px;
    border: 1px solid var(--p-surface-200, #e5e7eb);
    line-height: 1.6;
}

.reply-content p {
    margin: 0 0 0.5rem 0;
    color: var(--p-text-color, #374151);
}

.reply-content small {
    display: block;
    font-size: 0.8125rem;
    color: var(--p-text-color-secondary, #6b7280);
}
</style>

<!-- Dialog는 body로 텔레포트되므로 비-scoped로 X 버튼만 타깃 -->
<style>
.inquiry-detail-dialog .p-dialog-close-button,
.inquiry-detail-dialog .p-dialog-close-button:focus,
.inquiry-detail-dialog .p-dialog-close-button:focus-visible {
    border: none;
    outline: none;
    box-shadow: none;
}
</style>
