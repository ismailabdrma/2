// src/app/core/services/import-log.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ImportLogService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/import-logs`;

  getAllImportLogs(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getImportLogsBySupplierId(supplierId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/supplier/${supplierId}`);
  }
}
