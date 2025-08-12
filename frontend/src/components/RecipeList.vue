<template>
  <div class="grid gap-3 md:grid-cols-2">
    <RecipeCard v-for="r in recipes" :key="r.id" :recipe="r" />
  </div>
</template>

<script setup lang="ts">
import RecipeCard from './RecipeCard.vue'
const props = defineProps<{ recipes: any[] }>()
</script>