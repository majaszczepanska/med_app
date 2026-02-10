import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authData = sessionStorage.getItem('authData');
  if (authData) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: authData
      }
    });
    return next(clonedRequest);
  }
  return next(req);
};