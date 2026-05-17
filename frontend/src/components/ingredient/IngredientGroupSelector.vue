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
        <h3 class="group-heading">재료 그룹</h3>
        <div class="selector-row">
            <Button
                :style="{ opacity: leftButtonOpacity }"
                :class="['scroll-btn', 'scroll-btn-left', { 'scroll-btn-hidden': leftButtonOpacity === 0 }]"
                icon="pi pi-chevron-left"
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

.group-heading {
    margin: 0 0 1rem;
    font-size: 1.125rem;
    font-weight: 600;
    color: #374151;
}

@media (max-width: 768px) {
    .group-heading {
        font-size: 1rem;
        margin-bottom: 0.75rem;
    }
}

.selector-row {
    position: relative;
    width: 100%;
}

.scroll-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    z-index: 1;
    width: 40px;
    height: 40px;
    transition: opacity 0.25s ease;
}

.scroll-btn-hidden {
    pointer-events: none;
}

.scroll-btn-left {
    left: 0;
}

.scroll-btn-right {
    right: 0;
}

.groups-container {
    display: flex;
    flex-wrap: nowrap;
    gap: 16px;
    overflow-x: auto;
    overflow-y: hidden;
    scroll-behavior: smooth;
    scrollbar-width: thin;
    width: 100%;
    padding: 4px 0;
    -webkit-overflow-scrolling: touch;
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
    border: 1px solid rgba(234, 88, 12, 0.12);
    border-radius: 12px;
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
    cursor: pointer;
    transition: all 0.2s;
    min-width: 100px;
    flex-shrink: 0;
}

@media (hover: hover) and (pointer: fine) {
    .group-item:hover {
        border-color: rgba(234, 88, 12, 0.28);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
    }
}

.group-item.selected {
    border-color: rgba(234, 88, 12, 0.5);
    background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
    color: white;
}

.group-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 237, 213, 0.8);
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
    .ingredient-group-selector {
        margin-bottom: 1rem;
    }

    /* 터치 스크롤 위주: 좌우 화살표 숨김 */
    .scroll-btn {
        display: none;
    }

    .groups-container {
        gap: 10px;
        scroll-snap-type: x proximity;
        scroll-padding-inline: 2px;
        padding-bottom: 6px;
    }

    .group-item {
        min-width: 76px;
        padding: 10px 12px;
        scroll-snap-align: start;
    }

    .group-icon {
        width: 44px;
        height: 44px;
    }

    .group-label {
        font-size: 12px;
        max-width: 72px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}
</style>
