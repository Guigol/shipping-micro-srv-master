export interface TrackingResponse {
  shipmentId: string;
  currentStatus: string;
  history: StatusEntry[];
  userId: number;
}

export interface StatusEntry {
  status: string;
  userId?: number;
  timestamp: string; // Instant → ISO string
  location?: string;
  note?: string;
}
