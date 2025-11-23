import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BotService } from '../services/bot.service';
import { Subscription } from 'rxjs';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  botStatus = {
    running: true,
    balance: 1000.00,
    holdings: 0.00,
    currentPrice: 0,
    totalValue: 1000.00,
    lastUpdate: new Date()
  };

  // Price history for the chart (last 20 data points)
  priceHistory: Array<{time: Date, price: number}> = [];
  maxDataPoints = 20;
  lastRecordedPrice: number = 0;

  // Chart configuration
  public lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'BTC Price (USD)',
        fill: true,
        tension: 0.4,
        borderColor: '#4CAF50',
        backgroundColor: 'rgba(76, 175, 80, 0.1)',
        pointBackgroundColor: '#4CAF50',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: '#4CAF50'
      }
    ]
  };

  public lineChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      tooltip: {
        mode: 'index',
        intersect: false,
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
          text: 'Price (USD)'
        }
      }
    }
  };

  private statusSubscription?: Subscription;

  constructor(private botService: BotService) {}

  ngOnInit() {
    // Subscribe to the shared status observable
    this.statusSubscription = this.botService.status$.subscribe({
      next: (status) => {
        if (status) {
          this.botStatus = {
            running: status.running,
            balance: status.balance,
            holdings: status.holdings,
            currentPrice: status.currentPrice,
            totalValue: status.totalValue,
            lastUpdate: new Date()
          };

          // Update price history for the chart only if price changed
          if (status.currentPrice > 0 && status.currentPrice !== this.lastRecordedPrice) {
            this.updatePriceHistory(status.currentPrice);
            this.lastRecordedPrice = status.currentPrice;
          }
        }
      },
      error: (err) => {
        console.error('Error fetching bot status:', err);
      }
    });
  }

  ngOnDestroy() {
    // Unsubscribe when component is destroyed
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
  }

  updatePriceHistory(price: number) {
    const now = new Date();

    // Add new price to history
    this.priceHistory.push({
      time: now,
      price: price
    });

    // Keep only the last maxDataPoints
    if (this.priceHistory.length > this.maxDataPoints) {
      this.priceHistory.shift();
    }

    // Update chart data
    this.lineChartData.labels = this.priceHistory.map(p =>
      p.time.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    );
    this.lineChartData.datasets[0].data = this.priceHistory.map(p => p.price);

    // Trigger chart update
    this.chart?.update();
  }

  refreshData() {
    this.botService.getBotStatus().subscribe({
      next: (status) => {
        this.botStatus = {
          running: status.running,
          balance: status.balance,
          holdings: status.holdings,
          currentPrice: status.currentPrice,
          totalValue: status.totalValue,
          lastUpdate: new Date()
        };
      },
      error: (err) => {
        console.error('Error fetching bot status:', err);
      }
    });
  }
}
