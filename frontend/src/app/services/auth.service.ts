import { HttpClient } from '@angular/common/http';
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
  phoneNumber: string | number; 
  address: string;
  disease: string;
}

@Injectable({
  providedIn: 'root',
})

export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';
  private apiUrl2 = 'http://localhost:8080';
  constructor(private http: HttpClient) {}
  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data, { responseType: 'text' });
  }
  updateProfile(data: UpdateProfileRequest): Observable<any> {
    return this.http.put(`${this.apiUrl2}/patients/me/profile`, data, { responseType: 'text' });
  }
}
