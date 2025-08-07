import { Component, Inject, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoryService } from '@core/services/category.service'; // Or CategoryService

@Component({
  selector: 'app-category-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data ? 'Edit Category' : 'Create Category' }}</h2>

    <mat-dialog-content>
      <form [formGroup]="categoryForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Category Name</mat-label>
          <input matInput formControlName="name" required />
          <mat-error *ngIf="categoryForm.get('name')?.hasError('required')">
            Category name is required
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSave()"
        [disabled]="categoryForm.invalid || loading"
      >
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

      mat-dialog-content {
        min-width: 300px;
      }

      mat-spinner {
        margin-right: 8px;
      }
    `,
  ],
})
export class CategoryDialogComponent {
  private fb = inject(FormBuilder);
 private categoryService = inject(CategoryService); // Or CategoryService
  private dialogRef = inject(MatDialogRef<CategoryDialogComponent>);
  private snackBar = inject(MatSnackBar);

  categoryForm: FormGroup;
  loading = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any | null) {
    this.categoryForm = this.fb.group({
      name: [this.data?.name || '', Validators.required],
    });
  }

  onSave(): void {
    if (this.categoryForm.valid) {
      this.loading = true;
      const formValue = this.categoryForm.value;

      const request = this.data
 ? this.categoryService.updateCategory(this.data.id, formValue)
 : this.categoryService.createCategory(formValue);

      request.subscribe({
        next: (category) => {
          this.loading = false;
          this.snackBar.open(
            `Category ${this.data ? 'updated' : 'created'} successfully!`,
            'Close',
            { duration: 3000 }
          );
          this.dialogRef.close(category);
        },
        error: (error) => {
          console.error(`Error saving category:`, error);
          this.loading = false;
          this.snackBar.open(
            `Failed to ${this.data ? 'update' : 'create'} category.`,
            'Close',
            { duration: 3000, panelClass: ['error-snackbar'] }
          );
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
