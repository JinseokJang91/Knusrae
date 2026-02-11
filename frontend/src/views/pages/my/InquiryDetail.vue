<template>
    <div class="inquiry-detail">
        <div class="page-header mb-4">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goToList" />
                <h1 class="text-2xl font-bold text-gray-900 m-0">문의 상세</h1>
            </div>
        </div>

        <PageStateBlock
            v-if="loading"
            state="loading"
            loading-message="문의를 불러오는 중..."
        />
        <PageStateBlock
            v-else-if="error"
            state="error"
            error-title="문의를 불러올 수 없습니다"
            :error-message="error"
            retry-label="다시 시도"
            @retry="loadDetail"
        />
        <PageStateBlock
            v-else-if="!detail"
            state="empty"
            empty-icon="pi pi-inbox"
            empty-title="문의를 찾을 수 없습니다"
            empty-message="권한이 없거나 삭제된 문의일 수 있습니다."
            empty-button-label="목록으로"
            @empty-action="goToList"
        />

        <Card v-else>
            <template #content>
                <div class="detail-body">
                    <div class="detail-meta mb-4">
                        <span class="inquiry-type-badge">{{ getInquiryTypeLabel(detail.inquiryType) }}</span>
                        <span class="text-color-secondary text-sm ml-2">{{ formatDate(detail.createdAt) }}</span>
                    </div>
                    <h2 class="detail-title mb-3">{{ detail.title }}</h2>
                    <div class="detail-content mb-4">{{ detail.content }}</div>
                    <div v-if="detail.imageUrls && detail.imageUrls.length > 0" class="detail-images mb-4">
                        <img
                            v-for="(url, i) in detail.imageUrls"
                            :key="i"
                            :src="url"
                            alt="첨부 이미지"
                            class="detail-image"
                        />
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
            </template>
            <template #footer>
                <div class="flex justify-between flex-wrap gap-2">
                    <Button label="목록으로" severity="secondary" outlined @click="goToList" />
                    <div class="flex gap-2">
                        <Button
                            v-if="!detail.reply"
                            label="수정"
                            severity="secondary"
                            outlined
                            @click="openEditDialog"
                        />
                        <Button
                            label="삭제"
                            severity="danger"
                            outlined
                            @click="confirmDelete"
                        />
                    </div>
                </div>
            </template>
        </Card>

        <InquiryFormDialog
            v-model:visible="formDialogVisible"
            :inquiry-id="editingInquiryId"
            @saved="onInquirySaved"
            @closed="onFormClosed"
        />

        <ConfirmDialog group="inquiry-detail-delete" />
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import InquiryFormDialog from '@/components/inquiry/InquiryFormDialog.vue';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import ConfirmDialog from 'primevue/confirmdialog';
import { useConfirm } from 'primevue/useconfirm';
import { getInquiryDetail, deleteInquiry } from '@/api/inquiryApi';
import { getInquiryTypeLabel } from '@/types/inquiry';
import type { InquiryDetail as InquiryDetailType } from '@/types/inquiry';
import { useAppToast } from '@/utils/toast';

const route = useRoute();
const router = useRouter();
const confirm = useConfirm();
const { showSuccess, showError } = useAppToast();

const detail = ref<InquiryDetailType | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const formDialogVisible = ref(false);
const editingInquiryId = ref<number | null>(null);

const inquiryId = ref<number | null>(null);

async function loadDetail() {
    const id = inquiryId.value;
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

function goToList() {
    router.push('/my?tab=inquiries');
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
        acceptLabel: '삭제',
        acceptClass: 'p-button-danger',
        accept: async () => {
            try {
                await deleteInquiry(detail.value!.id);
                showSuccess('문의가 삭제되었습니다.');
                goToList();
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

onMounted(() => {
    const id = route.params.id;
    const num = typeof id === 'string' ? parseInt(id, 10) : NaN;
    if (!Number.isNaN(num)) {
        inquiryId.value = num;
        loadDetail();
    } else {
        error.value = '잘못된 문의 번호입니다.';
    }
});

watch(
    () => route.params.id,
    (id) => {
        const num = typeof id === 'string' ? parseInt(id, 10) : NaN;
        inquiryId.value = Number.isNaN(num) ? null : num;
        if (inquiryId.value) loadDetail();
    }
);
</script>

<style scoped>
.inquiry-detail {
    max-width: 720px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 1rem;
}

.detail-meta {
    display: flex;
    align-items: center;
}

.inquiry-type-badge {
    display: inline-block;
    padding: 0.25rem 0.5rem;
    background: var(--p-primary-color);
    color: white;
    border-radius: 6px;
    font-size: 0.875rem;
}

.detail-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
}

.detail-content {
    white-space: pre-wrap;
    line-height: 1.6;
}

.detail-images {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
}

.detail-image {
    max-width: 200px;
    max-height: 200px;
    object-fit: cover;
    border-radius: 8px;
    border: 1px solid var(--p-surface-300);
}

.reply-section {
    padding-top: 0.5rem;
}

.reply-heading {
    font-size: 1rem;
    font-weight: 600;
    margin: 0 0 0.5rem 0;
}

.reply-content {
    padding: 1rem;
    background: var(--p-surface-50);
    border-radius: 8px;
    border-left: 4px solid var(--p-primary-color);
}
</style>
