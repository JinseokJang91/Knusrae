<template>
  <div class="flex gap-2">
    <input v-model="q" type="search" placeholder="레시피 검색" class="flex-1" @keydown.enter="emit('search', q)" />
    <button @click="emit('search', q)">검색</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const q = ref('')
const emit = defineEmits<{ (e: 'search', q: string): void }>()
</script>