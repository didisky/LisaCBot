import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StrategyConfig } from '../models/strategy-config.model';
import { BotService } from '../services/bot.service';
import { forkJoin } from 'rxjs';

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
    pollIntervalSeconds: 30,
    smaPeriod: 5,
    emaPeriod: 12,
    rsiPeriod: 14,
    rsiOversold: 30,
    rsiOverbought: 70,
    macdFastPeriod: 12,
    macdSlowPeriod: 26,
    macdSignalPeriod: 9,
    compositeBuyThreshold: 0.5,
    compositeSellThreshold: -0.5
  };

  currentConfig: StrategyConfig = {
    type: 'SMA',
    pollIntervalSeconds: 30,
    smaPeriod: 5,
    emaPeriod: 12,
    rsiPeriod: 14,
    rsiOversold: 30,
    rsiOverbought: 70,
    macdFastPeriod: 12,
    macdSlowPeriod: 26,
    macdSignalPeriod: 9,
    compositeBuyThreshold: 0.5,
    compositeSellThreshold: -0.5
  };

  strategies: Array<{value: string, label: string, description?: string}> = [];
  isLoading = true;
  activeStrategy: string = ''; // The strategy currently running on the bot

  constructor(
    private botService: BotService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // Load strategies, current bot status, and configuration
    this.loadStrategiesAndStatus();
    this.loadCurrentConfiguration();
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

  loadCurrentConfiguration() {
    // Fetch current configuration to populate all values
    this.botService.getCurrentConfiguration().subscribe({
      next: (config) => {
        console.log('Current configuration received:', config);
        // Update form input values
        this.config.pollIntervalSeconds = config.pollIntervalSeconds || this.config.pollIntervalSeconds;
        this.config.smaPeriod = config.smaPeriod || this.config.smaPeriod;
        this.config.emaPeriod = config.emaPeriod || this.config.emaPeriod;
        this.config.rsiPeriod = config.rsiPeriod || this.config.rsiPeriod;
        this.config.rsiOversold = config.rsiOversold || this.config.rsiOversold;
        this.config.rsiOverbought = config.rsiOverbought || this.config.rsiOverbought;
        this.config.macdFastPeriod = config.macdFastPeriod || this.config.macdFastPeriod;
        this.config.macdSlowPeriod = config.macdSlowPeriod || this.config.macdSlowPeriod;
        this.config.macdSignalPeriod = config.macdSignalPeriod || this.config.macdSignalPeriod;
        this.config.compositeBuyThreshold = config.compositeBuyThreshold ?? this.config.compositeBuyThreshold;
        this.config.compositeSellThreshold = config.compositeSellThreshold ?? this.config.compositeSellThreshold;

        // Update current (actual) configuration values for display
        this.currentConfig.pollIntervalSeconds = config.pollIntervalSeconds || this.currentConfig.pollIntervalSeconds;
        this.currentConfig.smaPeriod = config.smaPeriod || this.currentConfig.smaPeriod;
        this.currentConfig.emaPeriod = config.emaPeriod || this.currentConfig.emaPeriod;
        this.currentConfig.rsiPeriod = config.rsiPeriod || this.currentConfig.rsiPeriod;
        this.currentConfig.rsiOversold = config.rsiOversold || this.currentConfig.rsiOversold;
        this.currentConfig.rsiOverbought = config.rsiOverbought || this.currentConfig.rsiOverbought;
        this.currentConfig.macdFastPeriod = config.macdFastPeriod || this.currentConfig.macdFastPeriod;
        this.currentConfig.macdSlowPeriod = config.macdSlowPeriod || this.currentConfig.macdSlowPeriod;
        this.currentConfig.macdSignalPeriod = config.macdSignalPeriod || this.currentConfig.macdSignalPeriod;
        this.currentConfig.compositeBuyThreshold = config.compositeBuyThreshold ?? this.currentConfig.compositeBuyThreshold;
        this.currentConfig.compositeSellThreshold = config.compositeSellThreshold ?? this.currentConfig.compositeSellThreshold;

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading configuration:', err);
      }
    });
  }

  onSubmit() {
    console.log('Applying configuration:', this.config);

    // Prepare configuration parameters
    const configParams = {
      smaPeriod: this.config.smaPeriod,
      emaPeriod: this.config.emaPeriod,
      rsiPeriod: this.config.rsiPeriod,
      rsiOversold: this.config.rsiOversold,
      rsiOverbought: this.config.rsiOverbought,
      macdFastPeriod: this.config.macdFastPeriod,
      macdSlowPeriod: this.config.macdSlowPeriod,
      macdSignalPeriod: this.config.macdSignalPeriod,
      compositeBuyThreshold: this.config.compositeBuyThreshold,
      compositeSellThreshold: this.config.compositeSellThreshold
    };

    // Update strategy, poll interval, and all configuration parameters
    forkJoin({
      strategy: this.botService.updateStrategy(this.config.type),
      pollInterval: this.botService.updatePollInterval(this.config.pollIntervalSeconds),
      parameters: this.botService.updateConfigurationParameters(configParams)
    }).subscribe({
      next: (responses) => {
        console.log('Configuration update responses:', responses);

        const successMessages: string[] = [];
        const errorMessages: string[] = [];

        if (responses.strategy.success) {
          successMessages.push(`Strategy: ${responses.strategy.strategy}`);
        } else {
          errorMessages.push(`Strategy: ${responses.strategy.message}`);
        }

        if (responses.pollInterval.success) {
          successMessages.push(`Poll Interval: ${responses.pollInterval.pollIntervalSeconds}s`);
        } else {
          errorMessages.push(`Poll Interval: ${responses.pollInterval.message}`);
        }

        if (responses.parameters.success) {
          successMessages.push(`All strategy parameters updated`);
        } else {
          errorMessages.push(`Parameters: ${responses.parameters.message}`);
        }

        if (errorMessages.length === 0) {
          alert(`Configuration updated successfully!\n${successMessages.join('\n')}`);
          // Reload current strategy and configuration to reflect the changes
          this.loadCurrentStrategy();
          this.loadCurrentConfiguration();
        } else {
          alert(`Configuration update completed with errors:\n${errorMessages.join('\n')}`);
        }
      },
      error: (err) => {
        console.error('Error updating configuration:', err);
        alert('Failed to update configuration. Please check the console for details.');
      }
    });
  }
}
