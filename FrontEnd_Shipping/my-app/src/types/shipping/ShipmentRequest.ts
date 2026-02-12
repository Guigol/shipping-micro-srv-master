import type { ContactInfo } from "./ContactInfo";

export interface ShipmentRequest {
  shipmentId?: string;       
  userId?: number;

  sender: ContactInfo;
  receiver: ContactInfo;

  carrier: string;
  trackingNumber?: string;
  currentStatus?: string;

  statusHistory?: unknown;   
  files?: unknown;          

  createdAt?: string;
  updatedAt?: string;

  weight: number;
}


