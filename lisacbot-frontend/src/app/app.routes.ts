import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard.component';
import { StrategyConfigComponent } from './components/strategy-config.component';
import { BacktestComponent } from './components/backtest.component';
import { LoginComponent } from './components/login/login.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'config', component: StrategyConfigComponent, canActivate: [authGuard] },
  { path: 'backtest', component: BacktestComponent, canActivate: [authGuard] },
  { path: 'change-password', component: ChangePasswordComponent, canActivate: [authGuard] }
];
