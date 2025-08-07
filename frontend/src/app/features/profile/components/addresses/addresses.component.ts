import { Component, OnInit, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatListModule } from "@angular/material/list"
import { MatDividerModule } from "@angular/material/divider"
import { MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { MatTooltipModule } from "@angular/material/tooltip"
import { AddressService } from "../../../../core/services/address.service"
import { Address } from "../../../../core/models/user.model"
import { AddressDialogComponent } from "../address-dialog/address-dialog.component"
import { ConfirmDialogComponent } from "../../../../shared/components/confirm-dialog/confirm-dialog.component"

@Component({
  selector: "app-addresses",
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatTooltipModule,
    AddressDialogComponent,
    ConfirmDialogComponent,
  ],
  templateUrl: "./addresses.component.html",
  styleUrl: "./addresses.component.scss",
})
export class AddressesComponent implements OnInit {
  addresses: Address[] = []

  private addressService = inject(AddressService)
  private dialog = inject(MatDialog)
  private snackBar = inject(MatSnackBar)

  ngOnInit(): void {
    this.loadAddresses()
  }

  loadAddresses(): void {
    this.addressService.getAddresses().subscribe({
      next: (addresses) => {
        this.addresses = addresses
      },
      error: (err) => {
        console.error("Error loading addresses:", err)
        this.snackBar.open("Failed to load addresses. Please try again.", "Close", { duration: 3000 })
      },
    })
  }

  openAddressDialog(address?: Address): void {
    const dialogRef = this.dialog.open(AddressDialogComponent, {
      width: "400px",
      data: address ? { ...address } : null, // Pass a copy to avoid direct mutation
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        if (result.id) {
          // Update existing address
          this.addressService.updateAddress(result.id, result).subscribe({
            next: () => {
              this.snackBar.open("Address updated successfully!", "Close", { duration: 3000 })
              this.loadAddresses()
            },
            error: (err) => {
              this.snackBar.open(`Error updating address: ${err.error?.message || err.message}`, "Close", {
                duration: 3000,
              })
            },
          })
        } else {
          // Create new address
          this.addressService.addAddress(result).subscribe({
            next: () => {
              this.snackBar.open("Address added successfully!", "Close", { duration: 3000 })
              this.loadAddresses()
            },
            error: (err) => {
              this.snackBar.open(`Error adding address: ${err.error?.message || err.message}`, "Close", {
                duration: 3000,
              })
            },
          })
        }
      }
    })
  }

  setAsDefault(id: number): void {
    this.addressService.setDefaultAddress(id).subscribe({
      next: () => {
        this.snackBar.open("Default address set successfully!", "Close", { duration: 3000 })
        this.loadAddresses() // Reload to reflect changes
      },
      error: (err) => {
        this.snackBar.open(`Error setting default address: ${err.error?.message || err.message}`, "Close", {
          duration: 3000,
        })
      },
    })
  }

  deleteAddress(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: "Confirm Deletion",
        message: "Are you sure you want to delete this address? This action cannot be undone.",
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.addressService.deleteAddress(id).subscribe({
          next: () => {
            this.snackBar.open("Address deleted successfully!", "Close", { duration: 3000 })
            this.loadAddresses()
          },
          error: (err) => {
            this.snackBar.open(`Error deleting address: ${err.error?.message || err.message}`, "Close", {
              duration: 3000,
            })
          },
        })
      }
    })
  }
}