"""
Bitcoin trading analysis graph definition.
"""

from langgraph.graph import StateGraph, END
from trading_state import TradingState
from trading_nodes import calculate_indicators_node, analyze_signals_node


def create_trading_graph():
    """
    Creates and returns a Bitcoin trading analysis workflow.

    The workflow:
    1. Calculates technical indicators from price history
    2. Analyzes indicators to generate trading signals

    Returns:
        Compiled LangGraph application for trading analysis
    """
    # Initialize the graph with TradingState
    workflow = StateGraph(TradingState)

    # Add nodes
    workflow.add_node("calculate_indicators", calculate_indicators_node)
    workflow.add_node("analyze_signals", analyze_signals_node)

    # Define the flow: calculate → analyze → end
    workflow.set_entry_point("calculate_indicators")
    workflow.add_edge("calculate_indicators", "analyze_signals")
    workflow.add_edge("analyze_signals", END)

    # Compile the graph
    app = workflow.compile()

    return app
