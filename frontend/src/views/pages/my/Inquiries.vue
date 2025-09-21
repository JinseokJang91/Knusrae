<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">1:1 문의 내역</h2>
            <div class="flex gap-2 items-center">
                <select v-model="status" class="p-inputtext p-component">
                    <option value="all">전체 상태</option>
                    <option value="pending">대기</option>
                    <option value="answered">답변완료</option>
                </select>
                <input v-model="search" type="text" class="p-inputtext p-component" placeholder="제목 검색" />
            </div>
        </div>

        <div class="overflow-auto border rounded-md">
            <table class="w-full text-sm">
                <thead>
                    <tr class="bg-surface-100 text-left">
                        <th class="px-3 py-2 w-16">#</th>
                        <th class="px-3 py-2">제목</th>
                        <th class="px-3 py-2 w-28">상태</th>
                        <th class="px-3 py-2 w-40 text-right">액션</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(q, i) in filtered" :key="q.id" class="border-t">
                        <td class="px-3 py-2">{{ i + 1 }}</td>
                        <td class="px-3 py-2">{{ q.title }}</td>
                        <td class="px-3 py-2">
                            <span class="px-2 py-1 rounded text-xs" :class="q.status === 'pending' ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'">{{ q.status === 'pending' ? '대기' : '답변완료' }}</span>
                        </td>
                        <td class="px-3 py-2 text-right">
                            <button class="p-button p-component p-button-text">
                                <span class="pi pi-eye"></span>
                            </button>
                            <button class="p-button p-component p-button-text">
                                <span class="pi pi-comment"></span>
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

type InquiryStatus = 'pending' | 'answered';
interface InquiryRow {
    id: number;
    title: string;
    status: InquiryStatus;
}

const status = ref<'all' | InquiryStatus>('all');
const search = ref('');
const rows = ref<InquiryRow[]>([
    { id: 1, title: '배송 관련 문의', status: 'pending' },
    { id: 2, title: '레시피 오류 제보', status: 'answered' }
]);

const filtered = computed(() => {
    const q = search.value.trim();
    return rows.value.filter((r) => (status.value === 'all' ? true : r.status === status.value) && (!q || r.title.includes(q)));
});
</script>

<style scoped></style>
