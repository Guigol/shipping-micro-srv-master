import { useState } from "react";
import { useAuth } from "../../context/AuthContext";

type ProofType = "depositProof" | "deliveryProof";
type Message = { type: "success" | "error" | "info"; text: string } | null;

const UploadProofForm = () => {
  const { isAuthenticated } = useAuth(); // <-- plus de token
  const [shipmentId, setShipmentId] = useState("");
  const [depositFile, setDepositFile] = useState<File | null>(null);
  const [deliveryFile, setDeliveryFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<Message>(null);

  const uploadProof = async (type: ProofType) => {
    setMessage(null);

    if (!isAuthenticated) {
      setMessage({ type: "error", text: "Vous devez être connecté pour effectuer cette action." });
      return;
    }

    if (!shipmentId) {
      setMessage({ type: "error", text: "Veuillez renseigner le ShipmentId" });
      return;
    }

    const file = type === "depositProof" ? depositFile : deliveryFile;
    if (!file) {
      setMessage({ type: "error", text: "Veuillez sélectionner un fichier" });
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("type", type);

    try {
      setLoading(true);

      const res = await fetch(`/api/store/${shipmentId}/upload-proof`, {
        method: "POST",
        credentials: "include", // <-- cookie JWT sera envoyé automatiquement
        body: formData,
      });

      const data = res.ok ? await res.json() : await res.text();

      if (!res.ok) {
        console.error("Erreur backend :", data);
        throw new Error(data?.message || "Erreur lors de l'upload");
      }

      setMessage({ type: "success", text: "Preuve envoyée avec succès" });
      console.log("UPLOAD OK :", data);
    } catch (err: any) {
      console.error("Erreur front :", err);
      setMessage({ type: "error", text: err.message || "Erreur upload" });
    } finally {
      setLoading(false);
    }
  };

  const downloadProof = async (type: ProofType) => {
    setMessage(null);

    if (!isAuthenticated) {
      setMessage({ type: "error", text: "Vous devez être connecté pour effectuer cette action." });
      return;
    }

    if (!shipmentId) {
      setMessage({ type: "error", text: "Veuillez renseigner le ShipmentId" });
      return;
    }

    try {
      setLoading(true);

      const res = await fetch(`/api/store/${shipmentId}/proof/${type}`, {
        credentials: "include", // <-- cookie JWT
      });

      if (!res.ok) throw new Error("Preuve non trouvée");

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = url;
      a.download = `${type}-${shipmentId}`;
      a.click();
      window.URL.revokeObjectURL(url);

      setMessage({ type: "success", text: "Preuve téléchargée avec succès" });
    } catch (err: any) {
      console.error("Erreur front :", err);
      setMessage({ type: "error", text: err.message || "Erreur téléchargement" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="bg-white p-6 rounded-lg shadow-lg">
      <h2 className="text-lg font-semibold mb-4 text-gray-800">
        Preuves de dépôt / livraison
      </h2>

      {message && (
        <div
          className={`mb-4 rounded-md px-4 py-2 text-sm ${
            message.type === "success"
              ? "bg-green-100 text-green-700 border border-green-300"
              : message.type === "error"
              ? "bg-red-100 text-red-700 border border-red-300"
              : "bg-blue-100 text-blue-700 border border-blue-300"
          }`}
        >
          {message.text}
        </div>
      )}

      {/* Shipment ID */}
      <div className="mb-6">
        <label className="block text-sm text-gray-600 mb-1">Shipment ID</label>
        <input
          value={shipmentId}
          onChange={(e) => setShipmentId(e.target.value)}
          className="w-full border border-gray-300 rounded-md px-3 py-2"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Dépôt */}
        <div className="border border-gray-200 rounded-md p-4 bg-gray-50 shadow-sm flex flex-col">
          <h3 className="font-medium text-gray-700 mb-3">Preuve de dépôt</h3>
          <input
            type="file"
            onChange={(e) => setDepositFile(e.target.files?.[0] || null)}
            className="w-full border border-gray-300 rounded-md px-3 py-2 mb-4"
          />
          <div className="flex gap-2">
            <button
              disabled={loading}
              onClick={() => uploadProof("depositProof")}
              className="flex-1 bg-[#C21957] text-white px-4 py-2 rounded-md hover:bg-[#930136] transition shadow-sm"
            >
              Envoyer
            </button>
            <button
              disabled={loading}
              onClick={() => downloadProof("depositProof")}
              className="flex-1 bg-[#29565C] text-white px-4 py-2 rounded-md hover:bg-gray-700 transition shadow-sm"
            >
              Télécharger
            </button>
          </div>
        </div>

        {/* Livraison */}
        <div className="border border-gray-200 rounded-md p-4 bg-gray-50 shadow-sm flex flex-col">
          <h3 className="font-medium text-gray-700 mb-3">Preuve de livraison</h3>
          <input
            type="file"
            onChange={(e) => setDeliveryFile(e.target.files?.[0] || null)}
            className="w-full border border-gray-300 rounded-md px-3 py-2 mb-4"
          />
          <div className="flex gap-2">
            <button
              disabled={loading}
              onClick={() => uploadProof("deliveryProof")}
              className="flex-1 bg-[#C21957] text-white px-4 py-2 rounded-md hover:bg-[#930136] transition shadow-sm"
            >
              Envoyer
            </button>
            <button
              disabled={loading}
              onClick={() => downloadProof("deliveryProof")}
              className="flex-1 bg-[#29565C] text-white px-4 py-2 rounded-md hover:bg-gray-700 transition shadow-sm"
            >
              Télécharger
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};

export default UploadProofForm;
