import {Component, Inject, inject} from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms"
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog"
import { MatDialogModule } from "@angular/material/dialog"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatSelectModule } from "@angular/material/select"
import { MatButtonModule } from "@angular/material/button"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { ProductService } from "@core/services/product.service"
import { MatSnackBar } from "@angular/material/snack-bar"
import type { Product, Category } from "@core/models/product.model"
import { SupplierService } from "@core/services/supplier.service";
import type { Supplier } from "@core/models/supplier.model";

@Component({
    selector: "app-create-product-dialog",
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
    ],
    template: `
        <h2 mat-dialog-title>{{ data ? 'Edit Product' : 'Create Product' }}</h2>

        <mat-dialog-content>
            <form [formGroup]="productForm">
                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Product Name</mat-label>
                    <input matInput formControlName="name" required>
                    <mat-error *ngIf="productForm.get('name')?.hasError('required')">
                        Product name is required
                    </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Description</mat-label>
                    <textarea matInput formControlName="description" rows="3" required></textarea>
                    <mat-error *ngIf="productForm.get('description')?.hasError('required')">
                        Description is required
                    </mat-error>
                </mat-form-field>

                <div class="form-row">
                    <mat-form-field appearance="outline" class="half-width">
                        <mat-label>Price</mat-label>
                        <input matInput type="number" formControlName="displayedPrice" min="0" step="0.01" required>
                        <mat-error *ngIf="productForm.get('displayedPrice')?.hasError('required')">
                            Price is required
                        </mat-error>
                    </mat-form-field>

                    <mat-form-field appearance="outline" class="half-width">
                        <mat-label>Stock</mat-label>
                        <input matInput type="number" formControlName="syncedStock" min="0" required>
                        <mat-error *ngIf="productForm.get('syncedStock')?.hasError('required')">
                            Stock is required
                        </mat-error>
                    </mat-form-field>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Category</mat-label>
                    <mat-select formControlName="categoryId" required>
                        <mat-option *ngFor="let category of categories" [value]="category.id">
                            {{ category.name }}
                        </mat-option>
                    </mat-select>
                    <mat-error *ngIf="productForm.get('categoryId')?.hasError('required')">
                        Category is required
                    </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Supplier</mat-label>
                    <mat-select formControlName="supplierId" required>
                        <mat-option *ngFor="let supplier of suppliers" [value]="supplier.id">
                            {{ supplier.name }}
                        </mat-option>
                    </mat-select>
                    <mat-error *ngIf="productForm.get('supplierId')?.hasError('required')">
                        Supplier is required
                    </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Image URLs (comma separated)</mat-label>
                    <textarea matInput formControlName="imageUrls" rows="2" placeholder="https://example.com/image1.jpg, https://example.com/image2.jpg"></textarea>
                </mat-form-field>

                <div class="image-preview" *ngIf="currentImageUrl">
                    <img [src]="currentImageUrl" alt="Image preview" class="preview-img">
                    <button mat-icon-button (click)="currentImageUrl = null">
                        <mat-icon>close</mat-icon>
                    </button>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Upload Image</mat-label>
                    <input type="file" (change)="onFileSelected($event)" accept="image/*">
                    <mat-hint>Optional: Upload an image file</mat-hint>
                </mat-form-field>
            </form>
        </mat-dialog-content>

        <mat-dialog-actions align="end">
            <button mat-button (click)="onCancel()">Cancel</button>
            <button mat-raised-button color="primary"
                    (click)="onSave()"
                    [disabled]="productForm.invalid || loading">
                <mat-spinner diameter="20" *ngIf="loading"></mat-spinner>
                <span *ngIf="!loading">{{ data ? 'Update' : 'Create' }}</span>
            </button>
        </mat-dialog-actions>
    `,
    styles: [
        `
          .full-width {
            width: 100%;
            margin-bottom: 16px;
          }

          .form-row {
            display: flex;
            gap: 16px;
          }

          .half-width {
            flex: 1;
            margin-bottom: 16px;
          }

          mat-dialog-content {
            min-width: 400px;
            max-height: 70vh;
            overflow-y: auto;
          }

          mat-spinner {
            margin-right: 8px;
          }

          .image-preview {
            position: relative;
            display: inline-block;
            margin-top: 16px;
            width: 100%;
            max-width: 200px;
            height: auto;
          }

          .image-preview img {
            width: 100%;
            height: auto;
            border-radius: 4px;
          }

          .image-preview button {
            position: absolute;
            top: 4px;
            right: 4px;
            background: rgba(255, 255, 255, 0.8);
            border: none;
            border-radius: 50%;
            cursor: pointer;
          }

          @media (max-width: 768px) {
            .form-row {
              flex-direction: column;
            }

            mat-dialog-content {
              min-width: 300px;
            }
          }
        `,
    ],
})
export class CreateProductDialogComponent {
    private fb = inject(FormBuilder)
    private productService = inject(ProductService)
    private dialogRef = inject(MatDialogRef<CreateProductDialogComponent>)
    private snackBar = inject(MatSnackBar)
    private supplierService = inject(SupplierService)

    productForm: FormGroup
    categories: Category[] = []
    suppliers: Supplier[] = [];
    loading = false
    selectedFileName: string | null = null;
    currentImageUrl: string | null = null;

    // Fixed constructor with proper injection
    constructor(@Inject(MAT_DIALOG_DATA) public data: Product | null) {
        this.productForm = this.fb.group({
            name: [this.data?.name || "", Validators.required],
            description: [this.data?.description || "", Validators.required],
            displayedPrice: [this.data?.displayedPrice || 0, [Validators.required, Validators.min(0)]],
            syncedStock: [this.data?.syncedStock || 0, [Validators.required, Validators.min(0)]],
            categoryId: [this.data?.categoryId || "", Validators.required],
            supplierId: [this.data?.supplierId || "", Validators.required],
            imageUrls: [this.data?.imageUrls?.join(", ") || ""],
        })
        this.loadCategories();
        this.loadSuppliers();
        if (this.data?.imageUrls && this.data.imageUrls.length > 0) {
            this.currentImageUrl = this.data.imageUrls[0];
        }
    }

    loadCategories(): void {
        this.productService.getAllCategories().subscribe({
            next: (categories: Category[]) => {
                this.categories = categories
            },
            error: (error: any) => {
                console.error("Error loading categories:", error)
            },
        })
    }

    loadSuppliers(): void {
        this.supplierService.getAllSuppliers().subscribe({
            next: (suppliers: Supplier[]) => {
                this.suppliers = suppliers;
            },
            error: (error: any) => {
                console.error("Error loading suppliers:", error);
            },
        });
    }

    onSave(): void {
        if (this.productForm.valid) {
            this.loading = true
            const formValue = this.productForm.value

            // Convert comma-separated image URLs to array
            const imageUrls = formValue.imageUrls
                ? formValue.imageUrls
                    .split(",")
                    .map((url: string) => url.trim())
                    .filter((url: string) => url)
                : []

            const productData = {
                ...formValue,
                imageUrls,
            }

            const request = this.data
                ? this.productService.updateProduct(this.data.id, productData)
                : this.productService.createProduct(productData)

            request.subscribe({
                next: (product) => {
                    this.loading = false
                    this.dialogRef.close(product)
                },
                error: (error) => {
                    console.error("Error saving product:", error)
                    this.snackBar.open(`Error saving product: ${error.message || 'Unknown error'}`, 'Close', { duration: 3000 });
                    this.loading = false
                },
            })
        }
    }

    onCancel(): void {
        this.dialogRef.close()
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            const file = input.files[0];
            this.selectedFileName = file.name;
            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.currentImageUrl = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }
}
