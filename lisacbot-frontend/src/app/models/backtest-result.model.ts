import { Trade } from './trade.model';

export interface BacktestResult {
  initialBalance: number;
  finalBalance: number;
  totalTrades: number;
  buyTrades: number;
  sellTrades: number;
  profitLoss: number;
  profitLossPercentage: number;
  days: number;
  strategyName: string;
  strategyParameters: { [key: string]: string };
  trades: Trade[];
}
