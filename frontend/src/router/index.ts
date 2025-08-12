import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import Home from '../pages/Home.vue'
import Recipes from '../pages/Recipes.vue'
import RecipesView from '../pages/RecipeView.vue'
import CreateRecipe from '../pages/CreateRecipe.vue'
import Login from '../pages/Login.vue'
import { useAuthStore } from '../stores/auth'

const routes: RouteRecordRaw[] = [
    {path: '/', name: 'home', component: Home},
    {path: '/recipes', name: 'recipes', component: Recipes},
    {path: '/recipes/new', name: 'create', component: CreateRecipe, meta: {requiresAuth: true}},
    {path: '/recipes/:id', name: 'recipe-view', component: RecipesView, props: true},
    {path: '/login', name: 'login', component: Login}
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to) => {
    const auth = useAuthStore()

    if(to.meta.requiresAuth && !auth.isAuthenticated) {
        return {name: 'login', query: {redirect: to.fullPath}}
    }
})

export default router