<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">카테고리</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshCategories" />
                <Button icon="pi pi-search" label="검색" severity="secondary" @click="showSearchDialog = true" />
            </div>
        </div>

        <!-- 검색 다이얼로그 -->
        <Dialog v-model:visible="showSearchDialog" header="레시피 검색" :style="{ width: '400px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">검색어</label>
                    <InputText v-model="searchQuery" placeholder="레시피 이름을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">카테고리</label>
                    <Dropdown v-model="searchCategory" :options="categories" optionLabel="name" optionValue="value" placeholder="전체 카테고리" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">난이도</label>
                    <Dropdown v-model="searchDifficulty" :options="difficulties" optionLabel="name" optionValue="value" placeholder="전체 난이도" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showSearchDialog = false" />
                <Button label="검색" @click="performSearch" />
            </template>
        </Dialog>

        <!-- 카테고리 통계 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalCategories }}</h3>
                                <p class="text-600 m-0">총 카테고리</p>
                            </div>
                            <i class="pi pi-tags text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalRecipes }}</h3>
                                <p class="text-600 m-0">총 레시피</p>
                            </div>
                            <i class="pi pi-book text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ selectedCategory ? getCategoryName(selectedCategory) : '전체' }}</h3>
                                <p class="text-600 m-0">현재 카테고리</p>
                            </div>
                            <i class="pi pi-filter text-4xl text-orange-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ filteredRecipes.length }}</h3>
                                <p class="text-600 m-0">표시된 레시피</p>
                            </div>
                            <i class="pi pi-list text-4xl text-purple-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 카테고리 그리드 -->
        <div class="grid mb-4">
            <div v-for="category in categories" :key="category.value" class="col-12 md:col-6 lg:col-4">
                <Card class="category-card cursor-pointer" :class="{ selected: selectedCategory === category.value }" @click="selectCategory(category.value)">
                    <template #content>
                        <div class="text-center">
                            <div class="category-icon mb-3">
                                <i :class="category.icon" :style="{ color: category.color }"></i>
                            </div>
                            <h3 class="text-xl font-semibold text-900 m-0 mb-2">{{ category.name }}</h3>
                            <p class="text-600 m-0 mb-3">{{ category.description }}</p>
                            <div class="flex justify-content-between align-items-center">
                                <span class="text-sm text-500">{{ category.recipeCount }}개 레시피</span>
                                <Button icon="pi pi-arrow-right" size="small" severity="secondary" rounded @click.stop="viewCategory(category.value)" />
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 레시피 목록 섹션 -->
        <div class="recipe-section">
            <div class="flex justify-content-between align-items-center mb-3">
                <h2 class="text-2xl font-semibold text-900 m-0">
                    {{ selectedCategory ? getCategoryName(selectedCategory) + ' 레시피' : '전체 레시피' }}
                </h2>
                <div class="flex gap-2">
                    <Button icon="pi pi-th-large" :class="viewMode === 'grid' ? 'p-button-primary' : 'p-button-secondary'" size="small" @click="viewMode = 'grid'" />
                    <Button icon="pi pi-list" :class="viewMode === 'list' ? 'p-button-primary' : 'p-button-secondary'" size="small" @click="viewMode = 'list'" />
                </div>
            </div>

            <!-- 로딩 상태 -->
            <div v-if="loading" class="text-center py-8">
                <ProgressSpinner />
                <p class="text-600 mt-3">레시피를 불러오는 중...</p>
            </div>

            <!-- 에러 상태 -->
            <div v-else-if="error" class="text-center py-8">
                <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                <h3 class="text-2xl font-semibold text-600 mb-2">레시피를 불러올 수 없습니다</h3>
                <p class="text-600 mb-4">{{ error }}</p>
                <Button label="다시 시도" @click="loadRecipes" />
            </div>

            <!-- 레시피 목록이 있는 경우 -->
            <div v-else-if="displayRecipes.length > 0">
                <!-- 그리드 뷰 -->
                <div v-if="viewMode === 'grid'" class="grid">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="col-12 md:col-6 lg:col-4">
                        <Card class="recipe-card h-full">
                            <template #header>
                                <div class="relative">
                                    <img :src="recipe.image" :alt="recipe.title" class="w-full h-15rem object-cover" />
                                    <div class="absolute top-0 right-0 m-2">
                                        <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="small" rounded @click="toggleFavorite(recipe.id)" />
                                    </div>
                                    <Tag :value="recipe.category" severity="info" class="absolute bottom-0 left-0 m-2" />
                                </div>
                            </template>
                            <template #title>
                                <div class="flex justify-content-between align-items-start">
                                    <h3 class="text-xl font-semibold text-900 m-0">{{ recipe.title }}</h3>
                                    <Rating v-model="recipe.rating" readonly :cancel="false" />
                                </div>
                            </template>
                            <template #content>
                                <p class="text-600 mb-3">{{ recipe.description }}</p>
                                <div class="flex justify-content-between align-items-center text-sm text-500">
                                    <div class="flex align-items-center gap-1">
                                        <i class="pi pi-clock"></i>
                                        <span>{{ recipe.cookingTime }}분</span>
                                    </div>
                                    <div class="flex align-items-center gap-1">
                                        <i class="pi pi-users"></i>
                                        <span>{{ recipe.servings }}인분</span>
                                    </div>
                                    <div class="flex align-items-center gap-1">
                                        <i class="pi pi-star-fill text-yellow-500"></i>
                                        <span>{{ recipe.rating }}</span>
                                    </div>
                                </div>
                            </template>
                            <template #footer>
                                <div class="flex gap-2">
                                    <Button label="상세보기" class="flex-1" @click="viewRecipe(recipe.id)" />
                                    <Button icon="pi pi-bookmark" severity="secondary" @click="bookmarkRecipe(recipe.id)" />
                                </div>
                            </template>
                        </Card>
                    </div>
                </div>

                <!-- 리스트 뷰 -->
                <div v-else class="recipe-list">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-list-item">
                        <div class="flex align-items-center gap-3 p-3 border-round hover:surface-50 transition-colors transition-duration-150">
                            <img :src="recipe.image" :alt="recipe.title" class="recipe-thumbnail" />
                            <div class="flex-1">
                                <h4 class="text-lg font-semibold text-900 m-0 mb-1">{{ recipe.title }}</h4>
                                <p class="text-600 text-sm m-0 mb-2">{{ recipe.description }}</p>
                                <div class="flex align-items-center gap-3 text-sm text-500">
                                    <div class="flex align-items-center gap-1">
                                        <i class="pi pi-clock"></i>
                                        <span>{{ recipe.cookingTime }}분</span>
                                    </div>
                                    <div class="flex align-items-center gap-1">
                                        <i class="pi pi-users"></i>
                                        <span>{{ recipe.servings }}인분</span>
                                    </div>
                                    <div class="flex align-items-center gap-1">
                                        <i class="pi pi-star-fill text-yellow-500"></i>
                                        <span>{{ recipe.rating }}</span>
                                    </div>
                                    <Tag :value="recipe.category" severity="info" />
                                </div>
                            </div>
                            <div class="flex flex-column gap-2">
                                <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="small" rounded @click="toggleFavorite(recipe.id)" />
                                <Button label="상세보기" size="small" @click="viewRecipe(recipe.id)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 페이지네이션 -->
                <div class="flex justify-content-center mt-4">
                    <Paginator v-model:first="first" :rows="rows" :totalRecords="totalDisplayRecipes" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
                </div>
            </div>

            <!-- 빈 상태 -->
            <div v-else class="text-center py-8">
                <i class="pi pi-book text-6xl text-300 mb-4"></i>
                <h3 class="text-2xl font-semibold text-600 mb-2">레시피가 없습니다</h3>
                <p class="text-600 mb-4">{{ selectedCategory ? '선택한 카테고리에 레시피가 없습니다.' : '등록된 레시피가 없습니다.' }}</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { httpJson } from '@/utils/http';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Paginator from 'primevue/paginator';
import ProgressSpinner from 'primevue/progressspinner';
import Rating from 'primevue/rating';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// 반응형 데이터
const categories = ref([]);
const recipes = ref([]);
const selectedCategory = ref(null);
const searchResults = ref([]);
const showSearchDialog = ref(false);
const searchQuery = ref('');
const searchCategory = ref(null);
const searchDifficulty = ref(null);
const viewMode = ref('grid');
const first = ref(0);
const rows = ref(12);
const loading = ref(false);
const error = ref(null);

// API 기본 URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082';

const difficulties = ref([
    { name: '쉬움', value: 'easy' },
    { name: '보통', value: 'medium' },
    { name: '어려움', value: 'hard' }
]);

// 계산된 속성
const totalCategories = computed(() => categories.value.length);

const totalRecipes = computed(() => recipes.value.length);

const filteredRecipes = computed(() => {
    if (selectedCategory.value) {
        return recipes.value.filter((recipe) => recipe.category === selectedCategory.value);
    }
    return recipes.value;
});

const displayRecipes = computed(() => {
    const source = searchResults.value.length > 0 ? searchResults.value : filteredRecipes.value;
    return source.slice(first.value, first.value + rows.value);
});

const totalDisplayRecipes = computed(() => {
    return searchResults.value.length > 0 ? searchResults.value.length : filteredRecipes.value.length;
});

// 메서드
const loadCategories = () => {
    categories.value = [
        {
            value: 'korean',
            name: '한식',
            description: '전통 한국 요리',
            icon: 'pi pi-home',
            color: '#3B82F6',
            recipeCount: 45
        },
        {
            value: 'chinese',
            name: '중식',
            description: '중국 전통 요리',
            icon: 'pi pi-compass',
            color: '#EF4444',
            recipeCount: 32
        },
        {
            value: 'japanese',
            name: '일식',
            description: '일본 전통 요리',
            icon: 'pi pi-sun',
            color: '#F59E0B',
            recipeCount: 28
        },
        {
            value: 'western',
            name: '양식',
            description: '서양 요리',
            icon: 'pi pi-globe',
            color: '#10B981',
            recipeCount: 35
        },
        {
            value: 'dessert',
            name: '디저트',
            description: '달콤한 디저트',
            icon: 'pi pi-star',
            color: '#8B5CF6',
            recipeCount: 22
        },
        {
            value: 'snack',
            name: '간식',
            description: '간편한 간식',
            icon: 'pi pi-apple',
            color: '#F97316',
            recipeCount: 18
        }
    ];
};

const loadRecipes = async () => {
    try {
        loading.value = true;
        error.value = null;

        const response = await httpJson(API_BASE_URL, '/api/recipe/list', {
            method: 'GET'
        });
        console.log('response :' + response);

        recipes.value = response.data || response || [];

        toast.add({
            severity: 'success',
            summary: '레시피 로드 완료',
            detail: `${recipes.value.length}개의 레시피를 불러왔습니다.`,
            life: 3000
        });
    } catch (err) {
        console.error('레시피 로드 실패:', err);
        error.value = err.message || '레시피를 불러오는데 실패했습니다.';

        // API 실패 시 더미 데이터 사용
        recipes.value = [
            {
                id: 1,
                title: '김치찌개',
                description: '매콤하고 시원한 김치찌개입니다.',
                image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400',
                category: 'korean',
                difficulty: 'easy',
                cookingTime: 30,
                servings: 2,
                rating: 4.5,
                isFavorite: false
            },
            {
                id: 2,
                title: '짜장면',
                description: '진한 춘장소스의 짜장면입니다.',
                image: 'https://images.unsplash.com/photo-1563379091339-03246963d4d8?w=400',
                category: 'chinese',
                difficulty: 'medium',
                cookingTime: 20,
                servings: 2,
                rating: 4.3,
                isFavorite: false
            },
            {
                id: 3,
                title: '초밥',
                description: '신선한 생선으로 만든 초밥입니다.',
                image: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400',
                category: 'japanese',
                difficulty: 'hard',
                cookingTime: 45,
                servings: 4,
                rating: 4.8,
                isFavorite: false
            },
            {
                id: 4,
                title: '파스타',
                description: '크림소스가 일품인 파스타입니다.',
                image: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=400',
                category: 'western',
                difficulty: 'medium',
                cookingTime: 25,
                servings: 2,
                rating: 4.2,
                isFavorite: true
            },
            {
                id: 5,
                title: '치즈케이크',
                description: '부드럽고 달콤한 치즈케이크입니다.',
                image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400',
                category: 'dessert',
                difficulty: 'hard',
                cookingTime: 90,
                servings: 8,
                rating: 4.7,
                isFavorite: true
            },
            {
                id: 6,
                title: '불고기',
                description: '달콤한 양념의 불고기입니다.',
                image: 'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=400',
                category: 'korean',
                difficulty: 'easy',
                cookingTime: 15,
                servings: 3,
                rating: 4.4,
                isFavorite: false
            }
        ];

        toast.add({
            severity: 'warn',
            summary: 'API 연결 실패',
            detail: '더미 데이터를 표시합니다.',
            life: 3000
        });
    } finally {
        loading.value = false;
    }
};

const refreshCategories = async () => {
    await loadRecipes();
    toast.add({ severity: 'success', summary: '새로고침', detail: '카테고리 정보가 업데이트되었습니다.', life: 3000 });
};

const selectCategory = (categoryValue) => {
    selectedCategory.value = selectedCategory.value === categoryValue ? null : categoryValue;
    searchResults.value = [];
    first.value = 0;
};

const viewCategory = (categoryValue) => {
    selectedCategory.value = categoryValue;
    searchResults.value = [];
    first.value = 0;
};

const getCategoryName = (categoryValue) => {
    const category = categories.value.find((cat) => cat.value === categoryValue);
    return category ? category.name : categoryValue;
};

const performSearch = () => {
    showSearchDialog.value = false;
    selectedCategory.value = null;

    let results = recipes.value;

    if (searchQuery.value) {
        results = results.filter((recipe) => recipe.title.toLowerCase().includes(searchQuery.value.toLowerCase()) || recipe.description.toLowerCase().includes(searchQuery.value.toLowerCase()));
    }

    if (searchCategory.value) {
        results = results.filter((recipe) => recipe.category === searchCategory.value);
    }

    if (searchDifficulty.value) {
        results = results.filter((recipe) => recipe.difficulty === searchDifficulty.value);
    }

    searchResults.value = results;
    first.value = 0;

    toast.add({
        severity: 'info',
        summary: '검색 완료',
        detail: `${results.length}개의 레시피를 찾았습니다.`,
        life: 3000
    });
};

const toggleFavorite = (recipeId) => {
    const recipe = recipes.value.find((r) => r.id === recipeId);
    if (recipe) {
        recipe.isFavorite = !recipe.isFavorite;
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

const bookmarkRecipe = (recipeId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '레시피가 북마크되었습니다.', life: 3000 });
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 카테고리 선택 감시
watch(selectedCategory, (newCategory) => {
    first.value = 0; // 페이지 초기화
});

// 생명주기
onMounted(async () => {
    loadCategories();
    await loadRecipes();
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

.category-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.category-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.category-card.selected {
    border: 2px solid var(--primary-color);
    box-shadow: 0 0 0 0.2rem rgba(var(--primary-color-rgb), 0.25);
}

.category-icon {
    font-size: 3rem;
}

.recipe-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.recipe-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.recipe-list-item {
    border-bottom: 1px solid var(--surface-border);
}

.recipe-list-item:last-child {
    border-bottom: none;
}

.recipe-thumbnail {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
}

.recipe-section {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid var(--surface-border);
}
</style>
