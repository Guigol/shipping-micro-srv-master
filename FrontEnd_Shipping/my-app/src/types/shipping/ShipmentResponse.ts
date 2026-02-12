import type { ContactInfo } from "./ContactInfo";

export interface ShipmentResponse {
  shipmentId: string;
  userId: number;

  status: string;

  sender: ContactInfo;
  receiver: ContactInfo;

  trackingNumber: string;
  carrier: string;

  weight: number;

  statusHistory: Array<Record<string, unknown>>;
  files: Record<string, Record<string, unknown>>;

  createdAt: string;   // Instant → ISO string
  updatedAt: string;
}
