import UploadProofForm from "../components/forms/UploadProofForm";
import Navbar from "../components/layout/Navbar";
import Footer from "../components/layout/Footer";

export default function ProofsDepDelPage() {
  return (
    <div className="flex flex-col min-h-screen">
      {/* Navbar en haut */}
      <Navbar />

      {/* Contenu principal */}
      <main className="flex-1 p-8">
        <h1 className="text-2xl font-bold mb-6 text-gray-800">
          Dépôt / Livraison
        </h1>

        <div className="flex justify-center">
          <UploadProofForm />
        </div>
      </main>

      {/* Footer en bas */}
      <Footer />
    </div>
  );
}
