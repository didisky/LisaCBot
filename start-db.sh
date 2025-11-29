#!/bin/bash

echo "Starting PostgreSQL database for LisaCBot..."
docker-compose up -d

echo "Waiting for PostgreSQL to be ready..."
sleep 3

echo "Database started! Connection details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: lisacbot"
echo "  User: lisacbot"
echo "  Password: lisacbot"
echo ""
echo "To stop the database: docker-compose down"
echo "To view logs: docker-compose logs -f postgres"
