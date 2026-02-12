import Navbar from "../components/layout/Navbar";
import Footer from "../components/layout/Footer";
import CreateShipmentForm from "../components/forms/CreateShipmentForm";
import TrackShipmentForm from "../components/forms/TrackShipmentForm";
import img from "../assets/pigeon.png";

const MainPage = () => {
  return (
    <div className="min-h-screen flex flex-col text-gray-900">
      <Navbar />

      <main className="flex-1 p-8">
        <h1 className="text-2xl font-bold mb-6">
          Envoyer un colis
        </h1>

        <div className="flex flex-col lg:flex-row gap-6 items-start justify-center">
          <CreateShipmentForm />
          <div className="flex flex-col items-center">
            <TrackShipmentForm />
            {/* Image under the form */}
            <img
              src={img}
              alt="Illustration pigeon"
              className="mt-4 max-w-md max-h-64 object-contain"
            />
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default MainPage;
