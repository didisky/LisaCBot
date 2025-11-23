import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard.component';
import { StrategyConfigComponent } from './components/strategy-config.component';
import { BacktestComponent } from './components/backtest.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'config', component: StrategyConfigComponent },
  { path: 'backtest', component: BacktestComponent }
];
