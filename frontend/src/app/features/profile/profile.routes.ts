import { Routes } from "@angular/router"
import { ProfileComponent } from "./components/profile/profile.component"
import { AddressesComponent } from "./components/addresses/addresses.component"

export const PROFILE_ROUTES: Routes = [
  { path: "", component: ProfileComponent, title: "My Profile" },
  { path: "addresses", component: AddressesComponent, title: "My Addresses" },
]
