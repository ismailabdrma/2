import { Component, type OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SupplierPaymentService } from '@core/services/supplier-payment.service';
import { CreateSupplierPaymentDialogComponent } from '../create-supplier-payment-dialog/create-supplier-payment-dialog.component';

@Component({
  selector: 'app-admin-supplier-payments',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './admin-supplier-payments.component.html',
  styleUrl: './admin-supplier-payments.component.scss',
})
export class AdminSupplierPaymentsComponent implements OnInit {
  private supplierPaymentService = inject(SupplierPaymentService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  supplierPayments: any[] = [];
  loading = true;
  displayedColumns: string[] = ['id', 'supplier', 'amount', 'date', 'status', 'actions'];

  ngOnInit(): void {
    this.loadSupplierPayments();
  }

  loadSupplierPayments(): void {
    this.loading = true;
    this.supplierPaymentService.getAllSupplierPayments().subscribe({
      next: (payments) => {
        this.supplierPayments = payments;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading supplier payments:', error);
        this.snackBar.open('Failed to load supplier payments.', 'Close', { duration: 5000 });
        this.loading = false;
      },
    });
  }

  openCreatePaymentDialog(): void {
    const dialogRef = this.dialog.open(CreateSupplierPaymentDialogComponent, {
      width: '500px',
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.supplierPaymentService.createSupplierPayment(result).subscribe({
          next: () => {
            this.snackBar.open('Paiement fournisseur créé avec succès.', 'Fermer', { duration: 3000 });
            this.loadSupplierPayments();
          },
          error: () => {
            this.snackBar.open('Erreur lors de la création du paiement.', 'Fermer', { duration: 3000 });
          }
        });
      }
    });
  }

  viewPaymentDetails(id: number): void {
    console.log('View payment details for ID:', id);
  }

  editPayment(id: number): void {
    console.log('Edit payment with ID:', id);
  }

  deletePayment(id: number): void {
    console.log('Delete payment with ID:', id);
  }
}
