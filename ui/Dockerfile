FROM node:lts-alpine as build-stage
WORKDIR /app
COPY . .
RUN yarn install
RUN yarn run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist/spa /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
ENTRYPOINT ["nginx", "-g", "daemon off;"]
