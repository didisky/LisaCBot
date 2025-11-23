import { Component } from '@angular/core';
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

  constructor(private botService: BotService) {}

  runBacktest() {
    this.loading = true;
    this.error = null;
    this.result = null;
    this.logs = [];

    this.addLog('Starting backtest...');
    this.addLog(`Configuration: ${this.config.days} days, $${this.config.initialBalance} initial balance`);

    this.botService.runBacktest(this.config.days, this.config.initialBalance).subscribe({
      next: (result) => {
        this.result = result;
        this.addLog('Backtest completed successfully');
        this.addLog(`Total trades executed: ${result.totalTrades}`);
        this.addLog(`Final P&L: $${result.profitLoss.toFixed(2)} (${result.profitLossPercentage.toFixed(2)}%)`);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to run backtest: ' + err.message;
        this.addLog(`ERROR: ${err.message}`);
        this.loading = false;
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
}
