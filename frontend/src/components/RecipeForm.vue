<template>
  <form class="grid gap-3 max-w-2xl" @submit.prevent="submit">
    <input v-model="title" placeholder="제목" required />
    <textarea v-model="description" placeholder="설명" />
    <div>
      <label class="font-semibold">재료</label>
      <div v-for="(ing, i) in ingredients" :key="i" class="flex gap-2 items-center mt-1">
        <input v-model="ingredients[i]" placeholder="예: 달걀 2개" class="flex-1" />
        <button type="button" @click="removeIng(i)">-</button>
      </div>
      <button type="button" class="mt-1" @click="ingredients.push('')">재료 추가</button>
    </div>
    <div>
      <label class="font-semibold">만드는 법</label>
      <div v-for="(s, i) in steps" :key="i" class="flex gap-2 items-center mt-1">
        <input v-model="steps[i]" placeholder="단계 설명" class="flex-1" />
        <button type="button" @click="removeStep(i)">-</button>
      </div>
      <button type="button" class="mt-1" @click="steps.push('')">단계 추가</button>
    </div>
    <div>
      <label class="font-semibold">태그</label>
      <TagChips :selected="tags" @update:selected="(v) => tags = v" />
    </div>
    <button type="submit">저장</button>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TagChips from './TagChips.vue'
const emit = defineEmits<{ (e: 'submit', payload: any): void }>()

const title = ref('')
const description = ref('')
let ingredients = ref<string[]>([''])
let steps = ref<string[]>([''])
let tags = ref<string[]>([])

function removeIng(i: number) { ingredients.value.splice(i, 1) }
function removeStep(i: number) { steps.value.splice(i, 1) }
function submit() {
  emit('submit', { title: title.value, description: description.value, ingredients: ingredients.value.filter(Boolean), steps: steps.value.filter(Boolean), tags: tags.value })
}
</script>