# LisaCBot

A cryptocurrency trading bot with a Spring Boot 3.4 backend and Angular frontend that automatically trades Bitcoin based on configurable strategies.

## Overview

LisaCBot is an automated trading bot that:
- Fetches real-time Bitcoin prices from the CoinGecko API
- Analyzes price trends using trading strategies (currently Simple Moving Average)
- Executes buy/sell decisions automatically
- Tracks portfolio value in real-time
- Provides backtesting functionality to evaluate strategies
- Includes a web dashboard for monitoring and configuration

The bot uses an "all-in" approach, holding either 100% USD or 100% BTC at any given time.

## Features

### Backend (Spring Boot)
- **Scheduled Price Monitoring**: Configurable polling interval for price checks
- **Strategy-Based Trading**: Pluggable trading strategy interface
- **Simple Moving Average (SMA)**: Built-in SMA crossover strategy implementation
- **Portfolio Tracking**: Real-time logging of balance, holdings, and total portfolio value
- **Backtesting**: Test strategies against historical data
- **REST API**: Endpoints for status and backtesting

### Frontend (Angular)
- **Dashboard**: Real-time bot status monitoring
- **Backtest Interface**: Run backtests with custom parameters
- **Results Visualization**: Performance cards, charts, and detailed metrics
- **Execution Logs**: View backtest execution in real-time

## Architecture

### Core Components

- **[LisaCBotApplication](src/main/java/com/lisacbot/LisaCBotApplication.java)** - Spring Boot entry point
- **[Bot](src/main/java/com/lisacbot/Bot.java)** - Main bot component that runs scheduled price checks and executes trades
- **[PriceService](src/main/java/com/lisacbot/PriceService.java)** - Fetches BTC price from CoinGecko API
- **[TradingStrategy](src/main/java/com/lisacbot/TradingStrategy.java)** - Interface for implementing trading strategies
- **[SimpleMovingAverageStrategy](src/main/java/com/lisacbot/SimpleMovingAverageStrategy.java)** - SMA crossover strategy implementation
- **[Signal](src/main/java/com/lisacbot/Signal.java)** - Enum representing trading signals (BUY, SELL, HOLD)

### How It Works

1. **Initialization**: Bot starts with $1000 USD and 0 BTC
2. **Price Polling**: Every configured interval (default: 30 seconds), the bot fetches the current BTC price
3. **Strategy Analysis**: The price is passed to the trading strategy which returns a signal (BUY/SELL/HOLD)
4. **Trade Execution**:
   - **BUY**: Converts all USD balance to BTC at current price
   - **SELL**: Converts all BTC holdings to USD at current price
   - **HOLD**: No action taken
5. **Logging**: Portfolio value is logged after each tick

## Requirements

### Backend
- Java 21
- Maven 3.6+

### Frontend
- Node.js 22+
- npm 10+
- Angular CLI

## Installation

Clone the repository:

```bash
git clone <repository-url>
cd LisaCBot
```

### Install Frontend Dependencies

```bash
cd lisacbot-frontend
npm install
cd ..
```

## Configuration

Edit [application.properties](src/main/resources/application.properties) to configure the bot:

```properties
# Bot configuration
bot.poll.interval.seconds=30        # How often to check prices (seconds)
bot.strategy.sma.period=5           # SMA period for moving average calculation

# Logging
logging.level.com.lisacbot=INFO     # Log level
```

### Environment Variable Override

You can override any property using environment variables:

```bash
BOT_POLL_INTERVAL_SECONDS=10 mvn spring-boot:run
```

## Running the Application

### Quick Start (macOS)

Use the provided startup scripts to launch both backend and frontend:

```bash
# Launch both backend and frontend
./start.sh

# Or launch them separately
./start-backend.sh    # Backend only (Spring Boot on port 8080)
./start-frontend.sh   # Frontend only (Angular on port 4200)
```

The scripts will open two new terminal windows - one for the backend and one for the frontend. Simply close these terminal windows to stop the applications.

### Manual Start

#### Backend

```bash
cd lisacbot-backend
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

#### Frontend

```bash
cd lisacbot-frontend
ng serve
```

The frontend will be available at `http://localhost:4200`

### Other Commands

#### Compile Backend

```bash
cd lisacbot-backend
mvn compile
```

#### Run Backend Tests

```bash
cd lisacbot-backend
mvn test
```

#### Build Backend JAR

```bash
cd lisacbot-backend
mvn package
java -jar target/lisacbot-1.0-SNAPSHOT.jar
```

#### Build Frontend for Production

```bash
cd lisacbot-frontend
ng build
```

## Example Output

```
LisaCBot started
Starting balance: $1000.0
Checking prices every 30 seconds
BTC price: $42150.50
HOLD
Portfolio value: $1000.00
BTC price: $42200.75
BUY: Bought 0.023715 BTC
Portfolio value: $1000.00
BTC price: $42500.00
HOLD
Portfolio value: $1007.89
```

## Trading Strategy

The default strategy is a Simple Moving Average (SMA) crossover:
- Maintains a rolling window of recent prices (configurable period)
- **BUY**: When current price crosses above the SMA
- **SELL**: When current price crosses below the SMA
- **HOLD**: Otherwise

You can implement custom strategies by creating a class that implements the [TradingStrategy](src/main/java/com/lisacbot/TradingStrategy.java) interface.

## Disclaimer

This bot is for educational purposes only. It uses simulated trading with no real money involved. Cryptocurrency trading carries significant risk. Never trade with money you cannot afford to lose.

## License

[Add your license here]
