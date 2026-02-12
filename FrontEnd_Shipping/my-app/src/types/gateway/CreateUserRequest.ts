export interface CreateUserRequest {
  name: string;
  password?: string | null; 
  email: string;
  address: string;
  role?: string | null;
}
