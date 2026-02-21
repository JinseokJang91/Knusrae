<script setup lang="ts">
import Button from 'primevue/button';
import Popover from 'primevue/popover';
import Textarea from 'primevue/textarea';
import { ref } from 'vue';
import type { RecipeStepDraft } from '@/types/recipeForm';
import { generateRecipeFormId } from '@/types/recipeForm';

const props = withDefaults(
    defineProps<{
        modelValue: RecipeStepDraft[];
        validationErrors?: Record<string, boolean>;
        disabled?: boolean;
        guideImage?: string | null;
    }>(),
    { disabled: false, guideImage: null }
);

const emit = defineEmits<{
    'update:modelValue': [value: RecipeStepDraft[]];
    'step-image-change': [stepId: string, file: File, previewUrl: string];
    'step-image-clear': [stepId: string];
    'clear-validation': [key: string];
}>();

const guideIconRef = ref<HTMLElement | null>(null);
const guidePopoverRef = ref<{ toggle: (e: Event) => void } | null>(null);
const stepInputRefs = ref<Record<string, HTMLInputElement>>({});

function setStepInputRef(stepId: string, el: unknown): void {
    if (el) {
        stepInputRefs.value[stepId] = el as HTMLInputElement;
    }
}

function swap<T>(arr: T[], i: number, j: number): void {
    if (i < 0 || j < 0 || i >= arr.length || j >= arr.length) return;
    [arr[i], arr[j]] = [arr[j], arr[i]];
}

function emitUpdate(steps: RecipeStepDraft[]): void {
    emit('update:modelValue', steps);
}

function addStep(): void {
    emitUpdate([...props.modelValue, { id: generateRecipeFormId(), file: null, text: '', previewUrl: '' }]);
}

function removeStep(index: number): void {
    const removed = props.modelValue[index];
    if (removed?.previewUrl) {
        URL.revokeObjectURL(removed.previewUrl);
    }
    emitUpdate(props.modelValue.filter((_, i) => i !== index));
}

function moveStepUp(index: number): void {
    if (index <= 0) return;
    const next = [...props.modelValue];
    swap(next, index - 1, index);
    emitUpdate(next);
}

function moveStepDown(index: number): void {
    if (index >= props.modelValue.length - 1) return;
    const next = [...props.modelValue];
    swap(next, index, index + 1);
    emitUpdate(next);
}

function updateStepText(index: number, text: string): void {
    const next = props.modelValue.map((s, i) => (i === index ? { ...s, text } : s));
    emitUpdate(next);
}

function onStepImageChange(e: Event, step: RecipeStepDraft): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const previewUrl = URL.createObjectURL(file);
    emit('step-image-change', step.id, file, previewUrl);
    input.value = '';
}
</script>

<template>
    <div class="border border-gray-200 rounded-lg p-5 bg-white">
        <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
                <h3 class="text-xl font-semibold text-gray-600">
                    <span class="mr-1">조리 순서</span>
                    <template v-if="guideImage">
                        <i ref="guideIconRef" class="pi pi-question-circle help-button" style="cursor: pointer" @click="(e) => guidePopoverRef?.toggle?.(e)" />
                        <Popover ref="guidePopoverRef" :target="guideIconRef" :show-close-icon="true" :dismissable="true">
                            <div class="p-2">
                                <img :src="guideImage" alt="조리 순서 가이드" class="max-w-full h-auto" />
                            </div>
                        </Popover>
                    </template>
                </h3>
            </div>
            <div data-step-add-button>
                <Button label="단계 추가" icon="pi pi-plus" @click="addStep" :disabled="disabled" />
            </div>
        </div>
        <div v-if="modelValue.length === 0" class="p-3 text-gray-500 border rounded">아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.</div>

        <div v-for="(step, index) in modelValue" :key="step.id" class="border rounded p-3 mb-3 bg-gray-50" :data-step-index="index">
            <div class="flex items-center justify-between mb-3">
                <div class="font-medium">단계 {{ index + 1 }}</div>
                <div class="flex gap-2">
                    <Button icon="pi pi-arrow-up" severity="secondary" size="small" @click="moveStepUp(index)" :disabled="index === 0 || disabled" />
                    <Button icon="pi pi-arrow-down" severity="secondary" size="small" @click="moveStepDown(index)" :disabled="index === modelValue.length - 1 || disabled" />
                    <Button icon="pi pi-trash" severity="danger" size="small" @click="removeStep(index)" :disabled="disabled" />
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
                <div class="md:col-span-2">
                    <label class="block mb-2">이미지</label>
                    <input :ref="(el) => setStepInputRef(step.id, el)" type="file" accept="image/*" class="hidden" :disabled="disabled" @change="onStepImageChange($event, step)" />
                    <div
                        class="relative w-full aspect-[5/3] bg-gray-200 border-2 border-dashed border-gray-300 rounded-md cursor-pointer hover:bg-gray-300 hover:border-gray-400 transition-colors flex items-center justify-center"
                        @click="() => !disabled && stepInputRefs[step.id]?.click()"
                    >
                        <div v-if="!step.previewUrl" class="text-center text-gray-500">
                            <span class="pi pi-image text-4xl block mb-2"></span>
                            <span class="text-sm">이미지를 클릭하여 추가하세요</span>
                        </div>
                        <div v-else class="group relative w-full h-full">
                            <img :src="step.previewUrl" alt="step preview" class="w-full h-full object-cover rounded-md" />
                            <button
                                type="button"
                                class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                                :disabled="disabled"
                                @click.stop="$emit('step-image-clear', step.id)"
                            >
                                <span class="pi pi-times"></span>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="md:col-span-3">
                    <label class="block mb-2">설명</label>
                    <Textarea
                        :model-value="step.text"
                        placeholder="이 단계에서의 설명을 작성하세요"
                        class="w-full"
                        :class="{ 'border-red-500': validationErrors?.[`step-text-${index}`] }"
                        :rows="9"
                        @update:model-value="(v) => updateStepText(index, v)"
                        @input="$emit('clear-validation', `step-text-${index}`)"
                    />
                </div>
            </div>
        </div>
    </div>
</template>
