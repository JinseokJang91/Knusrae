<template>
    <div class="ingredient-register-admin">
        <div class="page-header mb-6">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goBack" />
                <h1 class="text-3xl font-bold text-gray-900">재료 등록</h1>
            </div>
            <p class="text-gray-600 mt-2">그룹에 속할 재료를 등록하세요</p>
        </div>

        <Card>
            <template #content>
                <form @submit.prevent="handleSubmit" class="space-y-6">
                    <div class="field">
                        <label for="groupId" class="block text-sm font-medium text-gray-700 mb-2">
                            재료 그룹 <span class="text-red-500">*</span>
                        </label>
                        <Select
                            id="groupId"
                            v-model="form.groupId"
                            :options="groups"
                            option-label="name"
                            option-value="id"
                            placeholder="그룹 선택"
                            class="w-full"
                            :class="{ 'p-invalid': errors.groupId }"
                            :loading="groupsLoading"
                        />
                        <small v-if="errors.groupId" class="p-error">{{ errors.groupId }}</small>
                    </div>

                    <div class="field">
                        <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
                            재료명 <span class="text-red-500">*</span>
                        </label>
                        <InputText
                            id="name"
                            v-model="form.name"
                            placeholder="예: 당근"
                            class="w-full"
                            :class="{ 'p-invalid': errors.name }"
                            maxlength="100"
                        />
                        <small v-if="errors.name" class="p-error">{{ errors.name }}</small>
                        <small v-else class="text-gray-500">{{ form.name?.length || 0 }}/100</small>
                    </div>

                    <div class="field">
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                            이미지 (선택)
                        </label>
                        <div class="image-input-section space-y-3">
                            <div class="flex gap-2">
                                <InputText
                                    id="imageUrl"
                                    v-model="form.imageUrl"
                                    placeholder="이미지 URL 입력 (https://...)"
                                    class="flex-1"
                                    maxlength="500"
                                    :disabled="!!imageFile"
                                />
                                <span class="self-center text-gray-400 text-sm">또는</span>
                                <div class="relative">
                                    <input
                                        ref="imageFileInputRef"
                                        type="file"
                                        accept="image/*"
                                        class="hidden"
                                        @change="onImageFileChange"
                                    />
                                    <Button
                                        type="button"
                                        label="파일 선택"
                                        icon="pi pi-upload"
                                        severity="secondary"
                                        :loading="imageUploading"
                                        :disabled="imageUploading"
                                        @click="triggerImageFileSelect"
                                    />
                                </div>
                            </div>
                            <div v-if="imagePreview || form.imageUrl" class="image-preview-area flex items-center gap-3">
                                <img
                                    v-if="imagePreview"
                                    :src="imagePreview"
                                    alt="미리보기"
                                    class="w-20 h-20 object-cover rounded border border-gray-200"
                                />
                                <img
                                    v-else-if="form.imageUrl"
                                    :src="form.imageUrl"
                                    alt="미리보기"
                                    class="w-20 h-20 object-cover rounded border border-gray-200"
                                    @error="onPreviewImageError"
                                />
                                <Button
                                    v-if="imagePreview || form.imageUrl"
                                    type="button"
                                    icon="pi pi-times"
                                    severity="secondary"
                                    text
                                    rounded
                                    @click="clearImage"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="field">
                        <label for="sortOrder" class="block text-sm font-medium text-gray-700 mb-2">
                            정렬 순서 (선택, 숫자 작을수록 먼저)
                        </label>
                        <InputNumber
                            id="sortOrder"
                            v-model="form.sortOrder"
                            placeholder="0"
                            class="w-full"
                            :min="0"
                        />
                    </div>

                    <div class="flex gap-3 justify-end">
                        <Button label="취소" severity="secondary" @click="goBack" :disabled="submitting" />
                        <Button label="저장" icon="pi pi-check" type="submit" :loading="submitting" :disabled="submitting" />
                    </div>
                </form>
            </template>
        </Card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useToast } from 'primevue/usetoast';
import Card from 'primevue/card';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Select from 'primevue/select';
import Button from 'primevue/button';
import { createIngredient, getIngredientGroups, uploadContentImage } from '@/api/ingredientApi';
import type { IngredientGroup } from '@/types/ingredient';

const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();

if (!authStore.isAdmin) {
    router.replace('/admin');
}

const groups = ref<IngredientGroup[]>([]);
const groupsLoading = ref(true);

const form = ref({
    groupId: null as number | null,
    name: '',
    imageUrl: '',
    sortOrder: 0 as number | null
});
const submitting = ref(false);
const errors = ref<{ groupId?: string; name?: string }>({});
const imageFileInputRef = ref<HTMLInputElement | null>(null);
const imageFile = ref<File | null>(null);
const imagePreview = ref<string | null>(null);
const imageUploading = ref(false);

const triggerImageFileSelect = () => {
    imageFileInputRef.value?.click();
};

const onImageFileChange = async (e: Event) => {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
        toast.add({ severity: 'warn', summary: '파일 형식', detail: '이미지 파일만 업로드할 수 있습니다.', life: 3000 });
        input.value = '';
        return;
    }
    imageFile.value = file;
    imagePreview.value = URL.createObjectURL(file);
    imageUploading.value = true;
    try {
        const { url } = await uploadContentImage(file);
        form.value.imageUrl = url;
    } catch (err) {
        const msg = err instanceof Error ? err.message : '이미지 업로드에 실패했습니다.';
        toast.add({ severity: 'error', summary: '업로드 실패', detail: msg, life: 5000 });
        clearImage();
    } finally {
        imageUploading.value = false;
        input.value = '';
    }
};

const onPreviewImageError = () => {
    // URL이 유효하지 않은 이미지인 경우 무시
};

const clearImage = () => {
    form.value.imageUrl = '';
    imageFile.value = null;
    if (imagePreview.value) {
        URL.revokeObjectURL(imagePreview.value);
        imagePreview.value = null;
    }
    if (imageFileInputRef.value) {
        imageFileInputRef.value.value = '';
    }
};

const loadGroups = async () => {
    groupsLoading.value = true;
    try {
        groups.value = await getIngredientGroups();
    } catch (e) {
        console.error('재료 그룹 목록 로드 실패:', e);
        toast.add({ severity: 'error', summary: '로드 실패', detail: '재료 그룹 목록을 불러올 수 없습니다.', life: 5000 });
    } finally {
        groupsLoading.value = false;
    }
};

const validateForm = (): boolean => {
    errors.value = {};
    if (form.value.groupId == null) {
        errors.value.groupId = '재료 그룹을 선택해주세요.';
    }
    const name = form.value.name?.trim();
    if (!name) {
        errors.value.name = '재료명을 입력해주세요.';
    }
    return Object.keys(errors.value).length === 0;
};

const handleSubmit = async () => {
    if (!validateForm()) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '입력한 내용을 확인해주세요.', life: 3000 });
        return;
    }
    submitting.value = true;
    try {
        await createIngredient({
            groupId: form.value.groupId!,
            name: form.value.name.trim(),
            imageUrl: form.value.imageUrl?.trim() || undefined,
            sortOrder: form.value.sortOrder ?? undefined
        });
        toast.add({ severity: 'success', summary: '등록 완료', detail: '재료가 등록되었습니다.', life: 3000 });
        router.push('/admin');
    } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : '등록 중 오류가 발생했습니다.';
        toast.add({ severity: 'error', summary: '등록 실패', detail: msg, life: 5000 });
    } finally {
        submitting.value = false;
    }
};

const goBack = () => {
    router.push('/admin');
};

onMounted(async () => {
    if (!authStore.isAdmin) {
        router.replace('/admin');
        return;
    }
    await loadGroups();
});
</script>

<style scoped>
.ingredient-register-admin {
    padding: 24px;
    max-width: 600px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 24px;
}

.field {
    margin-bottom: 24px;
}
</style>
