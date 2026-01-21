<template>
    <div class="comments-content">
        <div class="flex items-center justify-between mb-4">
            <div class="flex gap-2 items-center ml-auto">
                <select v-model="filter" class="p-inputtext p-component">
                    <option value="all">전체</option>
                    <option value="recipe">레시피</option>
                    <option value="review">후기</option>
                </select>
                <input v-model="search" type="text" class="p-inputtext p-component" placeholder="내용 검색" />
            </div>
        </div>

        <div class="overflow-auto border rounded-md">
            <table class="w-full text-sm">
                <thead>
                    <tr class="bg-surface-100 text-left">
                        <th class="px-3 py-2 w-16">#</th>
                        <th class="px-3 py-2">내용</th>
                        <th class="px-3 py-2 w-28">타입</th>
                        <th class="px-3 py-2 w-40 text-right">액션</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(c, i) in filtered" :key="c.id" class="border-t">
                        <td class="px-3 py-2">{{ i + 1 }}</td>
                        <td class="px-3 py-2 truncate max-w-[40ch]" :title="c.content">{{ c.content }}</td>
                        <td class="px-3 py-2">{{ c.type }}</td>
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

type CommentType = 'recipe' | 'review';
interface CommentRow {
    id: number;
    content: string;
    type: CommentType;
}

const filter = ref<'all' | CommentType>('all');
const search = ref('');
const rows = ref<CommentRow[]>([
    { id: 1, content: '맛있게 잘 만들어 먹었어요!', type: 'review' },
    { id: 2, content: '양념 비율이 딱 좋아요', type: 'recipe' }
]);

const filtered = computed(() => {
    const q = search.value.trim();
    return rows.value.filter((r) => (filter.value === 'all' ? true : r.type === filter.value) && (!q || r.content.includes(q)));
});
</script>

<style scoped></style>
