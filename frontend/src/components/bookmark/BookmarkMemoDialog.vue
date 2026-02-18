<template>
    <Dialog
        :visible="visible"
        @update:visible="$emit('update:visible', $event)"
        modal
        header="레시피 메모"
        :style="{ width: '420px' }"
        :draggable="false"
        @hide="onHide"
    >
        <div class="bookmark-memo-dialog">
            <p v-if="recipeTitle" class="text-sm text-gray-600 mb-3">{{ recipeTitle }}</p>
            <label class="block mb-2 font-medium">메모</label>
            <Textarea
                v-model="memoLocal"
                placeholder="이 레시피에 대한 메모를 남겨보세요 (예: 양념 2배로 했을 때 맛있음)"
                rows="4"
                class="w-full"
                :maxlength="500"
                auto-resize
            />
            <small class="text-gray-500">{{ memoLocal.length }} / 500</small>
        </div>
        <template #footer>
            <Button label="취소" text @click="close" />
            <Button label="저장" @click="save" :loading="saving" />
        </template>
    </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Textarea from 'primevue/textarea';

const props = defineProps<{
    visible: boolean;
    bookmarkId: number;
    recipeTitle?: string | null;
    memo: string | null;
    /** 저장 시 호출. Promise를 반환하면 완료 후 닫기 (선택) */
    saveAsync?: (payload: { bookmarkId: number; memo: string | null }) => Promise<void>;
}>();

const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'save', payload: { bookmarkId: number; memo: string | null }): void;
}>();

const memoLocal = ref('');
const saving = ref(false);

watch(
    () => [props.visible, props.memo],
    () => {
        if (props.visible) {
            memoLocal.value = props.memo ?? '';
        }
    },
    { immediate: true }
);

function close() {
    if (!saving.value) {
        emit('update:visible', false);
    }
}

function onHide() {
    memoLocal.value = props.memo ?? '';
}

async function save() {
    if (saving.value) return;
    const value = memoLocal.value.trim() || null;
    const payload = { bookmarkId: props.bookmarkId, memo: value };
    if (props.saveAsync) {
        saving.value = true;
        try {
            await props.saveAsync(payload);
            close();
        } finally {
            saving.value = false;
        }
    } else {
        emit('save', payload);
        close();
    }
}
</script>

<style scoped lang="scss">
.bookmark-memo-dialog {
    padding: 0.25rem 0;
}
</style>
