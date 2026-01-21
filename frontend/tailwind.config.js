/** @type {import('tailwindcss').Config} */
import PrimeUI from 'tailwindcss-primeui';

export default {
    darkMode: ['selector', '[class*="app-dark"]'],
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    plugins: [PrimeUI],
    theme: {
        extend: {
            colors: {
                orange: {
                    50: '#fff7ed',
                    100: '#ffedd5',
                    200: '#fed7aa',
                    300: '#fdba74',
                    400: '#fb923c',
                    500: '#f97316',  // 메인 오렌지 포인트 색상
                    600: '#ea580c',
                    700: '#c2410c',
                    800: '#9a3412',
                    900: '#7c2d12',
                    950: '#431407'
                }
            }
        },
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
