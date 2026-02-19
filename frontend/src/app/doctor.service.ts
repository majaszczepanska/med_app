import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
    private apiUrl = 'http://192.168.131.213:8080/doctors';
    //private apiUrl = 'http://localhost:8080/doctors';
    constructor(private http: HttpClient) { }

    getDoctors(): Observable<any> {
      return this.http.get(this.apiUrl);
    }
  
    createDoctor(doctor: any): Observable<any> {
      return this.http.post(this.apiUrl, doctor);
    }
  
    
    updateDoctor(id: number, doctor: any): Observable<any> {
      return this.http.put(`${this.apiUrl}/${id}`, doctor);
    }
  
  
    deleteDoctor(id: number): Observable<any> {
      return this.http.delete(`${this.apiUrl}/${id}`);
    }
}
