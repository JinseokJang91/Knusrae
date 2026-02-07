<template>
    <div class="comments-content">
        <PageStateBlock
            state="empty"
            empty-icon="pi pi-comments"
            empty-title="준비 중입니다"
            empty-message="댓글 관리 기능은 곧 만나보실 수 있어요."
        />
    </div>
</template>

<script setup lang="ts">
import PageStateBlock from '@/components/common/PageStateBlock.vue';
</script>

<style scoped>
.comments-content {
    min-height: 240px;
}
</style>
