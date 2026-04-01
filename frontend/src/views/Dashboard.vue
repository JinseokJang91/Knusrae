<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import PopularRecipes from '@/components/dashboard/PopularRecipes.vue';
import ThemeCollections from '@/components/dashboard/ThemeCollections.vue';
import CategorySections from '@/components/dashboard/CategorySections.vue';
import RecentViews from '@/components/dashboard/RecentViews.vue';
import TodayRecommendations from '@/components/dashboard/TodayRecommendations.vue';
import RecommendedCreators from '@/components/dashboard/RecommendedCreators.vue';

const authStore = useAuthStore();

const isLoggedIn = computed(() => authStore.isLoggedIn);
const nickname = computed(() => authStore.memberName);

// 앱 초기화 중에는 비로그인 문구를 보여주되, 인증이 끝나면 실제 로그인 상태로 전환
const showLoggedInGreeting = computed(() => authStore.isInitialized && isLoggedIn.value);

// 시간대별 환영 메시지 + 테마(1번 컬러) + 아이콘(3번 이모지)
const timeBasedGreeting = computed(() => {
    const hour = new Date().getHours();

    if (hour >= 5 && hour < 11) {
        return {
            theme: 'morning',
            icon: '🌅',
            message: '좋은 아침이에요!',
            subtitle: '오늘의 추천 레시피를 확인해보세요'
        };
    } else if (hour >= 11 && hour < 17) {
        return {
            theme: 'lunch',
            icon: '🍽️',
            message: '점심 메뉴 고민되시나요?',
            subtitle: '인기 레시피를 둘러보세요'
        };
    } else {
        return {
            theme: 'evening',
            icon: '🌙',
            message: '오늘 하루 수고하셨어요.',
            subtitle: '새로운 레시피를 발견해보세요'
        };
    }
});

// 로그인 상태에 따른 환영 문구
const greetingTitle = computed(() => {
    if (showLoggedInGreeting.value) {
        return `${nickname.value}님, ${timeBasedGreeting.value.message}`;
    }
    return timeBasedGreeting.value.message;
});
</script>

<template>
    <div class="page-container">
        <div class="dashboard-container">
            <!-- 1. 환영 문구 (1번 시간대별 컬러 + 3번 이모지) -->
            <section class="dashboard-section hero-section">
                <div class="hero-content" :class="`hero-theme-${timeBasedGreeting.theme}`">
                    <span class="hero-icon" aria-hidden="true">{{ timeBasedGreeting.icon }}</span>
                    <h4 class="hero-title">{{ greetingTitle }}</h4>
                    <p class="hero-subtitle">{{ timeBasedGreeting.subtitle }}</p>
                </div>
            </section>

            <!-- 2. 오늘의 레시피 추천 (주요 섹션) -->
            <section class="dashboard-section main-section">
                <div class="section-wrapper">
                    <TodayRecommendations />
                </div>
            </section>

            <!-- 3. 인기 레시피 TOP (주요 섹션) -->
            <section class="dashboard-section main-section">
                <div class="section-wrapper">
                    <PopularRecipes />
                </div>
            </section>

            <!-- 4. 테마 컬렉션 (큐레이션 섹션) -->
            <section class="dashboard-section theme-section">
                <div class="section-wrapper">
                    <ThemeCollections />
                </div>
            </section>

            <!-- 5. 카테고리 섹션 (보조 섹션) -->
            <section class="dashboard-section secondary-section">
                <div class="section-wrapper">
                    <CategorySections />
                </div>
            </section>

            <!-- 6. 최근 본 레시피 (개인화 섹션, 로그인 시만) -->
            <section v-if="isLoggedIn" class="dashboard-section personal-section">
                <div class="section-wrapper">
                    <RecentViews />
                </div>
            </section>

            <!-- 7. 추천 크리에이터 -->
            <section class="dashboard-section creator-section">
                <div class="section-wrapper">
                    <RecommendedCreators />
                </div>
            </section>
        </div>
    </div>
</template>

<style scoped lang="scss">
.dashboard-container {
    display: flex;
    flex-direction: column;
    gap: 32px;
}

.dashboard-section {
    width: 100%;

    // 공통 패딩
    &.hero-section {
        padding: 0;
    }

    &.main-section,
    &.secondary-section,
    &.personal-section {
        padding: 0;
    }
}

// 6번: 미니 애니메이션 (페이드인 + 살짝 올라옴)
@keyframes hero-fade-up {
    from {
        opacity: 0;
        transform: translateY(12px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes hero-fade-in {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

// 히어로 섹션 — 1번 + 3번 + 6번(미니 애니메이션)
.hero-section {
    .hero-content {
        border-radius: 20px;
        padding: 32px 40px;
        text-align: center;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        transition:
            background 0.4s ease,
            border-color 0.4s ease;

        .hero-icon {
            font-size: 2.5rem;
            line-height: 1;
            margin-bottom: 4px;
            animation: hero-fade-up 0.4s ease-out both;
        }

        .hero-title {
            font-size: 2rem;
            font-weight: 700;
            color: var(--text-color);
            margin: 0 0 4px;
            animation: hero-fade-up 0.4s ease-out 0.08s both;
        }

        .hero-subtitle {
            font-size: 1.1rem;
            color: var(--text-color-secondary);
            margin: 0;
            animation: hero-fade-in 0.4s ease-out 0.18s both;
        }

        &.hero-theme-morning {
            background: linear-gradient(135deg, rgba(251, 191, 36, 0.15) 0%, rgba(245, 158, 11, 0.06) 100%);
            border: 1px solid rgba(251, 191, 36, 0.35);
        }

        &.hero-theme-lunch {
            background: linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 159, 64, 0.06) 100%);
            border: 1px solid rgba(255, 107, 53, 0.25);
        }

        &.hero-theme-evening {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.06) 100%);
            border: 1px solid rgba(99, 102, 241, 0.3);
        }
    }
}

// 섹션 래퍼 (주요/보조/개인화 섹션 공통)
.section-wrapper {
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    transition: box-shadow 0.3s ease;

    &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }
}

// 주요 섹션 (오늘의 추천, 인기 레시피)
.main-section {
    .section-wrapper {
        background: linear-gradient(to bottom, var(--surface-card) 0%, rgba(255, 107, 53, 0.02) 100%);
        border-color: rgba(255, 107, 53, 0.15);
    }
}

// 테마 컬렉션 섹션
.theme-section {
    .section-wrapper {
        background: linear-gradient(to bottom, rgba(139, 92, 246, 0.03) 0%, var(--surface-card) 100%);
        border-color: rgba(139, 92, 246, 0.15);
    }
}

// 보조 섹션 (카테고리)
.secondary-section {
    .section-wrapper {
        background: var(--surface-card);
        border-color: var(--surface-border);
    }
}

// 개인화 섹션 (최근 본 레시피)
.personal-section {
    .section-wrapper {
        background: linear-gradient(to bottom, rgba(59, 130, 246, 0.03) 0%, var(--surface-card) 100%);
        border-color: rgba(59, 130, 246, 0.15);
    }
}

// 크리에이터 섹션
.creator-section {
    .section-wrapper {
        background: linear-gradient(to bottom, rgba(16, 185, 129, 0.03) 0%, var(--surface-card) 100%);
        border-color: rgba(16, 185, 129, 0.15);
    }
}

// 반응형 디자인
@media (max-width: 1024px) {
    .dashboard-container {
        gap: 24px;
    }

    .hero-section .hero-content {
        padding: 24px 32px;

        .hero-icon {
            font-size: 2.25rem;
        }

        .hero-title {
            font-size: 1.75rem;
        }

        .hero-subtitle {
            font-size: 1rem;
        }
    }

    .section-wrapper {
        padding: 24px;
    }
}

@media (max-width: 768px) {
    .dashboard-container {
        gap: 20px;
    }

    .hero-section .hero-content {
        padding: 20px 24px;
        border-radius: 16px;

        .hero-icon {
            font-size: 2rem;
        }

        .hero-title {
            font-size: 1.5rem;
        }

        .hero-subtitle {
            font-size: 0.95rem;
        }
    }

    .section-wrapper {
        padding: 20px;
        border-radius: 12px;
    }
}

@media (max-width: 480px) {
    .dashboard-container {
        gap: 16px;
    }

    .hero-section .hero-content {
        padding: 16px 20px;

        .hero-icon {
            font-size: 1.75rem;
        }

        .hero-title {
            font-size: 1.25rem;
        }

        .hero-subtitle {
            font-size: 0.9rem;
        }
    }

    .section-wrapper {
        padding: 16px;
    }
}
</style>
