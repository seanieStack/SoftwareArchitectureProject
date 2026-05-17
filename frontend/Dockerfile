# Stage 1: Build the Vite/React application
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Serve the compiled static assets with NGINX
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# SPA fallback: send all routes to index.html so React Router works correctly
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
