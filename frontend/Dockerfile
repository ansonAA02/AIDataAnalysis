# Build stage
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Inject API base URL (override via build arg in Railway / docker compose).
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Serve stage (Nginx). Use envsubst on a template so $PORT (Railway) works.
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# Nginx config template — $PORT is substituted at container start.
RUN printf 'server {\n\
    listen       %s;\n\
    server_name  _;\n\
    root         /usr/share/nginx/html;\n\
    index        index.html;\n\
    location / {\n\
        try_files $uri $uri/ /index.html;\n\
    }\n\
}\n' '${PORT}' > /etc/nginx/templates/default.conf.template

# Default port for local docker-compose; Railway overrides with its own PORT.
ENV PORT=80
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
