import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  pesel: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  pesel: string | number;
  phoneNumber: string | number; 
  address: string;
  disease: string;
}

@Injectable({
  providedIn: 'root',
})

export class AuthService {
  private apiUrl = 'http://192.168.131.213:8080';

  constructor(private http: HttpClient) {}

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, data, { responseType: 'text' });
  }
  
  updateProfile(data: UpdateProfileRequest): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.apiUrl}/patients/me/profile`, data, { headers, responseType: 'text' });
  }

  getProfile(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.apiUrl}/patients/me`, {headers});
  }

  login(credentials: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': 'Basic ' + credentials
    });
    return this.http.get(`${this.apiUrl}/auth/me`, { headers });
  }

  changePassword(data: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.apiUrl}/auth/change-password`, data, { headers });
  }

  forgotPassword(data: { email: string}) {
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, data);
  }

  resetPassword(data: { token: string, newPassword: string }) {
    return this.http.post(`${this.apiUrl}/auth/reset-password`, data);
  }

  private getAuthHeaders(): HttpHeaders {
    const authData = sessionStorage.getItem('authData'); 
    return new HttpHeaders({
      'Authorization': authData ? authData : ''
    });
  }
}
