# LangGraph Trading Analysis with OpenAI

Bitcoin trading analysis workflow using LangGraph and OpenAI API.

## Overview

This simplified implementation replaces manual technical indicator calculations with OpenAI's GPT-4 for intelligent trading signal generation.

## Architecture

```
Input (price_history, current_price)
           ↓
    [analyze_with_openai_node]
           ↓
Output (signal, confidence, reasoning, indicators)
```

## Setup

### 1. Install Dependencies

```bash
cd python-analysis/langgraph_trading
pip install -r requirements.txt
```

### 2. Set OpenAI API Key

```bash
export OPENAI_API_KEY="your-api-key-here"
```

Or add to your `.env` file:
```
OPENAI_API_KEY=sk-...
```

## Usage

### Command Line

```bash
python3 analyze.py <current_price> <price1> <price2> ...
```

Example:
```bash
python3 analyze.py 53400 50000 50200 50100 49900 50300 51000 51500
```

### Output Format

```json
{
  "signal": "BUY",
  "confidence": 75.5,
  "reasoning": "Price shows upward momentum above moving averages...",
  "indicators": {
    "sma_20": 50500.25,
    "sma_50": 49800.50,
    "rsi": 65.3,
    "volatility": 850.2,
    "price_change_pct": 2.45,
    "current_price": 53400,
    "price_above_sma20": true,
    "price_above_sma50": true
  },
  "error": null
}
```

## Integration with Java Backend

The Java backend (`LangGraphStrategy.java`) calls this script via subprocess:

```java
ProcessBuilder pb = new ProcessBuilder(pythonCommand);
pb.command().add(analyzeScript);
pb.command().add(String.valueOf(currentPrice));
priceHistory.forEach(p -> pb.command().add(String.valueOf(p)));
```

## OpenAI Model

- **Model**: `gpt-4o-mini`
- **Temperature**: 0.3 (for consistent analysis)
- **Response Format**: JSON mode

## Features

- Single node architecture (simplified from 2-node pipeline)
- AI-powered technical analysis
- Automatic indicator calculation
- Natural language reasoning
- Same input/output interface as previous implementation

## Cost Considerations

OpenAI API calls incur costs. Approximate costs per analysis:
- GPT-4o-mini: ~$0.0001 - $0.0003 per call
- For polling every 60 seconds: ~$0.43 - $1.30 per month

Monitor your usage at: https://platform.openai.com/usage
