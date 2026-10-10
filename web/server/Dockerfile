# Dedicated Express Backend Dockerfile for Cloud Run
FROM node:20-slim
WORKDIR /app

# Copy package descriptors
COPY package*.json ./
RUN npm ci --omit=dev

# Copy server code
COPY server/ ./server/

# Environment and Port bindings
ENV NODE_ENV=production
ENV PORT=8080
EXPOSE 8080

CMD ["node", "server/index.js"]
