import type { ContactInfo } from "./ContactInfo";

export interface ShipmentRequest {
  shipmentId?: string;       // peut venir de _id ou shipmentId
  userId?: number;

  sender: ContactInfo;
  receiver: ContactInfo;

  carrier: string;
  trackingNumber?: string;
  currentStatus?: string;

  statusHistory?: unknown;   // Object côté Java
  files?: unknown;           // Object côté Java

  createdAt?: string;
  updatedAt?: string;

  weight: number;
}
