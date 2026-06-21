FROM nginx:1.27-alpine

RUN apk add --no-cache openssl

COPY securityapp.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /docker-entrypoint.d/10-generate-local-certs.sh

RUN chmod +x /docker-entrypoint.d/10-generate-local-certs.sh
