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
    this.isLoading = true;

    // Fetch available strategies from backend
    this.botService.getAvailableStrategies().subscribe({
      next: (strategies) => {
        if (strategies && strategies.length > 0) {
          this.strategies = strategies;
          console.log('Loaded strategies from backend:', strategies);
        } else {
          console.warn('No strategies returned from backend');
        }

        // After strategies are loaded, fetch current bot status to set active strategy
        this.loadCurrentStrategy();
      },
      error: (err) => {
        console.error('Error loading strategies from backend:', err);
        this.isLoading = false;
        // Still try to load current strategy even if strategy list fails
        this.loadCurrentStrategy();
      }
    });
  }

  loadCurrentStrategy() {
    // Fetch current bot status to get active strategy
    this.botService.getBotStatus().subscribe({
      next: (status) => {
        console.log('Bot status received:', status);
        if (status.strategyName) {
          // Set the current strategy as default
          this.config.type = status.strategyName;
          console.log('Set active strategy to:', status.strategyName);
        } else {
          console.warn('No strategyName in bot status');
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading bot status:', err);
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    console.log('Configuration updated:', this.config);
    alert('Configuration will be saved! (Backend endpoint needed)');
  }
}
