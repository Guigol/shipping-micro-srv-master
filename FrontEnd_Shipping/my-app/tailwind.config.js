/** @type {import('tailwindcss').Config} */
import daisyui from "daisyui";
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
    "node_modules/daisyui/dist/**/*.js",
    "node_modules/react-daisyui/dist/**/*.js",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Poppins", "Arial", "sans-serif"], 
      },
    },
    fontFamily: {
      handy: ["Patrick Hand"],
    },
  },
  daisyui: {
    themes: [
      {
        mytheme: {
          primary: "#343434",
          secondary: "#29565C",
          accent: "#C21957",
          neutral: "#AFD8DA",
          "base-100": "#AFD8DA",
          info: "#AFD8DA",
          success: "#1B8C44",
          "success-content": "#ffffff",
          warning: "#BA3923",
          error: "#EA5D90",
          form:"#FEF8F1"
        },
      },
    ],
  },
  plugins: [daisyui],
};
