module.exports = {
  content: ["./src/**/*.{clj,cljs,cljc,html,js}"],
  safelist: [
    // Force include these patterns
    {
      pattern: /(bg|text|border|p|m|px|py|mx|my|w|h|flex|grid|rounded)-.*/,
    },
    'flex',
    'flex-col',
    'flex-wrap',
    'text-center',
    'font-bold',
    'font-poppins',
  ],
  theme: {
    extend: {
      fontFamily: {
        poppins: ["Poppins", "sans-serif"],
      },
    },
  },
  plugins: [],
}
