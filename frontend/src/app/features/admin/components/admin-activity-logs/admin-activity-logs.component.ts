import { Component, type OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityLogService } from "../../../../core/services/activity-log.service";
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-admin-activity-logs',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="admin-activity-logs-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Activity Logs</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="loading" class="loading-container">
            <mat-spinner></mat-spinner>
            <p>Loading activity logs...</p>
          </div>

          <div *ngIf="!loading && activityLogs.length > 0">
            <table mat-table [dataSource]="activityLogs" class="activity-logs-table">
              <ng-container matColumnDef="id">
                <th mat-header-cell *matHeaderCellDef>ID</th>
                <td mat-cell *matCellDef="let log">{{ log.id }}</td>
              </ng-container>

              <ng-container matColumnDef="timestamp">
                <th mat-header-cell *matHeaderCellDef>Timestamp</th>
                <td mat-cell *matCellDef="let log">{{ log.timestamp | date:'medium' }}</td>
              </ng-container>

              <ng-container matColumnDef="user">
                <th mat-header-cell *matHeaderCellDef>User</th>
                <td mat-cell *matCellDef="let log">{{ log.user || 'N/A' }}</td>
              </ng-container>

              <ng-container matColumnDef="action">
                <th mat-header-cell *matHeaderCellDef>Action</th>
                <td mat-cell *matCellDef="let log">{{ log.action }}</td>
              </ng-container>

              <ng-container matColumnDef="details">
                <th mat-header-cell *matHeaderCellDef>Details</th>
                <td mat-cell *matCellDef="let log">{{ log.details || 'N/A' }}</td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          </div>

          <div *ngIf="!loading && activityLogs.length === 0" class="no-logs">
            <p>No activity logs found.</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styleUrls: ['./admin-activity-logs.component.scss'],
})
export class AdminActivityLogsComponent implements OnInit {
  private activityLogService = inject(ActivityLogService);
  private snackBar = inject(MatSnackBar);

  activityLogs: any[] = [];
  loading = true;
  displayedColumns: string[] = ['id', 'timestamp', 'user', 'action', 'details'];

  ngOnInit(): void {
    this.loadActivityLogs();
  }

  loadActivityLogs(): void {
    this.loading = true;
    this.activityLogService.getAllActivityLogs().pipe(
      catchError(error => {
        console.error('Error fetching activity logs:', error);
        this.snackBar.open('Failed to load activity logs.', 'Close', { duration: 3000 });
        this.loading = false;
        return of([]); // Return an empty observable to continue
      })
    ).subscribe(logs => {
      this.activityLogs = logs;
      this.loading = false;
    });
  }
}
