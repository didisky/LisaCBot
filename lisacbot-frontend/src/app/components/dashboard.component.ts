import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BotService } from '../services/bot.service';
import { TradeEventService } from '../services/trade-event.service';
import { Trade } from '../models/trade.model';
import { Subscription } from 'rxjs';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { trigger, state, style, transition, animate } from '@angular/animations';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateY(-100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateY(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateY(-100%)', opacity: 0 }))
      ])
    ])
  ]
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  botStatus = {
    running: true,
    balance: 1000.00,
    holdings: 0.00,
    currentPrice: 0,
    totalValue: 1000.00,
    marketCycle: '',
    strategyName: '',
    lastUpdate: new Date()
  };

  // Price history for the chart (last 20 data points)
  priceHistory: Array<{time: Date, price: number}> = [];
  maxDataPoints = 20;
  lastRecordedPrice: number = 0;

  // Trade history
  trades: Trade[] = [];
  displayedTrades: Trade[] = [];
  maxDisplayedTrades = 10;

  // Chart configuration
  public lineChartData: any = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'BUY',
        pointRadius: 8,
        pointHoverRadius: 10,
        pointBackgroundColor: '#28a745',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        showLine: false,
        type: 'scatter'
      },
      {
        data: [],
        label: 'SELL',
        pointRadius: 8,
        pointHoverRadius: 10,
        pointBackgroundColor: '#dc3545',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        showLine: false,
        type: 'scatter'
      }
    ]
  };

  public lineChartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      tooltip: {
        mode: 'point',
        intersect: true,
        callbacks: {
          title: (context: any) => {
            // Format tooltip title with date and time
            const timestamp = context[0].parsed.x;
            const date = new Date(timestamp);
            return date.toLocaleString('en-US', {
              month: 'short',
              day: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            });
          },
          label: (context: any) => {
            const label = context.dataset.label || '';
            const value = context.parsed.y;
            return `${label}: $${value.toFixed(2)}`;
          }
        }
      }
    },
    scales: {
      x: {
        type: 'linear',
        display: true,
        title: {
          display: true,
          text: 'Date & Time'
        },
        ticks: {
          callback: (value: any) => {
            // Format x-axis labels to show date and time
            const date = new Date(value);
            return date.toLocaleString('en-US', {
              month: 'short',
              day: 'numeric',
              hour: '2-digit',
              minute: '2-digit'
            });
          }
        }
      },
      y: {
        display: true,
        title: {
          display: true,
          text: 'BTC Price (USD)'
        }
      }
    }
  };

  // Notification for new trades
  showNewTradeNotification = false;
  newTradeMessage = '';

  private tradeEventSubscription?: Subscription;

  constructor(
    private botService: BotService,
    private tradeEventService: TradeEventService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Load initial status
    this.refreshData();

    // Load trade history
    this.loadTradeHistory();

    // Connect to SSE for real-time trade notifications
    this.tradeEventService.connect();
    this.tradeEventSubscription = this.tradeEventService.getTradeEvents().subscribe({
      next: (trade) => {
        console.log('🔔 New trade notification:', trade);
        this.handleNewTrade(trade);
      },
      error: (err) => {
        console.error('Error receiving trade event:', err);
      }
    });
  }

  ngOnDestroy() {
    // Unsubscribe when component is destroyed
    if (this.tradeEventSubscription) {
      this.tradeEventSubscription.unsubscribe();
    }
    // Disconnect from SSE
    this.tradeEventService.disconnect();
  }

  handleNewTrade(trade: Trade) {
    // Add trade to the beginning of the list
    this.trades.unshift(trade);
    this.displayedTrades = this.trades.slice(0, this.maxDisplayedTrades);

    // Update trade markers on chart
    this.updateTradeMarkers();

    // Show notification
    const profitLoss = trade.profitLossPercentage
      ? ` (${trade.profitLossPercentage > 0 ? '+' : ''}${trade.profitLossPercentage.toFixed(2)}%)`
      : '';
    this.newTradeMessage = `${trade.type} at $${trade.price.toFixed(2)}${profitLoss}`;
    this.showNewTradeNotification = true;

    // Auto-hide notification after 5 seconds
    setTimeout(() => {
      this.showNewTradeNotification = false;
      this.cdr.detectChanges();
    }, 5000);

    // Force change detection
    this.cdr.detectChanges();
  }

  updatePriceHistory(price: number) {
    // Price history tracking removed - chart now only shows trade markers
    // This method is kept for compatibility but does nothing
  }

  refreshData() {
    // Refresh bot status
    this.botService.getBotStatus().subscribe({
      next: (status) => {
        this.botStatus = {
          running: status.running,
          balance: status.balance,
          holdings: status.holdings,
          currentPrice: status.lastPrice,
          totalValue: status.totalValue,
          marketCycle: status.marketCycle || '',
          strategyName: status.strategyName || '',
          lastUpdate: new Date()
        };
      },
      error: (err) => {
        console.error('Error fetching bot status:', err);
      }
    });

    // Refresh trade history
    this.loadTradeHistory();
  }

  loadTradeHistory() {
    this.botService.getTradeHistory().subscribe({
      next: (trades) => {
        this.trades = trades;
        // Display only the most recent trades
        this.displayedTrades = trades.slice(0, this.maxDisplayedTrades);

        // Update trade markers on chart
        this.updateTradeMarkers();

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching trade history:', err);
      }
    });
  }

  updateTradeMarkers() {
    // Clear existing trade markers
    this.lineChartData.datasets[0].data = [];
    this.lineChartData.datasets[1].data = [];

    // Sort trades from oldest to newest
    const sortedTrades = [...this.trades].sort((a, b) => {
      return new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime();
    });

    // Add trade markers to chart
    sortedTrades.forEach(trade => {
      const tradeTime = new Date(trade.timestamp);

      // Create data point with x/y coordinates using timestamp
      const dataPoint = {
        x: tradeTime.getTime(), // Use timestamp for proper time-series
        y: trade.price
      };

      if (trade.type === 'BUY') {
        this.lineChartData.datasets[0].data.push(dataPoint);
      } else if (trade.type === 'SELL') {
        this.lineChartData.datasets[1].data.push(dataPoint);
      }
    });

    // Trigger chart update
    this.chart?.update();
  }

  getTradeTypeClass(type: string): string {
    return type === 'BUY' ? 'trade-buy' : 'trade-sell';
  }

  getMarketCycleClass(): string {
    const cycle = this.botStatus.marketCycle?.toUpperCase();
    switch (cycle) {
      case 'ACCUMULATION':
        return 'market-cycle-accumulation';
      case 'MARKUP':
        return 'market-cycle-markup';
      case 'DISTRIBUTION':
        return 'market-cycle-distribution';
      case 'MARKDOWN':
        return 'market-cycle-markdown';
      default:
        return '';
    }
  }

  startBot() {
    this.botService.startBot().subscribe({
      next: (response) => {
        console.log('Bot started:', response);
        // Refresh status to update UI
        this.refreshData();
      },
      error: (err) => {
        console.error('Error starting bot:', err);
      }
    });
  }

  stopBot() {
    this.botService.stopBot().subscribe({
      next: (response) => {
        console.log('Bot stopped:', response);
        // Refresh status to update UI
        this.refreshData();
      },
      error: (err) => {
        console.error('Error stopping bot:', err);
      }
    });
  }
}
