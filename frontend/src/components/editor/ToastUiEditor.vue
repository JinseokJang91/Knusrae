<template>
    <div ref="containerRef" class="toast-ui-editor-wrap"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
// @ts-expect-error ESM default export; types exist but exports resolution may fail
import Editor from '@toast-ui/editor';
import '@toast-ui/editor/dist/toastui-editor.css';

const props = withDefaults(
    defineProps<{
        initialValue?: string;
        options?: Record<string, unknown>;
        height?: string;
        initialEditType?: 'markdown' | 'wysiwyg';
        previewStyle?: 'vertical' | 'tab';
    }>(),
    {
        initialValue: '',
        options: () => ({}),
        height: '300px',
        initialEditType: 'markdown',
        previewStyle: 'vertical'
    }
);

const emit = defineEmits<{
    (e: 'change'): void;
}>();

const containerRef = ref<HTMLDivElement | null>(null);
let editorInstance: InstanceType<typeof Editor> | null = null;

onMounted(() => {
    if (!containerRef.value) return;
    const opts = (props.options ?? {}) as { events?: Record<string, (a?: unknown) => void>; [k: string]: unknown };
    const { events: userEvents = {}, ...restOpts } = opts;
    const mergedEvents = {
        ...userEvents,
        change: (editorType?: 'markdown' | 'wysiwyg') => {
            emit('change');
            userEvents.change?.(editorType);
        }
    };
    editorInstance = new Editor({
        el: containerRef.value,
        initialValue: props.initialValue,
        height: props.height,
        initialEditType: props.initialEditType,
        previewStyle: props.previewStyle,
        usageStatistics: false,
        ...restOpts,
        events: mergedEvents
    });
});

onBeforeUnmount(() => {
    editorInstance?.destroy();
    editorInstance = null;
});

defineExpose({
    getMarkdown: () => editorInstance?.getMarkdown() ?? '',
    invoke: <T>(name: string, ...args: unknown[]): T => {
        const fn = (editorInstance as unknown as Record<string, (...a: unknown[]) => T>)?.[name];
        return typeof fn === 'function' ? fn(...args) : (undefined as T);
    }
});
</script>

<style scoped>
.toast-ui-editor-wrap {
    width: 100%;
}
</style>
