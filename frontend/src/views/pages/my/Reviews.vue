<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">후기 관리</h2>
            <div class="flex gap-2 items-center">
                <select v-model="rating" class="p-inputtext p-component">
                    <option value="all">전체 평점</option>
                    <option value="5">5점</option>
                    <option value="4">4점</option>
                    <option value="3">3점</option>
                    <option value="2">2점</option>
                    <option value="1">1점</option>
                </select>
                <input v-model="search" type="text" class="p-inputtext p-component" placeholder="후기 검색" />
            </div>
        </div>

        <div class="overflow-auto border rounded-md">
            <table class="w-full text-sm">
                <thead>
                    <tr class="bg-surface-100 text-left">
                        <th class="px-3 py-2 w-16">#</th>
                        <th class="px-3 py-2">후기</th>
                        <th class="px-3 py-2 w-24">평점</th>
                        <th class="px-3 py-2 w-40 text-right">액션</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(r, i) in filtered" :key="r.id" class="border-t">
                        <td class="px-3 py-2">{{ i + 1 }}</td>
                        <td class="px-3 py-2 truncate max-w-[40ch]" :title="r.content">{{ r.content }}</td>
                        <td class="px-3 py-2">{{ r.rating }}점</td>
                        <td class="px-3 py-2 text-right">
                            <button class="p-button p-component p-button-text">
                                <span class="pi pi-external-link"></span>
                            </button>
                            <button class="p-button p-component p-button-text text-red-600">
                                <span class="pi pi-trash"></span>
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

interface ReviewRow {
    id: number;
    content: string;
    rating: 1 | 2 | 3 | 4 | 5;
}

const rating = ref<'all' | '1' | '2' | '3' | '4' | '5'>('all');
const search = ref('');
const rows = ref<ReviewRow[]>([
    { id: 1, content: '정말 맛있어요!', rating: 5 },
    { id: 2, content: '괜찮았어요.', rating: 4 },
    { id: 3, content: '보통이에요', rating: 3 }
]);

const filtered = computed(() => {
    const q = search.value.trim();
    return rows.value.filter((r) => (rating.value === 'all' ? true : String(r.rating) === rating.value) && (!q || r.content.includes(q)));
});
</script>

<style scoped></style>
