<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">재료 손질법</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshPreparation" />
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

        <!-- 추가 다이얼로그 -->
        <Dialog v-model:visible="showAddDialog" header="재료 손질법 추가" :style="{ width: '600px' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block text-900 font-medium mb-2">재료명 *</label>
                    <InputText v-model="newPreparation.name" placeholder="재료 이름을 입력하세요" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">카테고리 *</label>
                    <Dropdown v-model="newPreparation.category" :options="categories" optionLabel="name" optionValue="value" placeholder="카테고리 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">난이도 *</label>
                    <Dropdown v-model="newPreparation.difficulty" :options="difficulties" optionLabel="name" optionValue="value" placeholder="난이도 선택" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">손질 방법 *</label>
                    <Textarea v-model="newPreparation.method" placeholder="손질 방법을 단계별로 입력하세요" rows="4" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">소요 시간</label>
                    <InputText v-model="newPreparation.time" placeholder="예: 10분" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">필요한 도구</label>
                    <InputText v-model="newPreparation.tools" placeholder="예: 칼, 도마, 그릇" class="w-full" />
                </div>
                <div>
                    <label class="block text-900 font-medium mb-2">팁</label>
                    <Textarea v-model="newPreparation.tips" placeholder="손질 팁을 입력하세요" rows="2" class="w-full" />
                </div>
            </div>
            <template #footer>
                <Button label="취소" severity="secondary" @click="cancelAdd" />
                <Button label="추가" @click="addPreparation" />
            </template>
        </Dialog>

        <!-- 통계 카드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalPreparations }}</h3>
                                <p class="text-600 m-0">총 손질법</p>
                            </div>
                            <i class="pi pi-cut text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ easyCount }}</h3>
                                <p class="text-600 m-0">쉬운 손질법</p>
                            </div>
                            <i class="pi pi-check-circle text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ mediumCount }}</h3>
                                <p class="text-600 m-0">보통 손질법</p>
                            </div>
                            <i class="pi pi-exclamation-triangle text-4xl text-orange-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ hardCount }}</h3>
                                <p class="text-600 m-0">어려운 손질법</p>
                            </div>
                            <i class="pi pi-times-circle text-4xl text-red-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 필터 -->
        <div class="flex flex-wrap gap-2 mb-4">
            <Button v-for="category in categories" :key="category.value" :label="category.name" :severity="selectedCategory === category.value ? 'primary' : 'secondary'" size="small" @click="filterByCategory(category.value)" />
            <Button label="전체" :severity="selectedCategory === null ? 'primary' : 'secondary'" size="small" @click="filterByCategory(null)" />
        </div>

        <!-- 재료 손질법 목록 -->
        <div class="grid">
            <div v-for="preparation in filteredPreparations" :key="preparation.id" class="col-12 md:col-6 lg:col-4">
                <Card class="preparation-card h-full">
                    <template #header>
                        <div class="relative">
                            <img :src="preparation.image" :alt="preparation.name" class="w-full h-12rem object-cover" />
                            <div class="absolute top-0 right-0 m-2">
                                <Tag :value="preparation.category" :severity="getCategorySeverity(preparation.category)" />
                            </div>
                            <div class="absolute bottom-0 left-0 m-2">
                                <Tag :value="getDifficultyName(preparation.difficulty)" :severity="getDifficultySeverity(preparation.difficulty)" />
                            </div>
                        </div>
                    </template>
                    <template #title>
                        <div class="flex justify-content-between align-items-start">
                            <h3 class="text-xl font-semibold text-900 m-0">{{ preparation.name }}</h3>
                            <div class="flex align-items-center gap-1 text-sm text-500">
                                <i class="pi pi-clock"></i>
                                <span>{{ preparation.time }}</span>
                            </div>
                        </div>
                    </template>
                    <template #content>
                        <div class="mb-3">
                            <h4 class="text-900 font-medium mb-2">손질 방법</h4>
                            <p class="text-600 text-sm m-0">{{ preparation.method }}</p>
                        </div>
                        <div class="mb-3" v-if="preparation.tools">
                            <h4 class="text-900 font-medium mb-2">필요한 도구</h4>
                            <p class="text-600 text-sm m-0">{{ preparation.tools }}</p>
                        </div>
                        <div v-if="preparation.tips">
                            <h4 class="text-900 font-medium mb-2">팁</h4>
                            <p class="text-600 text-sm m-0">{{ preparation.tips }}</p>
                        </div>
                    </template>
                    <template #footer>
                        <div class="flex gap-2">
                            <Button label="상세보기" class="flex-1" @click="viewPreparation(preparation)" />
                            <Button icon="pi pi-pencil" severity="secondary" size="small" @click="editPreparation(preparation)" />
                            <Button icon="pi pi-trash" severity="danger" size="small" @click="deletePreparation(preparation.id)" />
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="filteredPreparations.length === 0" class="text-center py-8">
            <i class="pi pi-cut text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-600 mb-2">손질법이 없습니다</h3>
            <p class="text-600 mb-4">새로운 재료 손질법을 추가해보세요!</p>
            <Button label="손질법 추가" @click="showAddDialog = true" />
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredPreparations.length > 0" class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredPreparations" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>

        <!-- 상세보기 다이얼로그 -->
        <Dialog v-model:visible="showDetailDialog" :header="selectedPreparation?.name" :style="{ width: '700px' }">
            <div v-if="selectedPreparation" class="flex flex-column gap-4">
                <div class="flex align-items-center gap-3">
                    <img :src="selectedPreparation.image" :alt="selectedPreparation.name" class="w-8rem h-8rem object-cover border-round" />
                    <div class="flex-1">
                        <h3 class="text-2xl font-semibold text-900 m-0 mb-2">{{ selectedPreparation.name }}</h3>
                        <div class="flex gap-2 mb-2">
                            <Tag :value="selectedPreparation.category" :severity="getCategorySeverity(selectedPreparation.category)" />
                            <Tag :value="getDifficultyName(selectedPreparation.difficulty)" :severity="getDifficultySeverity(selectedPreparation.difficulty)" />
                        </div>
                        <div class="flex align-items-center gap-3 text-sm text-500">
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-clock"></i>
                                <span>{{ selectedPreparation.time }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div>
                    <h4 class="text-900 font-medium mb-2">손질 방법</h4>
                    <p class="text-600 m-0">{{ selectedPreparation.method }}</p>
                </div>
                <div v-if="selectedPreparation.tools">
                    <h4 class="text-900 font-medium mb-2">필요한 도구</h4>
                    <p class="text-600 m-0">{{ selectedPreparation.tools }}</p>
                </div>
                <div v-if="selectedPreparation.tips">
                    <h4 class="text-900 font-medium mb-2">팁</h4>
                    <p class="text-600 m-0">{{ selectedPreparation.tips }}</p>
                </div>
            </div>
            <template #footer>
                <Button label="닫기" @click="showDetailDialog = false" />
            </template>
        </Dialog>
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
const preparations = ref([]);
const selectedCategory = ref(null);
const searchResults = ref([]);
const showSearchDialog = ref(false);
const showAddDialog = ref(false);
const showDetailDialog = ref(false);
const selectedPreparation = ref(null);
const searchQuery = ref('');
const searchCategory = ref(null);
const searchDifficulty = ref(null);
const first = ref(0);
const rows = ref(12);

const newPreparation = ref({
    name: '',
    category: '',
    difficulty: '',
    method: '',
    time: '',
    tools: '',
    tips: ''
});

const categories = ref([
    { name: '채소', value: 'vegetable' },
    { name: '육류', value: 'meat' },
    { name: '해산물', value: 'seafood' },
    { name: '과일', value: 'fruit' },
    { name: '기타', value: 'other' }
]);

const difficulties = ref([
    { name: '쉬움', value: 'easy' },
    { name: '보통', value: 'medium' },
    { name: '어려움', value: 'hard' }
]);

// 계산된 속성
const totalPreparations = computed(() => preparations.value.length);

const easyCount = computed(() => {
    return preparations.value.filter((prep) => prep.difficulty === 'easy').length;
});

const mediumCount = computed(() => {
    return preparations.value.filter((prep) => prep.difficulty === 'medium').length;
});

const hardCount = computed(() => {
    return preparations.value.filter((prep) => prep.difficulty === 'hard').length;
});

const filteredPreparations = computed(() => {
    let filtered = searchResults.value.length > 0 ? searchResults.value : preparations.value;

    if (selectedCategory.value) {
        filtered = filtered.filter((preparation) => preparation.category === selectedCategory.value);
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalFilteredPreparations = computed(() => {
    let filtered = searchResults.value.length > 0 ? searchResults.value : preparations.value;
    if (selectedCategory.value) {
        filtered = filtered.filter((preparation) => preparation.category === selectedCategory.value);
    }
    return filtered.length;
});

// 메서드
const loadPreparations = () => {
    preparations.value = [
        {
            id: 1,
            name: '양파 다지기',
            category: 'vegetable',
            difficulty: 'easy',
            method: '1. 양파의 꼭지와 뿌리 부분을 제거합니다.\n2. 양파를 반으로 자릅니다.\n3. 껍질을 벗기고 얇게 썬 후 가로로 자릅니다.\n4. 세로로 자르면 다진 양파가 완성됩니다.',
            time: '5분',
            tools: '칼, 도마',
            tips: '양파를 찬물에 담가두면 눈물이 덜 납니다.',
            image: 'https://images.unsplash.com/photo-1518977956812-cd3dbadaaf31?w=400'
        },
        {
            id: 2,
            name: '닭고기 손질',
            category: 'meat',
            difficulty: 'medium',
            method: '1. 닭고기를 흐르는 물에 깨끗이 씻습니다.\n2. 불필요한 지방과 껍질을 제거합니다.\n3. 원하는 크기로 자릅니다.\n4. 소금과 후추로 간을 맞춥니다.',
            time: '15분',
            tools: '칼, 도마, 키친타월',
            tips: '닭고기는 충분히 익혀야 식중독을 방지할 수 있습니다.',
            image: 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400'
        },
        {
            id: 3,
            name: '생선 비늘 제거',
            category: 'seafood',
            difficulty: 'hard',
            method: '1. 생선을 찬물에 씻어냅니다.\n2. 비늘 제거기를 사용하여 꼬리에서 머리 방향으로 긁어냅니다.\n3. 모든 비늘이 제거될 때까지 반복합니다.\n4. 깨끗한 물로 헹구어냅니다.',
            time: '20분',
            tools: '비늘 제거기, 칼, 도마',
            tips: '비늘 제거 시 물을 뿌리면 비늘이 잘 떨어집니다.',
            image: 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400'
        },
        {
            id: 4,
            name: '사과 껍질 벗기기',
            category: 'fruit',
            difficulty: 'easy',
            method: '1. 사과를 깨끗이 씻습니다.\n2. 꼭지 부분을 제거합니다.\n3. 껍질 벗기기를 사용하여 껍질을 벗깁니다.\n4. 씨 부분을 제거하고 원하는 모양으로 자릅니다.',
            time: '3분',
            tools: '껍질 벗기기, 칼, 도마',
            tips: '껍질을 벗긴 사과는 레몬즙을 뿌리면 갈변을 방지할 수 있습니다.',
            image: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400'
        },
        {
            id: 5,
            name: '마늘 으깨기',
            category: 'vegetable',
            difficulty: 'easy',
            method: '1. 마늘의 껍질을 벗깁니다.\n2. 칼의 넓은 면으로 마늘을 눌러 으깹니다.\n3. 칼로 다지면 마늘 으깨기가 완성됩니다.',
            time: '2분',
            tools: '칼, 도마',
            tips: '마늘을 미리 으깨두면 향이 더 잘 나옵니다.',
            image: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400'
        },
        {
            id: 6,
            name: '새우 등줄기 제거',
            category: 'seafood',
            difficulty: 'medium',
            method: '1. 새우의 머리와 껍질을 제거합니다.\n2. 등 부분에 칼로 얕게 칼집을 냅니다.\n3. 등줄기를 제거합니다.\n4. 깨끗한 물로 헹구어냅니다.',
            time: '10분',
            tools: '칼, 도마',
            tips: '새우는 신선할수록 등줄기가 잘 제거됩니다.',
            image: 'https://images.unsplash.com/photo-1553909489-cd47e0ef937f?w=400'
        }
    ];
};

const refreshPreparation = () => {
    loadPreparations();
    toast.add({ severity: 'success', summary: '새로고침', detail: '재료 손질법 정보가 업데이트되었습니다.', life: 3000 });
};

const filterByCategory = (category) => {
    selectedCategory.value = category;
    first.value = 0;
};

const performSearch = () => {
    showSearchDialog.value = false;

    let results = preparations.value;

    if (searchQuery.value) {
        results = results.filter((preparation) => preparation.name.toLowerCase().includes(searchQuery.value.toLowerCase()) || preparation.method.toLowerCase().includes(searchQuery.value.toLowerCase()));
    }

    if (searchCategory.value) {
        results = results.filter((preparation) => preparation.category === searchCategory.value);
    }

    if (searchDifficulty.value) {
        results = results.filter((preparation) => preparation.difficulty === searchDifficulty.value);
    }

    searchResults.value = results;
    first.value = 0;

    toast.add({
        severity: 'info',
        summary: '검색 완료',
        detail: `${results.length}개의 손질법을 찾았습니다.`,
        life: 3000
    });
};

const getCategorySeverity = (category) => {
    const severityMap = {
        vegetable: 'success',
        meat: 'danger',
        seafood: 'info',
        fruit: 'help',
        other: 'primary'
    };
    return severityMap[category] || 'primary';
};

const getDifficultyName = (difficulty) => {
    const difficultyMap = {
        easy: '쉬움',
        medium: '보통',
        hard: '어려움'
    };
    return difficultyMap[difficulty] || difficulty;
};

const getDifficultySeverity = (difficulty) => {
    const severityMap = {
        easy: 'success',
        medium: 'warning',
        hard: 'danger'
    };
    return severityMap[difficulty] || 'primary';
};

const addPreparation = () => {
    if (!newPreparation.value.name || !newPreparation.value.category || !newPreparation.value.difficulty || !newPreparation.value.method) {
        toast.add({ severity: 'error', summary: '입력 오류', detail: '필수 항목을 모두 입력해주세요.', life: 3000 });
        return;
    }

    const newId = Math.max(...preparations.value.map((p) => p.id)) + 1;
    const preparation = {
        id: newId,
        name: newPreparation.value.name,
        category: newPreparation.value.category,
        difficulty: newPreparation.value.difficulty,
        method: newPreparation.value.method,
        time: newPreparation.value.time,
        tools: newPreparation.value.tools,
        tips: newPreparation.value.tips,
        image: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=400' // 기본 이미지
    };

    preparations.value.unshift(preparation);
    cancelAdd();
    toast.add({ severity: 'success', summary: '추가 완료', detail: '새로운 재료 손질법이 추가되었습니다.', life: 3000 });
};

const cancelAdd = () => {
    newPreparation.value = {
        name: '',
        category: '',
        difficulty: '',
        method: '',
        time: '',
        tools: '',
        tips: ''
    };
    showAddDialog.value = false;
};

const viewPreparation = (preparation) => {
    selectedPreparation.value = preparation;
    showDetailDialog.value = true;
};

const editPreparation = (preparation) => {
    // 편집 기능 구현
    toast.add({ severity: 'info', summary: '편집', detail: '편집 기능은 준비 중입니다.', life: 3000 });
};

const deletePreparation = (preparationId) => {
    const index = preparations.value.findIndex((preparation) => preparation.id === preparationId);
    if (index > -1) {
        preparations.value.splice(index, 1);
        toast.add({ severity: 'success', summary: '삭제 완료', detail: '손질법이 삭제되었습니다.', life: 3000 });
    }
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 생명주기
onMounted(() => {
    loadPreparations();
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

.preparation-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.preparation-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
</style>
