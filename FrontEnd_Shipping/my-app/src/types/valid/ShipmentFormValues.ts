export type Person = {
  name: string;
  address: string;
};

export type ShipmentFormValues = {
  carrier: string;
  weight: number | "";
  sender: Person;
  receiver: Person;
};
