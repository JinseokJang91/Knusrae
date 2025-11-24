<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: '추천',
        items: [
            {
                label: '오늘의 레시피',
                icon: 'pi pi-fw pi-sun',
                to: '/recommend/today'
            },
            {
                label: '주간 랭킹',
                icon: 'pi pi-fw pi-flag',
                to: '/recommend/weekly'
            },
            {
                label: '월간 랭킹',
                icon: 'pi pi-fw pi-flag-fill',
                to: '/recommend/monthly'
            }
        ]
    },
    {
        label: '레시피',
        items: [
            {
                label: '카테고리',
                icon: 'pi pi-fw pi-list',
                to: '/recipe/category'
            },
            {
                label: '재료 보관법',
                icon: 'pi pi-fw pi-box',
                to: '/recipe/storage'
            },
            {
                label: '재료 손질법',
                icon: 'pi pi-fw pi-clipboard',
                to: '/recipe/preparation'
            }
        ]
    },
    {
        label: '소통',
        items: [
            {
                label: '요리 후기',
                icon: 'pi pi-fw pi-receipt',
                to: '/community/reviews'
            },
            {
                label: '자유 게시판',
                icon: 'pi pi-fw pi-warehouse',
                to: '/community/board'
            },
            {
                label: '이벤트',
                icon: 'pi pi-fw pi-lightbulb',
                to: '/community/events'
            },
            {
                label: '질문과 답변',
                icon: 'pi pi-fw pi-question-circle',
                to: '/community/qna'
            }
        ]
    }
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
