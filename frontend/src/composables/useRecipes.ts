import { storeToRefs } from "pinia";
import { useRecipeStore } from "../stores/recipes";
import { computed } from "vue";

export function useRecipes() {
    const store = useRecipeStore()
    const { items, total, loading, current } = storeToRefs(store)

    const pageCount = computed(() => Math.ceil(total.value / 20))

    return {
        items, total, loading, current, pageCount,
        fetch: store.fetch,
        loadOne: store.loadOne,
        create: store.create
    }
}