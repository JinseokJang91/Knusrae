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
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import type { IngredientGroup, Ingredient } from '@/types/ingredient';
import { getIngredientGroups, getIngredients, createIngredientGroup, updateIngredientGroup, deleteIngredientGroup, createIngredient, updateIngredient, deleteIngredient, uploadContentImage } from '@/api/ingredientApi';

const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();

const groups = ref<IngredientGroup[]>([]);
const loading = ref(false);
const createGroupDialogVisible = ref(false);
const createGroupSubmitting = ref(false);
const createGroupForm = ref({ name: '', imageUrl: '', sortOrder: 0 as number | null });
const createGroupErrors = ref<Record<string, string>>({});
const createGroupFileRef = ref<HTMLInputElement | null>(null);
const createGroupImageFile = ref<File | null>(null);
const createGroupImagePreview = ref<string | null>(null);
const createGroupImageUploading = ref(false);

const editGroupDialogVisible = ref(false);
const editingGroup = ref<IngredientGroup | null>(null);
const editGroupSubmitting = ref(false);
const editGroupForm = ref({ name: '', imageUrl: '', sortOrder: 0 as number | null });
const editGroupErrors = ref<Record<string, string>>({});
const editGroupFileRef = ref<HTMLInputElement | null>(null);
const editGroupImageFile = ref<File | null>(null);
const editGroupImagePreview = ref<string | null>(null);
const editGroupImageUploading = ref(false);

const detailDialogVisible = ref(false);
const detailGroup = ref<IngredientGroup | null>(null);
const detailIngredients = ref<Ingredient[]>([]);
const detailLoading = ref(false);

const addIngredientDialogVisible = ref(false);
const addIngredientSubmitting = ref(false);
const addIngredientForm = ref({ name: '', imageUrl: '', sortOrder: 0 as number | null });
const addIngredientErrors = ref<Record<string, string>>({});
const addIngredientFileRef = ref<HTMLInputElement | null>(null);
const addIngredientImageFile = ref<File | null>(null);
const addIngredientImagePreview = ref<string | null>(null);
const addIngredientImageUploading = ref(false);

const editIngredientDialogVisible = ref(false);
const editingIngredient = ref<Ingredient | null>(null);
const editIngredientSubmitting = ref(false);
const editIngredientForm = ref({ name: '', imageUrl: '', sortOrder: 0 as number | null });
const editIngredientErrors = ref<Record<string, string>>({});
const editIngredientFileRef = ref<HTMLInputElement | null>(null);
const editIngredientImageFile = ref<File | null>(null);
const editIngredientImagePreview = ref<string | null>(null);
const editIngredientImageUploading = ref(false);

onMounted(() => {
    if (!authStore.isAdmin) {
        router.replace('/');
        return;
    }
    loadGroups();
});

function goBack() {
    router.push('/admin');
}

async function loadGroups() {
    loading.value = true;
    try {
        groups.value = await getIngredientGroups();
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '조회 실패',
            detail: e instanceof Error ? e.message : '재료 그룹 목록을 불러오지 못했습니다.'
        });
    } finally {
        loading.value = false;
    }
}

function onThumbError(event: Event, _data: IngredientGroup) {
    (event.target as HTMLImageElement).style.display = 'none';
}

function triggerCreateGroupFileSelect() {
    createGroupFileRef.value?.click();
}

function triggerEditGroupFileSelect() {
    editGroupFileRef.value?.click();
}

function triggerAddIngredientFileSelect() {
    addIngredientFileRef.value?.click();
}

function triggerEditIngredientFileSelect() {
    editIngredientFileRef.value?.click();
}

function openCreateGroupDialog() {
    createGroupErrors.value = {};
    createGroupForm.value = { name: '', imageUrl: '', sortOrder: 0 };
    clearCreateGroupImage();
    createGroupDialogVisible.value = true;
}

function resetCreateGroupForm() {
    createGroupErrors.value = {};
    clearCreateGroupImage();
}

function clearCreateGroupImage() {
    createGroupForm.value.imageUrl = '';
    createGroupImageFile.value = null;
    if (createGroupImagePreview.value) {
        URL.revokeObjectURL(createGroupImagePreview.value);
        createGroupImagePreview.value = null;
    }
    if (createGroupFileRef.value) createGroupFileRef.value.value = '';
}

async function onCreateGroupImageChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
        toast.add({ severity: 'warn', summary: '파일 형식', detail: '이미지 파일만 업로드할 수 있습니다.', life: 3000 });
        input.value = '';
        return;
    }
    createGroupImageFile.value = file;
    createGroupImagePreview.value = URL.createObjectURL(file);
    createGroupImageUploading.value = true;
    try {
        const { url } = await uploadContentImage(file);
        createGroupForm.value.imageUrl = url;
    } catch (err) {
        const msg = err instanceof Error ? err.message : '이미지 업로드에 실패했습니다.';
        toast.add({ severity: 'error', summary: '업로드 실패', detail: msg, life: 5000 });
        clearCreateGroupImage();
    } finally {
        createGroupImageUploading.value = false;
        input.value = '';
    }
}

function validateCreateGroup(): boolean {
    const err: Record<string, string> = {};
    if (!createGroupForm.value.name?.trim()) err.name = '그룹명을 입력하세요.';
    createGroupErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitCreateGroup() {
    if (!validateCreateGroup()) return;
    createGroupSubmitting.value = true;
    try {
        await createIngredientGroup({
            name: createGroupForm.value.name.trim(),
            imageUrl: createGroupForm.value.imageUrl?.trim() || undefined,
            sortOrder: createGroupForm.value.sortOrder ?? undefined
        });
        toast.add({ severity: 'success', summary: '등록 완료', detail: '재료 그룹이 등록되었습니다.' });
        createGroupDialogVisible.value = false;
        await loadGroups();
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: e instanceof Error ? e.message : '등록에 실패했습니다.'
        });
    } finally {
        createGroupSubmitting.value = false;
    }
}

function openEditGroupDialog(group: IngredientGroup) {
    editingGroup.value = group;
    editGroupForm.value = {
        name: group.name,
        imageUrl: group.imageUrl ?? '',
        sortOrder: group.sortOrder ?? 0
    };
    editGroupErrors.value = {};
    clearEditGroupImage();
    editGroupDialogVisible.value = true;
}

function resetEditGroupForm() {
    editingGroup.value = null;
    editGroupErrors.value = {};
    clearEditGroupImage();
}

function clearEditGroupImage() {
    editGroupForm.value.imageUrl = '';
    editGroupImageFile.value = null;
    if (editGroupImagePreview.value) {
        URL.revokeObjectURL(editGroupImagePreview.value);
        editGroupImagePreview.value = null;
    }
    if (editGroupFileRef.value) editGroupFileRef.value.value = '';
}

async function onEditGroupImageChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
        toast.add({ severity: 'warn', summary: '파일 형식', detail: '이미지 파일만 업로드할 수 있습니다.', life: 3000 });
        input.value = '';
        return;
    }
    editGroupImageFile.value = file;
    editGroupImagePreview.value = URL.createObjectURL(file);
    editGroupImageUploading.value = true;
    try {
        const { url } = await uploadContentImage(file);
        editGroupForm.value.imageUrl = url;
    } catch (err) {
        const msg = err instanceof Error ? err.message : '이미지 업로드에 실패했습니다.';
        toast.add({ severity: 'error', summary: '업로드 실패', detail: msg, life: 5000 });
        clearEditGroupImage();
    } finally {
        editGroupImageUploading.value = false;
        input.value = '';
    }
}

function validateEditGroup(): boolean {
    const err: Record<string, string> = {};
    if (!editGroupForm.value.name?.trim()) err.name = '그룹명을 입력하세요.';
    editGroupErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitEditGroup() {
    if (!editingGroup.value || !validateEditGroup()) return;
    editGroupSubmitting.value = true;
    try {
        const updated = await updateIngredientGroup(editingGroup.value.id, {
            name: editGroupForm.value.name.trim(),
            imageUrl: editGroupForm.value.imageUrl?.trim() || undefined,
            sortOrder: editGroupForm.value.sortOrder ?? undefined
        });
        const idx = groups.value.findIndex((g) => g.id === updated.id);
        if (idx !== -1) {
            const next = [...groups.value];
            next[idx] = updated;
            groups.value = next;
        }
        if (detailGroup.value?.id === updated.id) {
            detailGroup.value = updated;
        }
        toast.add({ severity: 'success', summary: '수정 완료', detail: '재료 그룹이 수정되었습니다.' });
        editGroupDialogVisible.value = false;
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '수정 실패',
            detail: e instanceof Error ? e.message : '수정에 실패했습니다.'
        });
    } finally {
        editGroupSubmitting.value = false;
    }
}

function confirmDeleteGroup(group: IngredientGroup) {
    confirm.require({
        message: `"${group.name}" 재료 그룹을 삭제하시겠습니까? 해당 그룹에 속한 재료도 함께 삭제됩니다.`,
        header: '삭제 확인',
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        acceptLabel: '삭제',
        acceptClass: 'p-button-danger',
        async accept() {
            try {
                await deleteIngredientGroup(group.id);
                toast.add({ severity: 'success', summary: '삭제 완료', detail: '재료 그룹이 삭제되었습니다.' });
                if (detailGroup.value?.id === group.id) {
                    detailDialogVisible.value = false;
                    detailGroup.value = null;
                    detailIngredients.value = [];
                }
                await loadGroups();
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

async function openGroupDetailDialog(group: IngredientGroup) {
    detailGroup.value = group;
    detailIngredients.value = [];
    detailDialogVisible.value = true;
    detailLoading.value = true;
    try {
        const res = await getIngredients({ groupId: group.id });
        detailIngredients.value = res.ingredients || [];
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '조회 실패',
            detail: e instanceof Error ? e.message : '재료 목록을 불러오지 못했습니다.'
        });
    } finally {
        detailLoading.value = false;
    }
}

function resetDetailDialog() {
    detailGroup.value = null;
    detailIngredients.value = [];
    addIngredientDialogVisible.value = false;
    editIngredientDialogVisible.value = false;
    editingIngredient.value = null;
}

function openAddIngredientDialog() {
    addIngredientErrors.value = {};
    addIngredientForm.value = { name: '', imageUrl: '', sortOrder: 0 };
    clearAddIngredientImage();
    addIngredientDialogVisible.value = true;
}

function resetAddIngredientForm() {
    addIngredientErrors.value = {};
    clearAddIngredientImage();
}

function clearAddIngredientImage() {
    addIngredientForm.value.imageUrl = '';
    addIngredientImageFile.value = null;
    if (addIngredientImagePreview.value) {
        URL.revokeObjectURL(addIngredientImagePreview.value);
        addIngredientImagePreview.value = null;
    }
    if (addIngredientFileRef.value) addIngredientFileRef.value.value = '';
}

async function onAddIngredientImageChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
        toast.add({ severity: 'warn', summary: '파일 형식', detail: '이미지 파일만 업로드할 수 있습니다.', life: 3000 });
        input.value = '';
        return;
    }
    addIngredientImageFile.value = file;
    addIngredientImagePreview.value = URL.createObjectURL(file);
    addIngredientImageUploading.value = true;
    try {
        const { url } = await uploadContentImage(file);
        addIngredientForm.value.imageUrl = url;
    } catch (err) {
        const msg = err instanceof Error ? err.message : '이미지 업로드에 실패했습니다.';
        toast.add({ severity: 'error', summary: '업로드 실패', detail: msg, life: 5000 });
        clearAddIngredientImage();
    } finally {
        addIngredientImageUploading.value = false;
        input.value = '';
    }
}

function validateAddIngredient(): boolean {
    const err: Record<string, string> = {};
    if (!addIngredientForm.value.name?.trim()) err.name = '재료명을 입력하세요.';
    addIngredientErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitAddIngredient() {
    if (!detailGroup.value || !validateAddIngredient()) return;
    addIngredientSubmitting.value = true;
    try {
        const created = await createIngredient({
            groupId: detailGroup.value.id,
            name: addIngredientForm.value.name.trim(),
            imageUrl: addIngredientForm.value.imageUrl?.trim() || undefined,
            sortOrder: addIngredientForm.value.sortOrder ?? undefined
        });
        detailIngredients.value = [...detailIngredients.value, created];
        addIngredientDialogVisible.value = false;
        toast.add({ severity: 'success', summary: '등록 완료', detail: '재료가 등록되었습니다.' });
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: e instanceof Error ? e.message : '재료 등록에 실패했습니다.'
        });
    } finally {
        addIngredientSubmitting.value = false;
    }
}

function openEditIngredientDialog(ingredient: Ingredient) {
    editingIngredient.value = ingredient;
    editIngredientForm.value = {
        name: ingredient.name,
        imageUrl: ingredient.imageUrl ?? '',
        sortOrder: ingredient.sortOrder ?? 0
    };
    editIngredientErrors.value = {};
    clearEditIngredientImage();
    editIngredientDialogVisible.value = true;
}

function resetEditIngredientForm() {
    editingIngredient.value = null;
    editIngredientErrors.value = {};
    clearEditIngredientImage();
}

function clearEditIngredientImage() {
    editIngredientForm.value.imageUrl = '';
    editIngredientImageFile.value = null;
    if (editIngredientImagePreview.value) {
        URL.revokeObjectURL(editIngredientImagePreview.value);
        editIngredientImagePreview.value = null;
    }
    if (editIngredientFileRef.value) editIngredientFileRef.value.value = '';
}

async function onEditIngredientImageChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
        toast.add({ severity: 'warn', summary: '파일 형식', detail: '이미지 파일만 업로드할 수 있습니다.', life: 3000 });
        input.value = '';
        return;
    }
    editIngredientImageFile.value = file;
    editIngredientImagePreview.value = URL.createObjectURL(file);
    editIngredientImageUploading.value = true;
    try {
        const { url } = await uploadContentImage(file);
        editIngredientForm.value.imageUrl = url;
    } catch (err) {
        const msg = err instanceof Error ? err.message : '이미지 업로드에 실패했습니다.';
        toast.add({ severity: 'error', summary: '업로드 실패', detail: msg, life: 5000 });
        clearEditIngredientImage();
    } finally {
        editIngredientImageUploading.value = false;
        input.value = '';
    }
}

function validateEditIngredient(): boolean {
    const err: Record<string, string> = {};
    if (!editIngredientForm.value.name?.trim()) err.name = '재료명을 입력하세요.';
    editIngredientErrors.value = err;
    return Object.keys(err).length === 0;
}

async function submitEditIngredient() {
    if (!editingIngredient.value || !detailGroup.value || !validateEditIngredient()) return;
    editIngredientSubmitting.value = true;
    try {
        const updated = await updateIngredient(editingIngredient.value.id, {
            groupId: detailGroup.value.id,
            name: editIngredientForm.value.name.trim(),
            imageUrl: editIngredientForm.value.imageUrl?.trim() || undefined,
            sortOrder: editIngredientForm.value.sortOrder ?? undefined
        });
        const idx = detailIngredients.value.findIndex((i) => i.id === updated.id);
        if (idx !== -1) {
            const next = [...detailIngredients.value];
            next[idx] = { ...updated, group: detailGroup.value };
            detailIngredients.value = next;
        }
        toast.add({ severity: 'success', summary: '수정 완료', detail: '재료가 수정되었습니다.' });
        editIngredientDialogVisible.value = false;
    } catch (e) {
        toast.add({
            severity: 'error',
            summary: '수정 실패',
            detail: e instanceof Error ? e.message : '재료 수정에 실패했습니다.'
        });
    } finally {
        editIngredientSubmitting.value = false;
    }
}

function confirmDeleteIngredient(ingredient: Ingredient) {
    confirm.require({
        message: `"${ingredient.name}" 재료를 삭제하시겠습니까?`,
        header: '삭제 확인',
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        acceptLabel: '삭제',
        acceptClass: 'p-button-danger',
        async accept() {
            try {
                await deleteIngredient(ingredient.id);
                detailIngredients.value = detailIngredients.value.filter((i) => i.id !== ingredient.id);
                toast.add({ severity: 'success', summary: '삭제 완료', detail: '재료가 삭제되었습니다.' });
            } catch (e) {
                toast.add({
                    severity: 'error',
                    summary: '삭제 실패',
                    detail: e instanceof Error ? e.message : '재료 삭제에 실패했습니다.'
                });
            }
        }
    });
}
</script>

<template>
    <div class="ingredient-group-management">
        <div class="page-header mb-6">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goBack" />
                <h1 class="text-3xl font-bold text-gray-900">재료·재료 그룹 관리</h1>
            </div>
            <p class="text-gray-600 mt-2">재료 그룹을 조회·등록·수정·삭제하고, 그룹별 재료를 등록·수정·삭제할 수 있습니다.</p>
        </div>

        <Card>
            <template #content>
                <div class="flex justify-end mb-4">
                    <Button label="재료 그룹 추가" icon="pi pi-plus" @click="openCreateGroupDialog" />
                </div>
                <DataTable :value="groups" :loading="loading" data-key="id" striped-rows class="p-datatable-sm" responsive-layout="scroll">
                    <Column field="id" header="ID" style="width: 80px" />
                    <Column field="name" header="그룹명" sortable />
                    <Column header="이미지" style="width: 80px">
                        <template #body="{ data }">
                            <img v-if="data.imageUrl" :src="data.imageUrl" alt="" class="w-10 h-10 object-cover rounded border border-gray-200" @error="onThumbError($event, data)" />
                            <span v-else class="text-gray-400 text-sm">-</span>
                        </template>
                    </Column>
                    <Column field="sortOrder" header="정렬" style="width: 80px" />
                    <Column header="작업" style="width: 220px">
                        <template #body="{ data }">
                            <div class="flex gap-2 flex-wrap">
                                <Button label="재료 관리" icon="pi pi-list" text size="small" severity="secondary" @click="openGroupDetailDialog(data)" />
                                <Button icon="pi pi-pencil" text rounded size="small" severity="secondary" aria-label="수정" @click="openEditGroupDialog(data)" />
                                <Button icon="pi pi-trash" text rounded size="small" severity="danger" aria-label="삭제" @click="confirmDeleteGroup(data)" />
                            </div>
                        </template>
                    </Column>
                    <template #empty>
                        <div class="text-center text-gray-500 py-8">등록된 재료 그룹이 없습니다.</div>
                    </template>
                    <template #loading>목록을 불러오는 중입니다.</template>
                </DataTable>
            </template>
        </Card>

        <!-- 재료 그룹 등록 다이얼로그 -->
        <Dialog v-model:visible="createGroupDialogVisible" header="재료 그룹 등록" modal :style="{ width: '480px' }" :closable="true" @hide="resetCreateGroupForm">
            <div class="space-y-4 py-2">
                <div class="field">
                    <label for="create-name" class="block text-sm font-medium text-gray-700 mb-2"> 그룹명 <span class="text-red-500">*</span> </label>
                    <InputText id="create-name" v-model="createGroupForm.name" placeholder="예: 채소" class="w-full" :class="{ 'p-invalid': createGroupErrors.name }" maxlength="50" />
                    <small v-if="createGroupErrors.name" class="p-error">{{ createGroupErrors.name }}</small>
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">이미지 (선택)</label>
                    <div class="image-input-section space-y-3">
                        <div class="flex gap-2 flex-wrap">
                            <InputText v-model="createGroupForm.imageUrl" placeholder="이미지 URL (https://...)" class="flex-1 min-w-0" maxlength="500" :disabled="!!createGroupImageFile" />
                            <input ref="createGroupFileRef" type="file" accept="image/*" class="hidden" @change="onCreateGroupImageChange" />
                            <Button type="button" label="파일 선택" icon="pi pi-upload" severity="secondary" :loading="createGroupImageUploading" @click="triggerCreateGroupFileSelect" />
                        </div>
                        <div v-if="createGroupImagePreview || createGroupForm.imageUrl" class="flex items-center gap-3">
                            <img v-if="createGroupImagePreview" :src="createGroupImagePreview" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" />
                            <img v-else-if="createGroupForm.imageUrl" :src="createGroupForm.imageUrl" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" @error="createGroupForm.imageUrl = ''" />
                            <Button v-if="createGroupImagePreview || createGroupForm.imageUrl" type="button" icon="pi pi-times" severity="secondary" text rounded @click="clearCreateGroupImage" />
                        </div>
                    </div>
                </div>
                <div class="field">
                    <label for="create-sortOrder" class="block text-sm font-medium text-gray-700 mb-2">정렬 순서</label>
                    <InputNumber id="create-sortOrder" v-model="createGroupForm.sortOrder" class="w-full" :min="0" placeholder="0" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="createGroupDialogVisible = false" />
                <Button label="등록" icon="pi pi-check" :loading="createGroupSubmitting" @click="submitCreateGroup" />
            </template>
        </Dialog>

        <!-- 재료 그룹 수정 다이얼로그 -->
        <Dialog v-model:visible="editGroupDialogVisible" header="재료 그룹 수정" modal :style="{ width: '480px' }" :closable="true" @hide="resetEditGroupForm">
            <div v-if="editingGroup" class="space-y-4 py-2">
                <div class="field">
                    <label for="edit-group-name" class="block text-sm font-medium text-gray-700 mb-2"> 그룹명 <span class="text-red-500">*</span> </label>
                    <InputText id="edit-group-name" v-model="editGroupForm.name" placeholder="예: 채소" class="w-full" :class="{ 'p-invalid': editGroupErrors.name }" maxlength="50" />
                    <small v-if="editGroupErrors.name" class="p-error">{{ editGroupErrors.name }}</small>
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">이미지 (선택)</label>
                    <div class="image-input-section space-y-3">
                        <div class="flex gap-2 flex-wrap">
                            <InputText v-model="editGroupForm.imageUrl" placeholder="이미지 URL (https://...)" class="flex-1 min-w-0" maxlength="500" :disabled="!!editGroupImageFile" />
                            <input ref="editGroupFileRef" type="file" accept="image/*" class="hidden" @change="onEditGroupImageChange" />
                            <Button type="button" label="파일 선택" icon="pi pi-upload" severity="secondary" :loading="editGroupImageUploading" @click="triggerEditGroupFileSelect" />
                        </div>
                        <div v-if="editGroupImagePreview || editGroupForm.imageUrl" class="flex items-center gap-3">
                            <img v-if="editGroupImagePreview" :src="editGroupImagePreview" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" />
                            <img v-else-if="editGroupForm.imageUrl" :src="editGroupForm.imageUrl" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" @error="editGroupForm.imageUrl = ''" />
                            <Button v-if="editGroupImagePreview || editGroupForm.imageUrl" type="button" icon="pi pi-times" severity="secondary" text rounded @click="clearEditGroupImage" />
                        </div>
                    </div>
                </div>
                <div class="field">
                    <label for="edit-group-sortOrder" class="block text-sm font-medium text-gray-700 mb-2">정렬 순서</label>
                    <InputNumber id="edit-group-sortOrder" v-model="editGroupForm.sortOrder" class="w-full" :min="0" placeholder="0" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="editGroupDialogVisible = false" />
                <Button label="저장" icon="pi pi-check" :loading="editGroupSubmitting" @click="submitEditGroup" />
            </template>
        </Dialog>

        <!-- 그룹 상세(재료 목록 + 재료 추가) 다이얼로그 -->
        <Dialog v-model:visible="detailDialogVisible" :header="detailGroup ? `재료 관리: ${detailGroup.name}` : '재료 관리'" modal :style="{ width: '720px' }" :closable="true" @hide="resetDetailDialog">
            <div v-if="detailGroup" class="space-y-4 py-2">
                <div class="flex items-center justify-between mb-2">
                    <span class="text-sm text-gray-600">그룹 ID: {{ detailGroup.id }} · 그룹명: {{ detailGroup.name }}</span>
                    <Button label="재료 추가" icon="pi pi-plus" size="small" severity="secondary" @click="openAddIngredientDialog" />
                </div>
                <DataTable :value="detailIngredients" :loading="detailLoading" data-key="id" striped-rows class="p-datatable-sm" responsive-layout="scroll">
                    <Column field="id" header="ID" style="width: 80px" />
                    <Column field="name" header="재료명" />
                    <Column header="이미지" style="width: 80px">
                        <template #body="{ data }">
                            <img v-if="data.imageUrl" :src="data.imageUrl" alt="" class="w-10 h-10 object-cover rounded border border-gray-200" />
                            <span v-else class="text-gray-400 text-sm">-</span>
                        </template>
                    </Column>
                    <Column field="sortOrder" header="정렬" style="width: 80px" />
                    <Column header="작업" style="width: 120px">
                        <template #body="{ data }">
                            <div class="flex gap-2">
                                <Button icon="pi pi-pencil" text rounded size="small" severity="secondary" aria-label="수정" @click="openEditIngredientDialog(data)" />
                                <Button icon="pi pi-trash" text rounded size="small" severity="danger" aria-label="삭제" @click="confirmDeleteIngredient(data)" />
                            </div>
                        </template>
                    </Column>
                    <template #empty>
                        <div class="text-center text-gray-500 py-6">등록된 재료가 없습니다. 재료 추가 버튼으로 등록하세요.</div>
                    </template>
                    <template #loading>재료 목록을 불러오는 중입니다.</template>
                </DataTable>
            </div>
            <template #footer>
                <Button label="닫기" @click="detailDialogVisible = false" />
            </template>
        </Dialog>

        <!-- 재료 추가 다이얼로그 -->
        <Dialog v-model:visible="addIngredientDialogVisible" header="재료 추가" modal :style="{ width: '480px' }" :closable="true" @hide="resetAddIngredientForm">
            <div v-if="detailGroup" class="space-y-4 py-2">
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">재료 그룹</label>
                    <InputText :model-value="detailGroup.name" class="w-full" disabled />
                </div>
                <div class="field">
                    <label for="ingredient-name" class="block text-sm font-medium text-gray-700 mb-2"> 재료명 <span class="text-red-500">*</span> </label>
                    <InputText id="ingredient-name" v-model="addIngredientForm.name" placeholder="예: 당근" class="w-full" :class="{ 'p-invalid': addIngredientErrors.name }" maxlength="100" />
                    <small v-if="addIngredientErrors.name" class="p-error">{{ addIngredientErrors.name }}</small>
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">이미지 (선택)</label>
                    <div class="image-input-section space-y-3">
                        <div class="flex gap-2 flex-wrap">
                            <InputText v-model="addIngredientForm.imageUrl" placeholder="이미지 URL (https://...)" class="flex-1 min-w-0" maxlength="500" :disabled="!!addIngredientImageFile" />
                            <input ref="addIngredientFileRef" type="file" accept="image/*" class="hidden" @change="onAddIngredientImageChange" />
                            <Button type="button" label="파일 선택" icon="pi pi-upload" severity="secondary" :loading="addIngredientImageUploading" @click="triggerAddIngredientFileSelect" />
                        </div>
                        <div v-if="addIngredientImagePreview || addIngredientForm.imageUrl" class="flex items-center gap-3">
                            <img v-if="addIngredientImagePreview" :src="addIngredientImagePreview" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" />
                            <img v-else-if="addIngredientForm.imageUrl" :src="addIngredientForm.imageUrl" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" @error="addIngredientForm.imageUrl = ''" />
                            <Button v-if="addIngredientImagePreview || addIngredientForm.imageUrl" type="button" icon="pi pi-times" severity="secondary" text rounded @click="clearAddIngredientImage" />
                        </div>
                    </div>
                </div>
                <div class="field">
                    <label for="ingredient-sortOrder" class="block text-sm font-medium text-gray-700 mb-2">정렬 순서</label>
                    <InputNumber id="ingredient-sortOrder" v-model="addIngredientForm.sortOrder" class="w-full" :min="0" placeholder="0" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="addIngredientDialogVisible = false" />
                <Button label="등록" icon="pi pi-check" :loading="addIngredientSubmitting" @click="submitAddIngredient" />
            </template>
        </Dialog>

        <!-- 재료 수정 다이얼로그 -->
        <Dialog v-model:visible="editIngredientDialogVisible" header="재료 수정" modal :style="{ width: '480px' }" :closable="true" @hide="resetEditIngredientForm">
            <div v-if="editingIngredient && detailGroup" class="space-y-4 py-2">
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">재료 그룹</label>
                    <InputText :model-value="detailGroup.name" class="w-full" disabled />
                </div>
                <div class="field">
                    <label for="edit-ingredient-name" class="block text-sm font-medium text-gray-700 mb-2"> 재료명 <span class="text-red-500">*</span> </label>
                    <InputText id="edit-ingredient-name" v-model="editIngredientForm.name" placeholder="예: 당근" class="w-full" :class="{ 'p-invalid': editIngredientErrors.name }" maxlength="100" />
                    <small v-if="editIngredientErrors.name" class="p-error">{{ editIngredientErrors.name }}</small>
                </div>
                <div class="field">
                    <label class="block text-sm font-medium text-gray-700 mb-2">이미지 (선택)</label>
                    <div class="image-input-section space-y-3">
                        <div class="flex gap-2 flex-wrap">
                            <InputText v-model="editIngredientForm.imageUrl" placeholder="이미지 URL (https://...)" class="flex-1 min-w-0" maxlength="500" :disabled="!!editIngredientImageFile" />
                            <input ref="editIngredientFileRef" type="file" accept="image/*" class="hidden" @change="onEditIngredientImageChange" />
                            <Button type="button" label="파일 선택" icon="pi pi-upload" severity="secondary" :loading="editIngredientImageUploading" @click="triggerEditIngredientFileSelect" />
                        </div>
                        <div v-if="editIngredientImagePreview || editIngredientForm.imageUrl" class="flex items-center gap-3">
                            <img v-if="editIngredientImagePreview" :src="editIngredientImagePreview" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" />
                            <img v-else-if="editIngredientForm.imageUrl" :src="editIngredientForm.imageUrl" alt="미리보기" class="w-20 h-20 object-cover rounded border border-gray-200" @error="editIngredientForm.imageUrl = ''" />
                            <Button v-if="editIngredientImagePreview || editIngredientForm.imageUrl" type="button" icon="pi pi-times" severity="secondary" text rounded @click="clearEditIngredientImage" />
                        </div>
                    </div>
                </div>
                <div class="field">
                    <label for="edit-ingredient-sortOrder" class="block text-sm font-medium text-gray-700 mb-2">정렬 순서</label>
                    <InputNumber id="edit-ingredient-sortOrder" v-model="editIngredientForm.sortOrder" class="w-full" :min="0" placeholder="0" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="editIngredientDialogVisible = false" />
                <Button label="저장" icon="pi pi-check" :loading="editIngredientSubmitting" @click="submitEditIngredient" />
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.ingredient-group-management {
    padding: 24px;
    max-width: 1000px;
    margin: 0 auto;
}
</style>
