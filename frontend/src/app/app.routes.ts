import { Routes } from '@angular/router';
import { LoginComponent } from './login/login';
import { RegisterComponent } from './register/register';
import { Profile } from './profile/profile';
import { guestGuard } from './services/guest-guard';
import { ForgotPassword } from './forgot-password/forgot-password';
import { ResetPassword } from './reset-password/reset-password';

export const routes: Routes = [
    {path: '', redirectTo: 'login', pathMatch: 'full'},
    {path: 'login', component: LoginComponent, canActivate: [guestGuard]},
    {path: 'register', component: RegisterComponent, canActivate: [guestGuard]},
    //{path: 'profile', component: Profile, canActivate: [guestGuard]},
    {path: 'reset-password', component: ResetPassword, canActivate: [guestGuard]},
    {path: 'forgot-password', component: ForgotPassword, canActivate: [guestGuard]},
    {path: 'dashboard', children: []},
    { path: '**', redirectTo: 'login', pathMatch: 'full' }
];
