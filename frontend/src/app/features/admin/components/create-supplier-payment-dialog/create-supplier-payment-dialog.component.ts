import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { SupplierService } from '@core/services/supplier.service';
import type { Supplier } from '@core/models/supplier.model';

@Component({
  selector: 'app-create-supplier-payment-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>Créer un paiement fournisseur</h2>
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-form-field class="full-width">
        <mat-label>Fournisseur</mat-label>
        <mat-select formControlName="supplierId" required>
          <mat-option *ngFor="let supplier of suppliers" [value]="supplier.id">{{ supplier.name }}</mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field class="full-width">
        <mat-label>Montant</mat-label>
        <input matInput type="number" formControlName="amount" required min="0.01">
      </mat-form-field>
      <mat-form-field class="full-width">
        <mat-label>Date</mat-label>
        <input matInput type="date" formControlName="date" required>
      </mat-form-field>
      <mat-form-field class="full-width">
        <mat-label>Statut</mat-label>
        <mat-select formControlName="status" required>
          <mat-option value="PENDING">En attente</mat-option>
          <mat-option value="PAID">Payé</mat-option>
        </mat-select>
      </mat-form-field>
      <div mat-dialog-actions align="end">
        <button mat-button type="button" (click)="onCancel()">Annuler</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Créer</button>
      </div>
    </form>
  `,
  styles: [
    `.full-width { width: 100%; margin-bottom: 16px; }`
  ]
})
export class CreateSupplierPaymentDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<CreateSupplierPaymentDialogComponent>);
  private supplierService = inject(SupplierService);

  form: FormGroup;
  suppliers: Supplier[] = [];

  constructor() {
    this.form = this.fb.group({
      supplierId: [null, Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      date: [null, Validators.required],
      status: ['PENDING', Validators.required],
    });
    this.loadSuppliers();
  }

  loadSuppliers() {
    this.supplierService.getAllSuppliers().subscribe({
      next: (suppliers: Supplier[]) => this.suppliers = suppliers,
      error: () => this.suppliers = []
    });
  }

  onSubmit() {
    if (this.form.valid) {
      this.dialogRef.close(this.form.value);
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}

