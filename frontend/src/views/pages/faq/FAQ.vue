<template>
    <div class="faq-page">
        <div class="card">
            <div class="flex justify-between items-center mb-4">
                <h1 class="text-3xl font-bold text-gray-900">FAQ</h1>
            </div>
        </div>
        <div class="faq-content">
            <QnA />
        </div>
    </div>
</template>

<script setup>
import QnA from '@/views/pages/community/QnA.vue';
</script>

<style scoped>
.faq-content :deep(.card > div:first-child) {
    display: none;
}
</style>

