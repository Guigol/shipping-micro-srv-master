import type { UserRole } from "./UserRole";

export interface UserDto {
  userId: number;
  name?: string;
  password?: string;
  email?: string;
  address?: string;
  role: UserRole;
}
