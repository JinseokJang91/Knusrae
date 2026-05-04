<script setup lang="ts">
import { onMounted } from 'vue';
import ConfirmDialog from 'primevue/confirmdialog';
import AppToast from '@/components/common/AppToast.vue';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();

onMounted(() => {
    // 앱 초기화 시 Token Refresh를 통해 로그인 상태 확인
    // isInitialized가 이미 true이면 router guard 또는 login()에서 이미 처리된 것이므로 재호출 불필요
    const initializeAuth = async () => {
        if (!authStore.isInitialized) {
            await authStore.checkAuth();
        }
    };
    initializeAuth();
});
</script>

<template>
    <ConfirmDialog />
    <AppToast />
    <router-view />
</template>

<!-- ConfirmDialog는 appendTo body로 뜨므로 scoped가 아닌 전역 규칙으로 \n 줄바꿈 반영 -->
<style>
.p-confirmdialog-message {
    white-space: pre-line;
}
</style>
