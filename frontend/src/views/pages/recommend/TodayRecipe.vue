<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">오늘의 레시피</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshRecipes" />
                <Button icon="pi pi-filter" label="필터" severity="secondary" @click="showFilterDialog = true" />
            </div>
        </div>

        <!-- 필터 다이얼로그 -->
        <Dialog v-model:visible="showFilterDialog" header="레시피 필터" :style="{ width: '400px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">카테고리</label>
                    <Dropdown v-model="selectedCategory" :options="categories" optionLabel="name" placeholder="카테고리 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">난이도</label>
                    <Dropdown v-model="selectedDifficulty" :options="difficulties" optionLabel="name" placeholder="난이도 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">조리시간</label>
                    <Dropdown v-model="selectedTime" :options="timeOptions" optionLabel="name" placeholder="조리시간 선택" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showFilterDialog = false" />
                <Button label="적용" @click="applyFilter" />
            </template>
        </Dialog>

        <!-- 레시피 그리드 -->
        <div class="grid">
            <div v-for="recipe in filteredRecipes" :key="recipe.id" class="col-12 md:col-6 lg:col-4">
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

        <!-- 페이지네이션 -->
        <div class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalRecipes" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>
    </div>
</template>

<script setup>
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import Paginator from 'primevue/paginator';
import Rating from 'primevue/rating';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// 반응형 데이터
const recipes = ref([]);
const showFilterDialog = ref(false);
const selectedCategory = ref(null);
const selectedDifficulty = ref(null);
const selectedTime = ref(null);
const first = ref(0);
const rows = ref(12);

// 필터 옵션
const categories = ref([
    { name: '한식', value: 'korean' },
    { name: '중식', value: 'chinese' },
    { name: '일식', value: 'japanese' },
    { name: '양식', value: 'western' },
    { name: '디저트', value: 'dessert' }
]);

const difficulties = ref([
    { name: '쉬움', value: 'easy' },
    { name: '보통', value: 'medium' },
    { name: '어려움', value: 'hard' }
]);

const timeOptions = ref([
    { name: '15분 이하', value: '15' },
    { name: '30분 이하', value: '30' },
    { name: '1시간 이하', value: '60' },
    { name: '1시간 이상', value: '60+' }
]);

// 계산된 속성
const filteredRecipes = computed(() => {
    let filtered = recipes.value;

    if (selectedCategory.value) {
        filtered = filtered.filter((recipe) => recipe.category === selectedCategory.value.name);
    }
    if (selectedDifficulty.value) {
        filtered = filtered.filter((recipe) => recipe.difficulty === selectedDifficulty.value.value);
    }
    if (selectedTime.value) {
        const time = selectedTime.value.value;
        if (time === '15') {
            filtered = filtered.filter((recipe) => recipe.cookingTime <= 15);
        } else if (time === '30') {
            filtered = filtered.filter((recipe) => recipe.cookingTime <= 30);
        } else if (time === '60') {
            filtered = filtered.filter((recipe) => recipe.cookingTime <= 60);
        } else if (time === '60+') {
            filtered = filtered.filter((recipe) => recipe.cookingTime > 60);
        }
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalRecipes = computed(() => {
    let filtered = recipes.value;

    if (selectedCategory.value) {
        filtered = filtered.filter((recipe) => recipe.category === selectedCategory.value.name);
    }
    if (selectedDifficulty.value) {
        filtered = filtered.filter((recipe) => recipe.difficulty === selectedDifficulty.value.value);
    }
    if (selectedTime.value) {
        const time = selectedTime.value.value;
        if (time === '15') {
            filtered = filtered.filter((recipe) => recipe.cookingTime <= 15);
        } else if (time === '30') {
            filtered = filtered.filter((recipe) => recipe.cookingTime <= 30);
        } else if (time === '60') {
            filtered = filtered.filter((recipe) => recipe.cookingTime <= 60);
        } else if (time === '60+') {
            filtered = filtered.filter((recipe) => recipe.cookingTime > 60);
        }
    }

    return filtered.length;
});

// 메서드
const loadRecipes = () => {
    // 실제로는 API 호출
    recipes.value = [
        {
            id: 1,
            title: '김치찌개',
            description: '매콤하고 시원한 김치찌개입니다.',
            image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400',
            category: '한식',
            difficulty: 'easy',
            cookingTime: 30,
            servings: 2,
            rating: 4.5,
            isFavorite: false
        },
        {
            id: 2,
            title: '파스타',
            description: '크림소스가 일품인 파스타입니다.',
            image: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=400',
            category: '양식',
            difficulty: 'medium',
            cookingTime: 25,
            servings: 2,
            rating: 4.2,
            isFavorite: true
        },
        {
            id: 3,
            title: '초밥',
            description: '신선한 생선으로 만든 초밥입니다.',
            image: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400',
            category: '일식',
            difficulty: 'hard',
            cookingTime: 45,
            servings: 4,
            rating: 4.8,
            isFavorite: false
        },
        {
            id: 4,
            title: '짜장면',
            description: '진한 춘장소스의 짜장면입니다.',
            image: 'https://images.unsplash.com/photo-1563379091339-03246963d4d8?w=400',
            category: '중식',
            difficulty: 'medium',
            cookingTime: 20,
            servings: 2,
            rating: 4.3,
            isFavorite: false
        },
        {
            id: 5,
            title: '치즈케이크',
            description: '부드럽고 달콤한 치즈케이크입니다.',
            image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400',
            category: '디저트',
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
            category: '한식',
            difficulty: 'easy',
            cookingTime: 15,
            servings: 3,
            rating: 4.4,
            isFavorite: false
        }
    ];
};

const refreshRecipes = () => {
    loadRecipes();
    toast.add({ severity: 'success', summary: '새로고침', detail: '레시피 목록이 업데이트되었습니다.', life: 3000 });
};

const applyFilter = () => {
    showFilterDialog.value = false;
    first.value = 0;
    toast.add({ severity: 'info', summary: '필터 적용', detail: '선택한 조건으로 레시피를 필터링했습니다.', life: 3000 });
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

const bookmarkRecipe = (recipeId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '레시피가 북마크되었습니다.', life: 3000 });
};

const viewRecipe = (recipeId) => {
    router.push(`/recipe/detail/${recipeId}`);
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 생명주기
onMounted(() => {
    loadRecipes();
});
</script>

<style scoped>
.recipe-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.recipe-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
</style>
