import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BotService } from '../services/bot.service';
import { BacktestResult } from '../models/backtest-result.model';
import { BacktestConfig } from '../models/strategy-config.model';

@Component({
  selector: 'app-backtest',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './backtest.component.html',
  styleUrls: ['./backtest.component.css']
})
export class BacktestComponent {
  config: BacktestConfig = {
    days: 30,
    initialBalance: 1000
  };

  result: BacktestResult | null = null;
  loading = false;
  error: string | null = null;
  logs: string[] = [];
  Math = Math; // Expose Math to template

  constructor(
    private botService: BotService,
    private cdr: ChangeDetectorRef
  ) {}

  runBacktest() {
    this.loading = true;
    this.error = null;
    this.result = null;
    this.logs = [];

    this.addLog('Starting backtest...');
    this.addLog(`Configuration: ${this.config.days} days, $${this.config.initialBalance} initial balance`);
    console.log('🔵 Backtest started with config:', this.config);

    this.botService.runBacktest(this.config.days, this.config.initialBalance).subscribe({
      next: (result) => {
        console.log('✅ Backtest result received:', result);
        this.result = result;
        this.addLog('Backtest completed successfully');
        this.addLog(`Total trades executed: ${result.totalTrades}`);
        this.addLog(`Final P&L: $${result.profitLoss.toFixed(2)} (${result.profitLossPercentage.toFixed(2)}%)`);
        this.loading = false;
        console.log('🟢 Backtest state - loading:', this.loading, 'result:', this.result);

        // Force change detection
        this.cdr.detectChanges();
        console.log('🔄 Change detection triggered');
      },
      error: (err) => {
        console.error('❌ Backtest error:', err);
        this.error = 'Failed to run backtest: ' + (err.error?.message || err.message || 'Unknown error');
        this.addLog(`ERROR: ${err.error?.message || err.message || 'Unknown error'}`);
        this.loading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        console.log('🏁 Backtest observable completed');
      }
    });
  }

  addLog(message: string) {
    const timestamp = new Date().toLocaleTimeString();
    this.logs.push(`[${timestamp}] ${message}`);
  }

  getResultClass(): string {
    if (!this.result) return '';
    return this.result.profitLoss >= 0 ? 'profit' : 'loss';
  }

  getPerformanceIcon(): string {
    if (!this.result) return '';
    if (this.result.profitLoss > 0) return '📈';
    if (this.result.profitLoss < 0) return '📉';
    return '➡️';
  }

  getParametersArray(): Array<{key: string, value: string}> {
    if (!this.result || !this.result.strategyParameters) return [];
    return Object.entries(this.result.strategyParameters).map(([key, value]) => ({
      key,
      value
    }));
  }
}
