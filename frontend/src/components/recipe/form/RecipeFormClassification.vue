<script setup lang="ts">
import Message from 'primevue/message';
import Popover from 'primevue/popover';
import Select from 'primevue/select';
import { ref } from 'vue';
import type { CommonCodeOption } from '@/types/recipeForm';

defineProps<{
    categoryOptions: CommonCodeOption[];
    categories: Record<string, string>;
    categoriesLoading?: boolean;
    categoriesError?: string | null;
    cookingTipsOptions: CommonCodeOption[];
    cookingTips: Record<string, string>;
    cookingTipsLoading?: boolean;
    cookingTipsError?: string | null;
    validationErrors?: Record<string, boolean>;
    disabled?: boolean;
    guideImage?: string | null;
}>();

defineEmits<{
    'update:categories': [value: Record<string, string>];
    'update:cooking-tips': [value: Record<string, string>];
    'clear-validation': [key: string];
}>();

const guideIconRef = ref<HTMLElement | null>(null);
const guidePopoverRef = ref<{ toggle: (e: Event) => void } | null>(null);

function getDetailOptions(option: CommonCodeOption): Array<{ label: string; value: string }> {
    return [{ label: '선택하세요', value: '' }, ...(option.details || []).map((d) => ({ label: d.codeName, value: d.detailCodeId }))];
}
</script>

<template>
    <div class="border border-gray-200 rounded-lg p-5 bg-white">
        <div class="flex items-center gap-1 mb-1">
            <h3 class="text-xl font-semibold text-gray-600">
                <span class="mr-1">분류 정보</span>
                <template v-if="guideImage">
                    <i ref="guideIconRef" class="pi pi-question-circle help-button" style="cursor: pointer" @click="(e) => guidePopoverRef?.toggle?.(e)" />
                    <Popover ref="guidePopoverRef" :target="guideIconRef" :show-close-icon="true" :dismissable="true">
                        <div class="p-2">
                            <img :src="guideImage" alt="분류 정보 가이드" class="max-w-full h-auto" />
                        </div>
                    </Popover>
                </template>
            </h3>
        </div>
        <div class="flex flex-col gap-6">
            <div>
                <label class="block mb-2 font-medium"><b>카테고리</b></label>
                <Message v-if="categoriesError" severity="error" :closable="false" class="mb-2">
                    {{ categoriesError }}
                </Message>
                <div v-else>
                    <div v-if="categoriesLoading" class="p-3 text-gray-500 border border-dashed rounded">카테고리 정보를 불러오는 중입니다...</div>
                    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div v-for="option in categoryOptions" :key="option.codeId" class="flex flex-col gap-2" :data-category-id="`category-${option.codeId}`">
                            <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                            <Select
                                :model-value="categories[option.codeId]"
                                :options="getDetailOptions(option)"
                                option-label="label"
                                option-value="value"
                                placeholder="선택하세요"
                                class="w-full"
                                :class="{ 'border-red-500': validationErrors?.[`category-${option.codeId}`] }"
                                @update:model-value="(v) => $emit('update:categories', { ...categories, [option.codeId]: v })"
                                @change="$emit('clear-validation', `category-${option.codeId}`)"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <label class="block mb-2 font-medium"><b>요리팁</b></label>
                <Message v-if="cookingTipsError" severity="error" :closable="false" class="mb-2">
                    {{ cookingTipsError }}
                </Message>
                <div v-else>
                    <div v-if="cookingTipsLoading" class="p-3 text-gray-500 border border-dashed rounded">요리팁 정보를 불러오는 중입니다...</div>
                    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div v-for="option in cookingTipsOptions" :key="option.codeId" class="flex flex-col gap-2" :data-cooking-tip-id="`cookingTip-${option.codeId}`">
                            <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                            <Select
                                :model-value="cookingTips[option.codeId]"
                                :options="getDetailOptions(option)"
                                option-label="label"
                                option-value="value"
                                placeholder="선택하세요"
                                class="w-full"
                                :class="{ 'border-red-500': validationErrors?.[`cookingTip-${option.codeId}`] }"
                                @update:model-value="(v) => $emit('update:cooking-tips', { ...cookingTips, [option.codeId]: v })"
                                @change="$emit('clear-validation', `cookingTip-${option.codeId}`)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
