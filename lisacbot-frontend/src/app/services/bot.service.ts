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

  runBacktest(days?: number): Observable<BacktestResult> {
    const url = days ? `${this.apiUrl}/backtest?days=${days}` : `${this.apiUrl}/backtest`;
    return this.http.post<BacktestResult>(url, {});
  }

  getBotStatus(): Observable<any> {
    return this.http.get(`${this.apiUrl}/status`);
  }
}
