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
        display: true,
        title: {
          display: true,
          text: 'Time'
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

  private statusSubscription?: Subscription;
  private tradeEventSubscription?: Subscription;

  constructor(
    private botService: BotService,
    private tradeEventService: TradeEventService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Subscribe to the shared status observable
    this.statusSubscription = this.botService.status$.subscribe({
      next: (status) => {
        if (status) {
          this.botStatus = {
            running: status.running,
            balance: status.balance,
            holdings: status.holdings,
            currentPrice: status.lastPrice,
            totalValue: status.totalValue,
            marketCycle: status.marketCycle || '',
            lastUpdate: new Date()
          };

          // Force change detection
          this.cdr.detectChanges();

          // Update price history for the chart only if price changed
          if (status.lastPrice > 0 && status.lastPrice !== this.lastRecordedPrice) {
            this.updatePriceHistory(status.lastPrice);
            this.lastRecordedPrice = status.lastPrice;
          }
        }
      },
      error: (err) => {
        console.error('Error fetching bot status:', err);
      }
    });

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
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
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
    this.botService.getBotStatus().subscribe({
      next: (status) => {
        this.botStatus = {
          running: status.running,
          balance: status.balance,
          holdings: status.holdings,
          currentPrice: status.lastPrice,
          totalValue: status.totalValue,
          marketCycle: status.marketCycle || '',
          lastUpdate: new Date()
        };
      },
      error: (err) => {
        console.error('Error fetching bot status:', err);
      }
    });
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

    // Add trade markers to chart
    this.trades.forEach(trade => {
      const tradeTime = new Date(trade.timestamp);
      const timeLabel = tradeTime.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });

      // Create data point with x/y coordinates
      const dataPoint = {
        x: timeLabel,
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
}
