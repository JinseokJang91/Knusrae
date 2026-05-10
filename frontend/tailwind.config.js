/** @type {import('tailwindcss').Config} */
import PrimeUI from 'tailwindcss-primeui';

export default {
    darkMode: ['selector', '[class*="app-dark"]'],
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    plugins: [PrimeUI],
    theme: {
        extend: {
            // 레이아웃 토큰 (variables/_responsive-tokens.scss · :root 과 동기화)
            spacing: {
                'rsp-lg': 'var(--rsp-inline-lg)',
                'rsp-md': 'var(--rsp-inline-md)',
                'rsp-md-wide': 'var(--rsp-inline-md-wide)',
                'rsp-sm': 'var(--rsp-inline-sm)',
                'rsp-sm-wide': 'var(--rsp-inline-sm-wide)',
                'rsp-xs': 'var(--rsp-inline-xs)',
                'rsp-xs-wide': 'var(--rsp-inline-xs-wide)'
            }
        },
        // extend: {
        //     colors: {
        //         orange: {
        //             50: '#fff7ed',
        //             100: '#ffedd5',
        //             200: '#fed7aa',
        //             300: '#fdba74',
        //             400: '#fb923c',
        //             500: '#f97316',  // 메인 오렌지 포인트 색상
        //             600: '#ea580c',
        //             700: '#c2410c',
        //             800: '#9a3412',
        //             900: '#7c2d12',
        //             950: '#431407'
        //         }
        //     }
        // },
        screens: {
            sm: '640px', // 모바일 가로/큰 폰
            md: '768px', // 태블릿 세로
            lg: '1024px', // 태블릿 가로 / 작은 데스크톱
            xl: '1280px', // 데스크톱
            '2xl': '1536px' // 와이드 데스크톱
        }
    }
};
