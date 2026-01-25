"""
State definition for Bitcoin trading analysis workflow.
"""

from typing import TypedDict, Optional, List


class TradingState(TypedDict):
    """State structure for trading analysis workflow."""
    price_history: List[float]  # Historical Bitcoin prices
    current_price: float  # Current Bitcoin price
    signal: Optional[str]  # Trading signal: BUY, SELL, or HOLD
    confidence: Optional[float]  # Confidence level (0-100)
    indicators: Optional[dict]  # Technical indicators
    reasoning: Optional[str]  # Explanation of the decision
    error: Optional[str]  # Error message if any
