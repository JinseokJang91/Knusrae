<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
// @ts-expect-error ESM default export; types exist but exports resolution may fail
import Viewer from '@toast-ui/editor/viewer';
import '@toast-ui/editor/dist/toastui-editor-viewer.css';

const props = withDefaults(
    defineProps<{
        initialValue?: string;
        height?: string;
    }>(),
    {
        initialValue: '',
        height: 'auto'
    }
);

const containerRef = ref<HTMLDivElement | null>(null);
let viewerInstance: { setMarkdown: (md: string) => void; destroy: () => void } | null = null;

onMounted(() => {
    if (!containerRef.value) return;
    viewerInstance = new (Viewer as unknown as new (opts: { el: HTMLDivElement; initialValue: string; height?: string }) => { setMarkdown: (md: string) => void; destroy: () => void })({
        el: containerRef.value,
        initialValue: props.initialValue,
        height: props.height
    });
});

onBeforeUnmount(() => {
    viewerInstance?.destroy();
    viewerInstance = null;
});

watch(
    () => props.initialValue,
    (val) => {
        viewerInstance?.setMarkdown(val ?? '');
    },
    { flush: 'post' }
);
</script>

<template>
    <div ref="containerRef" class="toast-ui-viewer-wrap"></div>
</template>

<style scoped>
.toast-ui-viewer-wrap {
    width: 100%;
}
</style>
