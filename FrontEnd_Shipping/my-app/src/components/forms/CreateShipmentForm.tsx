import { useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { Formik, Form, Field, ErrorMessage } from "formik";
import { shipmentSchema } from "../validation/shipmentSchema";
import type { Person } from "../../types/valid/ShipmentFormValues";
import {
  FormikInput,
  FormikTextarea,
  PersonForm,
} from "../formik/FormikFields";


type ShipmentFormValues = {
  carrier: string;
  weight: number | "";
  sender: Person;
  receiver: Person;
};

const CreateShipmentForm = () => {
  const { isAuthenticated } = useAuth();

  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [trackingNumber, setTrackingNumber] = useState<string | null>(null);

  const initialValues: ShipmentFormValues = {
    carrier: "",
    weight: "",
    sender: { name: "", address: "" },
    receiver: { name: "", address: "" },
  };

  return (
    <section className="bg-gray-50 p-5 rounded-lg border border-gray-300 shadow-lg max-w-xl lg:w-[640px]">
      <h2 className="text-lg font-semibold mb-4 text-gray-800">
        Envoyer un colis
      </h2>

      {success && <p className="text-green-600 mb-2">{success}</p>}
      {trackingNumber && (
        <p className="text-sm text-gray-700 mb-2">
          Numéro de suivi :{" "}
          <span className="font-semibold">{trackingNumber}</span>
        </p>
      )}
      {error && <p className="text-red-600 mb-2">{error}</p>}

      <Formik
        initialValues={initialValues}
        validationSchema={shipmentSchema}
        onSubmit={async (values, { resetForm }) => {
          setError(null);
          setSuccess(null);
          setTrackingNumber(null);

          if (!isAuthenticated) {
            setError("Vous devez être connecté pour créer un colis");
            return;
          }

          const payload = {
            sender: values.sender,
            receiver: values.receiver,
            carrier: values.carrier,
            metadata: {
              weight_kg: values.weight,
            },
          };

          try {
            setLoading(true);

            const response = await fetch("/api/shipping", {
              method: "POST",
              credentials: "include",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(payload),
            });

            const text = await response.text();
            const parsedData = text ? JSON.parse(text) : null;

            if (!response.ok) {
              console.error("Erreur backend :", parsedData);
              throw new Error(
                parsedData?.message || "Erreur lors de la création du colis"
              );
            }

            setSuccess("Colis créé avec succès");
            setTrackingNumber(parsedData?.data?.trackingNumber || null);
            resetForm();
          } catch (err: any) {
            console.error("Erreur front :", err);
            setError(err.message || "Erreur lors de la création du colis");
          } finally {
            setLoading(false);
          }
        }}
         >
        {() => (
          <Form className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <PersonForm title="Expéditeur" prefix="sender"/>
              <PersonForm title="Destinataire" prefix="receiver" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <FormikInput name="carrier" label="Transporteur" placeholder="Obligatoire"/>

              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Poids (kg)
                </label>
                <Field
                  type="number"
                  placeholder="Obligatoire (Max 9 Kg)"
                  name="weight"
                  className="w-full border border-gray-300 focus:border-gray-700 focus:ring-0 px-3 py-2 rounded-md bg-white"
                />
                <ErrorMessage
                  name="weight"
                  component="p"
                  className="text-red-600 text-xs mt-1"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-[#29565C] text-white px-4 py-2 rounded-md hover:bg-[#C21957] transition shadow-sm"
            >
              {loading ? "Création..." : "Créer le colis"}
            </button>
          </Form>
        )}
      </Formik>
    </section>
  );
};

export default CreateShipmentForm;
