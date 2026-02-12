export interface ProofUploadRequest {
  shipmentId: string;
  type: string;          // ex: "deposit" | "delivery"
  filename: string;
  contentType: string;
  fileBase64: string;    // base64 string
}
