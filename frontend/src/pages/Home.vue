<template>
  <section>
    <h1 class="text-2xl font-bold mb-2">어서오세요 👋</h1>
    <p>검색하거나 최신 레시피를 확인해보세요.</p>
    <SearchBar class="mt-4" @search="onSearch" />
    <RecipeList class="mt-6" :recipes="recipes" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRecipes } from '../composables/useRecipes'
import SearchBar from '../components/SearchBar.vue'
import RecipeList from '../components/RecipeList.vue'

const { items, fetch } = useRecipes()
const recipes = items

onMounted(() => fetch({ page: 1, pageSize: 20 }))

function onSearch(q: string) { fetch({ q, page: 1, pageSize: 20 }) }
</script>