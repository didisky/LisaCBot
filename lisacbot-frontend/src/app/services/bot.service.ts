import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, timer } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { BacktestResult } from '../models/backtest-result.model';
import { Trade } from '../models/trade.model';

export interface BotStatus {
  running: boolean;
  balance: number;
  holdings: number;
  lastPrice: number;
  totalValue: number;
  marketCycle?: any;  // Optional market cycle information
}

@Injectable({
  providedIn: 'root'
})
export class BotService {
  private apiUrl = 'http://localhost:8080/api';
  private statusSubject = new BehaviorSubject<BotStatus | null>(null);
  public status$ = this.statusSubject.asObservable();

  constructor(private http: HttpClient) {
    // Start polling status every 10 seconds
    this.startPolling();
  }

  private startPolling() {
    timer(0, 10000).pipe(
      switchMap(() => this.http.get<BotStatus>(`${this.apiUrl}/status`)),
      tap(status => this.statusSubject.next(status))
    ).subscribe({
      error: (err) => console.error('Error fetching bot status:', err)
    });
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
}
