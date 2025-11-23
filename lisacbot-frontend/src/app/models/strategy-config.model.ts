export interface StrategyConfig {
  type: string;
  smaPeriod: number;
  pollIntervalSeconds: number;
}

export interface BacktestConfig {
  days: number;
  initialBalance: number;
}
