FROM nginx:1.23.3-alpine-slim

COPY nginx.conf /etc/nginx/nginx.conf

RUN mkdir -p /usr/share/nginx/html/maps
COPY ./maps-static/ /usr/share/nginx/html/maps-static

COPY ./*.txt /usr/share/nginx/html/
COPY ./*.png /usr/share/nginx/html/
COPY ./*.ico /usr/share/nginx/html/
COPY ./*.css /usr/share/nginx/html/
COPY ./*.html /usr/share/nginx/html/
COPY ./*.js /usr/share/nginx/html/
COPY ./*.js.map /usr/share/nginx/html/
