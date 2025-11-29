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
- **Multiple Trading Strategies**:
  - Simple Moving Average (SMA)
  - EMA + RSI
  - MACD
  - Composite Strategy (weighted voting of multiple strategies)
- **Risk Management**:
  - Trailing stop-loss (protects profits by following price upward)
  - Take-profit (automatic profit taking at target percentage)
- **Market Cycle Detection**: 6-phase cycle analysis (ACCUMULATION, MARKUP, BULL_MARKET, DECLINE, CRASH, UNKNOWN)
- **Portfolio Tracking**: Real-time logging of balance, holdings, and total portfolio value
- **Trade Persistence**: All trades stored in PostgreSQL database
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
- Docker (for PostgreSQL)

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

### Start PostgreSQL Database

The bot requires PostgreSQL to store trade history. Start it using Docker:

```bash
./start-db.sh
```

This will start PostgreSQL in a Docker container with the following configuration:
- Host: `localhost`
- Port: `5432`
- Database: `lisacbot`
- User: `lisacbot`
- Password: `lisacbot`

To stop the database:
```bash
docker-compose down
```

## Configuration

Edit [application.properties](lisacbot-backend/src/main/resources/application.properties) to configure the bot:

```properties
# Bot configuration
bot.poll.interval.seconds=30        # How often to check prices (seconds)
bot.initial.balance=1000.0          # Starting balance in USD

# Risk management
bot.trailing.stop.loss.enabled=true
bot.trailing.stop.loss.percentage=5.0    # Trailing stop at 5% from peak
bot.take.profit.enabled=true
bot.take.profit.percentage=8.0           # Take profit at +8%

# Strategy selection: sma, ema-rsi, macd, or composite
bot.strategy.type=composite

# Composite strategy configuration (weighted voting)
bot.strategy.composite.strategies=sma,ema-rsi,macd
bot.strategy.composite.weights=30,40,30           # Must sum to 100
bot.strategy.composite.buy.threshold=0.5          # Buy when score >= 0.5
bot.strategy.composite.sell.threshold=-0.5        # Sell when score <= -0.5

# Market cycle detection
bot.cycle.allowed=ACCUMULATION,MARKUP,BULL_MARKET  # Only trade in these cycles
bot.cycle.update.interval.hours=24                 # Re-analyze cycle every 24h

# Logging
logging.level.com.lisacbot=INFO
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

## Trading Strategies

LisaCBot supports multiple trading strategies:

### 1. Simple Moving Average (SMA)
- Maintains a rolling window of recent prices
- **BUY**: When price crosses above SMA
- **SELL**: When price crosses below SMA

### 2. EMA + RSI
- Combines Exponential Moving Average with Relative Strength Index
- **BUY**: Price above EMA AND RSI is oversold (< 30)
- **SELL**: Price below EMA AND RSI is overbought (> 70)

### 3. MACD (Moving Average Convergence Divergence)
- Tracks fast EMA (12), slow EMA (26), and signal line (9)
- **BUY**: MACD line crosses above signal line (bullish crossover)
- **SELL**: MACD line crosses below signal line (bearish crossover)

### 4. Composite Strategy (Default)
- Combines multiple strategies using weighted voting
- Each strategy votes BUY (+1), HOLD (0), or SELL (-1)
- Final decision based on weighted score and thresholds
- Example: SMA (30%), EMA-RSI (40%), MACD (30%)

### Custom Strategies
You can implement custom strategies by creating a class that implements the `TradingStrategy` interface in the domain layer.

## Disclaimer

This bot is for educational purposes only. It uses simulated trading with no real money involved. Cryptocurrency trading carries significant risk. Never trade with money you cannot afford to lose.

## License

[Add your license here]
