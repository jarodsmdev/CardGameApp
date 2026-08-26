/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        rm: {
          blue: '#1E88E5',
          'blue-dark': '#1565C0',
          'blue-light': '#42A5F5',
          gold: '#F9A825',
          'gold-dark': '#F57F17',
          'gold-light': '#FDD835',
          black: '#1B1B1B',
          gray: {
            900: '#212121',
            800: '#333333',
            700: '#555555',
            400: '#9E9E9E',
            200: '#E0E0E0',
          },
          red: '#C62828',
          green: '#2E7D32',
          bg: '#0D1117',
          'bg-card': '#161B22',
          'bg-card-hover': '#1C2333',
          text: '#E6EDF3',
          'text-muted': '#8B949E',
          border: '#30363D',
        }
      },
      fontFamily: {
        display: ['Playfair Display', 'Georgia', 'serif'],
        body: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
      },
      animation: {
        'float': 'float 4s ease-in-out infinite',
        'pulse-glow': 'pulse-glow 3s ease-in-out infinite',
        'bounce-down': 'bounce-down 2s ease-in-out infinite',
        'fade-in-up': 'fade-in-up 0.8s ease',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-12px)' },
        },
        'pulse-glow': {
          '0%, 100%': { boxShadow: '0 0 20px rgba(249, 168, 37, 0.3)' },
          '50%': { boxShadow: '0 0 40px rgba(249, 168, 37, 0.6)' },
        },
        'bounce-down': {
          '0%, 100%': { transform: 'translateX(-50%) translateY(0)' },
          '50%': { transform: 'translateX(-50%) translateY(10px)' },
        },
        'fade-in-up': {
          from: { opacity: '0', transform: 'translateY(30px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        }
      }
    },
  },
  plugins: [],
}
