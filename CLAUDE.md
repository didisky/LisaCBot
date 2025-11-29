# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LisaCBot is a crypto trading bot with a Spring Boot backend and Angular frontend.

## Project Structure

```
LisaCBot/
├── lisacbot-backend/          # Spring Boot 3.4 backend (Java 21)
│   ├── src/
│   ├── pom.xml
│   └── target/
├── lisacbot-frontend/         # Angular frontend
│   ├── src/
│   ├── package.json
│   └── node_modules/
└── README.md
```

## Backend - Build Commands

```bash
cd lisacbot-backend
mvn compile                    # Compile
mvn test                       # Run tests
mvn package                    # Build JAR
mvn spring-boot:run            # Run the application (http://localhost:8080)
```

## Frontend - Build Commands

```bash
cd lisacbot-frontend
npm install                    # Install dependencies
ng serve                       # Run dev server (http://localhost:4200)
ng build                       # Build for production
```

## Configuration

### Configuration Files

1. **application.properties** - Spring Boot defaults (committed to git)
   - Located in `lisacbot-backend/src/main/resources/`
   - Contains default values for local development
   - Database credentials use environment variable placeholders

2. **.env** - Docker configuration (NOT committed, in .gitignore)
   - Used by docker-compose.yml
   - Contains production secrets (passwords, etc.)
   - Required for Docker deployment

3. **.env.local** - Local development (NOT committed, in .gitignore)
   - Optional file for running backend with `mvn spring-boot:run`
   - Overrides application.properties values

### Environment Variable Override Examples

```bash
# Override bot configuration
BOT_POLL_INTERVAL_SECONDS=10 mvn spring-boot:run

# Override database configuration (local development)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lisacbot \
SPRING_DATASOURCE_PASSWORD=mypassword \
mvn spring-boot:run
```

### Docker Deployment

See DEPLOYMENT.md for full Docker deployment guide with automated scripts.

## Architecture

### Backend
- `LisaCBotApplication` - Spring Boot entry point
- `Bot` - Main bot component, runs scheduled price checks
- `PriceService` - Fetches BTC price from CoinGecko API
- `TradingStrategy` - Interface for trading strategies
- `SimpleMovingAverageStrategy` - SMA crossover implementation
- `BacktestService` - Backtesting functionality
- `BacktestController` - REST API for backtests

### Frontend
- `DashboardComponent` - Bot status monitoring
- `StrategyConfigComponent` - Strategy configuration UI
- `BacktestComponent` - Backtesting interface
- `BotService` - HTTP client for backend API
