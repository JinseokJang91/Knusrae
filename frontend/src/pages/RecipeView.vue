<template>
  <article v-if="recipe">
    <h1 class="text-xl font-bold">{{ recipe.title }}</h1>
    <p class="text-gray-600">{{ recipe.description }}</p>
    <h2 class="mt-4 font-semibold">재료</h2>
    <ul class="list-disc ml-6">
      <li v-for="(ing, idx) in recipe.ingredients" :key="idx">{{ ing }}</li>
    </ul>
    <h2 class="mt-4 font-semibold">만드는 법</h2>
    <ol class="list-decimal ml-6">
      <li v-for="(s, idx) in recipe.steps" :key="idx">{{ s }}</li>
    </ol>
    <TagChips :selected="recipe.tags" readonly class="mt-3" />
  </article>
  <p v-else>로딩중…</p>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useRecipes } from '../composables/useRecipes'
import TagChips from '../components/TagChips.vue'

const route = useRoute()
const { current, loadOne } = useRecipes()
const recipe = computed(() => current.value)

onMounted(() => loadOne(route.params.id as string))
</script>