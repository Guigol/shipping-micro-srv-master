import { useState } from "react";
import { useAuth } from "../../context/AuthContext";

type HistoryItem = {
  status: string;
  userId: number;
  timestamp: string;
  location: string;
  note: string;
};

const TrackShipmentForm = () => {
  const { user, isAuthenticated } = useAuth();

  const [trackingNumber, setTrackingNumber] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentStatus, setCurrentStatus] = useState<string | null>(null);
  const [history, setHistory] = useState<HistoryItem[]>([]);

  const handleTrack = async () => {
    setError(null);
    setCurrentStatus(null);
    setHistory([]);

    if (!isAuthenticated) {
      setError("Vous devez être connecté pour suivre un colis");
      return;
    }

    if (!trackingNumber.trim()) {
      setError("Veuillez saisir un numéro de tracking");
      return;
    }

    try {
      setLoading(true);

      const response = await fetch(`/api/tracking/${trackingNumber}`, {
        credentials: "include",
      });

      const text = await response.text();
      const parsedData = text ? JSON.parse(text) : null;

      // Vérification du success
      if (!parsedData.success) {
        if (parsedData.message?.toLowerCase().includes("trackingnumber")) {
          setError("Numéro de tracking inconnu");
        } else {
          setError(parsedData.message || "Erreur lors du suivi du colis");
        }
        setCurrentStatus(null);
        setHistory([]);
        return;
      }

      // Cas succès
      setCurrentStatus(parsedData.data?.currentStatus || null);
      setHistory(parsedData.data?.history || []);
    } catch (err: any) {
      console.error("Erreur front :", err);
      setError(err.message || "Erreur lors du suivi du colis");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="w-full lg:w-[420px] bg-gray-50 p-5 rounded-lg border border-gray-300 shadow-lg">
      <h2 className="text-lg font-semibold mb-4">Suivre un colis</h2>

      {error && <p className="text-red-600 mb-2">{error}</p>}

      <input
        value={trackingNumber}
        onChange={(e) => setTrackingNumber(e.target.value)}
        placeholder="Numéro de tracking"
        className="w-full border border-gray-300 focus:border-black focus:ring-0 px-3 py-2 rounded-md bg-white shadow-sm mb-4"
      />

      <button
        onClick={handleTrack}
        disabled={loading}
        className="bg-[#29565C] text-white px-4 py-2 rounded-md"
      >
        {loading ? "Recherche..." : "Suivre"}
      </button>

      {currentStatus && (
        <div className="mt-4">
          <p className="font-semibold text-gray-800">
            Statut actuel : {currentStatus}
          </p>

          {history.length > 0 && (
            <ul className="mt-3 space-y-2 text-sm text-gray-700">
              {history.map((item, index) => (
                <li
                  key={index}
                  className="border border-gray-200 rounded-md p-2 bg-white"
                >
                  <div className="font-medium">{item.status}</div>
                  <div>{item.location}</div>
                  <div className="text-xs text-gray-500">
                    {new Date(item.timestamp).toLocaleString()}
                  </div>
                  <div className="italic">{item.note}</div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  );
};

export default TrackShipmentForm;
