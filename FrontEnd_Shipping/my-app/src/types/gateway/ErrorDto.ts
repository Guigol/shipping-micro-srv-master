export interface ErrorDto {
  code: string;
  message: string;
  timestamp: string; // LocalDateTime → string ISO
}
