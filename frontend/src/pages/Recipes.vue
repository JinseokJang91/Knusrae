<template>
  <div>
    <div class="flex items-center gap-3 mb-4">
      <SearchBar @search="onSearch" />
      <TagChips :selected="tags" @update:selected="onTags" />
    </div>
    <RecipeList :recipes="recipes" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRecipes } from '../composables/useRecipes'
import SearchBar from '../components/SearchBar.vue'
import TagChips from '../components/TagChips.vue'
import RecipeList from '../components/RecipeList.vue'

const { items, fetch } = useRecipes()
const recipes = items
const tags = ref<string[]>([])

onMounted(() => fetch({ page: 1, pageSize: 20 }))

function onSearch(q: string) { fetch({ q, tags: tags.value }) }
function onTags(v: string[]) { tags.value = v; fetch({ tags: v }) }
</script>