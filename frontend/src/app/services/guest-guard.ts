import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const guestGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authData = sessionStorage.getItem('authData');
  if (authData) {
    router.navigate(['/dashboard']);
    return false;
  }
  return true;
};
