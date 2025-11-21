# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LisaCBot is a crypto trading bot built with Spring Boot 3.4 and Java 21.

## Build Commands

```bash
mvn compile                    # Compile
mvn test                       # Run tests
mvn package                    # Build JAR
mvn spring-boot:run            # Run the application
```

## Configuration

Properties in `src/main/resources/application.properties` can be overridden via environment variables:

```bash
BOT_POLL_INTERVAL_SECONDS=10 mvn spring-boot:run
```

## Architecture

- `LisaCBotApplication` - Spring Boot entry point
- `Bot` - Main bot component, runs scheduled price checks
- `PriceService` - Fetches BTC price from CoinGecko API
- `TradingStrategy` - Interface for trading strategies
- `SimpleMovingAverageStrategy` - SMA crossover implementation
