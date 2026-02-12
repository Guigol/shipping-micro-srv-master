import { useState } from "react";
import { useAuth } from "../../context/AuthContext";

type Props = { onClose: () => void };

export default function LoginForm({ onClose }: Props) {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    try {
      await login(email, password);
      // Connexion OK -> close modal
      onClose();
    } catch (err: any) {
      // message sent from the back
      setError(err.message || "Erreur de connexion");
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <form
        onSubmit={handleSubmit}
        className="bg-[#29565C] p-6 rounded shadow-md w-80"
      >
        <h2 className="text-xl mb-4">Connexion</h2>

        {error && <p className="text-red-500 mb-2">{error}</p>}

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="border-2 border-gray-700 text-black placeholder:text-gray-500 p-2 w-full mb-2 focus:outline-none focus:border-gray-700"
          required
        />

        <input
          type="password"
          placeholder="Mot de passe"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="border-2 border-gray-700 text-black placeholder:text-gray-500 p-2 w-full mb-4 focus:outline-none focus:border-black"
          required
        />


        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-black rounded bg-gray-200 hover:bg-gray-300 transition"
          >
            Annuler
          </button>

          <button
            type="submit"
            className="px-4 py-2 bg-[#C21957] text-white rounded hover:bg-[#930136]  transition"
          >
            Se connecter
          </button>
        </div>
      </form>
    </div>
  );
}
