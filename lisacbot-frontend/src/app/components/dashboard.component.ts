import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BotService } from '../services/bot.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  botStatus = {
    running: true,
    balance: 1000.00,
    holdings: 0.00,
    currentPrice: 0,
    totalValue: 1000.00,
    lastUpdate: new Date()
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
