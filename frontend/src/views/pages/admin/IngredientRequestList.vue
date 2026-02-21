<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import Button from 'primevue/button';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Select from 'primevue/select';
import Tag from 'primevue/tag';
import Dialog from 'primevue/dialog';
import ProgressSpinner from 'primevue/progressspinner';
import { useToast } from 'primevue/usetoast';
import { getAdminIngredientRequests, updateIngredientRequestStatus } from '@/api/ingredientApi';
import type { IngredientRequestResponse } from '@/types/ingredient';

const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();

const requests = ref<IngredientRequestResponse[]>([]);
const totalCount = ref(0);
const totalPages = ref(0);
const currentPage = ref(0);
const pageSize = ref(20);
const loading = ref(false);
const error = ref<string | null>(null);
const filterStatus = ref<string | null>(null);
const detailVisible = ref(false);
const selectedRequest = ref<IngredientRequestResponse | null>(null);
const statusUpdating = ref(false);

const statusOptions = [
    { label: '전체', value: null },
    { label: '대기', value: 'PENDING' },
    { label: '처리 완료', value: 'PROCESSED' },
    { label: '반려', value: 'REJECTED' }
];

function requestTypeLabel(type: string): string {
    return type === 'PREPARATION' ? '손질법' : '보관법';
}

function requestTypeSeverity(type: string): 'info' | 'secondary' {
    return type === 'PREPARATION' ? 'info' : 'secondary';
}

function statusLabel(status: string): string {
    const map: Record<string, string> = {
        PENDING: '대기',
        PROCESSED: '처리 완료',
        REJECTED: '반려'
    };
    return map[status] ?? status;
}

function statusSeverity(status: string): 'warn' | 'success' | 'danger' | 'secondary' {
    const map: Record<string, 'warn' | 'success' | 'danger' | 'secondary'> = {
        PENDING: 'warn',
        PROCESSED: 'success',
        REJECTED: 'danger'
    };
    return map[status] ?? 'secondary';
}

function messagePreview(message: string | undefined): string {
    if (!message) return '-';
    return message.length > 30 ? message.slice(0, 30) + '…' : message;
}

function formatDate(value: string | undefined): string {
    if (!value) return '-';
    const d = new Date(value);
    return d.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function goBack() {
    router.push('/admin');
}

function onPage(event: { first: number; rows: number }) {
    currentPage.value = event.rows ? Math.floor(event.first / event.rows) : 0;
    loadRequests();
}

async function loadRequests() {
    loading.value = true;
    error.value = null;
    try {
        const res = await getAdminIngredientRequests({
            page: currentPage.value,
            size: pageSize.value,
            status: filterStatus.value ?? undefined
        });
        requests.value = res.requests;
        totalCount.value = res.totalCount;
        totalPages.value = res.totalPages;
    } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : '요청 목록을 불러오지 못했습니다.';
        error.value = msg;
        requests.value = [];
    } finally {
        loading.value = false;
    }
}

function openDetail(req: IngredientRequestResponse) {
    selectedRequest.value = req;
    detailVisible.value = true;
}

async function updateStatus(status: string) {
    if (!selectedRequest.value) return;
    statusUpdating.value = true;
    try {
        await updateIngredientRequestStatus(selectedRequest.value.id, status);
        selectedRequest.value = { ...selectedRequest.value, status };
        toast.add({
            severity: 'success',
            summary: '상태 변경',
            detail: status === 'PROCESSED' ? '처리 완료로 변경했습니다.' : '반려로 변경했습니다.',
            life: 3000
        });
        await loadRequests();
    } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : '상태 변경에 실패했습니다.';
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: msg,
            life: 3000
        });
    } finally {
        statusUpdating.value = false;
    }
}

onMounted(() => {
    if (!authStore.isAdmin) {
        router.replace('/');
        return;
    }
    loadRequests();
});
</script>

<template>
    <div class="ingredient-request-list">
        <div class="page-header mb-6">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goBack" />
                <h1 class="text-3xl font-bold text-gray-900">재료 정보 요청 목록</h1>
            </div>
            <p class="text-gray-600 mt-2">사용자가 재료 관리 페이지에서 요청한 목록을 확인하고 상태를 관리할 수 있습니다.</p>
        </div>

        <Card>
            <template #content>
                <!-- 필터 -->
                <div class="filter-row mb-4">
                    <label class="filter-label">상태</label>
                    <Select v-model="filterStatus" :options="statusOptions" option-label="label" option-value="value" placeholder="전체" class="filter-select" @change="loadRequests" />
                    <Button icon="pi pi-refresh" label="새로고침" severity="secondary" outlined @click="loadRequests" />
                </div>

                <!-- 로딩 -->
                <div v-if="loading" class="text-center py-8">
                    <ProgressSpinner />
                    <p class="text-gray-600 mt-3">요청 목록을 불러오는 중...</p>
                </div>

                <!-- 에러 -->
                <div v-else-if="error" class="text-center py-8">
                    <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                    <p class="text-gray-600 mb-4">{{ error }}</p>
                    <Button label="다시 시도" @click="loadRequests" />
                </div>

                <!-- 테이블 -->
                <div v-else>
                    <DataTable
                        :value="requests"
                        :paginator="true"
                        :first="currentPage * pageSize"
                        :rows="pageSize"
                        :total-records="totalCount"
                        :lazy="true"
                        :loading="loading"
                        data-key="id"
                        responsive-layout="scroll"
                        class="p-datatable-sm"
                        @page="onPage"
                    >
                        <template #empty>
                            <div class="text-center py-8 text-gray-500">
                                <i class="pi pi-inbox text-4xl mb-2"></i>
                                <p>요청이 없습니다.</p>
                            </div>
                        </template>
                        <Column field="createdAt" header="요청일시" sortable>
                            <template #body="{ data }">
                                {{ formatDate(data.createdAt) }}
                            </template>
                        </Column>
                        <Column field="ingredientName" header="재료명" />
                        <Column field="requestType" header="요청 유형">
                            <template #body="{ data }">
                                <Tag :value="requestTypeLabel(data.requestType)" :severity="requestTypeSeverity(data.requestType)" />
                            </template>
                        </Column>
                        <Column field="status" header="상태">
                            <template #body="{ data }">
                                <Tag :value="statusLabel(data.status)" :severity="statusSeverity(data.status)" />
                            </template>
                        </Column>
                        <Column header="요청자">
                            <template #body="{ data }">
                                {{ data.memberId != null ? `회원 #${data.memberId}` : '비회원' }}
                            </template>
                        </Column>
                        <Column header="메시지">
                            <template #body="{ data }">
                                <span class="message-preview">{{ messagePreview(data.message) }}</span>
                            </template>
                        </Column>
                        <Column header="관리" style="width: 100px">
                            <template #body="{ data }">
                                <Button icon="pi pi-eye" text rounded severity="secondary" title="상세 보기" @click="openDetail(data)" />
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </template>
        </Card>

        <!-- 상세 다이얼로그 -->
        <Dialog v-model:visible="detailVisible" header="재료 정보 요청 상세" :modal="true" :style="{ width: '90vw', maxWidth: '500px' }" :closable="true">
            <div v-if="selectedRequest" class="detail-content">
                <div class="detail-row">
                    <span class="detail-label">요청일시</span>
                    <span>{{ formatDate(selectedRequest.createdAt) }}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">재료명</span>
                    <span>{{ selectedRequest.ingredientName }}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">요청 유형</span>
                    <Tag :value="requestTypeLabel(selectedRequest.requestType)" :severity="requestTypeSeverity(selectedRequest.requestType)" />
                </div>
                <div class="detail-row">
                    <span class="detail-label">상태</span>
                    <Tag :value="statusLabel(selectedRequest.status)" :severity="statusSeverity(selectedRequest.status)" />
                </div>
                <div class="detail-row">
                    <span class="detail-label">요청자</span>
                    <span>{{ selectedRequest.memberId != null ? `회원 #${selectedRequest.memberId}` : '비회원' }}</span>
                </div>
                <div class="detail-row" v-if="selectedRequest.message">
                    <span class="detail-label">메시지</span>
                    <p class="detail-message">{{ selectedRequest.message }}</p>
                </div>
            </div>

            <template #footer>
                <div class="detail-actions">
                    <Button label="닫기" severity="secondary" outlined @click="detailVisible = false" />
                    <span v-if="selectedRequest && selectedRequest.status === 'PENDING'" class="status-actions">
                        <Button label="처리 완료" icon="pi pi-check" severity="success" :loading="statusUpdating" @click="updateStatus('PROCESSED')" />
                        <Button label="반려" icon="pi pi-times" severity="danger" outlined :loading="statusUpdating" @click="updateStatus('REJECTED')" />
                    </span>
                </div>
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.ingredient-request-list {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.filter-row {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.filter-label {
    font-weight: 600;
    color: var(--text-color);
    min-width: 60px;
}

.filter-select {
    width: 160px;
}

.message-preview {
    max-width: 180px;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.detail-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.detail-row {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.detail-label {
    font-weight: 600;
    color: var(--text-color-secondary);
    min-width: 90px;
    flex-shrink: 0;
}

.detail-message {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
}

.detail-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
}

.status-actions {
    display: flex;
    gap: 8px;
}

@media (max-width: 768px) {
    .ingredient-request-list {
        padding: 16px;
    }
}
</style>
