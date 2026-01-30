<template>
    <div class="ingredient-group-register">
        <div class="page-header mb-6">
            <div class="flex items-center gap-2 mb-2">
                <Button icon="pi pi-arrow-left" text rounded @click="goBack" />
                <h1 class="text-3xl font-bold text-gray-900">재료 그룹 등록</h1>
            </div>
            <p class="text-gray-600 mt-2">재료를 분류할 그룹을 등록하세요 (예: 채소, 과일, 육류)</p>
        </div>

        <Card>
            <template #content>
                <form @submit.prevent="handleSubmit" class="space-y-6">
                    <div class="field">
                        <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
                            그룹명 <span class="text-red-500">*</span>
                        </label>
                        <InputText
                            id="name"
                            v-model="form.name"
                            placeholder="예: 채소"
                            class="w-full"
                            :class="{ 'p-invalid': errors.name }"
                            maxlength="50"
                        />
                        <small v-if="errors.name" class="p-error">{{ errors.name }}</small>
                        <small v-else class="text-gray-500">{{ form.name?.length || 0 }}/50</small>
                    </div>

                    <div class="field">
                        <label for="imageUrl" class="block text-sm font-medium text-gray-700 mb-2">
                            이미지 URL (선택)
                        </label>
                        <InputText
                            id="imageUrl"
                            v-model="form.imageUrl"
                            placeholder="https://..."
                            class="w-full"
                            maxlength="200"
                        />
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
import Button from 'primevue/button';
import { createIngredientGroup } from '@/api/ingredientApi';

const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();

if (!authStore.isAdmin) {
    router.replace('/admin');
}

const form = ref({
    name: '',
    imageUrl: '',
    sortOrder: 0 as number | null
});
const submitting = ref(false);
const errors = ref<{ name?: string }>({});

const validateForm = (): boolean => {
    errors.value = {};
    const name = form.value.name?.trim();
    if (!name) {
        errors.value.name = '그룹명을 입력해주세요.';
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
        await createIngredientGroup({
            name: form.value.name.trim(),
            imageUrl: form.value.imageUrl?.trim() || undefined,
            sortOrder: form.value.sortOrder ?? undefined
        });
        toast.add({ severity: 'success', summary: '등록 완료', detail: '재료 그룹이 등록되었습니다.', life: 3000 });
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

onMounted(() => {
    if (!authStore.isAdmin) {
        router.replace('/admin');
    }
});
</script>

<style scoped>
.ingredient-group-register {
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
