// src/app/core/services/supplier.service.ts
import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import type { Supplier } from "../models/supplier.model";

@Injectable({ providedIn: "root" })
export class SupplierService {
    private api = "/api/suppliers";

    constructor(private http: HttpClient) {}

    getAllSuppliers(): Observable<Supplier[]> {
        return this.http.get<Supplier[]>(this.api);
    }

    createSupplier(supplier: any): Observable<Supplier> {
        return this.http.post<Supplier>(this.api, supplier);
    }

    updateSupplier(id: number, supplier: any): Observable<Supplier> {
        return this.http.put<Supplier>(`${this.api}/${id}`, supplier);
    }

    deleteSupplier(id: number): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }

    activateSupplier(id: number): Observable<void> {
        return this.http.put<void>(`${this.api}/${id}/activate`, {});
    }

    deactivateSupplier(id: number): Observable<void> {
        return this.http.put<void>(`${this.api}/${id}/deactivate`, {});
    }

    importProducts(id: number): Observable<any> {
        return this.http.post<any>(`${this.api}/${id}/import-products`, {});
    }
}
