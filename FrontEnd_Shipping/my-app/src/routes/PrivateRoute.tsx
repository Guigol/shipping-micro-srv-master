import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface PrivateRouteProps {
  requireAdmin?: boolean;
}

export const PrivateRoute = ({ requireAdmin = false }: PrivateRouteProps) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    // Non connecté → redirection vers MainPage
    return <Navigate to="/" replace />;
  }

  if (requireAdmin && !isAdmin) {
    // Page admin demandée mais utilisateur non admin → redirection vers MainPage
    return <Navigate to="/" replace />;
  }

  // Connecté (USER ou ADMIN autorisé)
  return <Outlet />;
};
