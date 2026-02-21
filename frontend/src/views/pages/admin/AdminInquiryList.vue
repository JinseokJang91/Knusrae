<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import Button from 'primevue/button';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Tag from 'primevue/tag';
import Dialog from 'primevue/dialog';
import Divider from 'primevue/divider';
import Textarea from 'primevue/textarea';
import ProgressSpinner from 'primevue/progressspinner';
import { getAdminInquiries, getAdminInquiryDetail, submitInquiryReply } from '@/api/inquiryApi';
import { getInquiryTypeLabel } from '@/types/inquiry';
import type { InquiryListItem } from '@/types/inquiry';
import type { InquiryDetail } from '@/types/inquiry';
import { useAppToast } from '@/utils/toast';

const router = useRouter();
const authStore = useAuthStore();
const { showSuccess, showError } = useAppToast();

const items = ref<InquiryListItem[]>([]);
const totalElements = ref(0);
const currentPage = ref(0);
const pageSize = ref(20);
const loading = ref(false);
const error = ref<string | null>(null);

const detailVisible = ref(false);
const detailLoading = ref(false);
const selectedDetail = ref<InquiryDetail | null>(null);
const replyContent = ref('');
const replySubmitting = ref(false);

async function loadInquiries() {
    try {
        loading.value = true;
        error.value = null;
        const res = await getAdminInquiries(currentPage.value, pageSize.value);
        items.value = res.content ?? [];
        totalElements.value = res.totalElements ?? 0;
    } catch (err: unknown) {
        console.error('관리자 문의 목록 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '문의 목록을 불러오는데 실패했습니다.';
        items.value = [];
    } finally {
        loading.value = false;
    }
}

async function openDetail(id: number) {
    detailVisible.value = true;
    selectedDetail.value = null;
    replyContent.value = '';
    try {
        detailLoading.value = true;
        selectedDetail.value = await getAdminInquiryDetail(id);
    } catch (err: unknown) {
        showError((err instanceof Error ? err.message : null) || '문의 상세를 불러오는데 실패했습니다.');
        detailVisible.value = false;
    } finally {
        detailLoading.value = false;
    }
}

async function submitReply() {
    if (!selectedDetail.value || !replyContent.value.trim()) return;
    try {
        replySubmitting.value = true;
        const updated = await submitInquiryReply(selectedDetail.value.id, replyContent.value.trim());
        selectedDetail.value = updated;
        replyContent.value = '';
        showSuccess('답변이 등록되었습니다.');
        await loadInquiries();
    } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : null;
        showError(msg || '답변 등록에 실패했습니다.');
    } finally {
        replySubmitting.value = false;
    }
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

function onPage(event: { first: number; rows: number }) {
    currentPage.value = Math.floor(event.first / event.rows);
    pageSize.value = event.rows;
    loadInquiries();
}

function goBack() {
    router.push('/admin');
}

watch(detailVisible, (visible) => {
    if (!visible) {
        selectedDetail.value = null;
        replyContent.value = '';
    }
});

onMounted(() => {
    if (authStore.isAdmin) {
        loadInquiries();
    } else {
        router.replace('/');
    }
});
</script>

<template>
    <div class="admin-inquiry-list">
        <div class="page-header mb-6">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goBack" />
                <h1 class="text-3xl font-bold text-gray-900">1:1 문의 목록</h1>
            </div>
            <p class="text-gray-600 mt-2">사용자가 등록한 문의 내역을 확인하고 답변을 등록할 수 있습니다.</p>
        </div>

        <Card>
            <template #content>
                <div v-if="loading" class="text-center py-8">
                    <ProgressSpinner />
                    <p class="text-gray-600 mt-3">문의 목록을 불러오는 중...</p>
                </div>

                <div v-else-if="error" class="text-center py-8">
                    <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                    <p class="text-gray-600 mb-4">{{ error }}</p>
                    <Button label="다시 시도" @click="loadInquiries" />
                </div>

                <div v-else>
                    <DataTable
                        :value="items"
                        :paginator="true"
                        :first="currentPage * pageSize"
                        :rows="pageSize"
                        :total-records="totalElements"
                        :lazy="true"
                        :loading="false"
                        data-key="id"
                        responsive-layout="scroll"
                        class="p-datatable-sm"
                        @page="onPage"
                    >
                        <template #empty>
                            <div class="text-center py-8 text-gray-500">
                                <i class="pi pi-inbox text-4xl mb-2"></i>
                                <p>등록된 문의가 없습니다.</p>
                            </div>
                        </template>
                        <Column header="No" style="width: 70px">
                            <template #body="{ index }">
                                {{ currentPage * pageSize + index + 1 }}
                            </template>
                        </Column>
                        <Column header="회원" style="width: 100px">
                            <template #body="{ data }"> 회원 #{{ data.memberId ?? '-' }} </template>
                        </Column>
                        <Column field="inquiryType" header="문의유형" style="min-width: 120px">
                            <template #body="{ data }">
                                {{ getInquiryTypeLabel(data.inquiryType) }}
                            </template>
                        </Column>
                        <Column field="title" header="제목" style="min-width: 200px">
                            <template #body="{ data }">
                                <span class="title-text">{{ data.title }}</span>
                            </template>
                        </Column>
                        <Column header="답변" style="width: 100px">
                            <template #body="{ data }">
                                <Tag :value="data.hasReply ? '답변완료' : '미답변'" :severity="data.hasReply ? 'success' : 'warning'" />
                            </template>
                        </Column>
                        <Column header="작성일시" style="min-width: 140px">
                            <template #body="{ data }">
                                {{ formatDate(data.createdAt) }}
                            </template>
                        </Column>
                        <Column header="관리" style="width: 90px">
                            <template #body="{ data }">
                                <Button icon="pi pi-eye" text rounded severity="secondary" title="상세 / 답변" @click="openDetail(data.id)" />
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </template>
        </Card>

        <!-- 상세 및 답변 다이얼로그 -->
        <Dialog v-model:visible="detailVisible" header="문의 상세 및 답변" :modal="true" :style="{ width: '90vw', maxWidth: '560px' }" :closable="true" class="inquiry-detail-dialog" @hide="selectedDetail = null">
            <div v-if="detailLoading" class="text-center py-6">
                <ProgressSpinner />
            </div>
            <div v-else-if="selectedDetail" class="detail-content">
                <div class="detail-meta mb-3">
                    <Tag :value="getInquiryTypeLabel(selectedDetail.inquiryType)" severity="info" class="mr-2" />
                    <span class="text-color-secondary text-sm">회원 #{{ selectedDetail.memberId }} · {{ formatDate(selectedDetail.createdAt) }}</span>
                </div>
                <h3 class="detail-title mb-2">{{ selectedDetail.title }}</h3>
                <p class="detail-body mb-3">{{ selectedDetail.content }}</p>
                <div v-if="selectedDetail.imageUrls?.length" class="detail-images mb-4">
                    <img v-for="(url, i) in selectedDetail.imageUrls" :key="i" :src="url" alt="첨부" class="detail-img" />
                </div>

                <Divider />

                <div class="reply-section">
                    <h4 class="reply-heading mb-2">관리자 답변</h4>
                    <div v-if="selectedDetail.reply" class="reply-content mb-3">
                        <p class="m-0 mb-1">{{ selectedDetail.reply.content }}</p>
                        <small class="text-color-secondary">{{ formatDate(selectedDetail.reply.repliedAt) }}</small>
                    </div>
                    <div v-else class="reply-form">
                        <Textarea v-model="replyContent" placeholder="답변 내용을 입력하세요" rows="4" class="w-full mb-2" />
                        <Button label="답변 등록" icon="pi pi-send" :disabled="!replyContent.trim() || replySubmitting" :loading="replySubmitting" @click="submitReply" />
                    </div>
                </div>
            </div>

            <template #footer>
                <Button label="닫기" severity="secondary" outlined @click="detailVisible = false" />
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.admin-inquiry-list {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 24px;
}

.title-text {
    display: inline-block;
    max-width: 280px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.detail-content .detail-title {
    font-size: 1.1rem;
    font-weight: 600;
    margin: 0;
}

.detail-content .detail-body {
    white-space: pre-wrap;
    line-height: 1.5;
    margin: 0;
}

.detail-images {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.detail-img {
    max-width: 120px;
    max-height: 120px;
    object-fit: cover;
    border-radius: 8px;
    border: 1px solid var(--p-surface-300);
}

.reply-heading {
    font-size: 0.95rem;
    font-weight: 600;
    margin: 0 0 0.5rem 0;
}

.reply-content {
    padding: 0.75rem;
    background: var(--p-surface-50);
    border-radius: 8px;
    border-left: 4px solid var(--p-primary-color);
}

.reply-form :deep(.p-inputtextarea) {
    width: 100%;
}
</style>
