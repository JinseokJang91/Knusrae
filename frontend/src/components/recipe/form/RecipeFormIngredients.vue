<template>
    <div class="border border-gray-200 rounded-lg p-5 bg-white">
        <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
                <h3 class="text-xl font-semibold text-gray-600">
                    <span class="mr-1">준비물</span>
                    <template v-if="guideImage">
                        <i
                            ref="guideIconRef"
                            class="pi pi-question-circle help-button"
                            style="cursor: pointer;"
                            @click="(e) => guidePopoverRef?.toggle?.(e)"
                        />
                        <Popover
                            ref="guidePopoverRef"
                            :target="guideIconRef"
                            :show-close-icon="true"
                            :dismissable="true"
                        >
                            <div class="p-2">
                                <img :src="guideImage" alt="준비물 가이드" class="max-w-full h-auto" />
                            </div>
                        </Popover>
                    </template>
                </h3>
            </div>
            <Button label="그룹 추가" icon="pi pi-plus" @click="addGroup" :disabled="disabled" />
        </div>
        <div v-if="modelValue.length === 0" class="p-3 text-gray-500 border rounded">
            아직 준비물 그룹이 없습니다. '그룹 추가'를 눌러 시작하세요.
        </div>

        <div v-for="(group, groupIndex) in modelValue" :key="group.id" class="border rounded p-4 mb-4 bg-gray-50">
            <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-3 flex-1">
                    <div class="font-medium text-gray-700">그룹 {{ groupIndex + 1 }}</div>
                    <div class="flex-1 max-w-xs">
                        <Message v-if="ingredientTypesError" severity="error" :closable="false" class="mb-2">
                            {{ ingredientTypesError }}
                        </Message>
                        <div v-else-if="ingredientTypesLoading" class="p-2 text-sm text-gray-500">
                            로딩 중...
                        </div>
                        <Select
                            v-else
                            :model-value="group.type"
                            :options="ingredientTypeSelectOptions"
                            option-label="label"
                            option-value="value"
                            placeholder="주제를 선택하세요"
                            class="w-full"
                            :class="{ 'border-red-500': validationErrors[`group-type-${group.id}`] }"
                            filter
                            show-clear
                            @update:model-value="(v) => updateGroupType(groupIndex, v)"
                            @change="$emit('clear-validation', `group-type-${group.id}`)"
                        />
                    </div>
                    <div v-if="group.type === 'CUSTOM'" class="flex-1 max-w-xs">
                        <InputText
                            :model-value="group.customTypeName"
                            placeholder="그룹 타입을 직접 입력하세요"
                            class="w-full"
                            @update:model-value="(v) => updateGroupCustomTypeName(groupIndex, v)"
                        />
                    </div>
                </div>
                <div class="flex gap-2">
                    <Button icon="pi pi-arrow-up" severity="secondary" size="small" @click="moveGroupUp(groupIndex)" :disabled="groupIndex === 0 || disabled" />
                    <Button icon="pi pi-arrow-down" severity="secondary" size="small" @click="moveGroupDown(groupIndex)" :disabled="groupIndex === modelValue.length - 1 || disabled" />
                    <Button icon="pi pi-trash" severity="danger" size="small" @click="removeGroup(groupIndex)" :disabled="disabled" />
                </div>
            </div>

            <div class="mb-3">
                <Button label="항목 추가" icon="pi pi-plus" size="small" @click="addItem(groupIndex)" :disabled="disabled" />
            </div>

            <div v-if="group.items.length === 0" class="p-2 text-sm text-gray-400 border border-dashed rounded mb-2">
                이 그룹에 항목이 없습니다. '항목 추가'를 눌러 추가하세요.
            </div>

            <div v-for="(item, itemIndex) in group.items" :key="item.id" class="border rounded p-3 mb-2 bg-white">
                <div class="flex items-center justify-between mb-2">
                    <div class="text-sm text-gray-600">항목 {{ itemIndex + 1 }}</div>
                    <div class="flex gap-2">
                        <Button icon="pi pi-arrow-up" severity="secondary" size="small" @click="moveItemUp(groupIndex, itemIndex)" :disabled="itemIndex === 0 || disabled" />
                        <Button icon="pi pi-arrow-down" severity="secondary" size="small" @click="moveItemDown(groupIndex, itemIndex)" :disabled="itemIndex === group.items.length - 1 || disabled" />
                        <Button icon="pi pi-trash" severity="danger" size="small" @click="removeItem(groupIndex, itemIndex)" :disabled="disabled" />
                    </div>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label class="block mb-2 text-sm">이름</label>
                        <InputText
                            :model-value="item.name"
                            placeholder="재료명을 입력하세요"
                            class="w-full"
                            @update:model-value="(v) => updateItemField(groupIndex, itemIndex, 'name', v)"
                        />
                    </div>
                    <div>
                        <label class="block mb-2 text-sm">수량 <span class="text-gray-400 text-xs">(선택사항, 분수 입력 가능: 1/2, 3/4 등)</span></label>
                        <InputText
                            :model-value="item.quantity"
                            placeholder="수량을 입력하세요 (예: 1, 1.5, 1/2)"
                            class="w-full"
                            @update:model-value="(v) => updateItemField(groupIndex, itemIndex, 'quantity', v)"
                        />
                    </div>
                    <div>
                        <label class="block mb-2 text-sm">단위 <span class="text-gray-400 text-xs">(선택사항)</span></label>
                        <Message v-if="unitsError" severity="error" :closable="false" class="mb-2 text-xs">
                            {{ unitsError }}
                        </Message>
                        <div v-else-if="unitsLoading" class="p-2 text-xs text-gray-500">
                            로딩 중...
                        </div>
                        <div v-else class="flex flex-col gap-2">
                            <Select
                                :model-value="item.unit"
                                :options="unitSelectOptions"
                                option-label="label"
                                option-value="value"
                                placeholder="단위를 선택하세요 (선택사항)"
                                class="w-full"
                                :class="{ 'border-red-500': validationErrors[`item-unit-${item.id}`] }"
                                filter
                                show-clear
                                @update:model-value="(v) => updateItemField(groupIndex, itemIndex, 'unit', v)"
                                @change="$emit('clear-validation', `item-unit-${item.id}`)"
                            />
                            <InputText
                                v-if="item.unit === 'CUSTOM'"
                                :model-value="item.customUnitName"
                                placeholder="단위를 직접 입력하세요"
                                class="w-full"
                                @update:model-value="(v) => updateItemField(groupIndex, itemIndex, 'customUnitName', v)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import Popover from 'primevue/popover';
import Select from 'primevue/select';
import { computed, ref } from 'vue';
import type { CommonCodeOption, IngredientGroupDraft, IngredientItemDraft } from '@/types/recipeForm';
import { generateRecipeFormId } from '@/types/recipeForm';

const props = withDefaults(
    defineProps<{
        modelValue: IngredientGroupDraft[];
        ingredientTypeOptions: CommonCodeOption[];
        unitOptions: CommonCodeOption[];
        ingredientTypesLoading?: boolean;
        ingredientTypesError?: string | null;
        unitsLoading?: boolean;
        unitsError?: string | null;
        validationErrors?: Record<string, boolean>;
        disabled?: boolean;
        guideImage?: string | null;
    }>(),
    {
        ingredientTypesLoading: false,
        ingredientTypesError: null,
        unitsLoading: false,
        unitsError: null,
        validationErrors: () => ({}),
        disabled: false,
        guideImage: null
    }
);

const emit = defineEmits<{
    'update:modelValue': [value: IngredientGroupDraft[]];
    'clear-validation': [key: string];
}>();

const guideIconRef = ref<HTMLElement | null>(null);
const guidePopoverRef = ref<{ toggle: (e: Event) => void } | null>(null);

const ingredientTypeSelectOptions = computed(() => {
    if (!props.ingredientTypeOptions?.length) {
        return [{ label: '직접 입력', value: 'CUSTOM' }];
    }
    const options = props.ingredientTypeOptions.flatMap((option) =>
        (option.details || []).map((detail) => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    );
    return [...options, { label: '직접 입력', value: 'CUSTOM' }];
});

const unitSelectOptions = computed(() => {
    if (!props.unitOptions?.length) {
        return [{ label: '직접 입력', value: 'CUSTOM' }];
    }
    const options = props.unitOptions.flatMap((option) =>
        (option.details || []).map((detail) => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    );
    return [...options, { label: '직접 입력', value: 'CUSTOM' }];
});

function swap<T>(arr: T[], i: number, j: number): void {
    if (i < 0 || j < 0 || i >= arr.length || j >= arr.length) return;
    [arr[i], arr[j]] = [arr[j], arr[i]];
}

function emitUpdate(groups: IngredientGroupDraft[]): void {
    emit('update:modelValue', groups);
}

function addGroup(): void {
    emitUpdate([
        ...props.modelValue,
        { id: generateRecipeFormId(), type: '', items: [] }
    ]);
}

function removeGroup(groupIndex: number): void {
    emitUpdate(props.modelValue.filter((_, i) => i !== groupIndex));
}

function moveGroupUp(groupIndex: number): void {
    if (groupIndex <= 0) return;
    const next = [...props.modelValue];
    swap(next, groupIndex - 1, groupIndex);
    emitUpdate(next);
}

function moveGroupDown(groupIndex: number): void {
    if (groupIndex >= props.modelValue.length - 1) return;
    const next = [...props.modelValue];
    swap(next, groupIndex, groupIndex + 1);
    emitUpdate(next);
}

function updateGroupType(groupIndex: number, type: string): void {
    const next = props.modelValue.map((g, i) =>
        i === groupIndex ? { ...g, type } : g
    );
    emitUpdate(next);
}

function updateGroupCustomTypeName(groupIndex: number, customTypeName: string): void {
    const next = props.modelValue.map((g, i) =>
        i === groupIndex ? { ...g, customTypeName } : g
    );
    emitUpdate(next);
}

function addItem(groupIndex: number): void {
    const group = props.modelValue[groupIndex];
    if (!group) return;
    const newItem: IngredientItemDraft = {
        id: generateRecipeFormId(),
        name: '',
        quantity: null,
        unit: ''
    };
    const next = props.modelValue.map((g, i) =>
        i === groupIndex ? { ...g, items: [...g.items, newItem] } : g
    );
    emitUpdate(next);
}

function removeItem(groupIndex: number, itemIndex: number): void {
    const next = props.modelValue.map((g, i) =>
        i === groupIndex
            ? { ...g, items: g.items.filter((_, j) => j !== itemIndex) }
            : g
    );
    emitUpdate(next);
}

function moveItemUp(groupIndex: number, itemIndex: number): void {
    if (itemIndex <= 0) return;
    const group = props.modelValue[groupIndex];
    if (!group) return;
    const items = [...group.items];
    swap(items, itemIndex - 1, itemIndex);
    const next = props.modelValue.map((g, i) =>
        i === groupIndex ? { ...g, items } : g
    );
    emitUpdate(next);
}

function moveItemDown(groupIndex: number, itemIndex: number): void {
    const group = props.modelValue[groupIndex];
    if (!group || itemIndex >= group.items.length - 1) return;
    const items = [...group.items];
    swap(items, itemIndex, itemIndex + 1);
    const next = props.modelValue.map((g, i) =>
        i === groupIndex ? { ...g, items } : g
    );
    emitUpdate(next);
}

function updateItemField(
    groupIndex: number,
    itemIndex: number,
    field: keyof IngredientItemDraft,
    value: string | null
): void {
    const next = props.modelValue.map((g, i) => {
        if (i !== groupIndex) return g;
        const items = g.items.map((it, j) =>
            j === itemIndex ? { ...it, [field]: value } : it
        );
        return { ...g, items };
    });
    emitUpdate(next);
}
</script>
