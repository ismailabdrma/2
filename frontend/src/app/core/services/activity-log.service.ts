import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ActivityLogService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/activity-logs`;

  getAllActivityLogs(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}