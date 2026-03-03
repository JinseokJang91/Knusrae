# ============================================
# Stage 1: Build (Vue + Vite)
# ============================================
FROM node:20-alpine AS builder

WORKDIR /app

# 패키지 파일 먼저 복사 (캐시 활용)
COPY package.json package-lock.json* ./

RUN npm ci

COPY . .

# 빌드 시 API URL 등 주입 (선택). docker-compose에서 build.args로 전달
ARG VITE_API_BASE_URL_AUTH
ARG VITE_API_BASE_URL_MEMBER
ARG VITE_API_BASE_URL_COOK
ARG VITE_NAVER_CLIENT_ID
ARG VITE_NAVER_REDIRECT_URI
ARG VITE_GOOGLE_CLIENT_ID
ARG VITE_GOOGLE_REDIRECT_URI
ARG VITE_KAKAO_CLIENT_ID
ARG VITE_KAKAO_REDIRECT_URI
ARG VITE_ADMIN_EMAILS

ENV VITE_API_BASE_URL_AUTH=$VITE_API_BASE_URL_AUTH
ENV VITE_API_BASE_URL_MEMBER=$VITE_API_BASE_URL_MEMBER
ENV VITE_API_BASE_URL_COOK=$VITE_API_BASE_URL_COOK
ENV VITE_NAVER_CLIENT_ID=$VITE_NAVER_CLIENT_ID
ENV VITE_NAVER_REDIRECT_URI=$VITE_NAVER_REDIRECT_URI
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID
ENV VITE_GOOGLE_REDIRECT_URI=$VITE_GOOGLE_REDIRECT_URI
ENV VITE_KAKAO_CLIENT_ID=$VITE_KAKAO_CLIENT_ID
ENV VITE_KAKAO_REDIRECT_URI=$VITE_KAKAO_REDIRECT_URI
ENV VITE_ADMIN_EMAILS=$VITE_ADMIN_EMAILS

RUN npm run build

# ============================================
# Stage 2: Serve (Nginx)
# ============================================
FROM nginx:alpine

# 기본 설정 덮어쓰기 (SPA 라우팅)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드 결과물만 복사
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
