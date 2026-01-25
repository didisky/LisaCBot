#!/usr/bin/env python3
"""
Standalone trading analysis script.
Called from Java with price data, returns trading signal.

Usage:
    python analyze.py <current_price> <price1> <price2> ...

Example:
    python analyze.py 53400 50000 50200 50100 49900 ...

Output (JSON):
    {"signal": "BUY", "confidence": 75.5, "reasoning": "..."}
"""

import sys
import json
from trading_state import TradingState
from trading_graph import create_trading_graph


def main():
    """Main entry point for command-line analysis."""

    # Parse command-line arguments
    if len(sys.argv) < 3:
        print(json.dumps({
            "signal": "HOLD",
            "confidence": 0.0,
            "error": "Insufficient arguments. Usage: python analyze.py <current_price> <price1> <price2> ..."
        }))
        sys.exit(1)

    try:
        # First argument is current price
        current_price = float(sys.argv[1])

        # Remaining arguments are price history
        price_history = [float(p) for p in sys.argv[2:]]

        if len(price_history) < 2:
            print(json.dumps({
                "signal": "HOLD",
                "confidence": 0.0,
                "error": "Need at least 2 historical prices"
            }))
            sys.exit(1)

        # Create the trading graph
        app = create_trading_graph()

        # Initial state
        initial_state = {
            "price_history": price_history,
            "current_price": current_price,
            "signal": None,
            "confidence": None,
            "indicators": None,
            "reasoning": None,
            "error": None
        }

        # Run analysis
        result = app.invoke(initial_state)

        # Return result as JSON
        output = {
            "signal": result.get("signal", "HOLD"),
            "confidence": result.get("confidence", 0.0),
            "reasoning": result.get("reasoning", ""),
            "indicators": result.get("indicators", {}),
            "error": result.get("error")
        }

        print(json.dumps(output))
        sys.exit(0)

    except Exception as e:
        print(json.dumps({
            "signal": "HOLD",
            "confidence": 0.0,
            "error": f"Analysis error: {str(e)}"
        }))
        sys.exit(1)


if __name__ == "__main__":
    main()
