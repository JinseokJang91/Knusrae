<script setup lang="ts">
import { computed } from 'vue';
import AppFooter from './AppFooter.vue';
import AppTopbar from './AppTopbar.vue';

const containerClass = computed(() => {
    return {
        'layout-wrapper': true,
        'layout-horizontal': true
    };
});
</script>

<template>
    <div :class="containerClass">
        <app-topbar></app-topbar>
        <div class="layout-main-container">
            <div class="layout-main">
                <router-view></router-view>
            </div>
            <app-footer></app-footer>
        </div>
    </div>
    <Toast position="bottom-left" />
</template>
