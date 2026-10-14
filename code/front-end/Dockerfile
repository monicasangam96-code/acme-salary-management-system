# ---- Stage 1: Build ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ENV NODE_ENV=production
RUN npx ng build --configuration production

# ---- Stage 2: Serve ----
FROM nginx:1.25-alpine
RUN rm -rf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/acme-salary-mgmt/browser /usr/share/nginx/html
COPY deploy/dev/nginx/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:4200/api/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
