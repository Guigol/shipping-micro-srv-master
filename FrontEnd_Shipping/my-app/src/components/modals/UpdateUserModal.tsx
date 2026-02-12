import { useState } from "react";
import type { UserResponse, UpdateUserRequest } from "../../types/user";

type Props = {
  user: UserResponse;
  onClose: () => void;
  onSubmit: (data: UpdateUserRequest) => void;
};

export default function UpdateUserModal({ user, onClose, onSubmit }: Props) {
  const [name, setName] = useState(user.name);
  const [email, setEmail] = useState(user.email);
  const [address, setAddress] = useState(user.address);
  const [role, setRole] = useState<"ADMIN" | "USER">(user.role);

  const handleSubmit = () => {
    onSubmit({ name, email, address, role });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-[#29565C] p-6 rounded-lg w-96">
        <h2 className="text-xl text-white font-bold mb-4">Modifier l'utilisateur</h2>

        <input className="border p-2 w-full mb-2" placeholder="Nom" value={name} onChange={(e) => setName(e.target.value)} />
        <input className="border p-2 w-full mb-2" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input className="border p-2 w-full mb-2" placeholder="Adresse" value={address} onChange={(e) => setAddress(e.target.value)} />

        <select className="border p-2 w-full mb-4" value={role} onChange={(e) => setRole(e.target.value as "ADMIN" | "USER")}>
          <option value="USER">USER</option>
          <option value="ADMIN">ADMIN</option>
        </select>

        <div className="flex justify-end gap-2">
          <button className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 transition" onClick={onClose}>Annuler</button>
          <button className="px-4 py-2 rounded bg-[#C21957] text-white hover:bg-[#930136] transition" onClick={handleSubmit}>Enregistrer</button>
        </div>
      </div>
    </div>
  );
}
