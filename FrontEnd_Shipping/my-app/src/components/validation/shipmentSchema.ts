import * as Yup from "yup";

const MAX_WEIGHT_KG = 9;

export const shipmentSchema = Yup.object({
  carrier: Yup.string()
    .trim()
    .required("Le transporteur est obligatoire"),

  weight: Yup.number()
    .typeError("Le poids est obligatoire")
    .moreThan(0, "Le poids doit être supérieur à 0")
    .max(MAX_WEIGHT_KG, `Le poids ne peut pas dépasser ${MAX_WEIGHT_KG} kg`)
    .required("Le poids est obligatoire"),

  sender: Yup.object({
    name: Yup.string().trim().required("Nom expéditeur obligatoire"),
    address: Yup.string().trim().required("Adresse expéditeur obligatoire"),
  }),

  receiver: Yup.object({
    name: Yup.string().trim().required("Nom destinataire obligatoire"),
    address: Yup.string().trim().required("Adresse destinataire obligatoire"),
  }),
});
