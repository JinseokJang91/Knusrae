<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">찜 목록</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshFavorites" />
                <Button icon="pi pi-filter" label="정렬" severity="secondary" @click="showSortDialog = true" />
                <Button icon="pi pi-trash" label="선택 삭제" severity="danger" @click="deleteSelected" :disabled="selectedRecipes.length === 0" />
            </div>
        </div>

        <!-- 정렬 다이얼로그 -->
        <Dialog v-model:visible="showSortDialog" header="정렬 옵션" :style="{ width: '300px' }">
            <div class="flex flex-column gap-3">
                <div v-for="option in sortOptions" :key="option.value" class="flex align-items-center">
                    <RadioButton v-model="selectedSort" :inputId="option.value" :value="option.value" />
                    <label :for="option.value" class="ml-2">{{ option.label }}</label>
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showSortDialog = false" />
                <Button label="적용" @click="applySort" />
            </template>
        </Dialog>

        <!-- 찜 목록 통계 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-4">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalFavorites }}</h3>
                                <p class="text-600 m-0">총 찜한 레시피</p>
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
                                <h3 class="text-2xl font-bold text-900 m-0">{{ categoryCount }}</h3>
                                <p class="text-600 m-0">다양한 카테고리</p>
                            </div>
                            <i class="pi pi-tags text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-4">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ selectedRecipes.length }}</h3>
                                <p class="text-600 m-0">선택된 항목</p>
                            </div>
                            <i class="pi pi-check-square text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 카테고리 필터 -->
        <div class="flex flex-wrap gap-2 mb-4">
            <Button v-for="category in categories" :key="category.value" :label="category.name" :severity="selectedCategory === category.value ? 'primary' : 'secondary'" size="small" @click="filterByCategory(category.value)" />
            <Button label="전체" :severity="selectedCategory === null ? 'primary' : 'secondary'" size="small" @click="filterByCategory(null)" />
        </div>

        <!-- 찜 목록 그리드 -->
        <div v-if="filteredFavorites.length > 0" class="grid">
            <div v-for="recipe in filteredFavorites" :key="recipe.id" class="col-12 md:col-6 lg:col-4">
                <Card class="favorite-card h-full">
                    <template #header>
                        <div class="relative">
                            <img :src="recipe.image" :alt="recipe.title" class="w-full h-15rem object-cover" />
                            <div class="absolute top-0 right-0 m-2">
                                <Checkbox v-model="selectedRecipes" :value="recipe.id" />
                            </div>
                            <div class="absolute top-0 left-0 m-2">
                                <Tag :value="recipe.category" severity="info" />
                            </div>
                            <div class="absolute bottom-0 right-0 m-2">
                                <Button icon="pi pi-heart-fill" class="p-button-danger" size="small" rounded @click="removeFavorite(recipe.id)" />
                            </div>
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
                        <div class="flex justify-content-between align-items-center text-sm text-500 mb-3">
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
                        <div class="text-sm text-500">
                            <i class="pi pi-calendar mr-1"></i>
                            찜한 날짜: {{ formatDate(recipe.favoriteDate) }}
                        </div>
                    </template>
                    <template #footer>
                        <div class="flex gap-2">
                            <Button label="상세보기" class="flex-1" @click="viewRecipe(recipe.id)" />
                            <Button icon="pi pi-bookmark" severity="secondary" @click="bookmarkRecipe(recipe.id)" />
                            <Button icon="pi pi-share-alt" severity="secondary" @click="shareRecipe(recipe)" />
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-else class="text-center py-8">
            <i class="pi pi-heart text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-600 mb-2">찜한 레시피가 없습니다</h3>
            <p class="text-600 mb-4">마음에 드는 레시피를 찜해보세요!</p>
            <Button label="레시피 둘러보기" @click="browseRecipes" />
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredFavorites.length > 0" class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredFavorites" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>
    </div>
</template>

<script setup>
import Button from 'primevue/button';
import Card from 'primevue/card';
import Checkbox from 'primevue/checkbox';
import Dialog from 'primevue/dialog';
import Paginator from 'primevue/paginator';
import RadioButton from 'primevue/radiobutton';
import Rating from 'primevue/rating';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// 반응형 데이터
const favoriteRecipes = ref([]);
const selectedRecipes = ref([]);
const showSortDialog = ref(false);
const selectedSort = ref('date');
const selectedCategory = ref(null);
const first = ref(0);
const rows = ref(12);

const sortOptions = ref([
    { label: '찜한 날짜순', value: 'date' },
    { label: '이름순', value: 'name' },
    { label: '평점순', value: 'rating' },
    { label: '조리시간순', value: 'time' },
    { label: '카테고리순', value: 'category' }
]);

const categories = ref([
    { name: '한식', value: 'korean' },
    { name: '중식', value: 'chinese' },
    { name: '일식', value: 'japanese' },
    { name: '양식', value: 'western' },
    { name: '디저트', value: 'dessert' }
]);

// 계산된 속성
const totalFavorites = computed(() => favoriteRecipes.value.length);

const categoryCount = computed(() => {
    const uniqueCategories = new Set(favoriteRecipes.value.map((recipe) => recipe.category));
    return uniqueCategories.size;
});

const filteredFavorites = computed(() => {
    let filtered = favoriteRecipes.value;

    // 카테고리 필터
    if (selectedCategory.value) {
        filtered = filtered.filter((recipe) => recipe.category === selectedCategory.value);
    }

    // 정렬
    switch (selectedSort.value) {
        case 'name':
            filtered = filtered.sort((a, b) => a.title.localeCompare(b.title));
            break;
        case 'rating':
            filtered = filtered.sort((a, b) => b.rating - a.rating);
            break;
        case 'time':
            filtered = filtered.sort((a, b) => a.cookingTime - b.cookingTime);
            break;
        case 'category':
            filtered = filtered.sort((a, b) => a.category.localeCompare(b.category));
            break;
        case 'date':
        default:
            filtered = filtered.sort((a, b) => new Date(b.favoriteDate) - new Date(a.favoriteDate));
            break;
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalFilteredFavorites = computed(() => {
    let filtered = favoriteRecipes.value;
    if (selectedCategory.value) {
        filtered = filtered.filter((recipe) => recipe.category === selectedCategory.value);
    }
    return filtered.length;
});

// 메서드
const loadFavorites = () => {
    // 실제로는 API 호출
    favoriteRecipes.value = [
        {
            id: 1,
            title: '김치찌개',
            description: '매콤하고 시원한 김치찌개입니다.',
            image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400',
            category: 'korean',
            cookingTime: 30,
            servings: 2,
            rating: 4.5,
            favoriteDate: '2024-01-15'
        },
        {
            id: 2,
            title: '파스타',
            description: '크림소스가 일품인 파스타입니다.',
            image: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=400',
            category: 'western',
            cookingTime: 25,
            servings: 2,
            rating: 4.2,
            favoriteDate: '2024-01-14'
        },
        {
            id: 3,
            title: '초밥',
            description: '신선한 생선으로 만든 초밥입니다.',
            image: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400',
            category: 'japanese',
            cookingTime: 45,
            servings: 4,
            rating: 4.8,
            favoriteDate: '2024-01-13'
        },
        {
            id: 4,
            title: '치즈케이크',
            description: '부드럽고 달콤한 치즈케이크입니다.',
            image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400',
            category: 'dessert',
            cookingTime: 90,
            servings: 8,
            rating: 4.7,
            favoriteDate: '2024-01-12'
        },
        {
            id: 5,
            title: '불고기',
            description: '달콤한 양념의 불고기입니다.',
            image: 'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=400',
            category: 'korean',
            cookingTime: 15,
            servings: 3,
            rating: 4.4,
            favoriteDate: '2024-01-11'
        }
    ];
};

const refreshFavorites = () => {
    loadFavorites();
    toast.add({ severity: 'success', summary: '새로고침', detail: '찜 목록이 업데이트되었습니다.', life: 3000 });
};

const applySort = () => {
    showSortDialog.value = false;
    first.value = 0;
    toast.add({ severity: 'info', summary: '정렬 적용', detail: '선택한 기준으로 정렬되었습니다.', life: 3000 });
};

const filterByCategory = (category) => {
    selectedCategory.value = category;
    first.value = 0;
};

const removeFavorite = (recipeId) => {
    const index = favoriteRecipes.value.findIndex((recipe) => recipe.id === recipeId);
    if (index > -1) {
        favoriteRecipes.value.splice(index, 1);
        toast.add({ severity: 'success', summary: '찜 해제', detail: '찜 목록에서 제거되었습니다.', life: 3000 });
    }
};

const deleteSelected = () => {
    if (selectedRecipes.value.length > 0) {
        favoriteRecipes.value = favoriteRecipes.value.filter((recipe) => !selectedRecipes.value.includes(recipe.id));
        selectedRecipes.value = [];
        toast.add({ severity: 'success', summary: '삭제 완료', detail: '선택한 레시피가 삭제되었습니다.', life: 3000 });
    }
};

const viewRecipe = (recipeId) => {
    router.push(`/recipe/detail/${recipeId}`);
};

const bookmarkRecipe = (recipeId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '레시피가 북마크되었습니다.', life: 3000 });
};

const shareRecipe = (recipe) => {
    if (navigator.share) {
        navigator.share({
            title: recipe.title,
            text: recipe.description,
            url: window.location.origin + `/recipe/detail/${recipe.id}`
        });
    } else {
        // 클립보드에 복사
        navigator.clipboard.writeText(window.location.origin + `/recipe/detail/${recipe.id}`);
        toast.add({ severity: 'info', summary: '링크 복사', detail: '레시피 링크가 클립보드에 복사되었습니다.', life: 3000 });
    }
};

const browseRecipes = () => {
    router.push('/recommend/today');
};

const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 생명주기
onMounted(() => {
    loadFavorites();
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

.favorite-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.favorite-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
</style>
