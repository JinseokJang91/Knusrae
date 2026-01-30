import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';

import Aura from '@primeuix/themes/aura';
import { definePreset } from '@primeuix/themes';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import '@/assets/styles.scss';

// White & Orange 테마 프리셋 정의 (메뉴/버튼 등 primary를 오렌지톤으로 통일)
const WhitePreset = definePreset(Aura, {
    semantic: {
        primary: {
            50: '#fff7ed',   // 매우 밝은 오렌지
            100: '#ffedd5',  // 밝은 오렌지
            200: '#fed7aa',  // 연한 오렌지
            300: '#fdba74',  // 중간 밝기 오렌지
            400: '#fb923c',  // 중간 오렌지
            500: '#f97316',  // 메인 오렌지 (포인트 색상)
            600: '#ea580c',  // 진한 오렌지
            700: '#c2410c',  // 더 진한 오렌지
            800: '#9a3412',  // 매우 진한 오렌지
            900: '#7c2d12',  // 어두운 오렌지
            950: '#431407'   // 가장 어두운 오렌지
        }
    }
});

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(PrimeVue, {
    theme: {
        preset: WhitePreset,
        options: {
            darkModeSelector: '.app-dark'
        }
    }
});
app.use(ConfirmationService);
app.use(ToastService);

app.mount('#app');
