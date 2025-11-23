# LisaCBot

A cryptocurrency trading bot built with Spring Boot 3.4 and Java 21 that automatically trades Bitcoin based on configurable strategies.

## Overview

LisaCBot is an automated trading bot that:
- Fetches real-time Bitcoin prices from the CoinGecko API
- Analyzes price trends using trading strategies (currently Simple Moving Average)
- Executes buy/sell decisions automatically
- Tracks portfolio value in real-time

The bot uses an "all-in" approach, holding either 100% USD or 100% BTC at any given time.

## Features

- **Scheduled Price Monitoring**: Configurable polling interval for price checks
- **Strategy-Based Trading**: Pluggable trading strategy interface
- **Simple Moving Average (SMA)**: Built-in SMA crossover strategy implementation
- **Portfolio Tracking**: Real-time logging of balance, holdings, and total portfolio value
- **Spring Boot**: Modern Java framework with dependency injection and scheduling

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

- Java 21
- Maven 3.6+

## Installation

Clone the repository:

```bash
git clone <repository-url>
cd LisaCBot
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

## Running the Bot

### Compile

```bash
mvn compile
```

### Run Tests

```bash
mvn test
```

### Run the Application

```bash
mvn spring-boot:run
```

### Build JAR

```bash
mvn package
java -jar target/lisacbot-1.0-SNAPSHOT.jar
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
