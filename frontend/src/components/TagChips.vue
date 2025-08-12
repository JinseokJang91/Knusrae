<template>
  <div class="flex flex-wrap gap-2">
    <template v-for="tag in allTags" :key="tag">
      <button
        class="px-2 py-1 border rounded-full"
        :class="{ 'bg-black text-white': selectedSet.has(tag) }"
        :disabled="readonly"
        @click="toggle(tag)"
      >{{ tag }}</button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ selected?: string[]; readonly?: boolean }>()
const emit = defineEmits<{ (e: 'update:selected', val: string[]): void }>()

const allTags = ['한식','양식','중식','디저트','비건']
const selectedSet = computed(() => new Set(props.selected || []))
function toggle(tag: string) {
  const set = new Set(selectedSet.value)
  if (set.has(tag)) set.delete(tag); else set.add(tag)
  emit('update:selected', Array.from(set))
}
</script>