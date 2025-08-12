<template>
  <div class="min-h-screen font-sans">
    <header class="p-4 border-b flex items-center gap-3">
      <RouterLink to="/" class="font-bold text-lg">rcstore recipes</RouterLink>
      <nav class="ml-auto flex gap-3">
        <RouterLink to="/recipes">레시피</RouterLink>
        <RouterLink to="/recipes/new">작성</RouterLink>
        <RouterLink v-if="!isAuthed" to="/login">로그인</RouterLink>
        <button v-else @click="logout">로그아웃</button>
      </nav>
    </header>
    <main class="p-4 max-w-5xl mx-auto">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from './stores/auth'
import { RouterLink, RouterView } from 'vue-router'
const auth = useAuthStore()
const isAuthed = auth.isAuthenticated
const logout = () => auth.logout()
</script>

<style>
/* 선택: 기본 스타일 */
</style>