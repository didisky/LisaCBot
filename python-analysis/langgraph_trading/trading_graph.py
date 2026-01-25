"""
Bitcoin trading analysis graph definition using OpenAI.
"""

from langgraph.graph import StateGraph, END
from trading_state import TradingState
from trading_nodes import analyze_with_openai_node


def create_trading_graph():
    """
    Creates and returns a Bitcoin trading analysis workflow.

    The workflow:
    1. Analyzes price history with OpenAI to generate trading signals and indicators

    Returns:
        Compiled LangGraph application for trading analysis
    """
    # Initialize the graph with TradingState
    workflow = StateGraph(TradingState)

    # Add single OpenAI analysis node
    workflow.add_node("analyze_with_openai", analyze_with_openai_node)

    # Define the flow: analyze → end
    workflow.set_entry_point("analyze_with_openai")
    workflow.add_edge("analyze_with_openai", END)

    # Compile the graph
    app = workflow.compile()

    return app
