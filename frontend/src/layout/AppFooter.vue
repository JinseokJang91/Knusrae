<script setup lang="ts">
import logoText from '@/assets/images/logo/logo-full.png';
import badgeAppStore from '@/assets/images/badges/badge_appstore.svg';
import badgeGooglePlay from '@/assets/images/badges/badge_googleplay.svg';
import iconYouTube from '@/assets/images/badges/badge_youtube.png';
import iconInstagram from '@/assets/images/badges/badge_instagram.svg';
import iconFacebook from '@/assets/images/badges/badge_facebook.png';
import iconKakaoTalk from '@/assets/images/badges/badge_kakaotalk.png';
</script>

<template>
    <footer class="layout-footer">
        <!-- Footer 상단 영역 -->
        <div class="footer-content">
            <!-- 좌측 영역: 로고, 소개 문구, 앱 다운로드 버튼 -->
            <div class="footer-left">
                <div class="footer-logo-container">
                    <img :src="logoText" alt="너스레" class="footer-logo" />
                </div>
                <p class="footer-description">내 마음에 스며드는 레시피 플랫폼</p>
                <div class="app-download-buttons">
                    <a href="#" class="download-btn app-store" aria-label="App Store에서 다운로드">
                        <img :src="badgeAppStore" alt="App Store" />
                    </a>
                    <a href="#" class="download-btn google-play" aria-label="Google Play에서 다운로드">
                        <img :src="badgeGooglePlay" alt="Google Play" />
                    </a>
                </div>
            </div>

            <!-- 우측 영역: 소셜 미디어 버튼 및 링크 메뉴 -->
            <div class="footer-right">
                <!-- 소셜 미디어 버튼 -->
                <div class="social-media-buttons">
                    <a href="#" class="social-btn youtube" aria-label="유튜브">
                        <img :src="iconYouTube" alt="유튜브" />
                    </a>
                    <a href="#" class="social-btn instagram" aria-label="인스타그램">
                        <img :src="iconInstagram" alt="인스타그램" />
                    </a>
                    <a href="#" class="social-btn facebook" aria-label="페이스북">
                        <img :src="iconFacebook" alt="페이스북" />
                    </a>
                    <a href="#" class="social-btn kakao" aria-label="카카오톡">
                        <img :src="iconKakaoTalk" alt="카카오톡" />
                    </a>
                </div>
                <div class="footer-links-group">
                    <a href="#" class="footer-link">ABOUT US</a><span class="divider">|</span> <router-link to="/support" class="footer-link">고객지원</router-link><span class="divider">|</span> <a href="#" class="footer-link">이용약관</a>
                    <span class="divider">|</span>
                    <a href="#" class="footer-link">개인정보처리방침</a>
                </div>
                <div class="footer-links-group"></div>
            </div>
        </div>

        <!-- Footer 하단: 사업자 정보 및 copyright -->
        <div class="footer-bottom">
            <div class="company-info">
                <span>(주)너스레</span>
                <span class="divider">|</span>
                <span>대표: 홍길동</span>
                <span class="divider">|</span>
                <span>사업자번호: 123-45-67890</span>
                <span class="divider">|</span>
                <span>주소: 서울시 강남구 아무개로 123</span>
                <span class="divider">|</span>
                <span>이메일: test@abc.com</span>
            </div>
            <p class="copyright">© 2026 KNUSRAE. All rights reserved.</p>
        </div>
    </footer>
</template>
