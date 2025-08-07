import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root',
})
export class SupplierPaymentService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/supplier-payments`;

  getAllSupplierPayments(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  createSupplierPayment(payment: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, payment);
  }
}
