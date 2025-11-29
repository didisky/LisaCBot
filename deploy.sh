#!/bin/bash

# LisaCBot Deployment Script
# Usage: ./deploy.sh [command] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Check if .env exists
check_env() {
    if [ ! -f .env ]; then
        print_warning ".env file not found!"
        print_info "Creating .env from .env.example..."
        cp .env.example .env
        print_warning "Please edit .env and set POSTGRES_PASSWORD before deploying!"
        exit 1
    fi
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
}

# Deploy backend only
deploy_backend() {
    print_header "Deploying Backend"
    check_env
    check_docker

    print_info "Building backend image..."
    docker-compose build --no-cache backend

    print_info "Starting backend..."
    docker-compose up -d backend

    print_success "Backend deployed!"
    print_info "Checking logs..."
    docker-compose logs --tail=50 backend
}

# Deploy frontend only
deploy_frontend() {
    print_header "Deploying Frontend"
    check_env
    check_docker

    print_info "Building frontend image..."
    docker-compose build --no-cache frontend

    print_info "Starting frontend..."
    docker-compose up -d frontend

    print_success "Frontend deployed!"
    print_info "Checking logs..."
    docker-compose logs --tail=50 frontend
}

# Deploy everything
deploy_all() {
    print_header "Deploying Full Stack"
    check_env
    check_docker

    print_info "Building all images..."
    docker-compose build --no-cache

    print_info "Starting all services..."
    docker-compose up -d

    print_success "All services deployed!"
    sleep 3

    print_info "Service status:"
    docker-compose ps
}

# Quick rebuild (with cache)
quick_deploy() {
    local SERVICE=$1

    if [ -z "$SERVICE" ]; then
        print_header "Quick Rebuild - All Services"
        docker-compose up -d --build
    else
        print_header "Quick Rebuild - $SERVICE"
        docker-compose up -d --build "$SERVICE"
    fi

    print_success "Quick rebuild completed!"
    docker-compose ps
}

# Show logs
show_logs() {
    local SERVICE=$1

    if [ -z "$SERVICE" ]; then
        print_header "Showing All Logs"
        docker-compose logs -f
    else
        print_header "Showing $SERVICE Logs"
        docker-compose logs -f "$SERVICE"
    fi
}

# Stop services
stop_services() {
    local SERVICE=$1

    if [ -z "$SERVICE" ]; then
        print_header "Stopping All Services"
        docker-compose down
        print_success "All services stopped"
    else
        print_header "Stopping $SERVICE"
        docker-compose stop "$SERVICE"
        print_success "$SERVICE stopped"
    fi
}

# Restart services
restart_services() {
    local SERVICE=$1

    if [ -z "$SERVICE" ]; then
        print_header "Restarting All Services"
        docker-compose restart
        print_success "All services restarted"
    else
        print_header "Restarting $SERVICE"
        docker-compose restart "$SERVICE"
        print_success "$SERVICE restarted"
    fi
}

# Status
show_status() {
    print_header "Service Status"
    docker-compose ps

    echo ""
    print_header "Resource Usage"
    docker stats --no-stream lisacbot-backend lisacbot-frontend lisacbot-postgres 2>/dev/null || print_warning "Some containers are not running"
}

# Clean everything
clean_all() {
    print_warning "This will stop all containers and remove volumes (DATA WILL BE LOST)!"
    read -p "Are you sure? (yes/no): " -r

    if [[ $REPLY == "yes" ]]; then
        print_header "Cleaning Everything"
        docker-compose down -v
        print_success "Cleanup completed"
    else
        print_info "Cleanup cancelled"
    fi
}

# Database backup
backup_db() {
    print_header "Backing up Database"

    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"

    print_info "Creating backup: $BACKUP_FILE"
    docker-compose exec -T postgres pg_dump -U lisacbot lisacbot > "$BACKUP_FILE"

    print_success "Database backed up to $BACKUP_FILE"
}

# Database restore
restore_db() {
    local BACKUP_FILE=$1

    if [ -z "$BACKUP_FILE" ]; then
        print_error "Please specify backup file: ./deploy.sh restore <backup_file.sql>"
        exit 1
    fi

    if [ ! -f "$BACKUP_FILE" ]; then
        print_error "Backup file not found: $BACKUP_FILE"
        exit 1
    fi

    print_header "Restoring Database"
    print_warning "This will overwrite the current database!"
    read -p "Are you sure? (yes/no): " -r

    if [[ $REPLY == "yes" ]]; then
        print_info "Restoring from $BACKUP_FILE..."
        cat "$BACKUP_FILE" | docker-compose exec -T postgres psql -U lisacbot lisacbot
        print_success "Database restored"
    else
        print_info "Restore cancelled"
    fi
}

# Show help
show_help() {
    cat << EOF
LisaCBot Deployment Script

Usage: ./deploy.sh [command] [options]

Commands:
  all                   Deploy all services (full rebuild)
  backend              Deploy backend only
  frontend             Deploy frontend only
  quick [service]      Quick rebuild with cache (optional: backend/frontend)

  logs [service]       Show logs (optional: backend/frontend/postgres)
  status               Show service status and resource usage

  stop [service]       Stop services (optional: backend/frontend/postgres)
  restart [service]    Restart services (optional: backend/frontend/postgres)

  backup               Backup PostgreSQL database
  restore <file>       Restore PostgreSQL database from backup file

  clean                Stop and remove all containers and volumes
  help                 Show this help message

Examples:
  ./deploy.sh all                    # Deploy everything
  ./deploy.sh backend                # Deploy only backend
  ./deploy.sh quick backend          # Quick rebuild backend
  ./deploy.sh logs backend           # Show backend logs
  ./deploy.sh status                 # Show service status
  ./deploy.sh backup                 # Backup database
  ./deploy.sh restore backup.sql     # Restore database

EOF
}

# Main script logic
COMMAND=${1:-help}
OPTION=${2:-}

case $COMMAND in
    all)
        deploy_all
        ;;
    backend)
        deploy_backend
        ;;
    frontend)
        deploy_frontend
        ;;
    quick)
        quick_deploy "$OPTION"
        ;;
    logs)
        show_logs "$OPTION"
        ;;
    status)
        show_status
        ;;
    stop)
        stop_services "$OPTION"
        ;;
    restart)
        restart_services "$OPTION"
        ;;
    backup)
        backup_db
        ;;
    restore)
        restore_db "$OPTION"
        ;;
    clean)
        clean_all
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        echo ""
        show_help
        exit 1
        ;;
esac
