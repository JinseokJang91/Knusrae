<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">월간 랭킹</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshRanking" />
                <Button icon="pi pi-calendar" :label="selectedMonth" severity="secondary" @click="showMonthDialog = true" />
            </div>
        </div>

        <!-- 월간 선택 다이얼로그 -->
        <Dialog v-model:visible="showMonthDialog" header="월간 선택" :style="{ width: '300px' }">
            <div class="flex flex-column gap-3">
                <div v-for="month in monthOptions" :key="month.value" class="flex align-items-center">
                    <RadioButton v-model="selectedMonth" :inputId="month.value" :value="month.value" />
                    <label :for="month.value" class="ml-2">{{ month.label }}</label>
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showMonthDialog = false" />
                <Button label="적용" @click="applyMonthFilter" />
            </template>
        </Dialog>

        <!-- 월간 통계 대시보드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ monthlyStats.totalViews }}</h3>
                                <p class="text-600 m-0">총 조회수</p>
                                <small class="text-green-500">+{{ monthlyStats.viewsGrowth }}%</small>
                            </div>
                            <i class="pi pi-eye text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ monthlyStats.totalFavorites }}</h3>
                                <p class="text-600 m-0">총 찜 수</p>
                                <small class="text-green-500">+{{ monthlyStats.favoritesGrowth }}%</small>
                            </div>
                            <i class="pi pi-heart text-4xl text-red-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ monthlyStats.totalRecipes }}</h3>
                                <p class="text-600 m-0">신규 레시피</p>
                                <small class="text-green-500">+{{ monthlyStats.recipesGrowth }}%</small>
                            </div>
                            <i class="pi pi-plus-circle text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ monthlyStats.totalUsers }}</h3>
                                <p class="text-600 m-0">활성 사용자</p>
                                <small class="text-green-500">+{{ monthlyStats.usersGrowth }}%</small>
                            </div>
                            <i class="pi pi-users text-4xl text-purple-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 월간 랭킹 차트와 목록 -->
        <div class="grid">
            <!-- 랭킹 차트 -->
            <div class="col-12 lg:col-8">
                <Card>
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-chart-line text-blue-500"></i>
                            <span>월간 인기 레시피 TOP 20</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="ranking-chart mb-4">
                            <Chart type="bar" :data="chartData" :options="chartOptions" height="300" />
                        </div>

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
                                            <div class="flex align-items-center gap-1">
                                                <i class="pi pi-arrow-up text-green-500"></i>
                                                <span class="text-green-500">+{{ recipe.growth }}%</span>
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

            <!-- 사이드바 -->
            <div class="col-12 lg:col-4">
                <!-- 카테고리별 성과 -->
                <Card>
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-chart-pie text-green-500"></i>
                            <span>카테고리별 성과</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="category-performance">
                            <div v-for="category in categoryPerformance" :key="category.name" class="category-item">
                                <div class="flex align-items-center justify-content-between p-3 border-round hover:surface-50 transition-colors transition-duration-150">
                                    <div class="flex align-items-center gap-2">
                                        <div class="category-color" :style="{ backgroundColor: category.color }"></div>
                                        <div>
                                            <div class="font-semibold">{{ category.name }}</div>
                                            <div class="text-sm text-600">{{ category.count }}개 레시피</div>
                                        </div>
                                    </div>
                                    <div class="text-right">
                                        <div class="font-semibold text-green-500">+{{ category.growth }}%</div>
                                        <div class="text-sm text-600">{{ category.views.toLocaleString() }} 조회</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- 월간 트렌드 -->
                <Card class="mt-3">
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-trending-up text-orange-500"></i>
                            <span>월간 트렌드</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="trend-list">
                            <div v-for="trend in monthlyTrends" :key="trend.id" class="trend-item">
                                <div class="flex align-items-center justify-content-between p-2 border-round hover:surface-50 transition-colors transition-duration-150">
                                    <div class="flex align-items-center gap-2">
                                        <i :class="trend.icon" :style="{ color: trend.color }"></i>
                                        <span class="text-sm">{{ trend.text }}</span>
                                    </div>
                                    <Badge :value="trend.change" :severity="trend.severity" />
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>

                <!-- 상위 요리사 -->
                <Card class="mt-3">
                    <template #title>
                        <div class="flex align-items-center gap-2">
                            <i class="pi pi-crown text-yellow-500"></i>
                            <span>상위 요리사</span>
                        </div>
                    </template>
                    <template #content>
                        <div class="top-chefs">
                            <div v-for="chef in topChefs" :key="chef.id" class="chef-item">
                                <div class="flex align-items-center gap-3 p-2 border-round hover:surface-50 transition-colors transition-duration-150">
                                    <Avatar :image="chef.avatar" :label="chef.name.charAt(0)" size="large" shape="circle" />
                                    <div class="flex-1">
                                        <div class="font-semibold">{{ chef.name }}</div>
                                        <div class="text-sm text-600">{{ chef.recipes }}개 레시피</div>
                                    </div>
                                    <div class="text-right">
                                        <div class="font-semibold text-green-500">{{ chef.views.toLocaleString() }}</div>
                                        <div class="text-sm text-600">조회수</div>
                                    </div>
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
import Avatar from 'primevue/avatar';
import Badge from 'primevue/badge';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Chart from 'primevue/chart';
import Dialog from 'primevue/dialog';
import RadioButton from 'primevue/radiobutton';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// 반응형 데이터
const rankingRecipes = ref([]);
const showMonthDialog = ref(false);
const selectedMonth = ref('이번 달');

const monthOptions = ref([
    { label: '이번 달', value: '이번 달' },
    { label: '지난 달', value: '지난 달' },
    { label: '2개월 전', value: '2개월 전' },
    { label: '3개월 전', value: '3개월 전' }
]);

const monthlyStats = ref({
    totalViews: '2,450,000',
    viewsGrowth: 12.5,
    totalFavorites: '485,000',
    favoritesGrowth: 8.3,
    totalRecipes: '1,250',
    recipesGrowth: 15.2,
    totalUsers: '45,000',
    usersGrowth: 6.7
});

const categoryPerformance = ref([
    { name: '한식', count: 450, growth: 18.5, views: 850000, color: '#3B82F6' },
    { name: '양식', count: 320, growth: 12.3, views: 620000, color: '#10B981' },
    { name: '일식', count: 280, growth: 8.7, views: 480000, color: '#F59E0B' },
    { name: '중식', count: 250, growth: 15.2, views: 380000, color: '#EF4444' },
    { name: '디저트', count: 180, growth: 22.1, views: 320000, color: '#8B5CF6' }
]);

const monthlyTrends = ref([
    { id: 1, text: '건강한 요리', icon: 'pi pi-heart', color: '#10B981', change: '+25%', severity: 'success' },
    { id: 2, text: '간편 요리', icon: 'pi pi-clock', color: '#3B82F6', change: '+18%', severity: 'info' },
    { id: 3, text: '디저트', icon: 'pi pi-star', color: '#F59E0B', change: '+15%', severity: 'warning' },
    { id: 4, text: '비건 요리', icon: 'pi pi-leaf', color: '#10B981', change: '+12%', severity: 'success' },
    { id: 5, text: '한식', icon: 'pi pi-home', color: '#EF4444', change: '+8%', severity: 'info' }
]);

const topChefs = ref([
    { id: 1, name: '김요리', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50', recipes: 45, views: 125000 },
    { id: 2, name: '이맛집', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50', recipes: 38, views: 98000 },
    { id: 3, name: '박요리사', avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612b786?w=50', recipes: 32, views: 87000 },
    { id: 4, name: '최맛있', avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=50', recipes: 28, views: 76000 },
    { id: 5, name: '정요리왕', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=50', recipes: 25, views: 68000 }
]);

// 차트 데이터
const chartData = ref({
    labels: [],
    datasets: [
        {
            label: '조회수',
            data: [],
            backgroundColor: '#3B82F6',
            borderColor: '#3B82F6',
            borderWidth: 1
        }
    ]
});

const chartOptions = ref({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            display: false
        }
    },
    scales: {
        y: {
            beginAtZero: true,
            ticks: {
                callback: function (value) {
                    return value.toLocaleString();
                }
            }
        }
    }
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
            views: 125000,
            favorites: 25000,
            rating: 4.5,
            growth: 18.5,
            isFavorite: false
        },
        {
            id: 2,
            title: '파스타',
            description: '크림소스가 일품인 파스타',
            image: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=100',
            views: 98000,
            favorites: 19800,
            rating: 4.2,
            growth: 12.3,
            isFavorite: true
        },
        {
            id: 3,
            title: '초밥',
            description: '신선한 생선으로 만든 초밥',
            image: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=100',
            views: 87000,
            favorites: 17500,
            rating: 4.8,
            growth: 8.7,
            isFavorite: false
        },
        {
            id: 4,
            title: '짜장면',
            description: '진한 춘장소스의 짜장면',
            image: 'https://images.unsplash.com/photo-1563379091339-03246963d4d8?w=100',
            views: 76000,
            favorites: 15200,
            rating: 4.3,
            growth: 15.2,
            isFavorite: false
        },
        {
            id: 5,
            title: '치즈케이크',
            description: '부드럽고 달콤한 치즈케이크',
            image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=100',
            views: 68000,
            favorites: 13600,
            rating: 4.7,
            growth: 22.1,
            isFavorite: true
        }
    ];

    // 차트 데이터 업데이트
    updateChartData();
};

const updateChartData = () => {
    const top10 = rankingRecipes.value.slice(0, 10);
    chartData.value.labels = top10.map((recipe) => recipe.title);
    chartData.value.datasets[0].data = top10.map((recipe) => recipe.views);
};

const refreshRanking = () => {
    loadRankingData();
    toast.add({ severity: 'success', summary: '새로고침', detail: '월간 랭킹 데이터가 업데이트되었습니다.', life: 3000 });
};

const applyMonthFilter = () => {
    showMonthDialog.value = false;
    loadRankingData();
    toast.add({ severity: 'info', summary: '월간 변경', detail: `${selectedMonth.value} 데이터를 불러왔습니다.`, life: 3000 });
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

.category-color {
    width: 12px;
    height: 12px;
    border-radius: 50%;
}

.category-item,
.trend-item,
.chef-item {
    border-bottom: 1px solid var(--surface-border);
}

.category-item:last-child,
.trend-item:last-child,
.chef-item:last-child {
    border-bottom: none;
}
</style>
