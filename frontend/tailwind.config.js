/** @type {import('tailwindcss').Config} */
import PrimeUI from 'tailwindcss-primeui';

export default {
    darkMode: ['selector', '[class*="app-dark"]'],
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    plugins: [PrimeUI],
    theme: {
        screens: {
            xs: '360px',      // 안드로이드 모바일 표준
            sm: '375px',      // iPhone 표준 (iPhone 12/13/14)
            md: '768px',      // 태블릿 세로 (iPad 기준)
            lg: '1024px',     // 태블릿 가로 / 작은 데스크톱
            xl: '1366px',     // 중소형 노트북
            '2xl': '1440px',  // 중형 노트북
            '3xl': '1920px'   // Full HD 데스크톱 (가장 보편적)
        }
    }
};
