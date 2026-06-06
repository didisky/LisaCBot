import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PriceEventService {
  private apiUrl = '/api/prices/events';
  private eventSource: EventSource | null = null;
  private priceSubject = new Subject<void>();

  constructor(private zone: NgZone, private authService: AuthService) {}

  getPriceEvents(): Observable<void> {
    return this.priceSubject.asObservable();
  }

  connect(): void {
    if (this.eventSource) return;

    const token = this.authService.getToken();
    const urlWithToken = token ? `${this.apiUrl}?token=${encodeURIComponent(token)}` : this.apiUrl;

    this.eventSource = new EventSource(urlWithToken);

    this.eventSource.addEventListener('price', () => {
      this.zone.run(() => this.priceSubject.next());
    });

    this.eventSource.onerror = () => {
      setTimeout(() => {
        this.disconnect();
        this.connect();
      }, 5000);
    };
  }

  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
