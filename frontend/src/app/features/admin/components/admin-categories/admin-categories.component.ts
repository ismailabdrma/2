import {Component, OnInit, ViewChild, AfterViewInit, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { CategoryService } from '@core/services/category.service'; // Using CategoryService for categories
import type { Category } from '@core/models/product.model';
import { CategoryDialogComponent } from '../category-dialog/category-dialog.component';
import { ConfirmDialogComponent } from '@shared/components/confirm-dialog/confirm-dialog.component';
import {MatSlideToggle} from "@angular/material/slide-toggle";

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatInputModule,
    MatFormFieldModule,
    MatSlideToggle,
    MatPaginator
  ],
  templateUrl: './admin-categories.component.html',
  styleUrls: ['./admin-categories.component.scss']
})
export class AdminCategoriesComponent implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  displayedColumns: string[] = ['id', 'name', 'description', 'active', 'actions'];
  dataSource: MatTableDataSource<Category>;
  loading = true;
  searchQuery = '';

  constructor() {
    this.dataSource = new MatTableDataSource<Category>([]);
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadCategories(): void {
    this.loading = true;
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.dataSource.data = categories;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.snackBar.open('Failed to load categories.', 'Close', { duration: 3000 });
        this.loading = false;
      },
    });
  }

  applyFilter(): void {
    this.dataSource.filter = this.searchQuery.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applyFilter();
  }

  openCategoryDialog(category?: Category): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, {
      width: '400px',
      data: category || {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (result.id) {
          this.updateCategory(result);
        } else {
          this.createCategory(result);
        }
      }
    });
  }

  createCategory(category: Category): void {
    this.categoryService.createCategory(category).subscribe({
      next: (newCategory) => {
        this.dataSource.data = [...this.dataSource.data, newCategory];
        this.snackBar.open('Category created successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error creating category:', error);
        this.snackBar.open('Failed to create category', 'Close', { duration: 3000 });
      }
    });
  }

  updateCategory(category: Category): void {
    this.categoryService.updateCategory(category.id, category).subscribe({
      next: (updatedCategory) => {
        const index = this.dataSource.data.findIndex(c => c.id === updatedCategory.id);
        if (index !== -1) {
          this.dataSource.data[index] = updatedCategory;
          this.dataSource.data = [...this.dataSource.data];
        }
        this.snackBar.open('Category updated successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error updating category:', error);
        this.snackBar.open('Failed to update category', 'Close', { duration: 3000 });
      }
    });
  }

  deleteCategory(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '250px',
      data: { title: 'Delete Category', message: 'Are you sure you want to delete this category?' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.categoryService.deleteCategory(id).subscribe({
          next: () => {
            this.dataSource.data = this.dataSource.data.filter(c => c.id !== id);
            this.snackBar.open('Category deleted successfully', 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error deleting category:', error);
            this.snackBar.open('Failed to delete category', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  toggleCategoryStatus(id: number, active: boolean): void {
    const categoryToUpdate = this.dataSource.data.find(c => c.id === id);
    if (categoryToUpdate) {
      const updatedCategory = { ...categoryToUpdate, active };
      this.categoryService.updateCategory(id, updatedCategory).subscribe({
        next: (updatedCategory: Category) => {
          const index = this.dataSource.data.findIndex(c => c.id === updatedCategory.id);
          if (index !== -1) {
            this.dataSource.data[index] = updatedCategory;
            this.dataSource.data = [...this.dataSource.data];
          }
          this.snackBar.open('Category status updated successfully', 'Close', { duration: 3000 });
        },
        error: (error: any) => {
          console.error('Error updating category status:', error);
          this.snackBar.open('Failed to update category status', 'Close', { duration: 3000 });
        }
      });
    }
  }
  }