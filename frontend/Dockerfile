# Stage 1: build the React application
FROM node:24-alpine AS build

WORKDIR /app

# Copy dependency files first so Docker can cache npm install.
COPY package.json package-lock.json ./

RUN npm ci

# Copy the frontend source code.
COPY . .

# An empty URL makes requests relative:
# /api/auth/login instead of http://localhost:8080/api/auth/login
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build


# Stage 2: serve the built frontend with Nginx
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build \
    /app/dist \
    /usr/share/nginx/html

EXPOSE 80