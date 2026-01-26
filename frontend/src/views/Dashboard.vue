<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import PopularRecipes from '@/components/dashboard/PopularRecipes.vue';
import CategorySections from '@/components/dashboard/CategorySections.vue';
import RecentViews from '@/components/dashboard/RecentViews.vue';
import TodayRecommendations from '@/components/dashboard/TodayRecommendations.vue';

const authStore = useAuthStore();

const isLoggedIn = computed(() => authStore.isLoggedIn);
const nickname = computed(() => authStore.memberName);

// 앱 초기화 중에는 비로그인 문구를 보여주되, 인증이 끝나면 실제 로그인 상태로 전환
const showLoggedInGreeting = computed(() => authStore.isInitialized && isLoggedIn.value);
</script>

<template>
    <div class="grid grid-cols-12 gap-8">
        <!-- 1. 환영 문구 -->
        <div class="col-span-12">
            <div class="card">
                <h4 class="text-2xl font-bold mb-2">
                    <template v-if="showLoggedInGreeting">
                        {{ nickname }} 요리사님, 환영해요 😄
                    </template>
                    <template v-else>
                        환영해요 😄
                    </template>
                </h4>
                <p class="text-gray-600">
                    <template v-if="showLoggedInGreeting">
                        오늘은 어떤 레시피가 당신에게 스며들까요?
                    </template>
                    <template v-else>
                        오늘 여러분이 선택한 레시피는 어떤 걸까요?
                    </template>
                </p>
            </div>
        </div>
        
        <!-- 2. 오늘의 레시피 추천 -->
        <div class="col-span-12">
            <TodayRecommendations />
        </div>
        
        <!-- 3. 인기 레시피 TOP -->
        <div class="col-span-12">
            <PopularRecipes />
        </div>
        
        <!-- 4. 카테고리 섹션 -->
        <div class="col-span-12">
            <CategorySections />
        </div>
        
        <!-- 5. 최근 본 레시피 (로그인 시만) -->
        <div class="col-span-12">
            <RecentViews />
        </div>
    </div>
</template>
