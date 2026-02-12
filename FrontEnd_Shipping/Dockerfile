# ===== Build stage =====
FROM node:20-alpine AS build

WORKDIR /app

# Package.json and package-lock.json to take advantage of Docker cache
COPY my-app/package*.json ./

# Install dependencies
RUN npm install

# Source code
COPY my-app/ ./

# Build project
RUN npm run build

# ===== Serve stage =====

# Copy final build from previous step
FROM nginx:1.29.5

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80


# Port 80 exposed
EXPOSE 80

# Run Nginx
CMD ["nginx", "-g", "daemon off;"]
