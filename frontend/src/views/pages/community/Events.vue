<template>
    <div class="card">
        <div class="flex justify-content-between align-items-center mb-4">
            <h1 class="text-3xl font-bold text-900">이벤트</h1>
            <div class="flex gap-2">
                <Button icon="pi pi-refresh" label="새로고침" severity="secondary" @click="refreshEvents" />
                <Button icon="pi pi-calendar" label="달력 보기" severity="secondary" @click="showCalendar = !showCalendar" />
            </div>
        </div>

        <!-- 달력 뷰 토글 -->
        <div v-if="showCalendar" class="mb-4">
            <Card>
                <template #title>
                    <div class="flex align-items-center gap-2">
                        <i class="pi pi-calendar text-blue-500"></i>
                        <span>이벤트 달력</span>
                    </div>
                </template>
                <template #content>
                    <Calendar v-model="selectedDate" :events="calendarEvents" inline />
                </template>
            </Card>
        </div>

        <!-- 통계 카드 -->
        <div class="grid mb-4">
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalEvents }}</h3>
                                <p class="text-600 m-0">총 이벤트</p>
                            </div>
                            <i class="pi pi-calendar text-4xl text-blue-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ ongoingEvents }}</h3>
                                <p class="text-600 m-0">진행 중</p>
                            </div>
                            <i class="pi pi-play text-4xl text-green-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ upcomingEvents }}</h3>
                                <p class="text-600 m-0">예정</p>
                            </div>
                            <i class="pi pi-clock text-4xl text-orange-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="col-12 md:col-3">
                <Card class="stat-card">
                    <template #content>
                        <div class="flex align-items-center">
                            <div class="flex-1">
                                <h3 class="text-2xl font-bold text-900 m-0">{{ totalParticipants }}</h3>
                                <p class="text-600 m-0">총 참여자</p>
                            </div>
                            <i class="pi pi-users text-4xl text-purple-500"></i>
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 필터 -->
        <div class="flex flex-wrap gap-2 mb-4">
            <Button v-for="filter in filters" :key="filter.value" :label="filter.label" :severity="selectedFilter === filter.value ? 'primary' : 'secondary'" size="small" @click="filterEvents(filter.value)" />
            <Dropdown v-model="selectedSort" :options="sortOptions" optionLabel="label" optionValue="value" placeholder="정렬" class="w-auto" />
        </div>

        <!-- 이벤트 목록 -->
        <div class="grid">
            <div v-for="event in filteredEvents" :key="event.id" class="col-12 md:col-6 lg:col-4">
                <Card class="event-card h-full">
                    <template #header>
                        <div class="relative">
                            <img :src="event.image" :alt="event.title" class="w-full h-12rem object-cover" />
                            <div class="absolute top-0 right-0 m-2">
                                <Tag :value="event.status" :severity="getStatusSeverity(event.status)" />
                            </div>
                            <div class="absolute bottom-0 left-0 m-2">
                                <Tag :value="event.type" :severity="getTypeSeverity(event.type)" />
                            </div>
                        </div>
                    </template>
                    <template #title>
                        <h3 class="text-xl font-semibold text-900 m-0">{{ event.title }}</h3>
                    </template>
                    <template #content>
                        <p class="text-600 mb-3">{{ event.description }}</p>
                        <div class="flex flex-column gap-2 text-sm text-500 mb-3">
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-calendar"></i>
                                <span>{{ formatDate(event.startDate) }} ~ {{ formatDate(event.endDate) }}</span>
                            </div>
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-users"></i>
                                <span>{{ event.participants }}명 참여</span>
                            </div>
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-gift"></i>
                                <span>{{ event.reward }}</span>
                            </div>
                        </div>
                        <div v-if="event.conditions" class="mb-3">
                            <h4 class="text-900 font-medium mb-2">참여 조건</h4>
                            <p class="text-600 text-sm m-0">{{ event.conditions }}</p>
                        </div>
                    </template>
                    <template #footer>
                        <div class="flex gap-2">
                            <Button
                                :label="event.isParticipated ? '참여 완료' : '참여하기'"
                                :disabled="event.status !== 'ongoing' || event.isParticipated"
                                :class="event.isParticipated ? 'p-button-success' : 'p-button-primary'"
                                class="flex-1"
                                @click="participateEvent(event.id)"
                            />
                            <Button icon="pi pi-info-circle" severity="secondary" @click="viewEventDetail(event)" />
                        </div>
                    </template>
                </Card>
            </div>
        </div>

        <!-- 빈 상태 -->
        <div v-if="filteredEvents.length === 0" class="text-center py-8">
            <i class="pi pi-calendar text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-600 mb-2">이벤트가 없습니다</h3>
            <p class="text-600 mb-4">새로운 이벤트를 기다려주세요!</p>
        </div>

        <!-- 페이지네이션 -->
        <div v-if="filteredEvents.length > 0" class="flex justify-content-center mt-4">
            <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFilteredEvents" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
        </div>

        <!-- 이벤트 상세 다이얼로그 -->
        <Dialog v-model:visible="showEventDetail" :header="selectedEvent?.title" :style="{ width: '700px' }">
            <div v-if="selectedEvent" class="flex flex-column gap-4">
                <div class="flex align-items-center gap-3">
                    <img :src="selectedEvent.image" :alt="selectedEvent.title" class="w-8rem h-8rem object-cover border-round" />
                    <div class="flex-1">
                        <div class="flex gap-2 mb-2">
                            <Tag :value="selectedEvent.status" :severity="getStatusSeverity(selectedEvent.status)" />
                            <Tag :value="selectedEvent.type" :severity="getTypeSeverity(selectedEvent.type)" />
                        </div>
                        <div class="flex flex-column gap-1 text-sm text-500">
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-calendar"></i>
                                <span>{{ formatDate(selectedEvent.startDate) }} ~ {{ formatDate(selectedEvent.endDate) }}</span>
                            </div>
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-users"></i>
                                <span>{{ selectedEvent.participants }}명 참여</span>
                            </div>
                            <div class="flex align-items-center gap-1">
                                <i class="pi pi-gift"></i>
                                <span>{{ selectedEvent.reward }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div>
                    <h4 class="text-900 font-medium mb-2">이벤트 설명</h4>
                    <p class="text-600 m-0">{{ selectedEvent.description }}</p>
                </div>
                <div v-if="selectedEvent.conditions">
                    <h4 class="text-900 font-medium mb-2">참여 조건</h4>
                    <p class="text-600 m-0">{{ selectedEvent.conditions }}</p>
                </div>
                <div v-if="selectedEvent.details">
                    <h4 class="text-900 font-medium mb-2">상세 내용</h4>
                    <p class="text-600 m-0">{{ selectedEvent.details }}</p>
                </div>
            </div>
            <template #footer>
                <Button label="닫기" @click="showEventDetail = false" />
                <Button
                    :label="selectedEvent?.isParticipated ? '참여 완료' : '참여하기'"
                    :disabled="selectedEvent?.status !== 'ongoing' || selectedEvent?.isParticipated"
                    :class="selectedEvent?.isParticipated ? 'p-button-success' : 'p-button-primary'"
                    @click="participateEvent(selectedEvent?.id)"
                />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import Button from 'primevue/button';
import Calendar from 'primevue/calendar';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import Paginator from 'primevue/paginator';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const toast = useToast();

// 반응형 데이터
const events = ref([]);
const selectedFilter = ref('all');
const selectedSort = ref('latest');
const showCalendar = ref(false);
const showEventDetail = ref(false);
const selectedEvent = ref(null);
const selectedDate = ref(new Date());
const first = ref(0);
const rows = ref(12);

const filters = ref([
    { label: '전체', value: 'all' },
    { label: '진행 중', value: 'ongoing' },
    { label: '예정', value: 'upcoming' },
    { label: '종료', value: 'ended' }
]);

const sortOptions = ref([
    { label: '최신순', value: 'latest' },
    { label: '인기순', value: 'popular' },
    { label: '시작일순', value: 'startDate' },
    { label: '참여자순', value: 'participants' }
]);

// 계산된 속성
const totalEvents = computed(() => events.value.length);

const ongoingEvents = computed(() => {
    return events.value.filter((event) => event.status === 'ongoing').length;
});

const upcomingEvents = computed(() => {
    return events.value.filter((event) => event.status === 'upcoming').length;
});

const totalParticipants = computed(() => {
    return events.value.reduce((sum, event) => sum + event.participants, 0);
});

const calendarEvents = computed(() => {
    return events.value.map((event) => ({
        date: new Date(event.startDate),
        title: event.title,
        severity: getStatusSeverity(event.status)
    }));
});

const filteredEvents = computed(() => {
    let filtered = events.value;

    // 필터 적용
    if (selectedFilter.value !== 'all') {
        filtered = filtered.filter((event) => event.status === selectedFilter.value);
    }

    // 정렬 적용
    switch (selectedSort.value) {
        case 'popular':
            filtered = filtered.sort((a, b) => b.participants - a.participants);
            break;
        case 'startDate':
            filtered = filtered.sort((a, b) => new Date(a.startDate) - new Date(b.startDate));
            break;
        case 'participants':
            filtered = filtered.sort((a, b) => b.participants - a.participants);
            break;
        case 'latest':
        default:
            filtered = filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            break;
    }

    return filtered.slice(first.value, first.value + rows.value);
});

const totalFilteredEvents = computed(() => {
    let filtered = events.value;
    if (selectedFilter.value !== 'all') {
        filtered = filtered.filter((event) => event.status === selectedFilter.value);
    }
    return filtered.length;
});

// 메서드
const loadEvents = () => {
    events.value = [
        {
            id: 1,
            title: '신년 요리 챌린지',
            description: '새해를 맞아 새로운 요리에 도전해보세요! 매일 다른 요리를 만들어보고 인증샷을 올려주세요.',
            type: 'challenge',
            status: 'ongoing',
            startDate: '2024-01-01',
            endDate: '2024-01-31',
            participants: 245,
            reward: '요리 도구 세트',
            conditions: '매일 요리 인증샷 업로드',
            details: '매일 다른 요리를 만들어보고 사진을 올려주세요. 30일 동안 참여하시면 특별한 선물을 드립니다.',
            image: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400',
            createdAt: '2024-01-01T00:00:00Z',
            isParticipated: false
        },
        {
            id: 2,
            title: '김치찌개 레시피 공모전',
            description: '가장 맛있는 김치찌개 레시피를 공유해주세요! 창의적이고 맛있는 레시피를 기다립니다.',
            type: 'contest',
            status: 'ongoing',
            startDate: '2024-01-10',
            endDate: '2024-01-25',
            participants: 89,
            reward: '상금 10만원',
            conditions: '김치찌개 레시피 및 사진 업로드',
            details: '김치찌개 레시피를 자세히 적어주시고, 만든 요리 사진을 함께 올려주세요. 가장 창의적이고 맛있는 레시피를 선정합니다.',
            image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400',
            createdAt: '2024-01-10T00:00:00Z',
            isParticipated: true
        },
        {
            id: 3,
            title: '요리 초보를 위한 워크샵',
            description: '요리를 처음 시작하는 분들을 위한 특별한 워크샵입니다. 전문 셰프와 함께 기본기를 배워보세요.',
            type: 'workshop',
            status: 'upcoming',
            startDate: '2024-02-01',
            endDate: '2024-02-01',
            participants: 0,
            reward: '수료증 및 요리 도구',
            conditions: '사전 신청 필수',
            details: '요리 초보자를 위한 기본기 워크샵입니다. 칼 사용법부터 기본 양념까지 차근차근 배워보세요.',
            image: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400',
            createdAt: '2024-01-15T00:00:00Z',
            isParticipated: false
        },
        {
            id: 4,
            title: '디저트 만들기 대회',
            description: '달콤한 디저트를 만들어보세요! 가장 예쁘고 맛있는 디저트를 만든 분께 상품을 드립니다.',
            type: 'contest',
            status: 'upcoming',
            startDate: '2024-02-10',
            endDate: '2024-02-20',
            participants: 0,
            reward: '디저트 도구 세트',
            conditions: '디저트 제작 및 사진 업로드',
            details: '자유 주제로 디저트를 만들어보세요. 케이크, 쿠키, 푸딩 등 어떤 디저트든 환영합니다.',
            image: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400',
            createdAt: '2024-01-20T00:00:00Z',
            isParticipated: false
        },
        {
            id: 5,
            title: '건강한 요리 챌린지',
            description: '건강한 재료로 만든 요리를 공유해보세요! 영양가 높고 맛있는 건강 요리를 기다립니다.',
            type: 'challenge',
            status: 'ended',
            startDate: '2023-12-01',
            endDate: '2023-12-31',
            participants: 156,
            reward: '건강 식품 세트',
            conditions: '건강한 재료 사용',
            details: '건강한 재료를 사용한 요리를 만들어보세요. 신선한 채소, 단백질, 곡물 등을 활용한 요리를 기다립니다.',
            image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400',
            createdAt: '2023-12-01T00:00:00Z',
            isParticipated: false
        }
    ];
};

const refreshEvents = () => {
    loadEvents();
    toast.add({ severity: 'success', summary: '새로고침', detail: '이벤트 목록이 업데이트되었습니다.', life: 3000 });
};

const filterEvents = (filter) => {
    selectedFilter.value = filter;
    first.value = 0;
};

const participateEvent = (eventId) => {
    const event = events.value.find((e) => e.id === eventId);
    if (event && event.status === 'ongoing' && !event.isParticipated) {
        event.isParticipated = true;
        event.participants += 1;
        toast.add({ severity: 'success', summary: '참여 완료', detail: '이벤트에 참여하셨습니다!', life: 3000 });
    }
};

const viewEventDetail = (event) => {
    selectedEvent.value = event;
    showEventDetail.value = true;
};

const getStatusSeverity = (status) => {
    const severityMap = {
        ongoing: 'success',
        upcoming: 'warning',
        ended: 'secondary'
    };
    return severityMap[status] || 'primary';
};

const getTypeSeverity = (type) => {
    const severityMap = {
        challenge: 'info',
        contest: 'help',
        workshop: 'warning'
    };
    return severityMap[type] || 'primary';
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
    loadEvents();
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

.event-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.event-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
</style>
