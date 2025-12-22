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

  constructor(private botService: BotService) {}

  ngOnInit() {
    // Fetch available strategies from backend
    this.botService.getAvailableStrategies().subscribe({
      next: (strategies) => {
        this.strategies = strategies;
        console.log('Loaded strategies:', strategies);
      },
      error: (err) => {
        console.error('Error loading strategies:', err);
        // Fallback to default strategies if API fails
        this.strategies = [
          { value: 'SMA', label: 'Simple Moving Average (SMA)' }
        ];
      }
    });
  }

  onSubmit() {
    console.log('Configuration updated:', this.config);
    alert('Configuration will be saved! (Backend endpoint needed)');
  }
}
