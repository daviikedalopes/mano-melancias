FROM nginx:alpine
ENV PORT=80
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY . /usr/share/nginx/html
EXPOSE 80
