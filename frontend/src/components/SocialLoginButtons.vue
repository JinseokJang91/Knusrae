<template>
    <div class="social-login">
    <button
    v-for="p in providers"
    :key="p.key"
    type="button"
    class="social-btn"
    :class="`social-btn--${p.key}`"
    :disabled="disabled"
    :aria-label="p.ariaLabel"
    @click="$emit('click', p.key)"
    >
        <img class="social-btn__icon" :src="p.icon" alt="" aria-hidden="true" />
        <span class="social-btn__label">{{ p.label }}</span>
    </button>
    </div>
</template>
  
  <script setup lang="ts">
  type Provider = "kakao" | "google" | "naver";
  
  defineProps<{
    disabled?: boolean;
  }>();
  
  defineEmits<{
    (e: "click", provider: Provider): void;
  }>();
  
  import kakaoIcon from "@/assets/images/social/kakao.png";
  import googleIcon from "@/assets/images/social/google.svg";
  import naverIcon from "@/assets/images/social/naver.png";
  
  const providers: Array<{
    key: Provider;
    label: string;
    ariaLabel: string;
    icon: string;
  }> = [
    {
      key: "kakao",
      label: "카카오 계정으로 계속하기",
      ariaLabel: "카카오 계정으로 계속하기",
      icon: kakaoIcon,
    },
    {
      key: "google",
      label: "Google 계정으로 계속하기",
      ariaLabel: "Google 계정으로 계속하기",
      icon: googleIcon,
    },
    {
      key: "naver",
      label: "네이버 계정으로 계속하기",
      ariaLabel: "네이버 계정으로 계속하기",
      icon: naverIcon,
    },
  ];
  </script>
  
  <style scoped>
  .social-login {
    display: grid;
    gap: 12px;
  }
  
  /* 공통 버튼 그리드 */
  .social-btn {
    width: 100%;
    height: 48px;
    border-radius: 12px;
    padding: 0 16px;
  
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
  
    font-size: 16px;
    line-height: 1;
  
    border: 1px solid transparent;
    cursor: pointer;
    user-select: none;
  
    transition: transform 0.02s ease, opacity 0.15s ease;
  }
  
  .social-btn:active {
    transform: scale(0.99);
  }
  
  .social-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  /* 키보드 포커스 접근성 */
  .social-btn:focus-visible {
    outline: 2px solid currentColor;
    outline-offset: 2px;
  }
  
  .social-btn__icon {
    width: 36px;
    height: 36px;
    flex: 0 0 36px;
  }
  
  .social-btn__label {
    white-space: nowrap;
  }
  
  /* 브랜드 스킨 */
  .social-btn--kakao {
    background: #fee500;
    color: #191919;
  }
  
  .social-btn--naver {
    background: #03A94D;
    color: #ffffff;
    gap: 8px;
  }
  
  /* 구글: 보통 흰 배경 + 테두리 + 진한 텍스트 스타일 */
  .social-btn--google {
    background: #ffffff;
    color: #1f1f1f;
    border-color: #dadce0;
  }
  </style>
  