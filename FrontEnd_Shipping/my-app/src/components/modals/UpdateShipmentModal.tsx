import { useState } from "react";
import type { ShipmentResponse, ShipmentRequest } from "../../types/shipping";

interface Props {
  shipment: ShipmentResponse;
  onClose: () => void;
  onSubmit: (data: ShipmentRequest) => Promise<void>;
}

export default function UpdateShipmentModal({ shipment, onClose, onSubmit }: Props) {
  const [form, setForm] = useState<ShipmentRequest>({
    sender: { ...shipment.sender },
    receiver: { ...shipment.receiver },
    carrier: shipment.carrier,
    weight: shipment.weight,
  });

  const [infoMessage, setInfoMessage] = useState(""); 
  const [loading, setLoading] = useState(false);

  const handleChange = (path: string, value: string) => {
    setForm((prev: ShipmentRequest) => {
      const copy: any = { ...prev };
      const keys = path.split(".");
      let obj = copy;
      while (keys.length > 1) {
        const k = keys.shift()!;
        obj[k] = { ...obj[k] };
        obj = obj[k];
      }
      obj[keys[0]] = value;
      return copy;
    });
  };

  const handleSave = async () => {
    if (
      !form.sender.name ||
      !form.sender.address ||
      !form.receiver.name ||
      !form.receiver.address ||
      !form.carrier ||
      !form.weight
    ) {
      setInfoMessage("❌ Tous les champs sont obligatoires !");
      return;
    }

    setInfoMessage("");
    setLoading(true);

    try {
      await onSubmit({
        sender: form.sender,
        receiver: form.receiver,
        carrier: form.carrier,
        weight: Number(form.weight),
      });

      setInfoMessage("✅ Colis mis à jour avec succès !");
      setTimeout(() => onClose(), 3000);
    } catch (err: any) {
      console.error(err);
      const msg = err.response?.data?.message || err.message || "Échec de la mise à jour du colis";
      setInfoMessage(`❌ ${msg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-[#29565C] p-6 w-[500px] rounded relative">
        <h2 className="text-xl text-white font-bold mb-4">Modifier le colis</h2>

        {shipment && (
          <p className="mb-4 text-white">
            N° Colis : <span className="font-mono">{shipment.shipmentId}</span>
          </p>
        )}

        {/* Sender */}
        <label className="text-white block mb-1">Nom expéditeur</label>
        <input
          className="border p-2 w-full mb-2"
          value={form.sender.name}
          onChange={(e) => handleChange("sender.name", e.target.value)}
        />

        <label className="text-white block mb-1">Adresse expéditeur</label>
        <input
          className="border p-2 w-full mb-4"
          value={form.sender.address}
          onChange={(e) => handleChange("sender.address", e.target.value)}
        />

        {/* Receiver */}
        <label className="text-white block mb-1">Nom destinataire</label>
        <input
          className="border p-2 w-full mb-2"
          value={form.receiver.name}
          onChange={(e) => handleChange("receiver.name", e.target.value)}
        />

        <label className="text-white block mb-1">Adresse destinataire</label>
        <input
          className="border p-2 w-full mb-4"
          value={form.receiver.address}
          onChange={(e) => handleChange("receiver.address", e.target.value)}
        />

        {/* Carrier */}
        <label className="text-white block mb-1">Transporteur</label>
        <input
          className="border p-2 w-full mb-2"
          value={form.carrier}
          onChange={(e) => handleChange("carrier", e.target.value)}
        />

        {/* Weight */}
        <label className="text-white block mb-1">Poids (kg)</label>
        <input
          className="border p-2 w-full mb-4"
          value={form.weight}
          onChange={(e) => handleChange("weight", e.target.value)}
        />

        {/* Validation Message */}
        {infoMessage && (
          <div className="mb-3 p-2 rounded text-sm" 
               style={{ color: infoMessage.startsWith("✅") ? "lightgreen" : "salmon", backgroundColor: "#1F3B3D" }}>
            {infoMessage}
          </div>
        )}

        {/* Buttons */}
        <div className="flex justify-end gap-2">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 transition"
          >
            Annuler
          </button>
          <button
            onClick={handleSave}
            className="px-4 py-1 rounded bg-[#C21957] text-white hover:bg-[#930136] transition"
            disabled={loading}
          >
            {loading ? "Enregistrement…" : "Enregistrer"}
          </button>
        </div>
      </div>
    </div>
  );
}
