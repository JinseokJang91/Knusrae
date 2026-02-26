<script setup lang="ts">
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import InquiryFormDialog from '@/components/inquiry/InquiryFormDialog.vue';
import InquiryDetailDialog from '@/components/inquiry/InquiryDetailDialog.vue';
import Button from 'primevue/button';
import Badge from 'primevue/badge';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ConfirmDialog from 'primevue/confirmdialog';
import { useConfirm } from 'primevue/useconfirm';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { getMyInquiries, deleteInquiry } from '@/api/inquiryApi';
import { getInquiryTypeLabel } from '@/types/inquiry';
import type { InquiryListItem } from '@/types/inquiry';
import type { PageState } from 'primevue/paginator';
import { useAppToast } from '@/utils/toast';

const router = useRouter();
const authStore = useAuthStore();
const confirm = useConfirm();
const { showSuccess, showError } = useAppToast();

const items = ref<InquiryListItem[]>([]);
const totalElements = ref(0);
const first = ref(0);
const rows = ref(10);
const loading = ref(false);
const error = ref<string | null>(null);
const formDialogVisible = ref(false);
const editingInquiryId = ref<number | null>(null);
const detailDialogVisible = ref(false);
const selectedInquiryId = ref<number | null>(null);

const currentMemberId = computed(() => authStore.memberInfo?.id ?? null);

async function loadInquiries() {
    if (!currentMemberId.value) return;
    try {
        loading.value = true;
        error.value = null;
        const page = Math.floor(first.value / rows.value);
        const response = await getMyInquiries(page, rows.value);
        items.value = response.content ?? [];
        totalElements.value = response.totalElements ?? 0;
    } catch (err: unknown) {
        console.error('문의 목록 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '문의 내역을 불러오는데 실패했습니다.';
        items.value = [];
    } finally {
        loading.value = false;
    }
}

function openCreateDialog() {
    if (!currentMemberId.value) {
        router.push({ path: '/auth/login', query: { redirect: '/my?tab=inquiries' } });
        return;
    }
    editingInquiryId.value = null;
    formDialogVisible.value = true;
}

function openDetailDialog(id: number) {
    selectedInquiryId.value = id;
    detailDialogVisible.value = true;
}

function onDetailDeleted() {
    detailDialogVisible.value = false;
    selectedInquiryId.value = null;
    loadInquiries();
}

function goToLogin() {
    router.push({ path: '/auth/login', query: { redirect: '/my?tab=inquiries' } });
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

function onPageChange(event: PageState) {
    first.value = event.first;
    rows.value = event.rows;
    loadInquiries();
}

function confirmDelete(row: InquiryListItem) {
    confirm.require({
        group: 'inquiry-delete',
        header: '문의 삭제',
        message: `"${row.title}" 문의를 삭제하시겠습니까?`,
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        rejectClass: 'p-button-secondary',
        acceptLabel: '확인',
        acceptClass: 'p-button-danger',
        accept: async () => {
            try {
                await deleteInquiry(row.id);
                showSuccess('문의가 삭제되었습니다.');
                await loadInquiries();
            } catch (err: unknown) {
                showError((err instanceof Error ? err.message : null) || '삭제에 실패했습니다.');
            }
        }
    });
}

function onInquirySaved() {
    formDialogVisible.value = false;
    editingInquiryId.value = null;
    loadInquiries();
}

function onFormClosed() {
    editingInquiryId.value = null;
}

onMounted(() => {
    if (currentMemberId.value) {
        loadInquiries();
    }
});

watch(currentMemberId, (id) => {
    if (id) {
        first.value = 0;
        loadInquiries();
    } else {
        items.value = [];
        totalElements.value = 0;
        error.value = null;
    }
});
</script>

<template>
    <div class="page-container page-container--card page-container--wide inquiries-card">
        <div class="inquiries-content">
            <div class="inquiries-notice mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
                <i class="pi pi-info-circle inquiries-notice__icon" aria-hidden="true"></i>
                <p class="inquiries-notice__text">제목을 클릭하면 문의 상세 팝업에서 답변을 확인할 수 있어요.</p>
            </div>

            <div class="flex justify-between items-center mb-3 flex-wrap gap-2">
                <h2 class="inquiries-title">문의내역</h2>
                <Button label="문의하기" icon="pi pi-plus" size="small" @click="openCreateDialog" />
            </div>

            <div class="inquiries-section">
                <PageStateBlock v-if="loading" state="loading" loading-message="문의 내역을 불러오는 중..." />
                <PageStateBlock v-else-if="error" state="error" error-title="문의 내역을 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadInquiries" />
                <PageStateBlock v-else-if="!currentMemberId" state="empty" empty-icon="pi pi-lock" empty-title="로그인이 필요합니다" empty-message="1:1 문의 내역을 보려면 로그인해 주세요." empty-button-label="로그인하기" @empty-action="goToLogin" />
                <PageStateBlock
                    v-else-if="items.length === 0"
                    state="empty"
                    empty-icon="pi pi-inbox"
                    empty-title="문의 내역이 없습니다"
                    empty-message="궁금한 점이 있으시면 문의해 주세요."
                    empty-button-label="문의하기"
                    @empty-action="openCreateDialog"
                />

                <template v-else>
                    <div class="inquiries-table-wrap">
                        <DataTable
                            :value="items"
                            :paginator="true"
                            :first="first"
                            :rows="rows"
                            :total-records="totalElements"
                            :lazy="true"
                            :loading="loading"
                            data-key="id"
                            responsive-layout="scroll"
                            class="p-datatable-sm inquiries-datatable"
                            @page="onPageChange"
                        >
                            <template #empty>
                                <div class="text-center py-8 text-gray-500">
                                    <i class="pi pi-inbox text-4xl mb-2"></i>
                                    <p>문의 내역이 없습니다.</p>
                                </div>
                            </template>
                            <Column header="No" style="width: 70px">
                                <template #body="{ index }">
                                    {{ first + index + 1 }}
                                </template>
                            </Column>
                            <Column field="inquiryType" header="문의유형" style="min-width: 120px">
                                <template #body="{ data }">
                                    {{ getInquiryTypeLabel(data.inquiryType) }}
                                </template>
                            </Column>
                            <Column header="제목" style="min-width: 200px">
                                <template #body="{ data }">
                                    <a href="#" class="inquiry-title-link" @click.prevent="openDetailDialog(data.id)">
                                        {{ data.title }}
                                    </a>
                                </template>
                            </Column>
                            <Column header="답변" style="width: 140px">
                                <template #body="{ data }">
                                    <Badge v-if="!data.hasReply" value="대기" severity="secondary" />
                                    <Badge v-else value="완료" severity="success" />
                                </template>
                            </Column>
                            <Column header="작성일시" style="min-width: 140px">
                                <template #body="{ data }">
                                    <span class="inquiry-date-cell">{{ formatDate(data.createdAt) }}</span>
                                </template>
                            </Column>
                            <Column header="삭제" style="width: 80px">
                                <template #body="{ data }">
                                    <Button icon="pi pi-trash" text rounded size="small" severity="danger" title="삭제" @click="confirmDelete(data)" />
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </template>
            </div>

            <InquiryFormDialog v-model:visible="formDialogVisible" :inquiry-id="editingInquiryId" @saved="onInquirySaved" @closed="onFormClosed" />
            <InquiryDetailDialog v-model:visible="detailDialogVisible" :inquiry-id="selectedInquiryId" @deleted="onDetailDeleted" />

            <ConfirmDialog group="inquiry-delete" />
        </div>
    </div>
</template>

<style scoped>
/* Comments 페이지와 동일한 오렌지 톤 카드 배경 */
.inquiries-card {
    background: #ffedd5;
}

.inquiries-notice {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
}

.inquiries-notice__icon {
    font-size: 1.25rem;
    color: var(--orange-500, #f97316);
    flex-shrink: 0;
    margin-top: 0.125rem;
}

.inquiries-notice__text {
    margin: 0;
    color: #374151;
    font-style: italic;
    font-size: 0.9375rem;
    line-height: 1.5;
    letter-spacing: 0.01em;
}

.inquiries-title {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--p-text-color, #374151);
}

/* 테이블 영역: 카드형 래퍼로 오렌지 배경과 자연스럽게 구분 */
.inquiries-table-wrap {
    background: var(--p-card-background, #ffffff);
    border: 1px solid var(--p-card-border-color, rgba(0, 0, 0, 0.08));
    border-radius: var(--p-card-border-radius, 12px);
    box-shadow: var(--p-card-shadow, 0 1px 3px rgba(0, 0, 0, 0.06));
    overflow: hidden;
    padding-top: 1rem;
}

.inquiries-datatable :deep(.p-datatable-wrapper) {
    border: none;
}

.inquiries-datatable :deep(.p-datatable-header),
.inquiries-datatable :deep(.p-datatable-thead > tr > th),
.inquiries-datatable :deep(.p-datatable-tbody > tr > td) {
    background: transparent;
    border-color: var(--p-surface-border, rgba(0, 0, 0, 0.08));
}

/* 헤더와 테이블 상단 테두리 사이 여백 */
.inquiries-datatable :deep(.p-datatable-thead > tr > th) {
    padding-top: 1rem;
}

/* No 컬럼 좌측 여백으로 카드 가장자리와 거리 두기 */
.inquiries-datatable :deep(.p-datatable-thead > tr > th:first-child),
.inquiries-datatable :deep(.p-datatable-tbody > tr > td:first-child) {
    padding-left: 1.25rem;
}

.inquiries-content {
    min-height: 500px;
}

.inquiries-section {
    width: 100%;
}

.inquiry-title-link {
    color: var(--p-primary-color);
    text-decoration: none;
}

.inquiry-title-link:hover {
    text-decoration: underline;
}

.inquiry-date-cell {
    font-size: 0.8125rem;
}

</style>
