import { useState } from "react";
import type { UserResponse } from "../../types/user";

type Props = {
  user: UserResponse;
  onClose: () => void;
  onSubmit: (message: string) => Promise<void>; // async
};

export default function NotifyUserModal({ user, onClose, onSubmit }: Props) {
  const [message, setMessage] = useState("");
  const [info, setInfo] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
  if (!message.trim()) return;

  setLoading(true);
  try {
    await onSubmit(message);
    setInfo("Message envoyé avec succès ✅");
    setMessage("");

    setTimeout(() => {
      onClose();
    }, 1500);
  } catch {
    setInfo("Erreur lors de l’envoi ❌");
  } finally {
    setLoading(false);
  }
};


  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-[#29565C] p-6 rounded-lg w-96">
        <h2 className="text-xl text-white font-bold mb-4">
          Notifier {user.name}
        </h2>

        {info && (
          <div className="mb-4 p-2 rounded bg-white text-[#29565C] text-sm">
            {info}
          </div>
        )}

        <textarea
          className="border rounded p-2 w-full mb-4 focus:ring-2 focus:ring-gray-600"
          placeholder="Votre message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />

        <div className="flex justify-end gap-2">
          <button
            className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300"
            onClick={onClose}
          >
            Annuler
          </button>
          <button
            className="px-4 py-2 bg-[#C21957] text-white hover:bg-[#930136] rounded disabled:opacity-50"
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? "Envoi..." : "Envoyer"}
          </button>
        </div>
      </div>
    </div>
  );
}
