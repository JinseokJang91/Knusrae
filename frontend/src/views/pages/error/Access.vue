<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

function goHome() {
    router.push('/');
}
</script>

<template>
    <div class="error-page">
        <div class="error-content">
            <i class="pi pi-lock error-icon" />
            <h1 class="error-title">접근 권한이 없습니다</h1>
            <p class="error-message">이 페이지는 관리자만 이용할 수 있습니다.</p>
            <Button label="홈으로" icon="pi pi-home" @click="goHome" />
        </div>
    </div>
</template>

<style scoped>
.error-page {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1rem;
}
.error-content {
    text-align: center;
    max-width: 24rem;
}
.error-icon {
    font-size: 4rem;
    color: var(--p-red-500);
    margin-bottom: 1rem;
}
.error-title {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}
.error-message {
    color: var(--p-text-muted-color);
    margin-bottom: 1.5rem;
}
</style>
