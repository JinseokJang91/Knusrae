<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">재료 보관법</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshStorage" />
                <Button icon="pi pi-search" label="검색" severity="secondary" @click="showSearchDialog = true" />
                <Button icon="pi pi-plus" label="추가" severity="success" @click="showAddDialog = true" />
            </div>
        </div>

        <!-- 검색 다이얼로그 -->
        <Dialog v-model:visible="showSearchDialog" header="재료 검색" :style="{ width: '300px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">재료명</label>
                    <InputText v-model="searchQuery" placeholder="재료 이름을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">카테고리</label>
                    <Dropdown v-model="searchCategory" :options="categories" optionLabel="name" optionValue="value" placeholder="전체 카테고리" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="showSearchDialog = false" />
                <Button label="검색" @click="performSearch" />
            </template>
        </Dialog>

        <!-- 추가 다이얼로그 -->
        <Dialog v-model:visible="showAddDialog" header="재료 보관법 추가" :style="{ width: '500px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">재료명 *</label>
                    <InputText v-model="newIngredient.name" placeholder="재료 이름을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">카테고리 *</label>
                    <Dropdown v-model="newIngredient.category" :options="categories" optionLabel="name" optionValue="value" placeholder="카테고리 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">보관 방법 *</label>
                    <Textarea v-model="newIngredient.storageMethod" placeholder="보관 방법을 입력하세요" rows="3" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">보관 기간</label>
                    <InputText v-model="newIngredient.storagePeriod" placeholder="예: 3-5일" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">주의사항</label>
                    <Textarea v-model="newIngredient.notes" placeholder="주의사항을 입력하세요" rows="2" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="cancelAdd" />
                <Button label="추가" @click="addIngredient" />
            </template>
        </Dialog>

        <!-- 통계 카드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalIngredients }}</h3>
                                <p class="text-600 m-0">총 재료</p>
                            </div>
                            <i class="pi pi-box text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ categoryCount }}</h3>
                                <p class="text-600 m-0">카테고리</p>
                            </div>
                            <i class="pi pi-tags text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ refrigeratedCount }}</h3>
                                <p class="text-600 m-0">냉장 보관</p>
                            </div>
                            <i class="pi pi-snowflake text-4xl text-cyan-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ frozenCount }}</h3>
                                <p class="text-600 m-0">냉동 보관</p>
                            </div>
                            <i class="pi pi-circle text-4xl text-blue-300"></i>
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

        <!-- 재료 목록 -->
        <div class="grid">
            <div v-for="ingredient in filteredIngredients" :key="ingredient.id" class="col-12 md:col-6 lg:col-4">
                <Card class="ingredient-card h-full">
                    <template #header>
                        <div class="relative">
                            <img :src="ingredient.image" :alt="ingredient.name" class="w-full h-12rem object-cover" />
                            <div class="absolute top-0 right-0 m-2">
                                <Tag :value="ingredient.category" :severity="getCategorySeverity(ingredient.category)" />
                            </div>
                            <div class="absolute bottom-0 left-0 m-2">
                                <div class="flex gap-1">
                                    <Button v-if="ingredient.storageType === 'refrigerated'" icon="pi pi-snowflake" size="small" severity="info" rounded v-tooltip="'냉장 보관'" />
                                    <Button v-if="ingredient.storageType === 'frozen'" icon="pi pi-circle" size="small" severity="secondary" rounded v-tooltip="'냉동 보관'" />
                                    <Button v-if="ingredient.storageType === 'room'" icon="pi pi-home" size="small" severity="warning" rounded v-tooltip="'실온 보관'" />
                                </div>
                            </div>
                        </div>
                    </template>
                    <template #title>
                        <h3 class="text-xl font-semibold text-900 m-0">{{ ingredient.name }}</h3>
                    </template>
                    <template #content>
                        <div class="mb-3">
                            <h4 class="text-900 font-medium mb-2">보관 방법</h4>
                            <p class="text-600 text-sm m-0">{{ ingredient.storageMethod }}</p>
                        </div>
                        <div class="mb-3" v-if="ingredient.storagePeriod">
                            <h4 class="text-900 font-medium mb-2">보관 기간</h4>
                            <p class="text-600 text-sm m-0">{{ ingredient.storagePeriod }}</p>
                        </div>
                        <div v-if="ingredient.notes">
                            <h4 class="text-900 font-medium mb-2">주의사항</h4>
                            <p class="text-600 text-sm m-0">{{ ingredient.notes }}</p>
                        </div>
                    </template>
                    <template #footer>
                        <div class="flex gap-2">
                            <Button label="수정" severity="secondary" size="small" @click="editIngredient(ingredient)" />
                            <Button label="삭제" severity="danger" size="small" @click="deleteIngredient(ingredient.id)" />
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="filteredIngredients.length === 0" class="text-center py-8">
            <i class="pi pi-box text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-600 mb-2">재료가 없습니다</h3>
            <p class="text-600 mb-4">새로운 재료 보관법을 추가해보세요!</p>
            <Button label="재료 추가" @click="showAddDialog = true" />
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredIngredients.length > 0" class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredIngredients" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>
    </div>
</template>

<script setup>
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Paginator from 'primevue/paginator';
import Tag from 'primevue/tag';
import Textarea from 'primevue/textarea';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const toast = useToast();

// 반응형 데이터
const ingredients = ref([]);
const selectedCategory = ref(null);
const searchResults = ref([]);
const showSearchDialog = ref(false);
const showAddDialog = ref(false);
const searchQuery = ref('');
const searchCategory = ref(null);
const first = ref(0);
const rows = ref(12);

const newIngredient = ref({
    name: '',
    category: '',
    storageMethod: '',
    storagePeriod: '',
    notes: ''
});

const categories = ref([
    { name: '채소', value: 'vegetable' },
    { name: '육류', value: 'meat' },
    { name: '해산물', value: 'seafood' },
    { name: '유제품', value: 'dairy' },
    { name: '곡물', value: 'grain' },
    { name: '과일', value: 'fruit' },
    { name: '조미료', value: 'seasoning' },
    { name: '기타', value: 'other' }
]);

// 계산된 속성
const totalIngredients = computed(() => ingredients.value.length);

const categoryCount = computed(() => {
    const uniqueCategories = new Set(ingredients.value.map((ingredient) => ingredient.category));
    return uniqueCategories.size;
});

const refrigeratedCount = computed(() => {
    return ingredients.value.filter((ingredient) => ingredient.storageType === 'refrigerated').length;
});

const frozenCount = computed(() => {
    return ingredients.value.filter((ingredient) => ingredient.storageType === 'frozen').length;
});

const filteredIngredients = computed(() => {
    let filtered = searchResults.value.length > 0 ? searchResults.value : ingredients.value;

    if (selectedCategory.value) {
        filtered = filtered.filter((ingredient) => ingredient.category === selectedCategory.value);
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalFilteredIngredients = computed(() => {
    let filtered = searchResults.value.length > 0 ? searchResults.value : ingredients.value;
    if (selectedCategory.value) {
        filtered = filtered.filter((ingredient) => ingredient.category === selectedCategory.value);
    }
    return filtered.length;
});

// 메서드
const loadIngredients = () => {
    ingredients.value = [
        {
            id: 1,
            name: '양파',
            category: 'vegetable',
            storageMethod: '통풍이 잘 되는 서늘한 곳에 보관하거나 냉장고의 채소칸에 보관합니다.',
            storagePeriod: '2-3개월',
            storageType: 'room',
            notes: '절단된 양파는 밀폐용기에 담아 냉장보관하세요.',
            image: 'https://images.unsplash.com/photo-1518977956812-cd3dbadaaf31?w=400'
        },
        {
            id: 2,
            name: '닭고기',
            category: 'meat',
            storageMethod: '냉장고에서 0-4°C로 보관하며, 구매 후 1-2일 내에 사용하세요.',
            storagePeriod: '1-2일',
            storageType: 'refrigerated',
            notes: '냉동보관 시 3-4개월까지 보관 가능합니다.',
            image: 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400'
        },
        {
            id: 3,
            name: '생선',
            category: 'seafood',
            storageMethod: '냉장고에서 0-2°C로 보관하며, 얼음과 함께 보관하면 더 오래 보관할 수 있습니다.',
            storagePeriod: '1-2일',
            storageType: 'refrigerated',
            notes: '냉동보관 시 2-3개월까지 보관 가능합니다.',
            image: 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400'
        },
        {
            id: 4,
            name: '우유',
            category: 'dairy',
            storageMethod: '냉장고에서 2-4°C로 보관하며, 개봉 후에는 뚜껑을 꼭 닫아 보관하세요.',
            storagePeriod: '5-7일',
            storageType: 'refrigerated',
            notes: '냉동보관은 권장하지 않습니다.',
            image: 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400'
        },
        {
            id: 5,
            name: '쌀',
            category: 'grain',
            storageMethod: '밀폐용기에 담아 서늘하고 건조한 곳에 보관하세요.',
            storagePeriod: '1-2년',
            storageType: 'room',
            notes: '습기와 벌레를 방지하기 위해 밀폐용기 사용을 권장합니다.',
            image: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400'
        },
        {
            id: 6,
            name: '사과',
            category: 'fruit',
            storageMethod: '냉장고의 과일칸에 보관하거나 서늘한 곳에 보관하세요.',
            storagePeriod: '2-4주',
            storageType: 'refrigerated',
            notes: '다른 과일과 함께 보관하면 익는 속도가 빨라집니다.',
            image: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400'
        }
    ];
};

const refreshStorage = () => {
    loadIngredients();
    toast.add({ severity: 'success', summary: '새로고침', detail: '재료 보관법 정보가 업데이트되었습니다.', life: 3000 });
};

const filterByCategory = (category) => {
    selectedCategory.value = category;
    first.value = 0;
};

const performSearch = () => {
    showSearchDialog.value = false;

    let results = ingredients.value;

    if (searchQuery.value) {
        results = results.filter((ingredient) => ingredient.name.toLowerCase().includes(searchQuery.value.toLowerCase()) || ingredient.storageMethod.toLowerCase().includes(searchQuery.value.toLowerCase()));
    }

    if (searchCategory.value) {
        results = results.filter((ingredient) => ingredient.category === searchCategory.value);
    }

    searchResults.value = results;
    first.value = 0;

    toast.add({
        severity: 'info',
        summary: '검색 완료',
        detail: `${results.length}개의 재료를 찾았습니다.`,
        life: 3000
    });
};

const getCategorySeverity = (category) => {
    const severityMap = {
        vegetable: 'success',
        meat: 'danger',
        seafood: 'info',
        dairy: 'warning',
        grain: 'secondary',
        fruit: 'help',
        seasoning: 'contrast',
        other: 'primary'
    };
    return severityMap[category] || 'primary';
};

const addIngredient = () => {
    if (!newIngredient.value.name || !newIngredient.value.category || !newIngredient.value.storageMethod) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '필수 항목을 모두 입력해주세요.', life: 3000 });
        return;
    }

    const newId = Math.max(...ingredients.value.map((i) => i.id)) + 1;
    const ingredient = {
        id: newId,
        name: newIngredient.value.name,
        category: newIngredient.value.category,
        storageMethod: newIngredient.value.storageMethod,
        storagePeriod: newIngredient.value.storagePeriod,
        storageType: 'room', // 기본값
        notes: newIngredient.value.notes,
        image: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=400' // 기본 이미지
    };

    ingredients.value.unshift(ingredient);
    cancelAdd();
    toast.add({ severity: 'success', summary: '추가 완료', detail: '새로운 재료 보관법이 추가되었습니다.', life: 3000 });
};

const cancelAdd = () => {
    newIngredient.value = {
        name: '',
        category: '',
        storageMethod: '',
        storagePeriod: '',
        notes: ''
    };
    showAddDialog.value = false;
};

const editIngredient = (ingredient) => {
    // 편집 기능 구현
    toast.add({ severity: 'info', summary: '편집', detail: '편집 기능은 준비 중입니다.', life: 3000 });
};

const deleteIngredient = (ingredientId) => {
    const index = ingredients.value.findIndex((ingredient) => ingredient.id === ingredientId);
    if (index > -1) {
        ingredients.value.splice(index, 1);
        toast.add({ severity: 'success', summary: '삭제 완료', detail: '재료가 삭제되었습니다.', life: 3000 });
    }
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 생명주기
onMounted(() => {
    loadIngredients();
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

.ingredient-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.ingredient-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
</style>
