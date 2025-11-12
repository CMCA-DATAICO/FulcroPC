cljs:
	npx shadow-cljs server

report:
	npx shadow-cljs run shadow.cljs.build-report main report.html

release:
	TIMBRE_LEVEL=:warn npx shadow-cljs release main

server:
	clj -A:dev -M -m com.example.components.server

tw:
	npm run tailwind:build

tailwind-watch:
	npm run tailwind:build