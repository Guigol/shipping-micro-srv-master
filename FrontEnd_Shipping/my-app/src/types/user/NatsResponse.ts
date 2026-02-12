import type { ErrorDto } from "./ErrorDto";

export interface NatsResponse<T = unknown> {
  success: boolean;
  data: T | null;
  error: ErrorDto | null;
}
