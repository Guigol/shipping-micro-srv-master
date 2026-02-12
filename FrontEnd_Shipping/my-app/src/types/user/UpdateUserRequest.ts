export interface UpdateUserRequest {
  name: string;
  email: string;
  address: string;
  role: "ADMIN" | "USER";
}
