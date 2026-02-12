import { Field, ErrorMessage } from "formik";

type FormikInputProps = {
  name: string;
  label: string;
  placeholder?: string;
};

export const FormikInput = ({ name, label, placeholder }: FormikInputProps) => (
  <div className="mb-2">
    <label className="block text-sm text-gray-600 mb-1">{label}</label>
    <Field
      name={name}
      placeholder={placeholder}
      className="w-full border border-gray-300 focus:border-gray-700 focus:ring-0 px-3 py-2 rounded-md bg-white"
    />
    <ErrorMessage
      name={name}
      component="p"
      className="text-red-600 text-xs mt-1"
    />
  </div>
);

type FormikTextareaProps = {
  name: string;
  label: string;
  placeholder?: string;
};

export const FormikTextarea = ({
  name,
  label,
  placeholder,
}: FormikTextareaProps) => (
  <div className="mb-2">
    <label className="block text-sm text-gray-600 mb-1">{label}</label>
    <Field
      as="textarea"
      rows={2}
      name={name}
      placeholder={placeholder}
      className="w-full border border-gray-300 focus:border-gray-700 focus:ring-0 px-3 py-2 rounded-md bg-white resize-none"
    />
    <ErrorMessage
      name={name}
      component="p"
      className="text-red-600 text-xs mt-1"
    />
  </div>
);

type PersonFormProps = {
  title: string;
  prefix: "sender" | "receiver";
};

export const PersonForm = ({ title, prefix }: PersonFormProps) => (
  <div className="border border-gray-200 rounded-md p-4 bg-white shadow-sm">
    <h3 className="text-sm font-semibold text-gray-700 mb-3">{title}</h3>

    <FormikInput
      name={`${prefix}.name`}
      label="Nom"
      placeholder="Obligatoire"
    />

    <FormikTextarea
      name={`${prefix}.address`}
      label="Adresse"
      placeholder="Obligatoire"
    />
  </div>
);
