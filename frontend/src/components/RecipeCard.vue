<template>
  <article class="border rounded p-3">
    <RouterLink :to="{ name: 'recipe-view', params: { id: recipe.id } }">
      <h3 class="font-semibold">{{ recipe.title }}</h3>
      <p class="text-gray-600 line-clamp-2">{{ recipe.description }}</p>
      <div class="text-sm mt-2">태그: {{ recipe.tags.join(', ') }}</div>
    </RouterLink>
  </article>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
interface Recipe { id: string; title: string; description?: string; tags: string[] }
const props = defineProps<{ recipe: Recipe }>()
</script>