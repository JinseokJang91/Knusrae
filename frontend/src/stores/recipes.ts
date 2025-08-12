import { defineStore } from "pinia";
import { listRecipes, getRecipe, createRecipe } from '../api/recipes'

export interface Recipe {
    id: string
    title: string
    description?: string
    ingredients: string[]
    steps: string[]
    tags: string[]
    authorId?: string
    createdAt?: string
}

interface RecipeState {
    items: Recipe[]
    total: number
    loading: boolean
    current: Recipe | null
}

export const useRecipeStore = defineStore('recipes', {
    state: (): RecipeState => ({ items: [], total: 0, loading: false, current: null }),
    actions: {
        async fetch(params: { q?: string; tags?: string[], page?: number; pageSize?: number } = {}) {
            this.loading = true
            try {
                const res = await listRecipes(params)
                this.items = res.items
                this.total = res.total
            } finally {
                this.loading = false
            }
        },
        async loadOne(id: string) {
            this.current = await getRecipe(id)
        },
        async create(data: Partial<Recipe>) {
            const created = await createRecipe(data)
            this.items.unshift(created)
            return created
        }
    }
})