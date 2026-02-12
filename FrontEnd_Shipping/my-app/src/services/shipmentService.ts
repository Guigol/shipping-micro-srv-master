import api from "./api";
import type {
  ShipmentRequest,
  ShipmentResponse,
  TrackingResponse,
  AddTrackingStatusRequest,
} from "../types/shipping";



// ------------------------------
// SHIPMENTS
// ------------------------------
export const createShipment = async (data: ShipmentRequest): Promise<ShipmentResponse> => {
  const res = await api.post<ShipmentResponse>("/api/shipping", data); // <-- garde /api/shipping
  return res.data;
};

export const getAllShipments = async (): Promise<ShipmentResponse[]> => {
  console.log("[LOAD SHIPMENTS] START");
  const res = await api.get<ShipmentResponse[]>("/api/shipping");
  console.log("[LOAD SHIPMENTS] RESPONSE", res.data);
  return res.data;
};



export const getShipmentById = async (shipmentId: string): Promise<ShipmentResponse> => {
  const res = await api.get<ShipmentResponse>(`/api/shipping/${shipmentId}`);
  return res.data;
};

export const updateShipment = async (
  shipmentId: string,
  data: Partial<ShipmentRequest>
): Promise<ShipmentResponse> => {
  const res = await api.put<ShipmentResponse>(`/api/shipping/${shipmentId}`, data);
  return res.data;
};

export const deleteShipment = async (shipmentId: string): Promise<void> => {
  await api.delete(`/api/shipping/${shipmentId}`);
};

// ------------------------------
// TRACKING
// ------------------------------
export const trackShipment = async (trackingNumber: string): Promise<TrackingResponse> => {
  const res = await api.get<TrackingResponse>(`/api/tracking/${trackingNumber}`);
  return res.data;
};

export const addTrackingStatus = async (
  trackingNumber: string,
  payload: AddTrackingStatusRequest
): Promise<void> => {
  await api.post(`/api/tracking/${trackingNumber}/add`, payload);
};

// ------------------------------
// FILE UPLOAD (PROOFS)
// ------------------------------
export const uploadProof = async (
  shipmentId: string,
  file: File,
  type: "deliveryProof" | "depositProof"
) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", type);

  const res = await api.post<any>(`/api/store/${shipmentId}/upload-proof`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });

  return res.data;
};
