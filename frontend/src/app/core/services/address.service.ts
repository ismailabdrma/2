import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AddressService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/addresses`;

  getAddresses(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  addAddress(address: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, address);
  }

  updateAddress(id: number, address: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, address);
  }

  deleteAddress(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  setDefaultAddress(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/set-default`, {});
  }
}
