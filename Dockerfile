# Multi-stage production Dockerfile for Construction Platform (API + Web)
FROM node:20-slim AS builder
WORKDIR /app
COPY web/package*.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

FROM node:20-slim AS runner
WORKDIR /app
COPY web/package*.json ./
RUN npm ci --omit=dev
COPY --from=builder /app/dist ./dist
COPY web/server ./server

ENV NODE_ENV=production
ENV PORT=8080
EXPOSE 8080

CMD ["node", "server/index.js"]
