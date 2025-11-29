export interface Trade {
  id: number;
  timestamp: string;
  type: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  balanceBefore: number;
  balanceAfter: number;
  profitLossPercentage: number | null;
  strategy: string;
  marketCycle: string;
  reason: string;
}
