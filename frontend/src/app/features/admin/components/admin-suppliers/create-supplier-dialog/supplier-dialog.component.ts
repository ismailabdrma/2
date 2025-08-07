import { Component, Inject, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SupplierService } from "@core/services/supplier.service";
import { HttpClient, HttpClientModule } from "@angular/common/http";

@Component({
    selector: "app-create-supplier-dialog",
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        HttpClientModule,
    ],
    template: `
        <h2 mat-dialog-title>{{ data ? 'Edit Supplier' : 'Add Supplier' }}</h2>
        <mat-dialog-content>
            <form [formGroup]="supplierForm">
                <mat-form-field class="full-width">
                    <mat-label>Name</mat-label>
                    <input matInput formControlName="name" required>
                </mat-form-field>
                <mat-form-field class="full-width">
                    <mat-label>Type</mat-label>
                    <mat-select formControlName="type" required (selectionChange)="onTypeChange()">
                        <mat-option value="API">API</mat-option>
                        <mat-option value="CSV">CSV</mat-option>
                        <mat-option value="EXCEL">Excel</mat-option>
                        <mat-option value="SOAP">SOAP</mat-option>
                    </mat-select>
                </mat-form-field>
                <mat-form-field class="full-width">
                    <mat-label>Endpoint / File URL</mat-label>
                    <input matInput formControlName="url" required placeholder="API endpoint or file path">
                </mat-form-field>
                <!-- SOAP-specific fields -->
                <ng-container *ngIf="supplierForm.get('type')?.value === 'SOAP'">
                    <mat-form-field class="full-width">
                        <mat-label>WSDL URL</mat-label>
                        <input matInput formControlName="wsdlUrl" (blur)="loadSoapOperations()" required>
                        <mat-hint>Enter WSDL, then select operation below</mat-hint>
                    </mat-form-field>
                    <mat-form-field class="full-width">
                        <mat-label>SOAP Operation</mat-label>
                        <mat-select formControlName="operation" [disabled]="soapOperations.length === 0" required>
                            <mat-option *ngFor="let op of soapOperations" [value]="op">
                                {{ op }}
                            </mat-option>
                        </mat-select>
                        <mat-progress-spinner *ngIf="loadingOps" diameter="24" mode="indeterminate"></mat-progress-spinner>
                    </mat-form-field>
                </ng-container>
            </form>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-button (click)="onCancel()">Cancel</button>
            <button mat-raised-button color="primary"
                    (click)="onSave()"
                    [disabled]="supplierForm.invalid || loadingOps">
                {{ data ? 'Update' : 'Create' }}
            </button>
        </mat-dialog-actions>
    `,
    styles: [`
      .full-width { width: 100%; margin-bottom: 16px; }
      mat-progress-spinner { display: inline-block; vertical-align: middle; margin-left: 12px; }
    `]
})
export class CreateSupplierDialogComponent {
    supplierForm: FormGroup;
    loadingOps = false;
    soapOperations: string[] = [];

    private fb = inject(FormBuilder);
    private dialogRef = inject(MatDialogRef<CreateSupplierDialogComponent>);
    private supplierService = inject(SupplierService);
    private http = inject(HttpClient);
 private snackBar = inject(MatSnackBar);

    constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
        this.supplierForm = this.fb.group({
            name: [data?.name || '', Validators.required],
            type: [data?.type || 'API', Validators.required],
            url: [data?.url || '', Validators.required],
            wsdlUrl: [data?.wsdlUrl || '', data?.type === 'SOAP' ? Validators.required : []],
            operation: [data?.operation || '', data?.type === 'SOAP' ? Validators.required : []]
        });
    }

    onTypeChange(): void {
        // Clear SOAP-specific fields if not SOAP
        if (this.supplierForm.get('type')?.value !== 'SOAP') {
            this.supplierForm.patchValue({ wsdlUrl: '', operation: '' });
            this.soapOperations = [];
        }
    }

    loadSoapOperations(): void {
        const wsdlUrl = this.supplierForm.get('wsdlUrl')?.value;
        if (!wsdlUrl) return;
        this.loadingOps = true;
        // Fetch the WSDL and parse for operation names
        this.http.get(wsdlUrl, { responseType: 'text' }).subscribe({
            next: (wsdl) => {
                // Extract <wsdl:operation name="...">
                const matches = Array.from(wsdl.matchAll(/<wsdl:operation[^>]*name="([^"]+)"/g));
                this.soapOperations = matches.map(m => m[1]);
                this.loadingOps = false;
            },
            error: () => {
                this.soapOperations = [];
                this.loadingOps = false;
            }
        });
    }

    onSave(): void {
        if (this.supplierForm.invalid) return;
        const supplier = this.supplierForm.value;
        if (this.data) {
            this.supplierService.updateSupplier(this.data.id, supplier).subscribe(() => {
 this.snackBar.open('Supplier updated successfully!', 'Close', {
 duration: 3000,
 });
                this.dialogRef.close(true);
 }, error => {
 console.error('Error updating supplier:', error);
 this.snackBar.open('Failed to update supplier.', 'Close', {
 duration: 3000,
 });
            });
        } else {
            this.supplierService.createSupplier(supplier).subscribe(() => {
 this.snackBar.open('Supplier created successfully!', 'Close', {
 duration: 3000,
 });
                this.dialogRef.close(true);
 }, error => {
 console.error('Error creating supplier:', error);
 this.snackBar.open('Failed to create supplier.', 'Close', { duration: 3000 });
            });
        }
    }

    onCancel(): void {
        this.dialogRef.close(false);
    }
}
