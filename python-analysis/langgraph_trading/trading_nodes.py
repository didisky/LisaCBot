"""
Node functions for Bitcoin trading analysis workflow.
"""

from trading_state import TradingState


def calculate_indicators_node(state: TradingState) -> TradingState:
    """
    Calculate technical indicators from price history.

    Args:
        state: Current workflow state with price history

    Returns:
        Updated state with calculated indicators
    """
    print(f"Calculating indicators for {len(state['price_history'])} price points...")

    try:
        price_history = state['price_history']
        current_price = state['current_price']

        if len(price_history) < 2:
            return {
                **state,
                "indicators": {},
                "error": "Insufficient price history (need at least 2 data points)"
            }

        # Calculate Simple Moving Average (SMA)
        sma_20 = sum(price_history[-20:]) / min(20, len(price_history))
        sma_50 = sum(price_history[-50:]) / min(50, len(price_history)) if len(price_history) >= 50 else sma_20

        # Calculate price change percentage
        price_change = ((current_price - price_history[-1]) / price_history[-1]) * 100

        # Calculate volatility (standard deviation of last 20 prices)
        recent_prices = price_history[-20:]
        mean_price = sum(recent_prices) / len(recent_prices)
        variance = sum((p - mean_price) ** 2 for p in recent_prices) / len(recent_prices)
        volatility = variance ** 0.5

        # Calculate RSI (Relative Strength Index) - simplified version
        gains = []
        losses = []
        for i in range(1, min(14, len(price_history))):
            change = price_history[-i] - price_history[-(i+1)]
            if change > 0:
                gains.append(change)
            else:
                losses.append(abs(change))

        avg_gain = sum(gains) / len(gains) if gains else 0
        avg_loss = sum(losses) / len(losses) if losses else 0
        rs = avg_gain / avg_loss if avg_loss != 0 else 0
        rsi = 100 - (100 / (1 + rs))

        indicators = {
            "sma_20": round(sma_20, 2),
            "sma_50": round(sma_50, 2),
            "price_change_pct": round(price_change, 2),
            "volatility": round(volatility, 2),
            "rsi": round(rsi, 2),
            "current_price": current_price,
            "price_above_sma20": current_price > sma_20,
            "price_above_sma50": current_price > sma_50
        }

        print(f"Indicators calculated: SMA20={sma_20:.2f}, RSI={rsi:.2f}")

        return {
            **state,
            "indicators": indicators,
            "error": None
        }

    except Exception as e:
        error_msg = f"Error calculating indicators: {str(e)}"
        print(f"Error: {error_msg}")
        return {
            **state,
            "indicators": {},
            "error": error_msg
        }


def analyze_signals_node(state: TradingState) -> TradingState:
    """
    Analyze indicators and generate trading signal.

    Args:
        state: Current workflow state with indicators

    Returns:
        Updated state with trading signal and reasoning
    """
    print("Analyzing trading signals...")

    try:
        indicators = state.get('indicators', {})

        if not indicators or state.get('error'):
            return {
                **state,
                "signal": "HOLD",
                "confidence": 0.0,
                "reasoning": "Insufficient data for analysis"
            }

        current_price = indicators['current_price']
        sma_20 = indicators['sma_20']
        sma_50 = indicators['sma_50']
        rsi = indicators['rsi']
        price_change_pct = indicators['price_change_pct']

        # Decision logic
        signal = "HOLD"
        confidence = 50.0
        reasons = []

        # Trend analysis
        if current_price > sma_20 and current_price > sma_50:
            signal = "BUY"
            confidence += 15
            reasons.append(f"Price ({current_price:.2f}) above both SMA20 ({sma_20:.2f}) and SMA50 ({sma_50:.2f})")
        elif current_price < sma_20 and current_price < sma_50:
            signal = "SELL"
            confidence += 15
            reasons.append(f"Price ({current_price:.2f}) below both SMA20 ({sma_20:.2f}) and SMA50 ({sma_50:.2f})")

        # RSI analysis
        if rsi < 30:
            if signal != "SELL":
                signal = "BUY"
                confidence += 10
                reasons.append(f"RSI ({rsi:.2f}) indicates oversold condition")
        elif rsi > 70:
            if signal != "BUY":
                signal = "SELL"
                confidence += 10
                reasons.append(f"RSI ({rsi:.2f}) indicates overbought condition")

        # Price momentum
        if price_change_pct > 2:
            if signal == "BUY":
                confidence += 10
                reasons.append(f"Strong upward momentum ({price_change_pct:.2f}%)")
        elif price_change_pct < -2:
            if signal == "SELL":
                confidence += 10
                reasons.append(f"Strong downward momentum ({price_change_pct:.2f}%)")

        # Golden/Death cross
        if sma_20 > sma_50:
            if signal == "BUY":
                confidence += 5
                reasons.append("Golden cross pattern (SMA20 > SMA50)")
        else:
            if signal == "SELL":
                confidence += 5
                reasons.append("Death cross pattern (SMA20 < SMA50)")

        # Cap confidence at 100
        confidence = min(confidence, 100.0)

        reasoning = " | ".join(reasons) if reasons else "Neutral market conditions"

        print(f"Signal: {signal} (confidence: {confidence:.1f}%)")
        print(f"Reasoning: {reasoning}")

        return {
            **state,
            "signal": signal,
            "confidence": confidence,
            "reasoning": reasoning,
            "error": None
        }

    except Exception as e:
        error_msg = f"Error analyzing signals: {str(e)}"
        print(f"Error: {error_msg}")
        return {
            **state,
            "signal": "HOLD",
            "confidence": 0.0,
            "reasoning": "Error in analysis",
            "error": error_msg
        }
