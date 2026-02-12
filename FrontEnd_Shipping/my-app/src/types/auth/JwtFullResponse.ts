import type { UserRole } from "../user";

export interface JwtFullResponse {
  token: string;
  email: string;
  userId: number;
  role: UserRole;
  name: string;
}
