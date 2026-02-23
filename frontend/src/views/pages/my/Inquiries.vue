<script setup lang="ts">
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import InquiryFormDialog from '@/components/inquiry/InquiryFormDialog.vue';
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

function goToDetail(id: number) {
    router.push({ name: 'inquiryDetail', params: { id: String(id) } });
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
}

function confirmDelete(row: InquiryListItem) {
    confirm.require({
        group: 'inquiry-delete',
        header: '문의 삭제',
        message: `"${row.title}" 문의를 삭제하시겠습니까?`,
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        acceptLabel: '삭제',
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
    <div class="page-container page-container--card">
        <div class="inquiries-content">
            <div class="inquiries-section">
                <div class="flex justify-between items-center mb-3 flex-wrap gap-2">
                    <h2 class="text-2xl font-semibold text-gray-900 m-0">1:1 문의 내역 ({{ totalElements }})</h2>
                    <Button label="문의하기" icon="pi pi-plus" @click="openCreateDialog" />
                </div>

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
                    <DataTable :value="items" :paginator="true" :first="first" :rows="rows" :total-records="totalElements" :lazy="true" :loading="false" data-key="id" responsive-layout="scroll" class="p-datatable-sm" @page="onPageChange">
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
                                <a href="#" class="inquiry-title-link" @click.prevent="goToDetail(data.id)">
                                    {{ data.title }}
                                </a>
                            </template>
                        </Column>
                        <Column header="답변" style="width: 140px">
                            <template #body="{ data }">
                                <span v-if="!data.hasReply" class="text-secondary"></span>
                                <button v-else type="button" class="inquiry-complete-badge" @click="goToDetail(data.id)">
                                    <Badge value="완료" severity="success" />
                                </button>
                            </template>
                        </Column>
                        <Column header="작성일시" style="min-width: 140px">
                            <template #body="{ data }">
                                {{ formatDate(data.createdAt) }}
                            </template>
                        </Column>
                        <Column header="삭제" style="width: 80px">
                            <template #body="{ data }">
                                <Button icon="pi pi-trash" text rounded severity="danger" title="삭제" @click="confirmDelete(data)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </div>

            <InquiryFormDialog v-model:visible="formDialogVisible" :inquiry-id="editingInquiryId" @saved="onInquirySaved" @closed="onFormClosed" />

            <ConfirmDialog group="inquiry-delete" />
        </div>
    </div>
</template>

<style scoped>
.inquiries-content {
    min-height: 240px;
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

.inquiry-complete-badge {
    border: none;
    background: none;
    padding: 0;
    cursor: pointer;
}

.inquiry-complete-badge:hover :deep(.p-badge) {
    opacity: 0.9;
}
</style>
