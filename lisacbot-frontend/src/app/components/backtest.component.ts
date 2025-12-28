import { Component, ChangeDetectorRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BotService } from '../services/bot.service';
import { BacktestResult } from '../models/backtest-result.model';
import { BacktestConfig } from '../models/strategy-config.model';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartType } from 'chart.js';
import zoomPlugin from 'chartjs-plugin-zoom';
import 'chartjs-adapter-date-fns';

// Register the zoom plugin
Chart.register(zoomPlugin);

@Component({
  selector: 'app-backtest',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './backtest.component.html',
  styleUrls: ['./backtest.component.css']
})
export class BacktestComponent implements OnInit {
  config: BacktestConfig = {
    days: 30,
    initialBalance: 1000
  };

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  result: BacktestResult | null = null;
  loading = false;
  error: string | null = null;
  logs: string[] = [];
  Math = Math; // Expose Math to template

  // Current strategy information
  currentStrategyName: string = '';
  currentStrategyParameters: { [key: string]: string } = {};

  // Chart configuration
  public lineChartData: ChartConfiguration['data'] = {
    datasets: [],
    labels: []
  };

  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        type: 'time',
        time: {
          unit: 'day'
        },
        title: {
          display: true,
          text: 'Date'
        }
      },
      y: {
        title: {
          display: true,
          text: 'Price (USD)'
        }
      }
    },
    plugins: {
      legend: {
        display: true
      },
      tooltip: {
        mode: 'index',
        intersect: false
      },
      zoom: {
        zoom: {
          wheel: {
            enabled: true,
            speed: 0.1
          },
          pinch: {
            enabled: true
          },
          mode: 'x'
        },
        pan: {
          enabled: true,
          mode: 'x'
        },
        limits: {
          x: { min: 'original', max: 'original' }
        }
      }
    }
  };

  public lineChartType: ChartType = 'line';

  constructor(
    private botService: BotService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadCurrentStrategy();
  }

  loadCurrentStrategy() {
    console.log('🔵 Loading current strategy...');
    this.botService.getCurrentConfiguration().subscribe({
      next: (config) => {
        console.log('✅ Strategy config received:', config);
        this.currentStrategyName = config.strategyName || 'Unknown';
        this.currentStrategyParameters = config.strategyParameters || {};
        console.log('📊 Strategy name:', this.currentStrategyName);
        console.log('📊 Strategy parameters:', this.currentStrategyParameters);
        this.cdr.detectChanges(); // Force change detection
      },
      error: (err) => {
        console.error('❌ Failed to load current strategy:', err);
        // Set a fallback so the card still shows
        this.currentStrategyName = 'Unknown';
        this.currentStrategyParameters = {};
      }
    });
  }

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
        console.log('📊 Trades array:', result.trades);
        console.log('📊 Trades length:', result.trades?.length);
        console.log('📊 First trade:', result.trades?.[0]);
        this.result = result;
        this.addLog('Backtest completed successfully');
        this.addLog(`Total trades executed: ${result.totalTrades}`);
        this.addLog(`Final P&L: $${result.profitLoss.toFixed(2)} (${result.profitLossPercentage.toFixed(2)}%)`);
        this.loading = false;
        console.log('🟢 Backtest state - loading:', this.loading, 'result:', this.result);

        // Update chart with historical price data
        this.updateChart(result);

        // Force change detection
        this.cdr.detectChanges();
        console.log('🔄 Change detection triggered');
      },
      error: (err) => {
        console.error('❌ Backtest error:', err);
        console.error('❌ Error status:', err.status);
        console.error('❌ Error details:', err.error);

        let errorMsg = 'Unknown error';
        if (err.status === 401) {
          errorMsg = 'Authentication failed. Please log in again.';
        } else if (err.status === 403) {
          errorMsg = 'Access forbidden. Check your permissions.';
        } else if (err.status === 0) {
          errorMsg = 'Cannot connect to backend. Check network connection.';
        } else {
          errorMsg = err.error?.message || err.message || `HTTP ${err.status} error`;
        }

        this.error = 'Failed to run backtest: ' + errorMsg;
        this.addLog(`ERROR [${err.status}]: ${errorMsg}`);
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

  getCurrentParametersArray(): Array<{key: string, value: string}> {
    if (!this.currentStrategyParameters) return [];
    return Object.entries(this.currentStrategyParameters).map(([key, value]) => ({
      key,
      value
    }));
  }

  getTradeTypeClass(type: string): string {
    return type === 'BUY' ? 'trade-buy' : 'trade-sell';
  }

  getValidTrades() {
    if (!this.result || !this.result.trades) return [];
    // Filter out any null/undefined trades and ensure all required fields are present
    // Reverse the array to show most recent trades first
    return this.result.trades.filter(trade =>
      trade &&
      trade.type &&
      trade.price !== null && trade.price !== undefined &&
      trade.quantity !== null && trade.quantity !== undefined &&
      trade.timestamp
    ).reverse();
  }

  formatProfitLoss(profitLoss: number | null | undefined): string {
    if (profitLoss === null || profitLoss === undefined || typeof profitLoss !== 'number') {
      return 'N/A';
    }
    return profitLoss.toFixed(2) + '%';
  }

  getProfitLossClass(profitLoss: number | null | undefined): string {
    if (profitLoss === null || profitLoss === undefined || typeof profitLoss !== 'number') {
      return '';
    }
    if (profitLoss > 0) return 'positive';
    if (profitLoss < 0) return 'negative';
    return '';
  }

  updateChart(result: BacktestResult) {
    if (!result.historicalPrices || result.historicalPrices.length === 0) {
      console.warn('No historical prices available for chart');
      return;
    }

    // Prepare price chart data
    const labels = result.historicalPrices.map(p => new Date(p.timestamp));
    const priceData = result.historicalPrices.map(p => p.value);

    // Prepare trade data
    const buyTrades = result.trades?.filter(t => t.type === 'BUY') || [];
    const sellTrades = result.trades?.filter(t => t.type === 'SELL') || [];

    const buyTradeData = buyTrades.map(t => ({
      x: new Date(t.timestamp).getTime(),
      y: t.price
    }));

    const sellTradeData = sellTrades.map(t => ({
      x: new Date(t.timestamp).getTime(),
      y: t.price
    }));

    this.lineChartData = {
      labels: labels,
      datasets: [
        {
          type: 'line',
          data: priceData,
          label: 'BTC Price',
          borderColor: '#f7931a',
          backgroundColor: 'rgba(247, 147, 26, 0.1)',
          pointRadius: 0,
          pointHoverRadius: 4,
          borderWidth: 2,
          tension: 0.1,
          order: 2
        },
        {
          type: 'scatter',
          data: buyTradeData,
          label: 'BUY',
          borderColor: '#28a745',
          backgroundColor: '#28a745',
          pointRadius: 8,
          pointHoverRadius: 10,
          pointStyle: 'circle',
          order: 1
        },
        {
          type: 'scatter',
          data: sellTradeData,
          label: 'SELL',
          borderColor: '#dc3545',
          backgroundColor: '#dc3545',
          pointRadius: 8,
          pointHoverRadius: 10,
          pointStyle: 'circle',
          order: 1
        }
      ]
    };

    console.log('📈 Chart updated with', result.historicalPrices.length, 'price points,', buyTrades.length, 'BUY trades,', sellTrades.length, 'SELL trades');
  }

  resetZoom() {
    if (this.chart?.chart) {
      this.chart.chart.resetZoom();
      console.log('🔍 Chart zoom reset');
    }
  }
}
