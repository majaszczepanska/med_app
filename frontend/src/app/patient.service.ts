import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable ({
  providedIn: 'root',
})
export class PatientService {
  private apiUrl = 'http://localhost:8080/patients';
  constructor(private http: HttpClient) { }
  
  getPatients(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  createPatient(patient: any): Observable<any> {
    return this.http.post(this.apiUrl, patient);
  }

  deletePatient(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
