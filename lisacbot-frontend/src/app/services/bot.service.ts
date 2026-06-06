import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { BacktestResult } from '../models/backtest-result.model';
import type { PriceEntry } from '../models/price.model';
import { Trade } from '../models/trade.model';

export interface BotStatus {
  running: boolean;
  balance: number;
  holdings: number;
  lastPrice: number;
  totalValue: number;
  marketCycle?: any;  // Optional market cycle information
  strategyName?: string;  // Active trading strategy
}

@Injectable({
  providedIn: 'root'
})
export class BotService {
  // Use relative URL to work with both development and Docker deployments
  // In dev (ng serve): proxied to localhost:8080 via proxy.conf.json
  // In Docker: proxied to backend via Nginx
  private apiUrl = '/api';
  private statusSubject = new BehaviorSubject<BotStatus | null>(null);
  public status$ = this.statusSubject.asObservable();

  constructor(private http: HttpClient) {
    // Polling removed - status is now loaded manually on dashboard init and refresh
  }

  runBacktest(days?: number, balance?: number): Observable<BacktestResult> {
    if (days !== undefined && balance !== undefined) {
      const url = `${this.apiUrl}/backtest/custom?days=${days}&balance=${balance}`;
      console.log('📡 Calling backtest endpoint:', url);
      return this.http.post<BacktestResult>(url, {}).pipe(
        tap(result => console.log('📥 Backtest response received:', result))
      );
    }
    console.log('📡 Calling default backtest endpoint');
    return this.http.post<BacktestResult>(`${this.apiUrl}/backtest`, {}).pipe(
      tap(result => console.log('📥 Backtest response received:', result))
    );
  }

  getBotStatus(): Observable<BotStatus> {
    return this.http.get<BotStatus>(`${this.apiUrl}/status`);
  }

  getTradeHistory(): Observable<Trade[]> {
    return this.http.get<Trade[]>(`${this.apiUrl}/trades`);
  }

  getTradesByRange(start: Date, end: Date): Observable<Trade[]> {
    const startStr = start.toISOString().slice(0, 19);
    const endStr = end.toISOString().slice(0, 19);
    return this.http.get<Trade[]>(`${this.apiUrl}/trades/range?start=${startStr}&end=${endStr}`);
  }

  startBot(): Observable<any> {
    return this.http.post(`${this.apiUrl}/bot/start`, {});
  }

  stopBot(): Observable<any> {
    return this.http.post(`${this.apiUrl}/bot/stop`, {});
  }

  getAvailableStrategies(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/config/strategies`);
  }

  updateStrategy(strategyType: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/config/strategy`, { type: strategyType });
  }

  updatePollInterval(pollIntervalSeconds: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/config/poll-interval`, { pollIntervalSeconds });
  }

  updateConfigurationParameters(params: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/config/parameters`, params);
  }

  getCurrentConfiguration(): Observable<any> {
    return this.http.get(`${this.apiUrl}/config/current`);
  }

  getPricesByRange(start: Date, end: Date): Observable<PriceEntry[]> {
    const startStr = start.toISOString().slice(0, 19);
    const endStr = end.toISOString().slice(0, 19);
    return this.http.get<PriceEntry[]>(`${this.apiUrl}/prices/range?start=${startStr}&end=${endStr}`);
  }
}
