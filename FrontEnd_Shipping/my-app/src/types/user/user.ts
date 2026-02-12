export type UserResponse = {
  userId: number;
  id: number | null;
  name: string;
  email: string;
  address: string;
  role: "ADMIN" | "USER";
  password: string | null;
  blank: boolean;
  createdAt: string;
  updatedAt: string;
};

export type NotifyUserRequest = {
  message: string;
};


export type CreateUserRequest = {
  name: string;
  email: string;
  address: string;
  role: "ADMIN" | "USER";
  password: string;
};

export type UpdateUserRequest = {
  name: string;
  email: string;
  address: string;
  role: "ADMIN" | "USER";
};


export type UserDto = UserResponse;
