module.exports = {
  content: {
    files: ["./src/**/*.{clj,cljs,cljc,html,js}"],
    extract: {
      // Custom extractor for ClojureScript syntax like :.p-10.flex.gap-4
      clj: (content) => {
        const matches = []
        // Match :. followed by dot-separated class names
        const regex = /:\.([\w\-\.]+)/g
        let match
        while ((match = regex.exec(content)) !== null) {
          // Split by dots and add each class
          match[1].split('.').forEach(cls => {
            if (cls) matches.push(cls)
          })
        }
        // Also match :class "..." patterns
        const classRegex = /:class\s+"([^"]+)"/g
        while ((match = classRegex.exec(content)) !== null) {
          match[1].split(/\s+/).forEach(cls => {
            if (cls) matches.push(cls)
          })
        }
        return matches
      },
      cljc: (content) => {
        const matches = []
        const regex = /:\.([\w\-\.]+)/g
        let match
        while ((match = regex.exec(content)) !== null) {
          match[1].split('.').forEach(cls => {
            if (cls) matches.push(cls)
          })
        }
        const classRegex = /:class\s+"([^"]+)"/g
        while ((match = classRegex.exec(content)) !== null) {
          match[1].split(/\s+/).forEach(cls => {
            if (cls) matches.push(cls)
          })
        }
        return matches
      },
      cljs: (content) => {
        const matches = []
        const regex = /:\.([\w\-\.]+)/g
        let match
        while ((match = regex.exec(content)) !== null) {
          match[1].split('.').forEach(cls => {
            if (cls) matches.push(cls)
          })
        }
        const classRegex = /:class\s+"([^"]+)"/g
        while ((match = classRegex.exec(content)) !== null) {
          match[1].split(/\s+/).forEach(cls => {
            if (cls) matches.push(cls)
          })
        }
        return matches
      },
    },
  },
  safelist: [
    // Margin and padding for even numbers up to 20
    ...['m', 'mx', 'my', 'mt', 'mr', 'mb', 'ml', 'p', 'px', 'py', 'pt', 'pr', 'pb', 'pl'].flatMap(prefix =>
      [2, 4, 6, 8, 10, 12, 14, 16, 18, 20].map(n => `${prefix}-${n}`)
    ),
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
