import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  botStatus = {
    running: true,
    balance: 1000.00,
    holdings: 0.00,
    currentPrice: 0,
    totalValue: 1000.00,
    lastUpdate: new Date()
  };

  ngOnInit() {
    // Simulation de données - à remplacer par un appel API réel
    this.simulateData();
  }

  simulateData() {
    // Simule un prix Bitcoin aléatoire
    this.botStatus.currentPrice = 42000 + Math.random() * 2000;
    this.botStatus.totalValue = this.botStatus.balance + (this.botStatus.holdings * this.botStatus.currentPrice);
    this.botStatus.lastUpdate = new Date();
  }

  refreshData() {
    this.simulateData();
  }
}
