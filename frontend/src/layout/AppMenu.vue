<script setup lang="ts">
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';

interface MenuItem {
    label?: string;
    icon?: string;
    to?: string;
    items?: MenuItem[];
    separator?: boolean;
}

const model = ref<MenuItem[]>([
    {
        label: '오늘의 레시피',
        icon: 'pi pi-fw pi-sun',
        to: '/recommend/today'
    },
    {
        label: '랭킹',
        icon: 'pi pi-fw pi-flag',
        to: '/ranking'
    },
    {
        label: '레시피',
        icon: 'pi pi-fw pi-list',
        to: '/recipe/category'
    },
    {
        label: '재료 관리',
        icon: 'pi pi-fw pi-box',
        to: '/ingredient/management'
    },
    {
        label: 'FAQ',
        icon: 'pi pi-fw pi-question-circle',
        to: '/faq'
    }
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item.label || i">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
