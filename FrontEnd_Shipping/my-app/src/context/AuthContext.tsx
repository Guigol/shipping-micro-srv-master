import { createContext, useState, useContext, ReactNode, useEffect } from "react";
import type { UserDto } from "../types/user";

export type UserType = UserDto | null;

export interface AuthContextType {
  user: UserType;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface Props {
  children: ReactNode;
}

export function AuthProvider({ children }: Props) {
  const [user, setUser] = useState<UserType>(null);

  const isAuthenticated = !!user;
  const isAdmin = user?.role === "ADMIN";

  // 🔁 CHECK SESSION AU CHARGEMENT
  useEffect(() => {
    fetch("/auth/me", {
      credentials: "include",
    })
      .then(res => (res.ok ? res.json() : null))
      .then(data => {
        if (data) {
          console.log("[AUTH] session restored", data);
          setUser(data);
        }
      })
      .catch(() => {
        console.warn("[AUTH] no session");
        setUser(null);
      });
  }, []);

  // ----- LOGIN -----
  const login = async (email: string, password: string) => {
    const res = await fetch("/auth/login", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    if (!res.ok) {
      throw new Error("Login failed");
    }

    const data = await res.json();
    setUser(data); // UserDto
  };

  // ----- LOGOUT -----
  const logout = async () => {
    await fetch("/auth/logout", {
      method: "POST",
      credentials: "include",
    });

    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isAdmin,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
