export interface Price {
  timestamp: number;
  value: number;
}

export interface PriceEntry {
  value: number;
  timestamp: string; // ISO datetime string from backend
}
