import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';

import { ImportLogService } from '@core/services/import-log.service';
import { SupplierService } from '@core/services/supplier.service';

@Component({
  selector: 'app-admin-import-logs',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBar,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
  ],
  template: `
    <div class="admin-import-logs-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Import Logs</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline">
              <mat-label>Filter by Supplier</mat-label>
              <mat-select [(ngModel)]="selectedSupplierId" (selectionChange)="onSupplierChange()">
                <mat-option [value]="null">All Suppliers</mat-option>
                <mat-option *ngFor="let supplier of suppliers" [value]="supplier.id">
                  {{ supplier.name }}
                </mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <div *ngIf="loading" class="loading-container">
            <mat-spinner></mat-spinner>
            <p>Loading import logs...</p>
          </div>

          <div *ngIf="!loading && importLogs.length > 0">
            <table mat-table [dataSource]="importLogs" class="import-logs-table">
              <ng-container matColumnDef="id">
                <th mat-header-cell *matHeaderCellDef>Log ID</th>
                <td mat-cell *matCellDef="let log">{{ log.id }}</td>
              </ng-container>

              <ng-container matColumnDef="supplier">
                <th mat-header-cell *matHeaderCellDef>Supplier</th>
                <td mat-cell *matCellDef="let log">{{ log.supplierName }}</td>
              </ng-container>

              <ng-container matColumnDef="startTime">
                <th mat-header-cell *matHeaderCellDef>Start Time</th>
                <td mat-cell *matCellDef="let log">{{ log.startTime | date:'medium' }}</td>
              </ng-container>

              <ng-container matColumnDef="endTime">
                <th mat-header-cell *matHeaderCellDef>End Time</th>
                <td mat-cell *matCellDef="let log">{{ log.endTime | date:'medium' }}</td>
              </ng-container>

              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Status</th>
                <td mat-cell *matCellDef="let log">{{ log.status }}</td>
              </ng-container>

              <ng-container matColumnDef="message">
                <th mat-header-cell *matHeaderCellDef>Message</th>
                <td mat-cell *matCellDef="let log">{{ log.message }}</td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          </div>

          <div *ngIf="!loading && importLogs.length === 0" class="no-logs">
            <h3>No import logs found.</h3>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .admin-import-logs-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px;
    }

    .filters {
        margin-bottom: 20px;
    }

    .filters mat-form-field {
        width: 200px;
    }

    .import-logs-table {
      width: 100%;
    }

    .no-logs {
      text-align: center;
      padding: 40px;
      color: #666;
    }
  `]
})
export class AdminImportLogsComponent implements OnInit {
  private importLogService = inject(ImportLogService);
  private supplierService = inject(SupplierService);
  private snackBar = inject(MatSnackBar);

  importLogs: any[] = [];
  suppliers: any[] = [];
  selectedSupplierId: number | null = null;
  loading = true;
  displayedColumns: string[] = ['id', 'supplier', 'startTime', 'endTime', 'status', 'message'];

  ngOnInit(): void {
    this.loadSuppliers();
    this.loadImportLogs();
  }

  loadImportLogs(): void {
    this.loading = true;
    const logsObservable = this.selectedSupplierId !== null
      ? this.importLogService.getImportLogsBySupplierId(this.selectedSupplierId)
      : this.importLogService.getAllImportLogs();

    logsObservable.subscribe({
      next: (logs) => {
        this.importLogs = logs;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading import logs:', error);
        this.snackBar.open('Failed to load import logs', 'Close', { duration: 3000 });
        this.importLogs = [];
        this.loading = false;
      }
    });
  }

  loadSuppliers(): void {
    this.supplierService.getAllSuppliers().subscribe({
      next: (suppliers) => {
        this.suppliers = suppliers;
      },
      error: (error) => {
        console.error('Error loading suppliers:', error);
        this.snackBar.open('Failed to load suppliers for filtering', 'Close', { duration: 3000 });
        this.suppliers = [];
      }
    });
  }

  onSupplierChange(): void {
    this.loadImportLogs();
  }
}
