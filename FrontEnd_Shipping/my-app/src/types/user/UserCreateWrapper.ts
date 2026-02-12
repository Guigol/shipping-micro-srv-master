import type { CreateUserRequest } from "./CreateUserRequest";

export interface UserCreateWrapper {
  data: CreateUserRequest;
  userId: string;
}
