export interface AddTrackingStatusRequest {
  status: string;
  location: string;
  note: string;
  trackingNumber: string;
  timestamp: string; // ISO date string
  userId: number;
}
