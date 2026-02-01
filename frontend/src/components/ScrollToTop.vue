<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const isVisible = ref(false);

const handleScroll = () => {
    // 300px 이상 스크롤되면 버튼 표시
    isVisible.value = window.scrollY > 300;
};

const scrollToTop = () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
};

onMounted(() => {
    window.addEventListener('scroll', handleScroll);
    // 초기 상태 확인
    handleScroll();
});

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
</script>

<template>
    <Transition name="fade">
        <button
            v-if="isVisible"
            class="scroll-to-top-btn"
            @click="scrollToTop"
            type="button"
            aria-label="맨 위로 이동"
        >
            <i class="pi pi-arrow-up"></i>
        </button>
    </Transition>
</template>

<style lang="scss" scoped>
.scroll-to-top-btn {
    position: fixed;
    bottom: 2rem;
    right: 2rem;
    width: 3rem;
    height: 3rem;
    border-radius: 50%;
    background: var(--primary-color, #f97316); /* 앱 버튼과 동일한 오렌지톤 (orange-500) */
    color: white;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.35);
    z-index: 1000;
    transition: all 0.3s ease;

    &:hover {
        background: #ea580c; /* hover 시 진한 오렌지 (orange-600) */
    }

    i {
        font-size: 1.25rem;
    }

    &:active {
        transform: translateY(0);
    }
}

// 페이드 인/아웃 애니메이션
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: scale(0.8);
}

// 반응형 디자인
@media (max-width: 768px) {
    .scroll-to-top-btn {
        bottom: 1.5rem;
        right: 1.5rem;
        width: 2.5rem;
        height: 2.5rem;
        
        i {
            font-size: 1rem;
        }
    }
}
</style>

