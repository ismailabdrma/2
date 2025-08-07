import { Component, type OnInit, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { RouterModule } from "@angular/router"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatTableModule } from "@angular/material/table"
import { MatPaginatorModule } from "@angular/material/paginator"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatDialog, MatDialogModule } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { ProductService } from "@core/services/product.service"
import type { Product } from "@core/models/product.model"
import { CreateProductDialogComponent } from "./create-product-dialog/create-product-dialog.component"
import { ConfirmDialogComponent } from "../../../../shared/components/confirm-dialog/confirm-dialog.component"
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatOptionModule } from '@angular/material/core';
import { Category } from '@core/models/product.model';
import { CategoryService } from '@core/services/category.service';

@Component({
  selector: "app-admin-products",
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatSortModule,
    ReactiveFormsModule,
    MatOptionModule,
  ],
  templateUrl: "./admin-products.component.html",
  styleUrl: "./admin-products.component.scss",
})
export class AdminProductsComponent implements OnInit {
  private productService = inject(ProductService)
  private dialog = inject(MatDialog)
  private snackBar = inject(MatSnackBar)
  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  filterForm: FormGroup;
  categories: Category[] = [];
  dataSource: Product[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  sort: Sort = {active: 'id', direction: 'asc'}

  products: Product[] = []
  displayedColumns: string[] = ["id", "name", "category", "price", "stock", "actions"]
  loading = false
  
  constructor() {
    this.filterForm = this.fb.group({
      search: [''],
      categoryId: [''],
      minPrice: [''],
      maxPrice: [''],
      active: [false],
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => this.categories = categories,
      error: () => this.categories = []
    });
  }

  loadProducts(): void {
    this.loading = true;
    const filters = this.filterForm.value;
    const params: any = {
      includeInactive: true,
      search: filters.search,
      category: filters.categoryId,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      active: filters.active,
      page: this.pageIndex + 1,
      size: this.pageSize,
      sortBy: this.sort.active + ',' + this.sort.direction
    };
    this.productService.getAllProducts(params).subscribe({
      next: (products) => {
        this.products = products;
        this.dataSource = products;
        this.totalElements = products.length;
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Error loading products.', 'Close', { duration: 3000 });
        this.products = [];
        this.dataSource = [];
        this.loading = false;
      },
    });
  }

  openProductDialog(product?: Product): void {
    const dialogRef = this.dialog.open(CreateProductDialogComponent, {
      width: '600px',
      data: product || null
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProducts();
      }
    });
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadProducts();
  }

  clearFilters(): void {
    this.filterForm.reset({ search: '', categoryId: '', minPrice: '', maxPrice: '', active: false });
    this.applyFilters();
  }

  onSortChange(sort: Sort): void {
    this.sort = sort;
    this.loadProducts();
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProducts();
  }

  deleteProduct(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: "Confirm Deletion",
        message: "Are you sure you want to delete this product?",
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.productService.deleteProduct(id).subscribe({
          next: () => {
            this.snackBar.open("Product deleted successfully.", "Close", { duration: 3000 })
            this.loadProducts()
          },
          error: (error) => {
            this.snackBar.open("Error deleting product.", "Close", { duration: 3000 })
          },
        },
        )
      }
    })
  }

  toggleProductStatus(product: Product): void {
    this.productService.updateProductStatus(product.id, !product.isActive).subscribe({
      next: (updatedProduct) => {
        this.snackBar.open(`Product ${updatedProduct.isActive ? 'activated' : 'deactivated'} successfully.`, 'Close', { duration: 3000 });
        this.loadProducts();
      },
      error: () => {
        this.snackBar.open('Error updating product status.', 'Close', { duration: 3000 });
      }
    });
  }
}
