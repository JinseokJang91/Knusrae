<script setup lang="ts">
import InputText from 'primevue/inputtext';
import Popover from 'primevue/popover';
import Textarea from 'primevue/textarea';
import { ref } from 'vue';

defineProps<{
    title: string;
    description: string;
    thumbnailPreview: string;
    disabled?: boolean;
    validationErrors?: Record<string, boolean>;
    guideImage?: string | null;
}>();

const emit = defineEmits<{
    'update:title': [value: string];
    'update:description': [value: string];
    'update:thumbnail': [payload: { file: File; preview: string }];
    'clear-thumbnail': [];
    'clear-validation': [key: string];
}>();

const fileInputRef = ref<HTMLInputElement | null>(null);
const titleInputRef = ref<InstanceType<typeof InputText> | null>(null);
const guideIconRef = ref<HTMLElement | null>(null);
const guidePopoverRef = ref<{ toggle: (e: Event) => void } | null>(null);

function onUpdateTitle(value: string): void {
    emit('update:title', value);
}
function onClearTitleValidation(): void {
    emit('clear-validation', 'title');
}

function onFileChange(e: Event): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const preview = URL.createObjectURL(file);
    emit('update:thumbnail', { file, preview });
    input.value = '';
}

defineExpose({
    focusTitle: () => (titleInputRef.value as unknown as { $el?: HTMLElement })?.$el?.querySelector<HTMLInputElement>('input')?.focus(),
    clickThumbnailInput: () => fileInputRef.value?.click()
});
</script>

<template>
    <div class="recipe-form-section">
        <div class="recipe-form-section__title-row">
            <h3 class="recipe-form-section__title">
                <span class="mr-1">기본 정보</span>
                <template v-if="guideImage">
                    <i ref="guideIconRef" class="pi pi-question-circle help-button" @click="(e) => guidePopoverRef?.toggle?.(e)" />
                    <Popover ref="guidePopoverRef" :target="guideIconRef" :show-close-icon="true" :dismissable="true">
                        <div class="p-2">
                            <img :src="guideImage" alt="기본 정보 가이드" class="max-w-full h-auto" />
                        </div>
                    </Popover>
                </template>
            </h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div class="md:col-span-2">
                <label class="recipe-form-section__label"><b>대표 사진</b></label>
                <input ref="fileInputRef" type="file" accept="image/*" class="hidden" :disabled="disabled" @change="onFileChange" />
                <div
                    class="recipe-form-upload"
                    role="button"
                    tabindex="0"
                    @click="() => !disabled && fileInputRef?.click()"
                    @keydown.enter.prevent="() => !disabled && fileInputRef?.click()"
                >
                    <div v-if="!thumbnailPreview" class="recipe-form-upload__placeholder">
                        <span class="pi pi-image recipe-form-upload__placeholder-icon" aria-hidden="true"></span>
                        <span>이미지를 클릭하여 추가하세요</span>
                    </div>
                    <div v-else class="relative w-full h-full min-h-0">
                        <img :src="thumbnailPreview" alt="thumbnail preview" class="w-full h-full object-cover rounded-md" />
                        <button
                            type="button"
                            class="recipe-form-upload__remove"
                            :disabled="disabled"
                            aria-label="대표 사진 삭제"
                            @click.stop="$emit('clear-thumbnail')"
                        >
                            <span class="pi pi-times" aria-hidden="true"></span>
                        </button>
                    </div>
                </div>
                <p class="recipe-form-section__hint">등록 시 썸네일이 대표 이미지로 사용됩니다.</p>
            </div>

            <div class="md:col-span-3 flex flex-col gap-4">
                <div>
                    <label class="recipe-form-section__label"><b>제목</b></label>
                    <InputText
                        ref="titleInputRef"
                        :model-value="title"
                        placeholder="레시피 제목을 입력하세요"
                        class="w-full"
                        :class="{ 'border-red-500': validationErrors?.title }"
                        @update:model-value="onUpdateTitle($event ?? '')"
                        @input="onClearTitleValidation"
                    />
                </div>
                <div class="flex-1">
                    <label class="recipe-form-section__label"><b>소개</b></label>
                    <Textarea :model-value="description" placeholder="간단한 소개나 메모를 작성하세요" class="w-full" :rows="9" @update:model-value="$emit('update:description', $event)" />
                </div>
            </div>
        </div>
    </div>
</template>
