import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable ({
  providedIn: 'root',
})
export class PatientService {
  //private apiUrl = 'http://192.168.131.213:8080/patients';
  private apiUrl = 'http://localhost:8080/patients';
  constructor(private http: HttpClient) { }
  
  getPatients(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  createPatient(patient: any): Observable<any> {
    return this.http.post(this.apiUrl, patient);
  }

  updatePatient(id: number, patient: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, patient);
  }

  deletePatient(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
