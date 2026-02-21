<script setup lang="ts">
import BookPage from './BookPage.vue';
</script>

<template>
    <div class="book-frame">
        <BookPage side="left" class="book-frame__left">
            <slot name="left" />
        </BookPage>
        <div class="book-frame__spine" aria-hidden="true"></div>
        <BookPage side="right" class="book-frame__right">
            <slot name="right" />
        </BookPage>
    </div>
</template>

<style scoped lang="scss">
.book-frame {
    display: flex;
    width: 100%;
    min-height: 320px;
    background: linear-gradient(to right, var(--surface-100) 0%, var(--surface-50) 48%, var(--surface-100) 50%, var(--surface-50) 52%, var(--surface-100) 100%);
    border-radius: 12px;
    box-shadow:
        0 4px 20px rgba(0, 0, 0, 0.08),
        inset 0 0 0 1px var(--surface-border);
    overflow: hidden;
    gap: 0;
}

.book-frame__left,
.book-frame__right {
    flex: 1;
    min-width: 0;
}

.book-frame__spine {
    width: 4px;
    flex-shrink: 0;
    background: linear-gradient(180deg, var(--surface-300) 0%, var(--surface-200) 20%, var(--surface-400) 50%, var(--surface-200) 80%, var(--surface-300) 100%);
    box-shadow: 0 0 4px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
    .book-frame {
        flex-direction: column;
        min-height: auto;
    }

    .book-frame__spine {
        width: 100%;
        height: 4px;
        background: linear-gradient(90deg, var(--surface-300) 0%, var(--surface-200) 20%, var(--surface-400) 50%, var(--surface-200) 80%, var(--surface-300) 100%);
    }

    :deep(.book-page--left) {
        border-radius: 8px 8px 0 0;
        border-right: 1px solid var(--surface-border);
        border-bottom: none;
    }

    :deep(.book-page--right) {
        border-radius: 0;
        border-left: 1px solid var(--surface-border);
        border-top: none;
    }
}
</style>
