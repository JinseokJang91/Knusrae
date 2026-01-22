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

// White & Orange 테마 프리셋 정의
const WhitePreset = definePreset(Aura, {
    semantic: {
        // primary: {
        //     50: '#fff7ed',   // 매우 밝은 오렌지
        //     100: '#ffedd5',  // 밝은 오렌지
        //     200: '#fed7aa',  // 연한 오렌지
        //     300: '#fdba74',  // 중간 밝기 오렌지
        //     400: '#fb923c',  // 중간 오렌지
        //     500: '#f97316',  // 메인 오렌지 (포인트 색상)
        //     600: '#ea580c',  // 진한 오렌지
        //     700: '#c2410c',  // 더 진한 오렌지
        //     800: '#9a3412',  // 매우 진한 오렌지
        //     900: '#7c2d12',  // 어두운 오렌지
        //     950: '#431407'   // 가장 어두운 오렌지
        // },
        // colorScheme: {
        //     light: {
        //         surface: {
        //             0: '#ffffff',   // 순수 흰색 배경
        //             50: '#ffffff',  // 순수 흰색
        //             100: '#ffffff', // 순수 흰색
        //             200: '#fafafa', // 약간 회색빛 흰색
        //             300: '#f5f5f5', // 연한 회색
        //             400: '#f0f0f0', // 밝은 회색
        //             500: '#e5e5e5', // 중간 회색
        //             600: '#d4d4d4', // 회색
        //             700: '#a3a3a3', // 진한 회색
        //             800: '#737373', // 더 진한 회색
        //             900: '#525252', // 어두운 회색
        //             950: '#1f2937'  // 매우 어두운 회색
        //         },
        //         text: {
        //             color: '#000000',        // 메인 텍스트 색상 (black)
        //             mutedColor: '#000000'    // 보조 텍스트 색상 (black)
        //         }
        //     }
        // }
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
