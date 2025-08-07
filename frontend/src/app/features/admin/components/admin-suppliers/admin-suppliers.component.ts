import { Component, OnInit, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatChipsModule } from "@angular/material/chips";
import { SupplierService } from "@core/services/supplier.service";
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CreateSupplierDialogComponent } from "./create-supplier-dialog/supplier-dialog.component";
import {MatFormField, MatLabel} from "@angular/material/input";
import {ReactiveFormsModule} from "@angular/forms";

@Component({
    selector: "app-admin-suppliers",
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatDialogModule,
        MatChipsModule,
        MatSnackBarModule,
        MatLabel,
        ReactiveFormsModule,
        MatFormField,
        MatOption,
    ],
    templateUrl: "./admin-suppliers.component.html",
    styleUrl: "./admin-suppliers.component.scss",
})
export class AdminSuppliersComponent implements OnInit {
    private supplierService = inject(SupplierService);
    private snackBar = inject(MatSnackBar);
    private dialog = inject(MatDialog);

    suppliers: any[] = [];
    displayedColumns: string[] = ["id", "name", "type", "active", "actions"];
    loading = false;

    ngOnInit(): void {
        this.loadSuppliers();
    }

    loadSuppliers(): void {
 this.loading = true;
        this.supplierService.getAllSuppliers().subscribe({
 next: (suppliers) => {
 this.suppliers = suppliers;
 this.loading = false;
 },
            error: (err) => {
                this.suppliers = [];
                this.snackBar.open('Failed to load suppliers.', 'Close', { duration: 3000 });
            },
        });
    }

    openCreateSupplierDialog(): void {
        const dialogRef = this.dialog.open(CreateSupplierDialogComponent, {
            width: "500px",
            data: null,
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) this.loadSuppliers();
        });
    }

    openEditSupplierDialog(supplier: any): void {
        const dialogRef = this.dialog.open(CreateSupplierDialogComponent, {
            width: "500px",
            data: supplier,
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) this.loadSuppliers();
        });
    }

    toggleActive(supplier: any): void {
        const operation = supplier.active ? this.supplierService.deactivateSupplier(supplier.id) : this.supplierService.activateSupplier(supplier.id);
        const successMessage = supplier.active ? 'Supplier deactivated successfully.' : 'Supplier activated successfully.';

        operation.subscribe({
            next: () => {
                this.snackBar.open(successMessage, 'Close', { duration: 3000 });
                this.loadSuppliers();
            },
            error: (err) => {
                console.error('Failed to toggle supplier status', err);
                this.snackBar.open('Failed to update supplier status.', 'Close', { duration: 3000 });
            }
        });
    }

    deleteSupplier(id: number): void {
        if (confirm("Are you sure you want to delete this supplier?")) {
            this.supplierService.deleteSupplier(id).subscribe({
                next: () => {
                    this.snackBar.open('Supplier deleted successfully.', 'Close', { duration: 3000 });
                    this.loadSuppliers();
                },
                error: (err) => {
                    console.error('Failed to delete supplier', err);
                    this.snackBar.open('Failed to delete supplier.', 'Close', { duration: 3000 });
                }
            });
        }
    }

    importProducts(supplierId: number): void {
        this.supplierService.importProducts(supplierId).subscribe({
            next: () => {
                this.snackBar.open('Product import initiated successfully.', 'Close', { duration: 3000 });
            },
            error: (err) => {
                console.error('Failed to initiate product import', err);
                this.snackBar.open('Failed to initiate product import.', 'Close', { duration: 3000 });
            }
        }
    }
