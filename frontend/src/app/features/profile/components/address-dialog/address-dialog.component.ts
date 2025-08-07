import { Component, OnInit, inject, Inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogTitle,
  MatDialogContent,
  MatDialogActions,
} from "@angular/material/dialog"
import { MatInputModule } from "@angular/material/input"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatButtonModule } from "@angular/material/button"
import { MatCheckboxModule } from "@angular/material/checkbox"
import { ReactiveFormsModule, FormBuilder, Validators } from "@angular/forms"
import { Address } from "../../../../core/models/user.model"

@Component({
  selector: "app-address-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCheckboxModule,
    ReactiveFormsModule,
  ],
  templateUrl: "./address-dialog.component.html",
  styleUrl: "./address-dialog.component.scss",
})
export class AddressDialogComponent implements OnInit {
  addressForm = inject(FormBuilder).group({
    label: ["", Validators.required],
    street: ["", Validators.required],
    city: ["", Validators.required],
    state: ["", Validators.required],
    zipCode: ["", Validators.required],
    country: ["", Validators.required],
    isDefault: [false],
  })

  private dialogRef = inject(MatDialogRef<AddressDialogComponent>)

  constructor(@Inject(MAT_DIALOG_DATA) public data: Address) {}

  ngOnInit(): void {
    if (this.data) {
      this.addressForm.patchValue({
        label: this.data.label,
        street: this.data.street,
        city: this.data.city,
        state: this.data.state,
        zipCode: this.data.zipCode,
        country: this.data.country,
        isDefault: this.data.isDefault,
      })
    }
  }

  onSave(): void {
    if (this.addressForm.valid) {
      const result = { ...this.data, ...this.addressForm.value }
      this.dialogRef.close(result)
    }
  }
}
