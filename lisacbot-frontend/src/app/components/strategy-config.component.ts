import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
  activeStrategy: string = ''; // The strategy currently running on the bot

  constructor(
    private botService: BotService,
    private cdr: ChangeDetectorRef
  ) {}

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
          // Force change detection to update the view
          this.cdr.detectChanges();
        } else {
          console.warn('No strategies returned from backend');
        }

        // After strategies are loaded, fetch current bot status to set active strategy
        this.loadCurrentStrategy();
      },
      error: (err) => {
        console.error('Error loading strategies from backend:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
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
          // Store the currently active strategy
          this.activeStrategy = status.strategyName;
          // Also set it as the default selected value in dropdown
          this.config.type = status.strategyName;
          console.log('Active strategy:', status.strategyName);
        } else {
          console.warn('No strategyName in bot status');
        }
        this.isLoading = false;
        // Force change detection to update the view
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading bot status:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit() {
    console.log('Applying configuration:', this.config);

    // Update strategy on the backend
    this.botService.updateStrategy(this.config.type).subscribe({
      next: (response) => {
        console.log('Strategy update response:', response);
        if (response.success) {
          alert(`Strategy updated successfully to ${response.strategy}!`);
          // Reload current strategy to reflect the change
          this.loadCurrentStrategy();
        } else {
          alert(`Failed to update strategy: ${response.message}`);
        }
      },
      error: (err) => {
        console.error('Error updating strategy:', err);
        alert('Failed to update strategy. Please check the console for details.');
      }
    });
  }
}
