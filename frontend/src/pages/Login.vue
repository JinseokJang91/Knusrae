<template>
  <form class="max-w-sm grid gap-3" @submit.prevent="onSubmit">
    <input v-model="email" type="email" placeholder="email" required />
    <input v-model="password" type="password" placeholder="password" required />
    <button type="submit">로그인</button>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

async function onSubmit() {
  await auth.login(email.value, password.value)
  const redirect = (route.query.redirect as string) || '/'
  router.push(redirect)
}
</script>