/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        ethiopia: {
          green: '#007A33',
          yellow: '#FFDE00',
          red: '#DA121A',
        },
        slate: {
          850: '#17202e',
          900: '#121722',
          950: '#0b0f17',
        },
        steel: {
          DEFAULT: '#252d3d',
          light: '#323c52',
          dark: '#1c222e',
        },
        accent: {
          teal: '#00A896',
          amber: '#F59E0B',
          blue: '#2563EB',
        }
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans Ethiopic', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
