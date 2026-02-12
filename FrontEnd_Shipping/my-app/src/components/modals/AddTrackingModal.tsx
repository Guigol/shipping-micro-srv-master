import { useState } from "react";
import type { ShipmentResponse } from "../../types/shipping";

interface Props {
  shipment?: ShipmentResponse;
  onClose: () => void;
  onSubmit: (status: string, location: string, note: string) => Promise<void>;
}

export default function AddTrackingModal({ shipment, onClose, onSubmit }: Props) {
  const [status, setStatus] = useState("");
  const [location, setLocation] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [infoMessage, setInfoMessage] = useState(""); // message unique ✅ / ❌

  const handleSubmit = async () => {
    if (!status || !location) {
      setInfoMessage("❌ Statut et Localisation sont obligatoires !");
      return;
    }

    setInfoMessage("");
    setLoading(true);

    try {
      await onSubmit(status, location, note);
      setInfoMessage("✅ Statut ajouté avec succès !");
      setTimeout(() => onClose(), 3000);
      setStatus("");
      setLocation("");
      setNote("");
    } catch (err: any) {
      console.error(err);
      const msg = err.response?.data?.message || err.message || "Échec de l'ajout du statut";
      setInfoMessage(`❌ ${msg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-[#29565C] rounded-xl shadow-lg w-full max-w-md p-6 relative">
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-3 right-3 text-gray-500 hover:text-gray-700"
          aria-label="Close"
        >
          ✖
        </button>

        {/* Header */}
        <h2 className="text-2xl text-white font-bold mb-4">Ajouter un statut</h2>
        {shipment && (
          <p className="mb-4 text-white">
            N° Colis : <span className="font-mono">{shipment.shipmentId}</span>
          </p>
        )}

        {/* Formulaire */}
        <div className="flex flex-col gap-3">
          <input
            type="text"
            className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-600"
            placeholder="Statut *"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          />
          <input
            type="text"
            className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-600"
            placeholder="Localisation *"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
          <textarea
            className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-600"
            placeholder="Note (facultatif)"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            rows={3}
          />
        </div>

        {/* Message d'information */}
        {infoMessage && (
          <p
            className={`mt-3 text-sm ${
              infoMessage.startsWith("✅") ? "text-green-400" : "text-red-400"
            }`}
          >
            {infoMessage}
          </p>
        )}

        {/* Buttons */}
        <div className="flex justify-end gap-3 mt-5">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 transition"
            disabled={loading}
          >
            Annuler
          </button>
          <button
            onClick={handleSubmit}
            className="px-4 py-2 rounded bg-[#C21957] text-white hover:bg-[#930136] transition"
            disabled={loading}
          >
            {loading ? "Ajout…" : "Ajouter"}
          </button>
        </div>
      </div>
    </div>
  );
}
