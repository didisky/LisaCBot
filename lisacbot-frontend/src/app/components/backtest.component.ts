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
  Math = Math; // Expose Math to template

  constructor(private botService: BotService) {}

  runBacktest() {
    this.loading = true;
    this.error = null;
    this.result = null;

    this.botService.runBacktest(this.config.days).subscribe({
      next: (result) => {
        this.result = result;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to run backtest: ' + err.message;
        this.loading = false;
      }
    });
  }

  getResultClass(): string {
    if (!this.result) return '';
    return this.result.profitLoss >= 0 ? 'profit' : 'loss';
  }
}
