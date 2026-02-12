import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Bell } from "lucide-react";
import LoginForm from "../forms/LoginForm";
import { useAuth } from "../../context/AuthContext";
import logo from "../../assets/pigeon.png";

export default function Navbar() {
  const [showLogin, setShowLogin] = useState(false);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false); // <-- burger menu
  const { user, logout } = useAuth();
  const location = useLocation();

  const isActive = (path: string) =>
    location.pathname === path
      ? "font-bold text-[#AFD8DA]"
      : "hover:text-[#AFD8DA] transition";

  const handleClickMenu = (path: string) => {
    if (!user && path !== "/") {
      setInfoMessage("Vous devez être connecté pour accéder à cette page.");
      setTimeout(() => setInfoMessage(null), 3000); // disparaît après 3s
      return false;
    }
    return true;
  };

  return (
    <nav className="flex flex-col md:flex-row justify-between items-start md:items-center p-4 bg-[#343434] text-white shadow-md relative">
      {/* Logo */}
      <div className="flex items-center space-x-2 font-bold text-lg mb-2 md:mb-0">
        <span>Welcome to... The Shipping Service !</span>
        <img src={logo} alt="Logo" className="h-16 w-auto" />
      </div>

      {/* Burger button */}
      <button
        className="md:hidden text-2xl ml-auto mb-2 focus:outline-none"
        onClick={() => setIsMenuOpen(!isMenuOpen)}
      >
        ☰
      </button>

      {/* Menu */}
      <div
        className={`flex-col md:flex-row md:flex space-y-2 md:space-y-0 md:space-x-6 items-start md:items-center w-full md:w-auto absolute md:static left-0 top-full md:top-auto bg-[#343434] md:bg-transparent p-4 md:p-0 transition-all ${
          isMenuOpen ? "flex" : "hidden md:flex"
        }`}
      >
        <Link to="/" className={isActive("/")}>
          Envoi colis
        </Link>

        <Link
          to={user ? "/parcel" : "#"}
          className={isActive("/parcel")}
          onClick={(e) => {
            if (!handleClickMenu("/parcel")) e.preventDefault();
            setIsMenuOpen(false);
          }}
        >
          Gestion des colis
        </Link>

        <Link
          to={user ? "/proofs" : "#"}
          className={isActive("/proofs")}
          onClick={(e) => {
            if (!handleClickMenu("/proofs")) e.preventDefault();
            setIsMenuOpen(false);
          }}
        >
          Dépôt / Livraison
        </Link>

        {user?.role === "ADMIN" && (
          <Link
            to="/admin"
            className={
              location.pathname === "/admin"
                ? "bg-[#930136] px-3 py-1 rounded font-bold"
                : "bg-[#C21957] px-3 py-1 rounded hover:bg-[#930136] transition"
            }
            onClick={() => setIsMenuOpen(false)}
          >
            Gestion Users
          </Link>
        )}
      </div>

      {/* Actions */}
      <div className="flex items-center space-x-4 mt-2 md:mt-0">
        <button className="relative hover:text-[#AFD8DA] transition">
          <Bell size={22} />
          <span className="absolute -top-1 -right-1 h-2 w-2 bg-red-500 rounded-full" />
        </button>

        {user ? (
          <button
            onClick={logout}
            className="bg-[#AFD8DA] text-black px-4 py-2 rounded hover:bg-white transition"
          >
            Déconnexion
          </button>
        ) : (
          <button
            onClick={() => setShowLogin(true)}
            className="bg-[#C21957] px-4 py-2 rounded hover:bg-[#29565C] transition"
          >
            Se connecter
          </button>
        )}
      </div>

      {/* Info message */}
      {infoMessage && (
        <div className="mt-2 text-[#AFD8DA] text-sm font-semibold">
          {infoMessage}
        </div>
      )}

      {/* Modal login */}
      {showLogin && <LoginForm onClose={() => setShowLogin(false)} />}
    </nav>
  );
}
