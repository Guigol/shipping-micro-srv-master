import logo from "../../assets/octopus.svg";

const Footer = () => {
  return (
    <footer className="p-4 bg-[#343434] text-white">
      <div className="flex justify-center items-center space-x-2">
        <span className="text-sm font-medium">© 2025 Shipping App</span>
        <img src={logo} alt="Logo" className="h-6 w-auto" />
      </div>
    </footer>
  );
};

export default Footer;
