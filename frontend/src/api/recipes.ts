import { api } from './client'
import type { Recipe } from '../stores/recipes'

export async function listRecipes(params: { q?: string; tags?: string[]; page?: number; pageSize?: number } = {}) {
    const { data } = await api.get('/recipes', { params })
    return data as { items: Recipe[]; total: number }
}

export async function getRecipe(id: string) {
    const { data } = await api.get(`/recipes/${id}`)
    return data as Recipe
}

export async function createRecipe(payload: Partial<Recipe>) {
    const { data } = await api.post('/recipes', payload)
    return data as Recipe
}