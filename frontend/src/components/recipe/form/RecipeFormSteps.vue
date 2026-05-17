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
    <div class="recipe-form-section">
        <div class="recipe-form-section__header">
            <div class="recipe-form-section__title-row mb-0">
                <h3 class="recipe-form-section__title">
                    <span class="mr-1">조리 순서</span>
                    <template v-if="guideImage">
                        <i ref="guideIconRef" class="pi pi-question-circle help-button" @click="(e) => guidePopoverRef?.toggle?.(e)" />
                        <Popover ref="guidePopoverRef" :target="guideIconRef" :show-close-icon="true" :dismissable="true">
                            <div class="p-2">
                                <img :src="guideImage" alt="조리 순서 가이드" class="max-w-full h-auto" />
                            </div>
                        </Popover>
                    </template>
                </h3>
            </div>
            <div data-step-add-button>
                <Button class="recipe-form-btn" label="단계 추가" icon="pi pi-plus" @click="addStep" :disabled="disabled" />
            </div>
        </div>
        <div v-if="modelValue.length === 0" class="recipe-form-section__empty">아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.</div>

        <div v-for="(step, index) in modelValue" :key="step.id" class="recipe-form-block" :data-step-index="index">
            <div class="recipe-form-block__toolbar">
                <div class="recipe-form-block__toolbar-title">단계 {{ index + 1 }}</div>
                <div class="recipe-form-block__toolbar-actions">
                    <Button class="recipe-form-btn recipe-form-btn--icon-only" icon="pi pi-arrow-up" severity="secondary" aria-label="위로 이동" @click="moveStepUp(index)" :disabled="index === 0 || disabled" />
                    <Button class="recipe-form-btn recipe-form-btn--icon-only" icon="pi pi-arrow-down" severity="secondary" aria-label="아래로 이동" @click="moveStepDown(index)" :disabled="index === modelValue.length - 1 || disabled" />
                    <Button class="recipe-form-btn recipe-form-btn--icon-only" icon="pi pi-trash" severity="danger" aria-label="단계 삭제" @click="removeStep(index)" :disabled="disabled" />
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
                <div class="md:col-span-2">
                    <label class="recipe-form-section__label">이미지</label>
                    <input :ref="(el) => setStepInputRef(step.id, el)" type="file" accept="image/*" class="hidden" :disabled="disabled" @change="onStepImageChange($event, step)" />
                    <div class="recipe-form-upload" role="button" tabindex="0" @click="() => !disabled && stepInputRefs[step.id]?.click()">
                        <div v-if="!step.previewUrl" class="recipe-form-upload__placeholder">
                            <span class="pi pi-image recipe-form-upload__placeholder-icon" aria-hidden="true"></span>
                            <span>이미지를 클릭하여 추가하세요</span>
                        </div>
                        <div v-else class="relative w-full h-full min-h-0">
                            <img :src="step.previewUrl" alt="step preview" class="w-full h-full object-cover rounded-md" />
                            <button
                                type="button"
                                class="recipe-form-upload__remove"
                                :disabled="disabled"
                                aria-label="단계 이미지 삭제"
                                @click.stop="$emit('step-image-clear', step.id)"
                            >
                                <span class="pi pi-times" aria-hidden="true"></span>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="md:col-span-3">
                    <label class="recipe-form-section__label">설명</label>
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
