<template>
    <div class="ingredient-group-selector">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">재료 그룹 선택</h3>
        <div class="groups-container">
            <button
                :class="['group-item', selectedGroupId === null ? 'selected' : '']"
                @click="handleSelect(null)"
            >
                <div class="group-icon">
                    <i class="pi pi-th-large text-2xl"></i>
                </div>
                <span class="group-label">전체</span>
            </button>
            
            <button
                v-for="group in groups"
                :key="group.id"
                :class="['group-item', selectedGroupId === group.id ? 'selected' : '']"
                @click="handleSelect(group.id)"
            >
                <div class="group-icon">
                    <img 
                        v-if="group.imageUrl" 
                        :src="group.imageUrl" 
                        :alt="group.name"
                        class="group-image"
                    />
                    <i v-else class="pi pi-box text-2xl"></i>
                </div>
                <span class="group-label">{{ group.name }}</span>
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { IngredientGroup } from '@/types/ingredient';

const props = defineProps<{
    groups: IngredientGroup[];
    selectedGroupId: number | null;
}>();

const emit = defineEmits<{
    (e: 'select', groupId: number | null): void;
}>();

const handleSelect = (groupId: number | null) => {
    emit('select', groupId);
};
</script>

<style scoped>
.ingredient-group-selector {
    margin-bottom: 24px;
}

.groups-container {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
}

.group-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 20px;
    border: 2px solid var(--surface-border);
    border-radius: 12px;
    background: var(--surface-card);
    cursor: pointer;
    transition: all 0.2s;
    min-width: 100px;
}

.group-item:hover {
    border-color: var(--primary-color);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.group-item.selected {
    border-color: var(--primary-color);
    background: var(--primary-color);
    color: white;
}

.group-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--surface-ground);
    overflow: hidden;
}

.group-item.selected .group-icon {
    background: rgba(255, 255, 255, 0.2);
}

.group-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.group-label {
    font-size: 14px;
    font-weight: 600;
    text-align: center;
}

@media (max-width: 768px) {
    .groups-container {
        gap: 12px;
    }
    
    .group-item {
        min-width: 80px;
        padding: 12px 16px;
    }
    
    .group-icon {
        width: 48px;
        height: 48px;
    }
}
</style>
