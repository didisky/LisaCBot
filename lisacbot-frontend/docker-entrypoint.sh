#!/bin/sh
set -e

# Default BACKEND_URL for Docker Compose (local development)
export BACKEND_URL=${BACKEND_URL:-http://backend:8080}

echo "========================================="
echo "Starting LisaCBot Frontend"
echo "Backend URL: $BACKEND_URL"
echo "========================================="

# Replace BACKEND_URL in nginx configuration template
envsubst '${BACKEND_URL}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# Remove the template file
rm -f /etc/nginx/conf.d/default.conf.template

# Test nginx configuration
nginx -t

# Start nginx
exec nginx -g "daemon off;"
