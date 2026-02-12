export interface ProofUploadRequest {
  shipmentId: string;
  type: string;          // "deposit" | "delivery" (à typer plus tard si voulu)
  filename: string;
  contentType: string;
  fileBase64: string;    // ⚠️ base64 string
}
