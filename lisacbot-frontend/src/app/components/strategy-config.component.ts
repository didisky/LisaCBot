import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StrategyConfig } from '../models/strategy-config.model';

@Component({
  selector: 'app-strategy-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './strategy-config.component.html',
  styleUrls: ['./strategy-config.component.css']
})
export class StrategyConfigComponent {
  config: StrategyConfig = {
    type: 'sma',
    smaPeriod: 5,
    pollIntervalSeconds: 30
  };

  strategies = [
    { value: 'sma', label: 'Simple Moving Average (SMA)' }
  ];

  onSubmit() {
    console.log('Configuration updated:', this.config);
    alert('Configuration will be saved! (Backend endpoint needed)');
  }
}
