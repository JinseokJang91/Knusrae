<template>
    <div
        v-if="ingredientGroups && ingredientGroups.length > 0"
        class="bg-white rounded-2xl shadow-lg p-8 mb-8"
    >
        <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center">
            <i class="pi pi-shopping-cart mr-3 text-orange-600"></i>
            준비물
        </h2>

        <div class="space-y-6">
            <div
                v-for="(group, groupIndex) in ingredientGroups"
                :key="`group-${groupIndex}-${group.order}`"
                class="bg-orange-50 rounded-xl p-6"
            >
                <div class="flex items-center mb-4">
                    <div class="w-8 h-8 bg-orange-500 text-white rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 mr-3">
                        {{ groupIndex + 1 }}
                    </div>
                    <h3 class="text-xl font-semibold text-gray-800">
                        {{ group.customTypeName || group.detailName || '재료' }}
                    </h3>
                </div>

                <div v-if="group.items && group.items.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div
                        v-for="(item, itemIndex) in group.items"
                        :key="`item-${groupIndex}-${itemIndex}-${item.name}`"
                        class="flex items-center p-3 bg-white rounded-lg border border-orange-200"
                    >
                        <i class="pi pi-circle-fill text-orange-400 text-xs mr-3"></i>
                        <span class="text-gray-800 text-lg font-medium flex-1">{{ item.name }}</span>
                        <span class="text-gray-600 text-lg ml-2">
                            <template v-if="item.quantity">{{ item.quantity }}{{ item.customUnitName || item.detailName }}</template>
                            <template v-else-if="item.customUnitName || item.detailName">{{ item.customUnitName || item.detailName }}</template>
                        </span>
                    </div>
                </div>

                <div v-else class="text-orange-700/70 text-center py-4">
                    항목이 없습니다.
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { RecipeIngredientGroup } from '@/types/recipe';

defineProps<{
    ingredientGroups: RecipeIngredientGroup[];
}>();
</script>
