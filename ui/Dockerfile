FROM node:lts-alpine as build-stage
WORKDIR /app
COPY . .
RUN yarn install
RUN yarn run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist/spa /usr/share/nginx/html
ENTRYPOINT ["nginx", "-g", "daemon off;"]
