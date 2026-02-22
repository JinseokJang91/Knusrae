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
    <div class="border border-gray-200 rounded-lg p-5 bg-white">
        <div class="flex items-center gap-1 mb-1">
            <h3 class="text-xl font-semibold text-gray-600">
                <span class="mr-1">기본 정보</span>
                <template v-if="guideImage">
                    <i ref="guideIconRef" class="pi pi-question-circle help-button" style="cursor: pointer" @click="(e) => guidePopoverRef?.toggle?.(e)" />
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
                <label class="block mb-2 font-medium"><b>대표 사진</b></label>
                <input ref="fileInputRef" type="file" accept="image/*" class="hidden" :disabled="disabled" @change="onFileChange" />
                <div
                    class="relative w-full aspect-[5/3] bg-gray-200 border-2 border-dashed border-gray-300 rounded-md cursor-pointer hover:bg-gray-300 hover:border-gray-400 transition-colors flex items-center justify-center"
                    @click="() => !disabled && fileInputRef?.click()"
                >
                    <div v-if="!thumbnailPreview" class="text-center text-gray-500">
                        <span class="pi pi-image text-4xl block mb-2"></span>
                        <span class="text-sm">이미지를 클릭하여 추가하세요</span>
                    </div>
                    <div v-else class="group relative w-full h-full">
                        <img :src="thumbnailPreview" alt="thumbnail preview" class="w-full h-full object-cover rounded-md" />
                        <button
                            type="button"
                            class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                            :disabled="disabled"
                            @click.stop="$emit('clear-thumbnail')"
                        >
                            <span class="pi pi-times"></span>
                        </button>
                    </div>
                </div>
                <p class="text-sm text-gray-500 mt-1">등록 시 썸네일이 대표 이미지로 사용됩니다.</p>
            </div>

            <div class="md:col-span-3 flex flex-col gap-4">
                <div>
                    <label class="block mb-2 font-medium"><b>제목</b></label>
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
                    <label class="block mb-2 font-medium"><b>소개</b></label>
                    <Textarea :model-value="description" placeholder="간단한 소개나 메모를 작성하세요" class="w-full" :rows="9" @update:model-value="$emit('update:description', $event)" />
                </div>
            </div>
        </div>
    </div>
</template>
