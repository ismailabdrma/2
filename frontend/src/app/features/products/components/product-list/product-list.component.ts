import { Component, OnInit, ViewChild, inject } from "@angular/core"
import { CommonModule, CurrencyPipe } from "@angular/common"
import { MatTableDataSource, MatTableModule } from "@angular/material/table"
import { MatPaginator, MatPaginatorModule, PageEvent } from "@angular/material/paginator"
import { MatSort, MatSortModule, Sort } from "@angular/material/sort"
import { MatInputModule } from "@angular/material/input"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatCardModule } from "@angular/material/card"
import { MatSelectModule } from "@angular/material/select"
import { ReactiveFormsModule, FormBuilder, FormGroup } from "@angular/forms"
import { ProductService } from "../../../../core/services/product.service"
import { Product, Category } from "../../../../core/models/product.model"
import { ProductCardComponent } from "../../../../shared/components/product-card/product-card.component"
import { MatSnackBar } from "@angular/material/snack-bar"
import { debounceTime, distinctUntilChanged } from "rxjs/operators"

@Component({
  selector: "app-product-list",
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, // Although not using mat-table directly, it's often imported with paginator/sort
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSelectModule,
    ReactiveFormsModule,
    ProductCardComponent,
    CurrencyPipe,
  ],
  templateUrl: "./product-list.component.html",
  styleUrl: "./product-list.component.scss",
})
export class ProductListComponent implements OnInit {
  dataSource = new MatTableDataSource<Product>()
  totalElements = 0
  pageSize = 10
  currentPage = 0
  sortBy = "name"
  sortDir = "asc"

  filterForm: FormGroup
  categories: Category[] = []

  private productService = inject(ProductService)
  private fb = inject(FormBuilder)
  private snackBar = inject(MatSnackBar)

  @ViewChild(MatPaginator) paginator!: MatPaginator
  @ViewChild(MatSort) sort!: MatSort

  constructor() {
    this.filterForm = this.fb.group({
      search: [""],
      categoryId: [""],
      minPrice: [null],
      maxPrice: [null],
    })
  }

  ngOnInit(): void {
    this.loadCategories()
    this.loadProducts()

    this.filterForm.valueChanges
      .pipe(debounceTime(500), distinctUntilChanged())
      .subscribe(() => this.applyFilters())
  }

  loadCategories(): void {
    this.productService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories
      },
      error: (err) => {
        console.error("Error loading categories:", err)
        this.snackBar.open("Failed to load categories.", "Close", { duration: 3000 })
      },
    })
  }

  loadProducts(): void {
    const filters = this.filterForm.value
    this.productService
      .getProducts(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDir,
        filters.search,
        filters.categoryId,
        filters.minPrice,
        filters.maxPrice,
        // Do not pass 'active' filter here, as this is the public product list
        // AdminProductsComponent handles 'active' filtering.
      )
      .subscribe({
        next: (response) => {
          this.dataSource.data = response.content
          this.totalElements = response.totalElements
        },
        error: (err) => {
          console.error("Error loading products:", err)
          this.snackBar.open("Failed to load products. Please try again.", "Close", {
            duration: 3000,
          })
        },
      })
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex
    this.pageSize = event.pageSize
    this.loadProducts()
  }

  // Sorting is typically handled by the backend for paginated data.
  // If client-side sorting is needed for the current page, implement it here.
  // For now, assuming backend handles sortBy/sortDir.
  onSortChange(event: Sort): void {
    this.sortBy = event.active
    this.sortDir = event.direction
    this.loadProducts()
  }

  applyFilters(): void {
    this.currentPage = 0 // Reset to first page on filter change
    this.loadProducts()
  }

  clearFilters(): void {
    this.filterForm.reset({
      search: "",
      categoryId: "",
      minPrice: null,
      maxPrice: null,
    })
    this.currentPage = 0
    this.loadProducts()
  }
}
