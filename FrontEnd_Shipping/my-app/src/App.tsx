import React from "react";
import { Routes, Route } from "react-router-dom";
import MainPage from "./pages/MainPage";
import AdminPage from "./pages/AdminPage";
import MgtParcelPage from "./pages/MgtParcelPage";
import UnauthorizedPage from "./pages/UnauthorizedPage";
import ProofsDepDelPage from "./pages/ProofsDepDelPage";
import { PrivateRoute } from "./routes/PrivateRoute";

export default function App() {
  return (
      <div className="min-h-screen">
    <Routes>
      {/* Main page for everyone */}
      <Route path="/" element={<MainPage />} />

      {/* Proofs Page for USER + ADMIN */}
      <Route element={<PrivateRoute />}>
        <Route path="/proofs" element={<ProofsDepDelPage />} />
      </Route>

      {/* Parcel Management Page for USER + ADMIN */}
      <Route element={<PrivateRoute />}>
        <Route path="/parcel" element={<MgtParcelPage />} />
      </Route>

      {/* Admin Page, only for admin */}
      <Route element={<PrivateRoute requireAdmin />}>
        <Route path="/admin" element={<AdminPage />} />
      </Route>

      {/* Page non autorisée */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
    </Routes>
    </div>
  );
}
