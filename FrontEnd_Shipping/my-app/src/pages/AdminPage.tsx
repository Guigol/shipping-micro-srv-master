import { useEffect, useState } from "react";
import Navbar from "../components/layout/Navbar";
import Footer from "../components/layout/Footer";
import { useAuth } from "../context/AuthContext";

import {
  getAllUsers,
  createUser,
  updateUser,
  deleteUserById,
  notifyUserById,
} from "../services/userService";

import type {
  UserDto,
  CreateUserRequest,
  UpdateUserRequest,
  NotifyUserRequest,
} from "../types/user";

// Modales
import CreateUserModal from "../components/modals/CreateUserModal";
import UpdateUserModal from "../components/modals/UpdateUserModal";
import NotifyUserModal from "../components/modals/NotifyUserModal";

export default function AdminPage() {
  const { user, isAdmin } = useAuth();

  const [users, setUsers] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState<UserDto | null>(null);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showNotifyModal, setShowNotifyModal] = useState(false);

  const [userToDelete, setUserToDelete] = useState<UserDto | null>(null);

  const formatIsoLocalDate = (isoDate: string): string =>
    new Date(isoDate)
      .toLocaleString("sv-SE", { hour12: false })
      .replace(" ", "T");

  // -------------------------
  // LOAD USERS
  // -------------------------
  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getAllUsers();

      if (!Array.isArray(data)) {
        throw new Error("Réponse invalide (users non array)");
      }

      setUsers(data);
    } catch (e: any) {
      setError(e.message || "Erreur chargement utilisateurs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin) {
      loadUsers();
    }
  }, [isAdmin]);

  // -------------------------
  // FILTER
  // -------------------------
  const filteredUsers = users.filter(
    (u) =>
      u.name.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase()) ||
      u.role.toLowerCase().includes(search.toLowerCase())
  );

  // -------------------------
  // PAGINATION
  // -------------------------
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const paginatedUsers = filteredUsers.slice(
    (page - 1) * pageSize,
    page * pageSize
  );

  // -------------------------
  // CRUD ACTIONS
  // -------------------------
  const handleCreateUser = async (data: CreateUserRequest) => {
    await createUser(data);
    setShowCreateModal(false);
    loadUsers();
  };

  const handleUpdateUser = async (data: UpdateUserRequest) => {
    if (!selectedUser) return;
    await updateUser(selectedUser.userId, data);
    setShowUpdateModal(false);
    loadUsers();
  };

  const handleNotifyUser = async (message: string) => {
    if (!selectedUser) return;
    const payload: NotifyUserRequest = { message };
    await notifyUserById(selectedUser.userId, payload);
    setShowNotifyModal(false);
  };

  const confirmDeleteUser = async () => {
    if (!userToDelete) return;

    if (user?.userId === userToDelete.userId) {
      alert("Vous ne pouvez pas supprimer votre propre compte.");
      return;
    }

    await deleteUserById(userToDelete.userId);
    setUserToDelete(null);
    loadUsers();
  };

  // -------------------------
  // RENDER
  // -------------------------
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />

      <div className="p-6 flex-1">
        <h1 className="text-2xl font-bold mb-4">
          Gestion des utilisateurs
        </h1>

        <div className="flex mb-4 gap-2">
          <input
            className="border p-2 w-full"
            placeholder="Recherche (nom, email, rôle)"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <button
            className="bg-[#C21957] text-white px-4 py-2 rounded hover:bg-[#930136]"
            onClick={() => setShowCreateModal(true)}
          >
            + Créer
          </button>
        </div>

        {loading && <p>Chargement…</p>}
        {error && <p className="text-red-600">{error}</p>}

        <div className="mt-10 overflow-hidden rounded-lg border border-gray-400 shadow-2xl">
          <table className="w-full">
            <thead className="bg-gray-300">
              <tr className="text-center">
                <th>ID</th>
                <th>Nom</th>
                <th>Email</th>
                <th>Adresse</th>
                <th>Rôle</th>
                <th>Créé le</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginatedUsers.map((u) => (
                <tr key={u.userId} className="border-t text-center">
                  <td>{u.userId}</td>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>{u.address}</td>
                  <td className="font-semibold">{u.role}</td>
                  <td>{formatIsoLocalDate(u.createdAt)}</td>
                  <td className="space-x-2">
                    <button
                      onClick={() => {
                        setSelectedUser(u);
                        setShowUpdateModal(true);
                      }}
                    >
                      ✏️
                    </button>
                    <button
                      onClick={() => {
                        setSelectedUser(u);
                        setShowNotifyModal(true);
                      }}
                    >
                      ✉️
                    </button>
                    {user?.userId !== u.userId && (
                      <button
                        onClick={() => setUserToDelete(u)}>
                        🗑️
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showCreateModal && (
        <CreateUserModal
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreateUser}
        />
      )}

      {showUpdateModal && selectedUser && (
        <UpdateUserModal
          user={selectedUser}
          onClose={() => setShowUpdateModal(false)}
          onSubmit={handleUpdateUser}
        />
      )}

      {showNotifyModal && selectedUser && (
        <NotifyUserModal
          user={selectedUser}
          onClose={() => setShowNotifyModal(false)}
          onSubmit={handleNotifyUser}
        />
      )}

      {/* DELETE CONFIRM MODAL */}
      {userToDelete && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center">
          <div className="bg-[#29565C] rounded-lg p-6 w-[420px] shadow-xl">
            <p className="mb-6 text-white">
              Supprimer l’utilisateur{" "}
              <strong>{userToDelete.email}</strong> ?
            </p>
            <div className="flex justify-end gap-3 mt-4">
              <button className="px-4 py-2 rounded bg-gray-300 hover:bg-gray-400"
               onClick={() => setUserToDelete(null)}>
                Annuler
              </button>
              <button className="px-4 py-2 rounded bg-[#C21957] text-white hover:bg-[#930136] transition"
               onClick={confirmDeleteUser}>
                Supprimer
              </button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}
