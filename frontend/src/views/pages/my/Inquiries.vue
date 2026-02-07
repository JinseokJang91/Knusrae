<template>
    <div class="inquiries-content">
        <PageStateBlock
            state="empty"
            empty-icon="pi pi-inbox"
            empty-title="준비 중입니다"
            empty-message="1:1 문의 내역은 곧 만나보실 수 있어요."
        />
    </div>
</template>

<script setup lang="ts">
import PageStateBlock from '@/components/common/PageStateBlock.vue';
</script>

<style scoped>
.inquiries-content {
    min-height: 240px;
}
</style>
