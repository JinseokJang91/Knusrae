<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">주간 랭킹</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshRanking" />
                <Button icon="pi pi-calendar" :label="selectedWeek" severity="secondary" @click="showWeekDialog = true" />
            </div>
        </div>

        <!-- 주간 선택 다이얼로그 -->
        <Dialog v-model:visible="showWeekDialog" header="주간 선택" :style="{ width: '300px' }">
            <div class="flex flex-column gap-3">
                <div v-for="week in weekOptions" :key="week.value" class="flex align-items-center">
                    <RadioButton v-model="selectedWeek" :inputId="week.value" :value="week.value" />
                    <label :for="week.value" class="ml-2">{{ week.label }}</label>
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showWeekDialog = false" />
                <Button label="적용" @click="applyWeekFilter" />
            </template>
        </Dialog>

        <!-- 랭킹 통계 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-4">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalViews }}</h3>
                                <p class="text-600 m-0">총 조회수</p>
                            </div>
                            <i class="pi pi-eye text-4xl text-gray-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-4">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalFavorites }}</h3>
                                <p class="text-600 m-0">총 찜 수</p>
                            </div>
                            <i class="pi pi-heart text-4xl text-red-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-4">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalRecipes }}</h3>
                                <p class="text-600 m-0">등록된 레시피</p>
                            </div>
                            <i class="pi pi-book text-4xl text-gray-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 랭킹 목록 -->
        <div class="grid">
            <div class="col-12 lg:col-8">
                <Card>
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-trophy text-yellow-500"></i>
                            <span>주간 인기 레시피 TOP 10</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="ranking-list">
                            <div v-for="(recipe, index) in rankingRecipes" :key="recipe.id" class="ranking-item">
                                <div class="flex align-items-center gap-3 p-3 border-round hover:surface-50 transition-colors transition-duration-150">
                                    <!-- 순위 -->
                                    <div class="rank-badge">
                                        <span v-if="index < 3" class="rank-number rank-top">{{ index + 1 }}</span>
                                        <span v-else class="rank-number">{{ index + 1 }}</span>
                                    </div>

                                    <!-- 레시피 이미지 -->
                                    <div class="recipe-image">
                                        <img :src="recipe.image" :alt="recipe.title" class="w-full h-full object-cover border-round" />
                                    </div>

                                    <!-- 레시피 정보 -->
                                    <div class="flex-1">
                                        <h4 class="text-lg font-semibold text-900 m-0 mb-1">{{ recipe.title }}</h4>
                                        <p class="text-600 text-sm m-0 mb-2">{{ recipe.description }}</p>
                                        <div class="flex align-items-center gap-3 text-sm text-500">
                                            <div class="flex align-items-center gap-1">
                                                <i class="pi pi-eye"></i>
                                                <span>{{ recipe.views.toLocaleString() }}</span>
                                            </div>
                                            <div class="flex align-items-center gap-1">
                                                <i class="pi pi-heart"></i>
                                                <span>{{ recipe.favorites.toLocaleString() }}</span>
                                            </div>
                                            <div class="flex align-items-center gap-1">
                                                <i class="pi pi-star-fill text-yellow-500"></i>
                                                <span>{{ recipe.rating }}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 액션 버튼 -->
                                    <div class="flex flex-column gap-2">
                                        <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="small" rounded @click="toggleFavorite(recipe.id)" />
                                        <Button icon="pi pi-eye" size="small" severity="secondary" rounded @click="viewRecipe(recipe.id)" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>

            <!-- 사이드바 - 카테고리별 랭킹 -->
            <div class="col-12 lg:col-4">
                <Card>
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-chart-bar text-gray-500"></i>
                            <span>카테고리별 인기</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="category-ranking">
                            <div v-for="category in categoryRanking" :key="category.name" class="category-item">
                                <div class="flex align-items-center justify-content-between p-2 border-round hover:surface-50 transition-colors transition-duration-150">
                                    <div class="flex align-items-center gap-2">
                                        <Tag :value="category.name" :severity="category.severity" />
                                        <span class="text-sm text-600">{{ category.count }}개</span>
                                    </div>
                                    <i class="pi pi-chevron-right text-500"></i>
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- 트렌드 키워드 -->
                <Card class="mt-3">
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-tag text-gray-500"></i>
                            <span>인기 키워드</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="trend-keywords">
                            <div v-for="keyword in trendKeywords" :key="keyword.text" class="keyword-item">
                                <div class="flex align-items-center justify-content-between p-2 border-round hover:surface-50 transition-colors transition-duration-150">
                                    <span class="text-sm">{{ keyword.text }}</span>
                                    <Badge :value="keyword.count" severity="info" />
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>
    </div>
</template>

<script setup>
import Badge from 'primevue/badge';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import RadioButton from 'primevue/radiobutton';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// 반응형 데이터
const rankingRecipes = ref([]);
const showWeekDialog = ref(false);
const selectedWeek = ref('이번 주');

const weekOptions = ref([
    { label: '이번 주', value: '이번 주' },
    { label: '지난 주', value: '지난 주' },
    { label: '2주 전', value: '2주 전' },
    { label: '3주 전', value: '3주 전' }
]);

const categoryRanking = ref([
    { name: '한식', count: 45, severity: 'info' },
    { name: '양식', count: 32, severity: 'success' },
    { name: '일식', count: 28, severity: 'warning' },
    { name: '중식', count: 25, severity: 'danger' },
    { name: '디저트', count: 18, severity: 'secondary' }
]);

const trendKeywords = ref([
    { text: '간단한', count: 156 },
    { text: '건강한', count: 134 },
    { text: '달콤한', count: 98 },
    { text: '매운', count: 87 },
    { text: '아이들', count: 76 }
]);

// 계산된 속성
const totalViews = computed(() => {
    return rankingRecipes.value.reduce((sum, recipe) => sum + recipe.views, 0).toLocaleString();
});

const totalFavorites = computed(() => {
    return rankingRecipes.value.reduce((sum, recipe) => sum + recipe.favorites, 0).toLocaleString();
});

const totalRecipes = computed(() => {
    return rankingRecipes.value.length;
});

// 메서드
const loadRankingData = () => {
    // 실제로는 API 호출
    rankingRecipes.value = [
        {
            id: 1,
            title: '김치찌개',
            description: '매콤하고 시원한 김치찌개',
            image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=100',
            views: 15420,
            favorites: 3240,
            rating: 4.5,
            isFavorite: false
        },
        {
            id: 2,
            title: '파스타',
            description: '크림소스가 일품인 파스타',
            image: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=100',
            views: 12850,
            favorites: 2890,
            rating: 4.2,
            isFavorite: true
        },
        {
            id: 3,
            title: '초밥',
            description: '신선한 생선으로 만든 초밥',
            image: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=100',
            views: 11200,
            favorites: 2560,
            rating: 4.8,
            isFavorite: false
        },
        {
            id: 4,
            title: '짜장면',
            description: '진한 춘장소스의 짜장면',
            image: 'https://images.unsplash.com/photo-1563379091339-03246963d4d8?w=100',
            views: 9850,
            favorites: 2100,
            rating: 4.3,
            isFavorite: false
        },
        {
            id: 5,
            title: '치즈케이크',
            description: '부드럽고 달콤한 치즈케이크',
            image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=100',
            views: 9200,
            favorites: 1980,
            rating: 4.7,
            isFavorite: true
        },
        {
            id: 6,
            title: '불고기',
            description: '달콤한 양념의 불고기',
            image: 'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=100',
            views: 8750,
            favorites: 1850,
            rating: 4.4,
            isFavorite: false
        },
        {
            id: 7,
            title: '라면',
            description: '간편하고 맛있는 라면',
            image: 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=100',
            views: 8200,
            favorites: 1720,
            rating: 4.1,
            isFavorite: false
        },
        {
            id: 8,
            title: '피자',
            description: '치즈가 가득한 피자',
            image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=100',
            views: 7800,
            favorites: 1650,
            rating: 4.6,
            isFavorite: false
        },
        {
            id: 9,
            title: '샐러드',
            description: '신선한 야채의 샐러드',
            image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=100',
            views: 7200,
            favorites: 1480,
            rating: 4.0,
            isFavorite: false
        },
        {
            id: 10,
            title: '스테이크',
            description: '부드러운 스테이크',
            image: 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=100',
            views: 6800,
            favorites: 1420,
            rating: 4.9,
            isFavorite: false
        }
    ];
};

const refreshRanking = () => {
    loadRankingData();
    toast.add({ severity: 'success', summary: '새로고침', detail: '랭킹 데이터가 업데이트되었습니다.', life: 3000 });
};

const applyWeekFilter = () => {
    showWeekDialog.value = false;
    loadRankingData();
    toast.add({ severity: 'info', summary: '주간 변경', detail: `${selectedWeek.value} 데이터를 불러왔습니다.`, life: 3000 });
};

const toggleFavorite = (recipeId) => {
    const recipe = rankingRecipes.value.find((r) => r.id === recipeId);
    if (recipe) {
        recipe.isFavorite = !recipe.isFavorite;
        recipe.favorites += recipe.isFavorite ? 1 : -1;
        toast.add({
            severity: 'success',
            summary: recipe.isFavorite ? '찜 추가' : '찜 해제',
            detail: recipe.isFavorite ? '찜 목록에 추가되었습니다.' : '찜 목록에서 제거되었습니다.',
            life: 3000
        });
    }
};

const viewRecipe = (recipeId) => {
    router.push(`/recipe/detail/${recipeId}`);
};

// 생명주기
onMounted(() => {
    loadRankingData();
});
</script>

<style scoped>
.stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.stat-card .text-900 {
    color: white !important;
}

.stat-card .text-600 {
    color: rgba(255, 255, 255, 0.8) !important;
}

.ranking-item {
    border-bottom: 1px solid var(--surface-border);
}

.ranking-item:last-child {
    border-bottom: none;
}

.rank-badge {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.rank-number {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 14px;
    background-color: var(--surface-200);
    color: var(--text-color);
}

.rank-top {
    background: linear-gradient(135deg, #ffd700, #ffed4e);
    color: #333;
}

.recipe-image {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    overflow: hidden;
}

.category-item,
.keyword-item {
    border-bottom: 1px solid var(--surface-border);
}

.category-item:last-child,
.keyword-item:last-child {
    border-bottom: none;
}
</style>
