import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Trade } from '../models/trade.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TradeEventService {
  // Use relative URL to work with both development and Docker deployments
  // In dev (ng serve): proxied to localhost:8080 via proxy.conf.json
  // In Docker: proxied to backend via Nginx
  private apiUrl = '/api/trades/events';
  private eventSource: EventSource | null = null;
  private tradeSubject = new Subject<Trade>();

  constructor(
    private zone: NgZone,
    private authService: AuthService
  ) {}

  /**
   * Connects to the SSE endpoint and returns an observable of trade events.
   */
  getTradeEvents(): Observable<Trade> {
    return this.tradeSubject.asObservable();
  }

  /**
   * Starts listening to SSE events from the backend.
   */
  connect(): void {
    if (this.eventSource) {
      console.warn('EventSource already connected');
      return;
    }

    // Get JWT token and add as query parameter (EventSource can't send custom headers)
    const token = this.authService.getToken();
    const urlWithToken = token ? `${this.apiUrl}?token=${encodeURIComponent(token)}` : this.apiUrl;

    console.log('🔌 Connecting to trade events SSE...');
    this.eventSource = new EventSource(urlWithToken);

    this.eventSource.addEventListener('trade', (event: MessageEvent) => {
      this.zone.run(() => {
        try {
          const trade: Trade = JSON.parse(event.data);
          console.log('📨 New trade event received:', trade);
          this.tradeSubject.next(trade);
        } catch (error) {
          console.error('Error parsing trade event:', error);
        }
      });
    });

    this.eventSource.onerror = (error) => {
      console.error('❌ SSE connection error:', error);
      // Auto-reconnect after 5 seconds
      setTimeout(() => {
        console.log('🔄 Attempting to reconnect...');
        this.disconnect();
        this.connect();
      }, 5000);
    };

    this.eventSource.onopen = () => {
      console.log('✅ SSE connection established');
    };
  }

  /**
   * Closes the SSE connection.
   */
  disconnect(): void {
    if (this.eventSource) {
      console.log('🔌 Disconnecting from trade events SSE...');
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
