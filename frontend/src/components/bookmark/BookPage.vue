<template>
    <div class="book-page" :class="{ 'book-page--left': side === 'left', 'book-page--right': side === 'right' }">
        <div class="book-page__inner">
            <slot />
        </div>
    </div>
</template>

<script setup lang="ts">
withDefaults(
    defineProps<{
        /** 왼쪽/오른쪽 페이지 구분 (스타일용) */
        side?: 'left' | 'right';
    }>(),
    { side: 'left' }
);
</script>

<style scoped lang="scss">
.book-page {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    padding: 1rem;
    background-color: var(--surface-ground);
    position: relative;

    &--left {
        border-radius: 8px 0 0 8px;
        border: 1px solid var(--surface-border);
        border-right: none;
        box-shadow: inset -2px 0 8px rgba(0, 0, 0, 0.04);
    }

    &--right {
        border-radius: 0 8px 8px 0;
        border: 1px solid var(--surface-border);
        border-left: none;
        box-shadow: inset 2px 0 8px rgba(0, 0, 0, 0.04);
    }
}

.book-page__inner {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}
</style>
