import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BacktestResult } from '../models/backtest-result.model';

@Injectable({
  providedIn: 'root'
})
export class BotService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  runBacktest(days?: number, balance?: number): Observable<BacktestResult> {
    if (days !== undefined && balance !== undefined) {
      const url = `${this.apiUrl}/backtest/custom?days=${days}&balance=${balance}`;
      return this.http.post<BacktestResult>(url, {});
    }
    return this.http.post<BacktestResult>(`${this.apiUrl}/backtest`, {});
  }

  getBotStatus(): Observable<any> {
    return this.http.get(`${this.apiUrl}/status`);
  }
}
