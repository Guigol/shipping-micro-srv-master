const UnauthorizedPage = () => {
  return (
    <div className="p-8 text-center">
      <h1 className="text-2xl font-bold text-red-600">Accès refusé</h1>
      <p className="mt-4">Vous n’avez pas les droits nécessaires.</p>
    </div>
  );
};

export default UnauthorizedPage;
