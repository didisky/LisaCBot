#!/bin/bash

# LisaCBot Production Deployment Script
# Usage: ./deploy-prod.sh [branch]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Configuration
BRANCH=${1:-main}
BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Pre-flight checks
preflight_checks() {
    print_header "Pre-flight Checks"

    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running"
        exit 1
    fi
    print_success "Docker is running"

    # Check if .env exists
    if [ ! -f .env ]; then
        print_error ".env file not found"
        exit 1
    fi
    print_success ".env file exists"

    # Check if git repo
    if [ ! -d .git ]; then
        print_error "Not a git repository"
        exit 1
    fi
    print_success "Git repository detected"

    # Check for uncommitted changes
    if [ -n "$(git status --porcelain)" ]; then
        print_warning "You have uncommitted changes"
        git status --short
        read -p "Continue anyway? (yes/no): " -r
        if [[ ! $REPLY == "yes" ]]; then
            print_info "Deployment cancelled"
            exit 1
        fi
    fi
}

# Backup database
backup_database() {
    print_header "Backing up Database"

    mkdir -p "$BACKUP_DIR"
    BACKUP_FILE="$BACKUP_DIR/backup_${TIMESTAMP}.sql"

    if docker compose ps postgres | grep -q "Up"; then
        print_info "Creating database backup: $BACKUP_FILE"
        docker compose exec -T postgres pg_dump -U lisacbot lisacbot > "$BACKUP_FILE"
        print_success "Database backed up"

        # Keep only last 10 backups
        ls -t "$BACKUP_DIR"/backup_*.sql | tail -n +11 | xargs -r rm
        print_info "Old backups cleaned (keeping last 10)"
    else
        print_warning "PostgreSQL not running, skipping backup"
    fi
}

# Pull latest code
pull_code() {
    print_header "Pulling Latest Code"

    print_info "Fetching from origin..."
    git fetch origin

    CURRENT_COMMIT=$(git rev-parse HEAD)
    LATEST_COMMIT=$(git rev-parse origin/$BRANCH)

    if [ "$CURRENT_COMMIT" == "$LATEST_COMMIT" ]; then
        print_info "Already up to date ($(git rev-parse --short HEAD))"
        read -p "Rebuild anyway? (yes/no): " -r
        if [[ ! $REPLY == "yes" ]]; then
            print_info "Deployment cancelled"
            exit 0
        fi
    else
        print_info "Current: $(git rev-parse --short $CURRENT_COMMIT)"
        print_info "Latest:  $(git rev-parse --short $LATEST_COMMIT)"

        print_info "Pulling changes..."
        git pull origin "$BRANCH"

        print_success "Code updated"

        # Show what changed
        echo ""
        print_info "Changes:"
        git log --oneline "$CURRENT_COMMIT..$LATEST_COMMIT"
    fi
}

# Build and deploy
deploy() {
    print_header "Building and Deploying"

    print_info "Building images (this may take a few minutes)..."
    docker compose build --no-cache

    print_info "Stopping old containers..."
    docker compose down

    print_info "Starting new containers..."
    docker compose up -d

    print_success "Deployment completed!"
}

# Health check
health_check() {
    print_header "Health Check"

    print_info "Waiting for services to start..."
    sleep 10

    # Check PostgreSQL
    if docker compose exec -T postgres pg_isready -U lisacbot > /dev/null 2>&1; then
        print_success "PostgreSQL is healthy"
    else
        print_error "PostgreSQL is not responding"
        return 1
    fi

    # Check backend
    for i in {1..30}; do
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Backend is healthy"
            break
        fi
        if [ $i -eq 30 ]; then
            print_error "Backend health check failed"
            return 1
        fi
        sleep 2
    done

    # Check frontend
    if curl -f http://localhost/health > /dev/null 2>&1; then
        print_success "Frontend is healthy"
    else
        print_error "Frontend is not responding"
        return 1
    fi

    return 0
}

# Show deployment summary
show_summary() {
    print_header "Deployment Summary"

    echo ""
    print_info "Service Status:"
    docker compose ps

    echo ""
    print_info "Current Version:"
    echo "  Commit: $(git rev-parse --short HEAD)"
    echo "  Branch: $(git branch --show-current)"
    echo "  Date:   $(git log -1 --format=%cd --date=format:'%Y-%m-%d %H:%M:%S')"

    echo ""
    print_info "Endpoints:"
    echo "  Frontend: http://localhost"
    echo "  Backend:  http://localhost:8080/api"

    echo ""
    print_success "Deployment completed successfully!"
}

# Rollback function
rollback() {
    print_header "Rolling Back"

    if [ -z "$(ls -A $BACKUP_DIR/backup_*.sql 2>/dev/null)" ]; then
        print_error "No backups found"
        exit 1
    fi

    LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/backup_*.sql | head -1)

    print_warning "This will restore database from: $LATEST_BACKUP"
    read -p "Continue? (yes/no): " -r

    if [[ $REPLY == "yes" ]]; then
        print_info "Restoring database..."
        cat "$LATEST_BACKUP" | docker compose exec -T postgres psql -U lisacbot lisacbot

        print_info "Restarting backend..."
        docker compose restart backend

        print_success "Rollback completed"
    else
        print_info "Rollback cancelled"
    fi
}

# Main deployment flow
main() {
    print_header "LisaCBot Production Deployment"
    echo "Branch: $BRANCH"
    echo "Timestamp: $TIMESTAMP"
    echo ""

    preflight_checks
    backup_database
    pull_code
    deploy

    if health_check; then
        show_summary
    else
        print_error "Health check failed!"
        print_warning "Check logs with: docker compose logs"
        print_info "To rollback database: ./deploy-prod.sh rollback"
        exit 1
    fi
}

# Handle commands
case ${1:-deploy} in
    rollback)
        rollback
        ;;
    deploy|main|master)
        main
        ;;
    *)
        BRANCH=$1
        main
        ;;
esac
