<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { getPopularRecipes } from '@/api/recipeApi';
import RecipeListItem from '@/components/recipe/RecipeListItem.vue';
import type { PopularRecipeItem } from '@/types/recipe';
import { useAppToast } from '@/utils/toast';

const { showError } = useAppToast();

// 상태
const popularRecipes = ref<PopularRecipeItem[]>([]);
const loading = ref(false);
const selectedPeriod = ref<'24h' | '7d' | '30d'>('24h');

// 기간 옵션
const periodOptions = [
    { label: '오늘', value: '24h' },
    { label: '이번 주', value: '7d' },
    { label: '이번 달', value: '30d' }
];

// 인기 레시피 로드
const loadPopularRecipes = async () => {
    loading.value = true;
    try {
        const recipes = await getPopularRecipes(10, selectedPeriod.value);
        popularRecipes.value = recipes;
    } catch (error) {
        console.error('Failed to load popular recipes:', error);
        showError('인기 레시피를 불러오는데 실패했습니다.');
    } finally {
        loading.value = false;
    }
};

// 기간 변경 핸들러
const changePeriod = (period: '24h' | '7d' | '30d') => {
    selectedPeriod.value = period;
    loadPopularRecipes();
};

// TOP 3 레시피 (순위 표시용)
const topThreeRecipes = computed(() => {
    return popularRecipes.value.slice(0, 3);
});

// 나머지 레시피
const remainingRecipes = computed(() => {
    return popularRecipes.value.slice(3);
});

// 트렌드 아이콘 가져오기
const getTrendIcon = (status: string) => {
    switch (status) {
        case 'UP':
            return 'pi-arrow-up text-green-500';
        case 'DOWN':
            return 'pi-arrow-down text-red-500';
        case 'NEW':
            return 'pi-star text-yellow-500';
        default:
            return 'pi-minus text-gray-400';
    }
};

// 마운트 시 로드
onMounted(() => {
    loadPopularRecipes();
});
</script>

<template>
    <div class="popular-recipes-section">
        <!-- 헤더 -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-3xl font-bold mb-2">지금 인기 레시피 TOP 🔥</h2>
                <p class="text-gray-600">지금 가장 많은 사람들이 관심을 갖는 레시피를 확인해보세요</p>
            </div>
            
            <!-- 기간 선택 탭 -->
            <div class="flex gap-2">
                <button
                    v-for="option in periodOptions"
                    :key="option.value"
                    :class="[
                        'px-4 py-2 rounded-lg font-medium transition-colors',
                        selectedPeriod === option.value
                            ? 'bg-primary-500 text-white'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    ]"
                    @click="changePeriod(option.value as '24h' | '7d' | '30d')"
                >
                    {{ option.label }}
                </button>
            </div>
        </div>
        
        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-20">
            <i class="pi pi-spinner pi-spin text-4xl text-primary-500"></i>
        </div>
        
        <!-- TOP 3 레시피 (큰 카드) -->
        <div v-else-if="topThreeRecipes.length > 0">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div
                    v-for="item in topThreeRecipes"
                    :key="item.recipe.id"
                    class="relative"
                >
                    <!-- 순위 배지 -->
                    <div 
                        :class="[
                            'absolute top-4 left-4 z-10 w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-xl shadow-lg',
                            item.rank === 1 ? 'bg-yellow-500' : item.rank === 2 ? 'bg-gray-400' : 'bg-orange-600'
                        ]"
                    >
                        {{ item.rank }}
                    </div>
                    
                    <!-- 트렌드 배지 -->
                    <div 
                        v-if="item.trendStatus !== 'SAME'"
                        class="absolute top-4 right-4 z-10 bg-white rounded-full p-2 shadow-md"
                    >
                        <i :class="['pi', getTrendIcon(item.trendStatus)]"></i>
                    </div>
                    
                    <!-- 레시피 카드 -->
                    <RecipeListItem 
                        :recipe="item.recipe" 
                        :show-stats="true"
                        :show-author="true"
                    />
                </div>
            </div>
            
            <!-- 나머지 레시피 (작은 리스트) -->
            <div v-if="remainingRecipes.length > 0" class="mt-8">
                <h3 class="text-xl font-bold mb-4">다른 인기 레시피</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <div
                        v-for="item in remainingRecipes"
                        :key="item.recipe.id"
                        class="relative"
                    >
                        <!-- 순위 표시 -->
                        <div class="absolute top-2 left-2 z-10 bg-gray-800 bg-opacity-75 text-white rounded-full w-8 h-8 flex items-center justify-center text-sm font-bold">
                            {{ item.rank }}
                        </div>
                        
                        <RecipeListItem 
                            :recipe="item.recipe" 
                            :show-stats="true"
                            :show-author="false"
                        />
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 데이터 없음 -->
        <div v-else class="text-center py-20">
            <i class="pi pi-inbox text-6xl text-gray-300 mb-4"></i>
            <p class="text-gray-500 text-lg">아직 인기 레시피가 없습니다.</p>
        </div>
    </div>
</template>

<style scoped>
.popular-recipes-section {
    padding: 0;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .popular-recipes-section {
        padding: 1rem 0;
    }
    
    .flex.items-center.justify-between {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }
}
</style>

