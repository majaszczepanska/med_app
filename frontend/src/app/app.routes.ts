import { Routes } from '@angular/router';
import { LoginComponent } from './login/login';
import { RegisterComponent } from './register/register';
import { Profile } from './profile/profile';
import { guestGuard } from './services/guest-guard';

export const routes: Routes = [
    {path: '', redirectTo: 'login', pathMatch: 'full'},
    {path: 'login', component: LoginComponent, canActivate: [guestGuard]},
    {path: 'register', component: RegisterComponent, canActivate: [guestGuard]},
    {path: 'profile', component: Profile, canActivate: [guestGuard]},
    {path: 'dashboard', children: []},
    { path: '**', redirectTo: 'login', pathMatch: 'full' }
];
