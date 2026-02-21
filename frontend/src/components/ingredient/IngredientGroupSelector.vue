<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import Button from 'primevue/button';
import type { IngredientGroup } from '@/types/ingredient';

const props = defineProps<{
    groups: IngredientGroup[];
    selectedGroupId: number | null;
}>();

const emit = defineEmits<{
    (e: 'select', groupId: number | null): void;
}>();

const scrollRef = ref<HTMLElement | null>(null);
const scrollState = ref({ scrollLeft: 0, scrollWidth: 0, clientWidth: 0 });

const scrollStep = 220; // 한 번에 스크롤할 너비 (그룹 아이템 + gap 정도)
const fadeThreshold = 60; // 이만큼 스크롤되면 버튼이 완전히 보이도록

const leftButtonOpacity = computed(() => {
    const { scrollLeft } = scrollState.value;
    if (scrollLeft <= 0) return 0;
    return Math.min(1, scrollLeft / fadeThreshold);
});

const rightButtonOpacity = computed(() => {
    const { scrollLeft, scrollWidth, clientWidth } = scrollState.value;
    const maxScroll = scrollWidth - clientWidth;
    if (maxScroll <= 0 || scrollLeft >= maxScroll - 1) return 0;
    return Math.min(1, (maxScroll - scrollLeft) / fadeThreshold);
});

function updateScrollState() {
    const el = scrollRef.value;
    if (!el) return;
    scrollState.value = {
        scrollLeft: el.scrollLeft,
        scrollWidth: el.scrollWidth,
        clientWidth: el.clientWidth
    };
}

function scroll(direction: -1 | 1) {
    const el = scrollRef.value;
    if (!el) return;
    el.scrollBy({ left: direction * scrollStep, behavior: 'smooth' });
}

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
    updateScrollState();
    nextTick(updateScrollState);
    resizeObserver = new ResizeObserver(() => updateScrollState());
    if (scrollRef.value) resizeObserver.observe(scrollRef.value);
});

onUnmounted(() => {
    resizeObserver?.disconnect();
});

watch(
    () => props.groups.length,
    () => {
        nextTick(updateScrollState);
    }
);

const handleSelect = (groupId: number | null) => {
    emit('select', groupId);
};
</script>

<template>
    <div class="ingredient-group-selector">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">재료 그룹 선택</h3>
        <div class="selector-row">
            <Button
                :style="{ opacity: leftButtonOpacity }"
                :class="['scroll-btn', 'scroll-btn-left', { 'scroll-btn-hidden': leftButtonOpacity === 0 }]"
                icon="pi pi-chevron-left"
                text
                rounded
                severity="secondary"
                aria-label="이전 그룹 보기"
                @click="scroll(-1)"
            />
            <div ref="scrollRef" class="groups-container" @scroll="updateScrollState">
                <button :class="['group-item', selectedGroupId === null ? 'selected' : '']" @click="handleSelect(null)">
                    <div class="group-icon">
                        <i class="pi pi-th-large text-2xl"></i>
                    </div>
                    <span class="group-label">전체</span>
                </button>
                <button v-for="group in groups" :key="group.id" :class="['group-item', selectedGroupId === group.id ? 'selected' : '']" @click="handleSelect(group.id)">
                    <div class="group-icon">
                        <img v-if="group.imageUrl" :src="group.imageUrl" :alt="group.name" class="group-image" />
                        <i v-else class="pi pi-box text-2xl"></i>
                    </div>
                    <span class="group-label">{{ group.name }}</span>
                </button>
            </div>
            <Button
                :style="{ opacity: rightButtonOpacity }"
                :class="['scroll-btn', 'scroll-btn-right', { 'scroll-btn-hidden': rightButtonOpacity === 0 }]"
                icon="pi pi-chevron-right"
                text
                rounded
                severity="secondary"
                aria-label="다음 그룹 보기"
                @click="scroll(1)"
            />
        </div>
    </div>
</template>

<style scoped>
.ingredient-group-selector {
    margin-bottom: 24px;
}

.selector-row {
    display: flex;
    align-items: center;
    gap: 8px;
}

.scroll-btn {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    transition: opacity 0.25s ease;
}

.scroll-btn-hidden {
    pointer-events: none;
}

.scroll-btn-left {
    margin-right: 4px;
}

.scroll-btn-right {
    margin-left: 4px;
}

.groups-container {
    display: flex;
    flex-wrap: nowrap;
    gap: 16px;
    overflow-x: auto;
    overflow-y: hidden;
    scroll-behavior: smooth;
    scrollbar-width: thin;
    min-width: 0;
    flex: 1;
    padding: 4px 0;
}

/* 스크롤바 숨기거나 얇게 (선택 사항) */
.groups-container::-webkit-scrollbar {
    height: 6px;
}

.groups-container::-webkit-scrollbar-thumb {
    background: var(--surface-border);
    border-radius: 3px;
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
    flex-shrink: 0;
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

    .scroll-btn {
        width: 36px;
        height: 36px;
    }
}
</style>
