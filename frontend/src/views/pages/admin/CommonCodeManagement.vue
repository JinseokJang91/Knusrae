<template>
    <div class="common-code-management">
        <div class="page-header mb-6">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goBack" />
                <h1 class="text-3xl font-bold text-gray-900">공통코드 관리</h1>
            </div>
            <p class="text-gray-600 mt-2">공통코드(코드 그룹)를 조회·등록·수정·삭제할 수 있습니다.</p>
        </div>

        <Card>
            <template #content>
                <div class="flex justify-end mb-4">
                    <Button
                        label="공통코드 추가"
                        icon="pi pi-plus"
                        @click="openCreateDialog"
                    />
                </div>
                <DataTable
                    :value="list"
                    :loading="loading"
                    data-key="codeId"
                    striped-rows
                    class="p-datatable-sm"
                    responsive-layout="scroll"
                >
                    <Column field="codeId" header="코드 ID" sortable />
                    <Column field="codeGroup" header="코드 그룹" sortable />
                    <Column field="codeName" header="코드명" sortable />
                    <Column field="useYn" header="사용 여부">
                        <template #body="{ data }">
                            <Tag :value="data.useYn === 'Y' ? '사용' : '미사용'" :severity="data.useYn === 'Y' ? 'success' : 'secondary'" />
                        </template>
                    </Column>
                    <Column header="작업" style="width: 160px">
                        <template #body="{ data }">
                            <div class="flex gap-2">
                                <Button
                                    icon="pi pi-pencil"
                                    text
                                    rounded
                                    severity="secondary"
                                    aria-label="수정"
                                    @click="openEditDialog(data)"
                                />
                                <Button
                                    icon="pi pi-trash"
                                    text
                                    rounded
                                    severity="danger"
                                    aria-label="삭제"
                                    @click="confirmDelete(data)"
                                />
                            </div>
                        </template>
                    </Column>
                    <template #empty>
                        <div class="text-center text-gray-500 py-8">등록된 공통코드가 없습니다.</div>
                    </template>
                    <template #loading> 목록을 불러오는 중입니다. </template>
                </DataTable>
            </template>
        </Card>

        <!-- 등록 다이얼로그 -->
        <Dialog
            v-model:visible="createDialogVisible"
            header="공통코드 등록"
            modal
            :style="{ width: '420px' }"
            :closable="true"
            @hide="resetCreateForm"
        >
            <div class="space-y-4 py-2">
                <div class="field">
                    <label for="create-codeId" class="block text-sm font-medium text-gray-700 mb-2">
                        코드 ID <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="create-codeId"
                        v-model="createForm.codeId"
                        placeholder="예: MY_CODE_GROUP"
                        class="w-full"
                        :class="{ 'p-invalid': createErrors.codeId }"
                        maxlength="30"
                    />
                    <small v-if="createErrors.codeId" class="p-error">{{ createErrors.codeId }}</small>
                </div>
                <div class="field">
                    <label for="create-codeGroup" class="block text-sm font-medium text-gray-700 mb-2">
                        코드 그룹 <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="create-codeGroup"
                        v-model="createForm.codeGroup"
                        placeholder="예: CATEGORY, INGREDIENTS"
                        class="w-full"
                        :class="{ 'p-invalid': createErrors.codeGroup }"
                        maxlength="30"
                    />
                    <small v-if="createErrors.codeGroup" class="p-error">{{ createErrors.codeGroup }}</small>
                </div>
                <div class="field">
                    <label for="create-codeName" class="block text-sm font-medium text-gray-700 mb-2">
                        코드명 <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="create-codeName"
                        v-model="createForm.codeName"
                        placeholder="예: 재료그룹"
                        class="w-full"
                        :class="{ 'p-invalid': createErrors.codeName }"
                        maxlength="50"
                    />
                    <small v-if="createErrors.codeName" class="p-error">{{ createErrors.codeName }}</small>
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">사용 여부</label>
                    <SelectButton v-model="createForm.useYn" :options="useYnOptions" option-value="value" option-label="label" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="createDialogVisible = false" />
                <Button label="등록" icon="pi pi-check" :loading="createSubmitting" @click="submitCreate" />
            </template>
        </Dialog>

        <!-- 수정 다이얼로그 (공통코드 + 상세코드 목록) -->
        <Dialog
            v-model:visible="editDialogVisible"
            header="공통코드 수정"
            modal
            :style="{ width: '720px' }"
            :closable="true"
            @hide="resetEditForm"
        >
            <div v-if="editingCodeId" class="space-y-4 py-2">
                <div class="grid grid-cols-2 gap-4">
                    <div class="field">
                        <label class="block text-sm font-medium text-gray-700 mb-2">코드 ID</label>
                        <InputText :model-value="editingCodeId" class="w-full" disabled />
                    </div>
                    <div class="field">
                        <label class="block text-sm font-medium text-gray-700 mb-2">사용 여부</label>
                        <SelectButton v-model="editForm.useYn" :options="useYnOptions" option-value="value" option-label="label" />
                    </div>
                </div>
                <div class="field">
                    <label for="edit-codeGroup" class="block text-sm font-medium text-gray-700 mb-2">
                        코드 그룹 <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="edit-codeGroup"
                        v-model="editForm.codeGroup"
                        class="w-full"
                        :class="{ 'p-invalid': editErrors.codeGroup }"
                        maxlength="30"
                    />
                    <small v-if="editErrors.codeGroup" class="p-error">{{ editErrors.codeGroup }}</small>
                </div>
                <div class="field">
                    <label for="edit-codeName" class="block text-sm font-medium text-gray-700 mb-2">
                        코드명 <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="edit-codeName"
                        v-model="editForm.codeName"
                        class="w-full"
                        :class="{ 'p-invalid': editErrors.codeName }"
                        maxlength="50"
                    />
                    <small v-if="editErrors.codeName" class="p-error">{{ editErrors.codeName }}</small>
                </div>

                <!-- 상세코드 섹션 -->
                <div class="border-t pt-4 mt-4">
                    <div class="flex items-center justify-between mb-2">
                        <label class="block text-sm font-medium text-gray-700">상세코드</label>
                        <Button
                            label="상세코드 추가"
                            icon="pi pi-plus"
                            size="small"
                            severity="secondary"
                            @click="openDetailAddDialog"
                        />
                    </div>
                    <DataTable
                        :value="editDetails"
                        data-key="detailCodeId"
                        striped-rows
                        class="p-datatable-sm"
                        responsive-layout="scroll"
                    >
                        <Column field="detailCodeId" header="상세코드 ID" style="min-width: 120px" />
                        <Column field="codeName" header="코드명" style="min-width: 140px" />
                        <Column field="sort" header="정렬">
                            <template #body="{ data }">
                                {{ data.sort ?? '-' }}
                            </template>
                        </Column>
                        <Column field="useYn" header="사용">
                            <template #body="{ data }">
                                <Tag :value="data.useYn === 'Y' ? '사용' : '미사용'" :severity="data.useYn === 'Y' ? 'success' : 'secondary'" />
                            </template>
                        </Column>
                        <Column header="작업" style="width: 100px">
                            <template #body="{ data }">
                                <div class="flex gap-1">
                                    <Button
                                        icon="pi pi-pencil"
                                        text
                                        rounded
                                        size="small"
                                        severity="secondary"
                                        aria-label="상세코드 수정"
                                        @click="openDetailEditDialog(data)"
                                    />
                                    <Button
                                        icon="pi pi-trash"
                                        text
                                        rounded
                                        size="small"
                                        severity="danger"
                                        aria-label="상세코드 삭제"
                                        @click="confirmDetailDelete(data)"
                                    />
                                </div>
                            </template>
                        </Column>
                        <template #empty>
                            <div class="text-center text-gray-500 py-4">상세코드가 없습니다. 추가 버튼으로 등록하세요.</div>
                        </template>
                    </DataTable>
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="editDialogVisible = false" />
                <Button label="저장" icon="pi pi-check" :loading="editSubmitting" @click="submitEdit" />
            </template>
        </Dialog>

        <!-- 상세코드 추가 다이얼로그 -->
        <Dialog
            v-model:visible="detailAddDialogVisible"
            header="상세코드 추가"
            modal
            :style="{ width: '400px' }"
            @hide="resetDetailAddForm"
        >
            <div class="space-y-4 py-2">
                <div class="field">
                    <label for="detail-add-id" class="block text-sm font-medium text-gray-700 mb-2">
                        상세코드 ID <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="detail-add-id"
                        v-model="detailAddForm.detailCodeId"
                        placeholder="예: 1001"
                        class="w-full"
                        :class="{ 'p-invalid': detailAddErrors.detailCodeId }"
                        maxlength="30"
                    />
                    <small v-if="detailAddErrors.detailCodeId" class="p-error">{{ detailAddErrors.detailCodeId }}</small>
                </div>
                <div class="field">
                    <label for="detail-add-name" class="block text-sm font-medium text-gray-700 mb-2">
                        코드명 <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="detail-add-name"
                        v-model="detailAddForm.codeName"
                        placeholder="예: 재료"
                        class="w-full"
                        :class="{ 'p-invalid': detailAddErrors.codeName }"
                        maxlength="50"
                    />
                    <small v-if="detailAddErrors.codeName" class="p-error">{{ detailAddErrors.codeName }}</small>
                </div>
                <div class="field">
                    <label for="detail-add-sort" class="block text-sm font-medium text-gray-700 mb-2">정렬 순서</label>
                    <InputNumber
                        id="detail-add-sort"
                        v-model="detailAddForm.sort"
                        class="w-full"
                        :min="0"
                        placeholder="숫자"
                    />
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">사용 여부</label>
                    <SelectButton v-model="detailAddForm.useYn" :options="useYnOptions" option-value="value" option-label="label" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="detailAddDialogVisible = false" />
                <Button label="등록" icon="pi pi-check" :loading="detailSubmitting" @click="submitDetailAdd" />
            </template>
        </Dialog>

        <!-- 상세코드 수정 다이얼로그 -->
        <Dialog
            v-model:visible="detailEditDialogVisible"
            header="상세코드 수정"
            modal
            :style="{ width: '400px' }"
            @hide="resetDetailEditForm"
        >
            <div v-if="editingDetailCodeId" class="space-y-4 py-2">
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">상세코드 ID</label>
                    <InputText :model-value="editingDetailCodeId" class="w-full" disabled />
                </div>
                <div class="field">
                    <label for="detail-edit-name" class="block text-sm font-medium text-gray-700 mb-2">
                        코드명 <span class="text-red-500">*</span>
                    </label>
                    <InputText
                        id="detail-edit-name"
                        v-model="detailEditForm.codeName"
                        class="w-full"
                        :class="{ 'p-invalid': detailEditErrors.codeName }"
                        maxlength="50"
                    />
                    <small v-if="detailEditErrors.codeName" class="p-error">{{ detailEditErrors.codeName }}</small>
                </div>
                <div class="field">
                    <label for="detail-edit-sort" class="block text-sm font-medium text-gray-700 mb-2">정렬 순서</label>
                    <InputNumber
                        id="detail-edit-sort"
                        v-model="detailEditForm.sort"
                        class="w-full"
                        :min="0"
                        placeholder="숫자"
                    />
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">사용 여부</label>
                    <SelectButton v-model="detailEditForm.useYn" :options="useYnOptions" option-value="value" option-label="label" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="detailEditDialogVisible = false" />
                <Button label="저장" icon="pi pi-check" :loading="detailSubmitting" @click="submitDetailEdit" />
            </template>
        </Dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import Card from 'primevue/card';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import SelectButton from 'primevue/selectbutton';
import type {
    CommonCodeListItem,
    AdminCommonCode,
    AdminCommonCodeDetailItem,
    CommonCodeCreateRequest,
    CommonCodeUpdateRequest,
    CommonCodeDetailCreateRequest,
    CommonCodeDetailUpdateRequest
} from '@/types/common';
import {
    getAdminCommonCodeList,
    getAdminCommonCodeOne,
    createCommonCode,
    updateCommonCode,
    deleteCommonCode,
    createCommonCodeDetail,
    updateCommonCodeDetail,
    deleteCommonCodeDetail
} from '@/api/commonCodeApi';

const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();

const list = ref<CommonCodeListItem[]>([]);
const loading = ref(false);
const createDialogVisible = ref(false);
const editDialogVisible = ref(false);
const editingCodeId = ref<string | null>(null);
const createSubmitting = ref(false);
const editSubmitting = ref(false);

const useYnOptions = [
    { label: '사용', value: 'Y' },
    { label: '미사용', value: 'N' }
];

const createForm = ref<CommonCodeCreateRequest>({
    codeId: '',
    codeGroup: '',
    codeName: '',
    useYn: 'Y'
});
const createErrors = ref<Record<string, string>>({});

const editForm = ref<CommonCodeUpdateRequest>({
    codeGroup: '',
    codeName: '',
    useYn: 'Y'
});
const editErrors = ref<Record<string, string>>({});
const editDetails = ref<AdminCommonCodeDetailItem[]>([]);

const detailAddDialogVisible = ref(false);
const detailEditDialogVisible = ref(false);
const editingDetailCodeId = ref<string | null>(null);
const detailSubmitting = ref(false);
const detailAddForm = ref<CommonCodeDetailCreateRequest>({
    detailCodeId: '',
    codeName: '',
    sort: null,
    useYn: 'Y'
});
const detailAddErrors = ref<Record<string, string>>({});
const detailEditForm = ref<CommonCodeDetailUpdateRequest>({
    codeName: '',
    sort: null,
    useYn: 'Y'
});
const detailEditErrors = ref<Record<string, string>>({});

onMounted(() => {
    if (!authStore.isAdmin) {
        router.replace('/');
        return;
    }
    loadList();
});

function goBack() {
    router.push('/admin');
}

async function loadList() {
    loading.value = true;
    try {
        list.value = await getAdminCommonCodeList();
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '조회 실패',
            detail: e instanceof Error ? e.message : '목록을 불러오지 못했습니다.'
        });
    } finally {
        loading.value = false;
    }
}

function openCreateDialog() {
    createErrors.value = {};
    createForm.value = { codeId: '', codeGroup: '', codeName: '', useYn: 'Y' };
    createDialogVisible.value = true;
}

function resetCreateForm() {
    createErrors.value = {};
}

function validateCreate(): boolean {
    const err: Record<string, string> = {};
    if (!createForm.value.codeId?.trim()) err.codeId = '코드 ID를 입력하세요.';
    if (!createForm.value.codeGroup?.trim()) err.codeGroup = '코드 그룹을 입력하세요.';
    if (!createForm.value.codeName?.trim()) err.codeName = '코드명을 입력하세요.';
    createErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitCreate() {
    if (!validateCreate()) return;
    createSubmitting.value = true;
    try {
        await createCommonCode({
            codeId: createForm.value.codeId!.trim(),
            codeGroup: createForm.value.codeGroup!.trim(),
            codeName: createForm.value.codeName!.trim(),
            useYn: createForm.value.useYn || 'Y'
        });
        toast.add({ severity: 'success', summary: '등록 완료', detail: '공통코드가 등록되었습니다.' });
        createDialogVisible.value = false;
        await loadList();
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: e instanceof Error ? e.message : '등록에 실패했습니다.'
        });
    } finally {
        createSubmitting.value = false;
    }
}

async function openEditDialog(row: CommonCodeListItem) {
    try {
        const one = await getAdminCommonCodeOne(row.codeId);
        editingCodeId.value = one.codeId;
        editForm.value = {
            codeGroup: one.codeGroup,
            codeName: one.codeName,
            useYn: one.useYn || 'Y'
        };
        editDetails.value = one.details ? [...one.details] : [];
        editErrors.value = {};
        editDialogVisible.value = true;
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '조회 실패',
            detail: e instanceof Error ? e.message : '데이터를 불러오지 못했습니다.'
        });
    }
}

function resetEditForm() {
    editingCodeId.value = null;
    editDetails.value = [];
    editErrors.value = {};
    detailAddDialogVisible.value = false;
    detailEditDialogVisible.value = false;
    editingDetailCodeId.value = null;
}

function openDetailAddDialog() {
    detailAddErrors.value = {};
    detailAddForm.value = { detailCodeId: '', codeName: '', sort: null, useYn: 'Y' };
    detailAddDialogVisible.value = true;
}

function resetDetailAddForm() {
    detailAddErrors.value = {};
}

function validateDetailAdd(): boolean {
    const err: Record<string, string> = {};
    if (!detailAddForm.value.detailCodeId?.trim()) err.detailCodeId = '상세코드 ID를 입력하세요.';
    if (!detailAddForm.value.codeName?.trim()) err.codeName = '코드명을 입력하세요.';
    detailAddErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitDetailAdd() {
    if (!editingCodeId.value || !validateDetailAdd()) return;
    detailSubmitting.value = true;
    try {
        const created = await createCommonCodeDetail(editingCodeId.value, {
            detailCodeId: detailAddForm.value.detailCodeId!.trim(),
            codeName: detailAddForm.value.codeName!.trim(),
            sort: detailAddForm.value.sort ?? undefined,
            useYn: detailAddForm.value.useYn || 'Y'
        });
        editDetails.value = [...editDetails.value, created];
        detailAddDialogVisible.value = false;
        toast.add({ severity: 'success', summary: '등록 완료', detail: '상세코드가 등록되었습니다.' });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: e instanceof Error ? e.message : '상세코드 등록에 실패했습니다.'
        });
    } finally {
        detailSubmitting.value = false;
    }
}

function openDetailEditDialog(row: AdminCommonCodeDetailItem) {
    editingDetailCodeId.value = row.detailCodeId;
    detailEditForm.value = {
        codeName: row.codeName,
        sort: row.sort ?? null,
        useYn: row.useYn || 'Y'
    };
    detailEditErrors.value = {};
    detailEditDialogVisible.value = true;
}

function resetDetailEditForm() {
    editingDetailCodeId.value = null;
    detailEditErrors.value = {};
}

function validateDetailEdit(): boolean {
    const err: Record<string, string> = {};
    if (!detailEditForm.value.codeName?.trim()) err.codeName = '코드명을 입력하세요.';
    detailEditErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitDetailEdit() {
    if (!editingCodeId.value || !editingDetailCodeId.value || !validateDetailEdit()) return;
    detailSubmitting.value = true;
    try {
        const updated = await updateCommonCodeDetail(editingCodeId.value, editingDetailCodeId.value, {
            codeName: detailEditForm.value.codeName!.trim(),
            sort: detailEditForm.value.sort ?? undefined,
            useYn: detailEditForm.value.useYn
        });
        const idx = editDetails.value.findIndex((d) => d.detailCodeId === editingDetailCodeId.value);
        if (idx !== -1) {
            const next = [...editDetails.value];
            next[idx] = updated;
            editDetails.value = next;
        }
        detailEditDialogVisible.value = false;
        toast.add({ severity: 'success', summary: '수정 완료', detail: '상세코드가 수정되었습니다.' });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '수정 실패',
            detail: e instanceof Error ? e.message : '상세코드 수정에 실패했습니다.'
        });
    } finally {
        detailSubmitting.value = false;
    }
}

function confirmDetailDelete(row: AdminCommonCodeDetailItem) {
    if (!editingCodeId.value) return;
    confirm.require({
        message: `"${row.codeName}"(상세코드 ID: ${row.detailCodeId}) 상세코드를 삭제하시겠습니까?`,
        header: '상세코드 삭제 확인',
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        acceptLabel: '삭제',
        acceptClass: 'p-button-danger',
        async accept() {
            try {
                await deleteCommonCodeDetail(editingCodeId.value!, row.detailCodeId);
                editDetails.value = editDetails.value.filter((d) => d.detailCodeId !== row.detailCodeId);
                toast.add({ severity: 'success', summary: '삭제 완료', detail: '상세코드가 삭제되었습니다.' });
            } catch (e) {
                toast.add({
                    severity: 'error',
                    summary: '삭제 실패',
                    detail: e instanceof Error ? e.message : '상세코드 삭제에 실패했습니다.'
                });
            }
        }
    });
}

function validateEdit(): boolean {
    const err: Record<string, string> = {};
    if (!editForm.value.codeGroup?.trim()) err.codeGroup = '코드 그룹을 입력하세요.';
    if (!editForm.value.codeName?.trim()) err.codeName = '코드명을 입력하세요.';
    editErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitEdit() {
    if (!editingCodeId.value || !validateEdit()) return;
    editSubmitting.value = true;
    try {
        await updateCommonCode(editingCodeId.value, {
            codeGroup: editForm.value.codeGroup!.trim(),
            codeName: editForm.value.codeName!.trim(),
            useYn: editForm.value.useYn
        });
        toast.add({ severity: 'success', summary: '수정 완료', detail: '공통코드가 수정되었습니다.' });
        editDialogVisible.value = false;
        await loadList();
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '수정 실패',
            detail: e instanceof Error ? e.message : '수정에 실패했습니다.'
        });
    } finally {
        editSubmitting.value = false;
    }
}

function confirmDelete(row: CommonCodeListItem) {
    confirm.require({
        message: `"${row.codeName}"(코드 ID: ${row.codeId}) 공통코드를 삭제하시겠습니까? 하위 상세코드도 함께 삭제됩니다.`,
        header: '삭제 확인',
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        acceptLabel: '삭제',
        acceptClass: 'p-button-danger',
        async accept() {
            try {
                await deleteCommonCode(row.codeId);
                toast.add({ severity: 'success', summary: '삭제 완료', detail: '공통코드가 삭제되었습니다.' });
                await loadList();
            } catch (e) {
                toast.add({
                    severity: 'error',
                    summary: '삭제 실패',
                    detail: e instanceof Error ? e.message : '삭제에 실패했습니다.'
                });
            }
        }
    });
}
</script>

<style scoped>
.common-code-management {
    padding: 24px;
    max-width: 1000px;
    margin: 0 auto;
}
</style>
