"""
Node functions for Bitcoin trading analysis workflow using OpenAI.
"""

import os
import json
from typing import Dict, Any
from openai import OpenAI
from trading_state import TradingState


def analyze_with_openai_node(state: TradingState) -> TradingState:
    """
    Analyze price history and generate trading signal using OpenAI API.

    Args:
        state: Current workflow state with price history

    Returns:
        Updated state with trading signal, confidence, reasoning, and indicators
    """
    print(f"Analyzing {len(state['price_history'])} price points with OpenAI...")

    try:
        price_history = state['price_history']
        current_price = state['current_price']

        if len(price_history) < 2:
            return {
                **state,
                "signal": "HOLD",
                "confidence": 0.0,
                "reasoning": "Insufficient price history (need at least 2 data points)",
                "indicators": {},
                "error": "Insufficient data"
            }

        client = OpenAI(api_key=os.environ.get("OPENAI_API_KEY"))

        prompt = f"""You are a Bitcoin trading analyst. Analyze the following price data and provide a trading recommendation.

Current Bitcoin Price: ${current_price:.2f}

Recent Price History (last {len(price_history)} prices):
{price_history}

Please analyze this data and provide:
1. A trading signal (BUY, SELL, or HOLD)
2. A confidence level from 0 to 100
3. Technical indicators you calculated (SMA20, SMA50, RSI, volatility, price_change_pct)
4. Clear reasoning for your recommendation

Respond ONLY with valid JSON in this exact format:
{{
  "signal": "BUY|SELL|HOLD",
  "confidence": 0-100,
  "reasoning": "your detailed reasoning",
  "indicators": {{
    "sma_20": number,
    "sma_50": number,
    "rsi": number,
    "volatility": number,
    "price_change_pct": number,
    "current_price": {current_price},
    "price_above_sma20": boolean,
    "price_above_sma50": boolean
  }}
}}"""

        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "You are a professional cryptocurrency trading analyst. Always respond with valid JSON only, no additional text."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.3,
            response_format={"type": "json_object"}
        )

        result = json.loads(response.choices[0].message.content)

        signal = result.get("signal", "HOLD")
        confidence = float(result.get("confidence", 50.0))
        reasoning = result.get("reasoning", "OpenAI analysis")
        indicators = result.get("indicators", {})

        print(f"OpenAI Signal: {signal} (confidence: {confidence:.1f}%)")
        print(f"Reasoning: {reasoning}")

        return {
            **state,
            "signal": signal,
            "confidence": confidence,
            "reasoning": reasoning,
            "indicators": indicators,
            "error": None
        }

    except Exception as e:
        error_msg = f"Error calling OpenAI API: {str(e)}"
        print(f"Error: {error_msg}")
        return {
            **state,
            "signal": "HOLD",
            "confidence": 0.0,
            "reasoning": "Error in OpenAI analysis",
            "indicators": {},
            "error": error_msg
        }
