// MgtParcelPage.tsx
import { useEffect, useState } from "react";
import {
  getAllShipments,
  getShipmentById,
  updateShipment,
  deleteShipment,
  addTrackingStatus
} from "../services/shipmentService";
import type { ShipmentResponse, ShipmentRequest, AddTrackingStatusRequest } from "../types/shipping";
import { useAuth } from "../context/AuthContext";
import Navbar from "../components/layout/Navbar";
import Footer from "../components/layout/Footer";

// Components
import UpdateShipmentModal from "../components/modals/UpdateShipmentModal";
import AddTrackingModal from "../components/modals/AddTrackingModal";
import TrackingTimeline from "../components/modals/TrackingTimeline";

export default function MgtParcelPage() {
  const { isAdmin, user } = useAuth();

  const [shipments, setShipments] = useState<ShipmentResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [search, setSearch] = useState("");
  const [selectedShipment, setSelectedShipment] = useState<ShipmentResponse | null>(null);

  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showTrackingModal, setShowTrackingModal] = useState(false);

  const [shipmentToDelete, setShipmentToDelete] = useState<ShipmentResponse | null>(null);
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null);

  const formatIsoLocalDate = (isoDate: string) =>
    new Date(isoDate).toLocaleString("sv-SE", { hour12: false }).replace(" ", "T");

  // -------------------------
  // LOAD SHIPMENTS
  // -------------------------
  const loadShipments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getAllShipments();
      setShipments(data);
    } catch (e: any) {
      setError(e.message || "Erreur chargement colis");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadShipments();
  }, []);

  useEffect(() => {
    if (deleteMessage) {
      const t = setTimeout(() => setDeleteMessage(null), 3000);
      return () => clearTimeout(t);
    }
  }, [deleteMessage]);

  // -------------------------
  // FILTER
  // -------------------------
  const filteredShipments = Array.isArray(shipments)
    ? shipments.filter(
        (s) =>
          s.shipmentId.toLowerCase().includes(search.toLowerCase()) ||
          s.trackingNumber.toLowerCase().includes(search.toLowerCase()) ||
          s.sender.name.toLowerCase().includes(search.toLowerCase()) ||
          s.receiver.name.toLowerCase().includes(search.toLowerCase()) ||
          s.carrier.toLowerCase().includes(search.toLowerCase()) ||
          s.status.toLowerCase().includes(search.toLowerCase())
      )
    : [];

  // -------------------------
  // PAGINATION
  // -------------------------
  const [page, setPage] = useState(1);
  const pageSize = 10;
  const paginatedShipments = filteredShipments.slice((page - 1) * pageSize, page * pageSize);

  // -------------------------
  // UPDATE SHIPMENT
  // -------------------------
  const handleUpdate = async (data: ShipmentRequest) => {
    if (!selectedShipment) return;
    try {
      await updateShipment(selectedShipment.shipmentId, data);
      await loadShipments();
    } catch (err) {
      console.error(err);
    }
  };

  // -------------------------
  // DELETE SHIPMENT
  // -------------------------
  const handleDelete = async (shipmentId: string) => {
    if (!window.confirm("Supprimer ce colis ?")) return;
    await deleteShipment(shipmentId);
    await loadShipments();
  };

  const confirmDeleteShipment = async () => {
    if (!shipmentToDelete) return;
    await deleteShipment(shipmentToDelete.shipmentId);
    setDeleteMessage("✅ Colis supprimé avec succès.");
    setShipmentToDelete(null);
    await loadShipments();
  };

  // -------------------------
  // ADD TRACKING
  // -------------------------
  const handleAddTracking = async (status: string, location: string, note: string) => {
    if (!selectedShipment || !user) return;

    const payload: AddTrackingStatusRequest = {
      status,
      location,
      note,
      trackingNumber: selectedShipment.trackingNumber,
      timestamp: new Date().toISOString(),
      userId: user.userId,
    };

    try {
      await addTrackingStatus(selectedShipment.trackingNumber, payload);

      const updatedShipment = await getShipmentById(selectedShipment.shipmentId);
      setSelectedShipment(updatedShipment);
      setShipments((prev) =>
        prev.map((s) => (s.shipmentId === updatedShipment.shipmentId ? { ...updatedShipment } : s))
      );
    } catch (err) {
      console.error(err);
      throw err; 
    }
  };

  // -------------------------
  // TRACKING HISTORY
  // -------------------------
  const trackingHistory =
    selectedShipment?.statusHistory && selectedShipment.statusHistory.length > 0
      ? selectedShipment.statusHistory
      : null;

  // -------------------------
  // RENDER
  // -------------------------
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />

      <div className="p-6 flex-1">
        <h1 className="text-2xl font-bold mb-4">Gestion des colis</h1>

        <input
          className="mt-2 border p-2 mb-4 w-full"
          placeholder="Recherche (shipmentId, tracking, expéditeur, destinataire, transporteur, statut)"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        {loading && <p>Chargement…</p>}
        {error && <p className="text-red-600">{error}</p>}
        {deleteMessage && (
          <div className="mb-4 rounded border border-green-500 bg-green-100 text-green-800 px-4 py-2">
            {deleteMessage}
          </div>
        )}

        <div className="mt-8 overflow-hidden rounded-lg border border-gray-400 shadow-2xl">
          <table className="w-full">
            <thead className="bg-gray-300">
              <tr className="text-center">
                <th className="py-2">Shipment ID</th>
                <th className="py-2">Tracking</th>
                <th className="py-2">Expéditeur</th>
                <th className="py-2">Destinataire</th>
                <th className="py-2">Transporteur</th>
                <th className="py-2">Statut</th>
                <th className="py-2">Poids</th>
                <th className="py-2">User ID</th>
                <th className="py-2">Créé le</th>
                <th className="py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginatedShipments.map((s) => (
                <tr key={s.shipmentId} className="border-t text-center">
                  <td className="py-2 align-middle">{s.shipmentId}</td>
                  <td className="py-2 align-middle">{s.trackingNumber}</td>
                  <td className="py-2 align-middle">{s.sender.name}</td>
                  <td className="py-2 align-middle">{s.receiver.name}</td>
                  <td className="py-2 align-middle">{s.carrier}</td>
                  <td className="py-2 align-middle">
                    {s.statusHistory && s.statusHistory.length > 0
                      ? String(s.statusHistory[s.statusHistory.length - 1].status)
                      : s.status}
                  </td>
                  <td className="py-2 align-middle">{s.weight} kg</td>
                  <td className="py-2 align-middle">{s.userId}</td>
                  <td className="py-2">{formatIsoLocalDate(s.createdAt)}</td>
                  <td className="py-2 align-middle space-x-2">
                    <button
                      title="Modifier"
                      onClick={() => {
                        setSelectedShipment(s);
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                    <button
                      title="Ajouter tracking"
                      onClick={() => {
                        setSelectedShipment(s);
                        setShowTrackingModal(true);
                      }}
                    >
                      ➕
                    </button>
                    {isAdmin && (
                      <button title="Supprimer" onClick={() => setShipmentToDelete(s)}>
                        🗑️
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* PAGINATION */}
        <div className="flex justify-center gap-3 mt-8">
          <button disabled={page === 1} onClick={() => setPage((p) => p - 1)}>◀</button>
          <span>Page {page} / {Math.ceil(filteredShipments.length / pageSize)}</span>
          <button
            disabled={page * pageSize >= filteredShipments.length}
            onClick={() => setPage((p) => p + 1)}
          >▶</button>
        </div>

        {/* MODALS */}
        {showUpdateModal && selectedShipment && (
          <UpdateShipmentModal
            shipment={selectedShipment}
            onClose={() => setShowUpdateModal(false)}
            onSubmit={handleUpdate}
          />
        )}

        {showTrackingModal && selectedShipment && (
          <AddTrackingModal
            shipment={selectedShipment}
            onClose={() => setShowTrackingModal(false)}
            onSubmit={handleAddTracking}
          />
        )}

        {/* TIMELINE */}
        {trackingHistory && (
          <div className="mt-6">
            <h2 className="font-bold mb-2">Historique de tracking</h2>
            <TrackingTimeline history={trackingHistory} />
          </div>
        )}
      </div>

      {/* DELETE CONFIRM MODAL */}
      {shipmentToDelete && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-[#29565C] rounded-lg p-6 w-[420px] shadow-xl">
            <h2 className="text-lg font-bold mb-4 text-white">Confirmer la suppression</h2>
            <p className="mb-6 text-white">
              Supprimer le colis <span className="font-semibold">{shipmentToDelete.trackingNumber}</span> ?
            </p>
            <div className="flex justify-end gap-3">
              <button className="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400" onClick={() => setShipmentToDelete(null)}>Annuler</button>
              <button className="px-4 py-2 rounded bg-[#C21957] text-white hover:bg-[#930136] transition" onClick={confirmDeleteShipment}>Supprimer</button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}
