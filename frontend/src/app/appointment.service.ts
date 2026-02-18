import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  //private apiUrl = 'http://192.168.131.213:8080/appointments';
  private apiUrl = 'http://localhost:8080/appointments';
    constructor(private http: HttpClient) { }

    getAppointments(): Observable<any> {
      return this.http.get(this.apiUrl);
    }
  
    createAppointment(appointment: any): Observable<any> {
      return this.http.post(this.apiUrl, appointment);
    }
  
    
    updateAppointment(id: number, appointment: any): Observable<any> {
      return this.http.put(`${this.apiUrl}/${id}`, appointment);
    }
    
    
    deleteAppointment(id: number): Observable<any> {
      return this.http.delete(`${this.apiUrl}/${id}`);
    }
    
    completeAppointment(id: number, data: any) {
    const headers = { 'Authorization': 'Basic ' + sessionStorage.getItem('authData') }; 
    return this.http.put(`${this.apiUrl}/${id}/complete`, data, { headers });
  }

    getPatientHistory(patientId: number): Observable<any> {
      return this.http.get(`${this.apiUrl}/patient/${patientId}`);
    }
    


    getPatientDetails(id: number): Observable<any> {
      const token = sessionStorage.getItem('authData');
      const headers = new HttpHeaders({
        'Authorization': token || ''
      });
  
      return this.http.get(`http://localhost:8080/patients/${id}`, { headers });
    }
  
    
}
