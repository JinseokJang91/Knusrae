<template>
    <div class="card">
        <!-- header : 페이지 제목, 새로고침 버튼, 상세검색 다이얼로그 버튼 -->
        <div class="flex justify-between items-center mb-4">
            <h1 class="text-3xl font-bold text-gray-900">카테고리</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshCategories" />
                <Button icon="pi pi-search" label="검색" severity="" @click="showSearchDialog = true" />
            </div>
        </div>

        <!-- dialog : 상세검색 -->
        <Dialog v-model:visible="showSearchDialog" header="레시피 상세 검색" :style="{ width: '400px' }">
            <div class="flex flex-col gap-3">
                <div>
                    <label class="block text-gray-900 font-medium mb-2">검색어</label>
                    <InputText v-model="searchQuery" placeholder="레시피 이름을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-gray-900 font-medium mb-2">카테고리</label>
                    <Dropdown v-model="searchCategory" :options="categories" optionLabel="name" optionValue="value" placeholder="전체 카테고리" class="w-full" />
                </div>
                <div>
                    <label class="block text-gray-900 font-medium mb-2">난이도</label>
                    <Dropdown v-model="searchDifficulty" :options="difficulties" optionLabel="name" optionValue="value" placeholder="전체 난이도" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showSearchDialog = false" />
                <Button label="검색" @click="performSearch" />
            </template>
        </Dialog>

        <!-- header : 카테고리 통계 -->
        <div class="grid grid-cols-12 gap-4 mb-4">
            <div class="col-span-12 sm:col-span-6 lg:col-span-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <p class="text-gray-600 m-0">총 카테고리</p>
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ totalCategories }}</h3>
                            </div>
                            <i class="pi pi-tags text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-span-12 sm:col-span-6 lg:col-span-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <p class="text-gray-600 m-0">총 레시피</p>
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ totalRecipes }}</h3>
                            </div>
                            <i class="pi pi-book text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-span-12 sm:col-span-6 lg:col-span-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <p class="text-gray-600 m-0">현재 카테고리</p>
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ selectedCategory ? getCategoryName(selectedCategory) : '전체' }}</h3>
                            </div>
                            <i class="pi pi-filter text-4xl text-orange-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-span-12 sm:col-span-6 lg:col-span-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex items-center">
                            <div class="flex-1">
                                <p class="text-gray-600 m-0">표시된 레시피</p>
                                <h3 class="text-2xl font-bold text-gray-900 m-0">{{ filteredRecipes.length }}</h3>
                            </div>
                            <i class="pi pi-list text-4xl text-purple-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- header : 카테고리 선택 영역 (라디오 버튼 형태) -->
        <div class="category-selector mb-4">
            <div class="flex flex-wrap gap-2 justify-center">
                <Button
                    v-for="category in categories"
                    :key="category.value"
                    :label="category.name"
                    :icon="category.icon"
                    :class="selectedCategory === category.value ? 'p-button-primary' : 'p-button-outlined'"
                    size="small"
                    @click="selectCategory(category.value)"
                    class="category-button"
                />
            </div>
        </div>

        <!-- body : 레시피 목록 섹션 -->
        <div class="recipe-section">
            <div class="flex justify-between items-center mb-3">
                <h2 class="text-2xl font-semibold text-gray-900 m-0">
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
                <p class="text-gray-600 mt-3">레시피를 불러오는 중...</p>
            </div>

            <!-- 에러 상태 -->
            <div v-else-if="error" class="text-center py-8">
                <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">레시피를 불러올 수 없습니다</h3>
                <p class="text-gray-600 mb-4">{{ error }}</p>
                <Button label="다시 시도" @click="loadRecipes" />
            </div>

            <!-- 레시피 목록이 있는 경우 -->
            <div v-else-if="displayRecipes.length > 0">
                <!-- 그리드 뷰 (카드 형태) -->
                <div v-if="viewMode === 'grid'" class="recipe-grid">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-card-wrapper">
                        <Card class="recipe-card h-full">
                            <template #header>
                                <div class="recipe-image-container">
                                    <img :src="recipe.thumbnail" :alt="recipe.title" class="recipe-image" />
                                    <div class="recipe-overlay">
                                        <div class="recipe-actions">
                                            <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="small" rounded @click="toggleFavorite(recipe.id)" />
                                            <Button icon="pi pi-bookmark" severity="secondary" size="small" rounded @click="bookmarkRecipe(recipe.id)" />
                                        </div>
                                        <Tag :value="getCategoryName(recipe.category)" severity="info" class="recipe-category-tag" />
                                    </div>
                                </div>
                            </template>
                            <template #content>
                                <div class="recipe-content">
                                    <h3 class="recipe-title">{{ recipe.title }}</h3>
                                    <p class="recipe-description">{{ recipe.description }}</p>
                                    <div class="recipe-meta">
                                        <div class="recipe-rating">
                                            <Rating v-model="recipe.rating" readonly :cancel="false" />
                                            <span class="rating-text">{{ recipe.rating }}</span>
                                        </div>
                                        <div class="recipe-info">
                                            <div class="info-item">
                                                <i class="pi pi-clock"></i>
                                                <span>{{ recipe.cookingTime }}분</span>
                                            </div>
                                            <div class="info-item">
                                                <i class="pi pi-users"></i>
                                                <span>{{ recipe.servings }}인분</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                            <template #footer>
                                <Button label="상세보기" class="w-full" @click="viewRecipe(recipe.id)" />
                            </template>
                        </Card>
                    </div>
                </div>

                <!-- 리스트 뷰 -->
                <div v-else class="recipe-list">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-list-item">
                        <div class="flex items-center gap-3 p-3 rounded hover:bg-gray-50 transition-colors duration-150">
                            <img :src="recipe.image" :alt="recipe.title" class="recipe-thumbnail" />
                            <div class="flex-1">
                                <h4 class="text-lg font-semibold text-gray-900 m-0 mb-1">{{ recipe.title }}</h4>
                                <p class="text-gray-600 text-sm m-0 mb-2">{{ recipe.description }}</p>
                                <div class="flex items-center gap-3 text-sm text-gray-500">
                                    <div class="flex items-center gap-1">
                                        <i class="pi pi-clock"></i>
                                        <span>{{ recipe.cookingTime }}분</span>
                                    </div>
                                    <div class="flex items-center gap-1">
                                        <i class="pi pi-users"></i>
                                        <span>{{ recipe.servings }}인분</span>
                                    </div>
                                    <div class="flex items-center gap-1">
                                        <i class="pi pi-star-fill text-yellow-500"></i>
                                        <span>{{ recipe.rating }}</span>
                                    </div>
                                    <Tag :value="recipe.category" severity="info" />
                                </div>
                            </div>
                            <div class="flex flex-col gap-2">
                                <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="small" rounded @click="toggleFavorite(recipe.id)" />
                                <Button label="상세보기" size="small" @click="viewRecipe(recipe.id)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- footer : 페이지네이션 -->
                <div class="flex justify-center mt-4">
                    <Paginator v-model:first="first" :rows="rows" :totalRecords="totalDisplayRecipes" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
                </div>
            </div>

            <!-- 빈 상태 -->
            <div v-else class="text-center py-8">
                <i class="pi pi-book text-6xl text-300 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">레시피가 없습니다</h3>
                <p class="text-gray-600 mb-4">{{ selectedCategory ? '선택한 카테고리에 레시피가 없습니다.' : '등록된 레시피가 없습니다.' }}</p>
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
// TODO 테스트는 로컬로 진행, 추후 분기처리로 EC2 연결 예정
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082';

// Enum Class - Difficulty (난이도)
const difficulties = ref([
    { name: '매우 쉬움', value: 'very easy' },
    { name: '쉬움', value: 'easy' },
    { name: '보통', value: 'medium' },
    { name: '조금 어려움', value: 'little hard' },
    { name: '어려움', value: 'hard' }
]);

// 카테고리 header > 총 카테고리 > 개수
const totalCategories = computed(() => categories.value.length);
// 카테고리 header > 총 레시피 > 개수
const totalRecipes = computed(() => recipes.value.length);
// 카테고리 header > 표시된 레시피 > 개수
const filteredRecipes = computed(() => {
    if (selectedCategory.value) {
        return recipes.value.filter((recipe) => recipe.category === selectedCategory.value);
    }
    return recipes.value;
});

// 카테고리 body > 레시피
const displayRecipes = computed(() => {
    const source = searchResults.value.length > 0 ? searchResults.value : filteredRecipes.value;
    return source.slice(first.value, first.value + rows.value);
});

// 카테고리 footer > 페이징 처리
const totalDisplayRecipes = computed(() => {
    return searchResults.value.length > 0 ? searchResults.value.length : filteredRecipes.value.length;
});

// Function > onMounted > 카테고리 조회
// TODO 카테고리 목록 조회 API 연결 예정
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

// Function > onMounted > 레시피 조회
const loadRecipes = async () => {
    try {
        loading.value = true;
        error.value = null;

        const response = await httpJson(API_BASE_URL, '/api/recipe/list', {
            method: 'GET'
        });

        recipes.value = response.data || response || [];

        console.log('RECIPE LIST : ' + recipes.value); // TODO 삭제

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

// Function > Button > header 새로고침
const refreshCategories = async () => {
    await loadRecipes();
    toast.add({ severity: 'success', summary: '새로고침', detail: '카테고리 정보가 업데이트되었습니다.', life: 3000 });
};

// Function > Button > body 카테고리 선택 시 목록 필터링
const selectCategory = (categoryValue) => {
    selectedCategory.value = selectedCategory.value === categoryValue ? null : categoryValue;
    searchResults.value = [];
    first.value = 0;
};

// const viewCategory = (categoryValue) => {
//     selectedCategory.value = categoryValue;
//     searchResults.value = [];
//     first.value = 0;
// };

// Function > header 현재 카테고리 값 동기화
const getCategoryName = (categoryValue) => {
    const category = categories.value.find((cat) => cat.value === categoryValue);
    return category ? category.name : categoryValue;
};

// Function > Button > header 상세검색 다이얼로그 검색
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

// Function > Button > 찜 목록 추가
// TODO 찜 목록 데이터로 관리 시 API 연결 필요
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

// Function > 레시피 상세 조회
const viewRecipe = (recipeId) => {
    router.push(`/recipe/${recipeId}`);
};

// Function > Button > 북마크 추가
// TODO 북마크 목록 데이터로 관리 시 API 연결 필요
const bookmarkRecipe = (recipeId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '레시피가 북마크되었습니다.', life: 3000 });
};

// Function > 페이징
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
    background: linear-gradient(135deg, #249461 0%, #afe6ca 100%);
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

/* 카테고리 선택기 스타일 */
.category-selector {
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.category-button {
    min-width: 120px;
    transition: all 0.2s ease;
}

.category-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 레시피 그리드 스타일 */
.recipe-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.recipe-card-wrapper {
    transition: transform 0.2s ease;
}

.recipe-card-wrapper:hover {
    transform: translateY(-4px);
}

.recipe-card {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
}

.recipe-card:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

/* 레시피 이미지 스타일 */
.recipe-image-container {
    position: relative;
    width: 100%;
    height: 200px;
    overflow: hidden;
}

.recipe-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.recipe-card:hover .recipe-image {
    transform: scale(1.05);
}

.recipe-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 50%, rgba(0, 0, 0, 0.5) 100%);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 1rem;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.recipe-card:hover .recipe-overlay {
    opacity: 1;
}

.recipe-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

.recipe-category-tag {
    align-self: flex-start;
}

/* 레시피 콘텐츠 스타일 */
.recipe-content {
    padding: 1rem;
}

.recipe-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--text-color);
    margin: 0 0 0.5rem 0;
    line-height: 1.4;
}

.recipe-description {
    color: var(--text-color-secondary);
    font-size: 0.9rem;
    line-height: 1.5;
    margin: 0 0 1rem 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.recipe-meta {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.recipe-rating {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.rating-text {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-color);
}

.recipe-info {
    display: flex;
    gap: 1rem;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    font-size: 0.85rem;
    color: var(--text-color-secondary);
}

.info-item i {
    font-size: 0.8rem;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .recipe-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }

    .category-selector {
        padding: 1rem;
    }

    .category-button {
        min-width: 100px;
        font-size: 0.9rem;
    }
}
</style>
