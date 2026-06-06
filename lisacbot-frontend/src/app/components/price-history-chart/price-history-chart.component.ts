import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import 'chartjs-adapter-date-fns';
import { BotService } from '../../services/bot.service';
import type { PriceEntry } from '../../models/price.model';

Chart.register(...registerables);

interface Period {
  label: string;
  hours: number;
}

@Component({
  selector: 'app-price-history-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './price-history-chart.component.html',
  styleUrls: ['./price-history-chart.component.css'],
})
export class PriceHistoryChartComponent implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  periods: Period[] = [
    { label: '1H', hours: 1 },
    { label: '6H', hours: 6 },
    { label: '24H', hours: 24 },
    { label: '7J', hours: 168 },
  ];
  selectedHours = 24;

  loading = false;
  hasData = false;

  chartData: ChartConfiguration['data'] = {
    datasets: [
      {
        data: [],
        label: 'BTC (USD)',
        borderColor: '#4f8ef7',
        backgroundColor: 'rgba(79,142,247,0.08)',
        borderWidth: 2,
        pointRadius: 0,
        pointHoverRadius: 4,
        fill: true,
        tension: 0.3,
      },
    ],
  };

  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => ctx.parsed.y == null ? '' : ` $${ctx.parsed.y.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
        },
      },
    },
    scales: {
      x: {
        type: 'time',
        time: { tooltipFormat: 'dd MMM HH:mm', displayFormats: { hour: 'HH:mm', day: 'dd MMM' } },
        ticks: { maxTicksLimit: 8, color: '#6c757d' },
        grid: { color: 'rgba(0,0,0,0.05)' },
      },
      y: {
        title: { display: true, text: 'USD', color: '#6c757d' },
        ticks: {
          color: '#6c757d',
          callback: (v) => '$' + Number(v).toLocaleString('en-US'),
        },
        grid: { color: 'rgba(0,0,0,0.05)' },
      },
    },
  };

  private refreshInterval?: ReturnType<typeof setInterval>;

  constructor(private botService: BotService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadData();
    this.refreshInterval = setInterval(() => this.loadData(), 30_000);
  }

  ngOnDestroy() {
    clearInterval(this.refreshInterval);
  }

  selectPeriod(hours: number) {
    this.selectedHours = hours;
    this.loadData();
  }

  loadData() {
    this.loading = true;
    const end = new Date();
    const start = new Date(end.getTime() - this.selectedHours * 3_600_000);

    this.botService.getPricesByRange(start, end).subscribe({
      next: (entries: PriceEntry[]) => {
        this.hasData = entries.length > 0;
        this.chartData = {
          datasets: [
            {
              ...this.chartData.datasets[0],
              data: entries.map((e) => ({ x: new Date(e.timestamp).getTime(), y: e.value })),
            },
          ],
        };
        this.loading = false;
        this.chart?.update();
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }
}
