# Production-ready Node.js environment
FROM node:20-slim

# Set environment variables
ENV NODE_ENV=production
ENV PORT=5050

# Set working directory to /usr/src/app
WORKDIR /usr/src/app

# Copy package files
COPY package*.json ./

# Install production dependencies
RUN npm ci --omit=dev || npm install --omit=dev

# Copy application source code
COPY . .

# Expose required port
EXPOSE 5050

# Start the backend service
CMD ["node", "index.js"]
