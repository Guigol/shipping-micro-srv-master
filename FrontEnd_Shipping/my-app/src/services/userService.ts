import api from "./api";
import type {
  UpdateUserRequest,
  UserDto,
  CreateUserRequest,
  NotifyUserRequest
  } from "../types/user";
import type { JwtFullResponse } from "../types/auth";


/**
 * LOGIN
 * Gateway → /auth/login
 */
export const loginUser = async (data: {
  email: string;
  password: string;
}): Promise<JwtFullResponse> => {
  const response = await api.post<JwtFullResponse>(
    "/auth/login",
    data
  );
  return response.data;
};

/**
 * REGISTER
 * Gateway → /auth/register
 */
export const registerUser = async (
  data: CreateUserRequest
): Promise<void> => {
  await api.post("/auth/register", data);
};

export const updateUser = async (userId: number, data: UpdateUserRequest) => {
  const response = await api.put<UserDto>(`/api/users/${userId}`, data);
   console.log("SENT DATA", data); 
  return response.data;
};

export const createUser = async (data: CreateUserRequest) => {
  const response = await api.post<UserDto>("/api/users", data);
  return response.data;
};

export const getUserById = async (userId: number) => {
  const response = await api.get<UserDto>(`/api/users/${userId}`);
  return response.data;
};

/**
 * =========================
 * ADMIN ONLY (NOUVEAU)
 * =========================
 */

/**
 * Get all users (ADMIN)
 */
export const getAllUsers = async (): Promise<UserDto[]> => {
  const response = await api.get<UserDto[]>("/api/users");
  return response.data;
};

/**
 * Delete user by id (ADMIN)
 */
export const deleteUserById = async (
  userId: number
): Promise<void> => {
  await api.delete(`/api/users/${userId}`);
};

/**
 * Notify user (ADMIN)
 */
export const notifyUserById = async (
  userId: number,
  payload: NotifyUserRequest
) => {
  const response = await api.post(
    `/api/users/${userId}/notify`,
    payload
  );
  return response.data;
};


