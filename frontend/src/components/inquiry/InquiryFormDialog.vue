<template>
    <Dialog
        v-model:visible="visible"
        :header="isEditMode ? '문의 수정' : '1:1 문의 작성'"
        modal
        :style="{ width: '520px' }"
        :draggable="false"
        class="inquiry-form-dialog"
        @hide="closeDialog"
    >
        <div class="inquiry-form">
            <div class="field mb-4">
                <label class="block mb-2 font-medium">제목 <span class="text-red-500">*</span></label>
                <InputText
                    v-model="form.title"
                    placeholder="제목을 입력하세요"
                    class="w-full"
                    :maxlength="100"
                />
                <small class="text-color-secondary">{{ form.title.length }}/100</small>
            </div>

            <div class="field mb-4">
                <label class="block mb-2 font-medium">문의 유형 <span class="text-red-500">*</span></label>
                <Select
                    v-model="form.inquiryType"
                    :options="INQUIRY_TYPES"
                    option-label="label"
                    option-value="value"
                    placeholder="선택하세요"
                    class="w-full"
                />
            </div>

            <div class="field mb-4">
                <label class="block mb-2 font-medium">내용 <span class="text-red-500">*</span></label>
                <Textarea
                    v-model="form.content"
                    placeholder="문의 내용을 입력하세요"
                    class="w-full"
                    rows="5"
                    :maxlength="1000"
                />
                <small class="text-color-secondary">{{ form.content.length }}/1000</small>
            </div>

            <div class="field mb-4">
                <label class="block mb-2 font-medium">사진 첨부 (최대 3장)</label>
                <input
                    ref="fileInputRef"
                    type="file"
                    accept="image/*"
                    multiple
                    class="hidden"
                    @change="onFileSelect"
                />
                <div class="flex flex-wrap gap-2 align-items-center">
                    <Button
                        label="파일 선택"
                        icon="pi pi-upload"
                        severity="secondary"
                        outlined
                        :disabled="form.images.length >= 3"
                        @click="fileInputRef?.click()"
                    />
                    <span v-if="form.images.length > 0" class="text-sm text-color-secondary">
                        {{ form.images.length }} / 3 장
                    </span>
                </div>
                <div v-if="form.images.length > 0" class="flex flex-wrap gap-2 mt-2">
                    <div
                        v-for="(file, idx) in form.images"
                        :key="idx"
                        class="image-preview-wrap"
                    >
                        <img
                            v-if="isPreviewUrl(file)"
                            :src="filePreview(file)"
                            alt="미리보기"
                            class="image-preview"
                        />
                        <img
                            v-else
                            :src="filePreview(file)"
                            alt="미리보기"
                            class="image-preview"
                        />
                        <Button
                            icon="pi pi-times"
                            text
                            rounded
                            severity="danger"
                            class="image-remove-btn"
                            @click="removeImage(idx)"
                        />
                    </div>
                </div>
            </div>
        </div>

        <template #footer>
            <Button label="취소" text @click="closeDialog" />
            <Button
                :label="isEditMode ? '수정' : '등록'"
                :disabled="!canSubmit || loading"
                :loading="loading"
                @click="submit"
            />
        </template>
    </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Select from 'primevue/select';
import { createInquiry, updateInquiry, getInquiryDetail } from '@/api/inquiryApi';
import { INQUIRY_TYPES } from '@/types/inquiry';
import type { InquiryType } from '@/types/inquiry';
import { useAppToast } from '@/utils/toast';

const props = defineProps<{
    visible: boolean;
    inquiryId?: number | null;
}>();

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'saved'): void;
    (e: 'closed'): void;
}>();

const { showSuccess, showError } = useAppToast();

const visible = ref(props.visible);
const loading = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);

const form = ref({
    title: '',
    inquiryType: '' as InquiryType | '',
    content: '',
    images: [] as (File | string)[]
});

const isEditMode = computed(() => !!props.inquiryId);

const canSubmit = computed(() => {
    const t = form.value.title.trim();
    const type = form.value.inquiryType;
    const c = form.value.content.trim();
    return t.length > 0 && type !== '' && c.length > 0;
});

watch(
    () => props.visible,
    (val) => {
        visible.value = val;
        if (val) {
            resetForm();
            if (props.inquiryId) {
                loadDetail();
            }
        }
    }
);

watch(visible, (val) => emit('update:visible', val));

function resetForm() {
    form.value = {
        title: '',
        inquiryType: '',
        content: '',
        images: []
    };
}

async function loadDetail() {
    if (!props.inquiryId) return;
    try {
        loading.value = true;
        const detail = await getInquiryDetail(props.inquiryId);
        form.value.title = detail.title;
        form.value.inquiryType = detail.inquiryType as InquiryType;
        form.value.content = detail.content;
        form.value.images = detail.imageUrls ?? [];
    } catch (err) {
        console.error('문의 상세 로드 실패:', err);
        showError('문의 정보를 불러오지 못했습니다.');
        closeDialog();
    } finally {
        loading.value = false;
    }
}

function onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    const current = form.value.images.filter((x): x is File => x instanceof File);
    const added = files.slice(0, 3 - current.length);
    form.value.images = [...current, ...added];
    input.value = '';
}

function isPreviewUrl(item: File | string): item is string {
    return typeof item === 'string';
}

function filePreview(item: File | string): string {
    if (typeof item === 'string') return item;
    return URL.createObjectURL(item);
}

function removeImage(index: number) {
    form.value.images.splice(index, 1);
}

function buildFormData(): FormData {
    const fd = new FormData();
    fd.append('title', form.value.title.trim());
    fd.append('inquiryType', form.value.inquiryType);
    fd.append('content', form.value.content.trim());
    const files = form.value.images.filter((x): x is File => x instanceof File);
    files.forEach((file) => fd.append('images', file));
    return fd;
}

async function submit() {
    if (!canSubmit.value) return;
    try {
        loading.value = true;
        if (isEditMode.value && props.inquiryId) {
            await updateInquiry(props.inquiryId, buildFormData());
            showSuccess('문의가 수정되었습니다.');
        } else {
            await createInquiry(buildFormData());
            showSuccess('문의가 등록되었습니다.');
        }
        emit('saved');
    } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err);
        if (msg.includes('답변이 달린') || msg.includes('409')) {
            showError('답변이 달린 문의는 수정할 수 없습니다.');
        } else {
            showError(msg || (isEditMode.value ? '수정에 실패했습니다.' : '등록에 실패했습니다.'));
        }
    } finally {
        loading.value = false;
    }
}

function closeDialog() {
    visible.value = false;
    emit('closed');
}
</script>

<style scoped>
.inquiry-form-dialog :deep(.p-dialog-content) {
    padding-top: 0.5rem;
}

.image-preview-wrap {
    position: relative;
    width: 80px;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid var(--p-surface-300);
}

.image-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.image-remove-btn {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 24px;
    height: 24px;
    padding: 0;
    min-width: unset;
    background: rgba(0, 0, 0, 0.5);
    color: white;
}

.image-remove-btn:hover {
    background: rgba(200, 0, 0, 0.8);
}
</style>
