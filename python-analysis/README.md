# LangGraph Trading Analysis

Analyse technique avancée pour Bitcoin utilisant LangGraph.

## 📦 Installation

```bash
cd python-analysis
pip3 install -r requirements.txt
```

## 🚀 Utilisation depuis Java

La stratégie `LangGraphStrategy` appelle automatiquement le script Python:

```java
import com.lisacbot.domain.strategy.LangGraphStrategy;

// Créer la stratégie (garde 100 prix en historique)
LangGraphStrategy strategy = new LangGraphStrategy(100);

// Analyser un prix
Signal signal = strategy.analyze(53400.0);
// Retourne: BUY, SELL, ou HOLD
```

## 🧪 Test Manuel du Script Python

```bash
cd langgraph_trading

# Test avec quelques prix
python3 analyze.py 53400 50000 50200 50100 49900 49800

# Sortie JSON:
# {
#   "signal": "BUY",
#   "confidence": 75.0,
#   "reasoning": "Price above both SMA20 and SMA50...",
#   "indicators": {...}
# }
```

## 📊 Indicateurs Calculés

- **SMA 20/50**: Moyennes mobiles simples
- **RSI**: Relative Strength Index (surachat/survente)
- **Volatilité**: Écart-type des prix
- **Price Change %**: Variation du prix

## 🎯 Logique de Trading

### BUY
- Prix > SMA20 ET SMA50
- RSI < 30 (survente)
- Momentum positif

### SELL
- Prix < SMA20 ET SMA50
- RSI > 70 (surachat)
- Momentum négatif

### HOLD
- Conditions neutres
- Données insuffisantes

## 📁 Structure

```
python-analysis/
├── requirements.txt
├── README.md
└── langgraph_trading/
    ├── __init__.py
    ├── analyze.py           # Script principal
    ├── trading_state.py     # Définition de l'état
    ├── trading_nodes.py     # Calcul des indicateurs
    └── trading_graph.py     # Workflow LangGraph
```

## 🔧 Configuration

Le chemin du script Python est détecté automatiquement. Pour le personnaliser:

```java
String customPath = "/path/to/analyze.py";
LangGraphStrategy strategy = new LangGraphStrategy(100, customPath);
```
