export interface StrategyConfig {
  type: string;
  pollIntervalSeconds: number;
  smaPeriod: number;
  emaPeriod: number;
  rsiPeriod: number;
  rsiOversold: number;
  rsiOverbought: number;
  macdFastPeriod: number;
  macdSlowPeriod: number;
  macdSignalPeriod: number;
  compositeBuyThreshold: number;
  compositeSellThreshold: number;
}

export interface BacktestConfig {
  days: number;
  initialBalance: number;
}
