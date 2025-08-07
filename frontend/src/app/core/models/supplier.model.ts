export interface Supplier {
    id: number
    name: string
    type: "API" | "EXCEL" | "SOAP"
    apiUrl?: string
    apiKey?: string
    authMethod?: string
    filePath?: string
    fileName?: string
    active: boolean
}
