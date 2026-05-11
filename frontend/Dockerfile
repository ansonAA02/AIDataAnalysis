# Build stage
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# 注入 API URL (可以透過環境變數覆蓋)
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Serve stage (Nginx)
FROM nginx:alpine
# 將構建好的靜態文件複製到 nginx 預設目錄
COPY --from=builder /app/dist /usr/share/nginx/html
# 覆蓋預設 nginx 設定以支援 Vue Router (History mode)
RUN echo 'server { \
    listen 80; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
