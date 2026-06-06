import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import 'chartjs-adapter-date-fns';
import { Subscription } from 'rxjs';
import { BotService } from '../../services/bot.service';
import { PriceEventService } from '../../services/price-event.service';
import type { PriceEntry } from '../../models/price.model';

Chart.register(...registerables);

interface Period {
  label: string;
  hours: number;
  timeUnit: 'minute' | 'hour' | 'day' | 'week' | 'month';
  tooltipFormat: string;
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
    { label: '1H',  hours: 1,    timeUnit: 'minute', tooltipFormat: 'HH:mm' },
    { label: '6H',  hours: 6,    timeUnit: 'hour',   tooltipFormat: 'HH:mm' },
    { label: '24H', hours: 24,   timeUnit: 'hour',   tooltipFormat: 'dd MMM HH:mm' },
    { label: '7J',  hours: 168,  timeUnit: 'day',    tooltipFormat: 'dd MMM' },
    { label: '1M',  hours: 720,  timeUnit: 'week',   tooltipFormat: 'dd MMM yyyy' },
    { label: '6M',  hours: 4380, timeUnit: 'month',  tooltipFormat: 'MMM yyyy' },
    { label: '1A',  hours: 8760, timeUnit: 'month',  tooltipFormat: 'MMM yyyy' },
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

  private priceEventSubscription?: Subscription;

  constructor(
    private botService: BotService,
    private cdr: ChangeDetectorRef,
    private priceEventService: PriceEventService
  ) {}

  ngOnInit() {
    this.loadData();
    this.priceEventService.connect();
    this.priceEventSubscription = this.priceEventService.getPriceEvents().subscribe(() => this.loadData());
  }

  ngOnDestroy() {
    this.priceEventSubscription?.unsubscribe();
    this.priceEventService.disconnect();
  }

  selectPeriod(hours: number) {
    this.selectedHours = hours;
    this.updateChartTimeScale();
    this.loadData();
  }

  private updateChartTimeScale() {
    const period = this.periods.find(p => p.hours === this.selectedHours)!;
    this.chartOptions = {
      ...this.chartOptions,
      scales: {
        ...this.chartOptions!['scales'],
        x: {
          type: 'time',
          time: {
            unit: period.timeUnit,
            tooltipFormat: period.tooltipFormat,
            displayFormats: {
              minute: 'HH:mm',
              hour: 'HH:mm',
              day: 'dd MMM',
              week: 'dd MMM',
              month: 'MMM yyyy',
            },
          },
          ticks: { maxTicksLimit: 8, color: '#6c757d' },
          grid: { color: 'rgba(0,0,0,0.05)' },
        },
      },
    };
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
