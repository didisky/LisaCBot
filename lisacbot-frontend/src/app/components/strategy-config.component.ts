import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StrategyConfig } from '../models/strategy-config.model';
import { BotService } from '../services/bot.service';

@Component({
  selector: 'app-strategy-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './strategy-config.component.html',
  styleUrls: ['./strategy-config.component.css']
})
export class StrategyConfigComponent implements OnInit {
  config: StrategyConfig = {
    type: 'SMA',
    smaPeriod: 5,
    pollIntervalSeconds: 30
  };

  strategies: Array<{value: string, label: string, description?: string}> = [];
  isLoading = true;

  constructor(private botService: BotService) {}

  ngOnInit() {
    // Load both strategies and current bot status
    this.loadStrategiesAndStatus();
  }

  loadStrategiesAndStatus() {
    // Fetch available strategies
    this.botService.getAvailableStrategies().subscribe({
      next: (strategies) => {
        this.strategies = strategies;
        console.log('Loaded strategies:', strategies);

        // After strategies are loaded, fetch current bot status to set active strategy
        this.loadCurrentStrategy();
      },
      error: (err) => {
        console.error('Error loading strategies:', err);
        // Fallback to default strategies if API fails
        this.strategies = [
          { value: 'SMA', label: 'Simple Moving Average (SMA)' }
        ];
        this.isLoading = false;
      }
    });
  }

  loadCurrentStrategy() {
    // Fetch current bot status to get active strategy
    this.botService.getBotStatus().subscribe({
      next: (status) => {
        if (status.strategyName) {
          // Set the current strategy as default
          this.config.type = status.strategyName;
          console.log('Current active strategy:', status.strategyName);
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading current strategy:', err);
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    console.log('Configuration updated:', this.config);
    alert('Configuration will be saved! (Backend endpoint needed)');
  }
}
