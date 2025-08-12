<template>
  <section>
    <h1 class="text-xl font-bold mb-4">레시피 작성</h1>
    <RecipeForm @submit="onSubmit" />
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useRecipes } from '../composables/useRecipes'
import RecipeForm from '../components/RecipeForm.vue'
const router = useRouter()
const { create } = useRecipes()

async function onSubmit(payload: any) {
  const created = await create(payload)
  router.push({ name: 'recipe-view', params: { id: created.id } })
}
</script>