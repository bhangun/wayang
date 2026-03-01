.PHONY: error-codes ci build-all dev

error-codes:
	./scripts/generate-error-codes.sh

ci: error-codes
	git diff --exit-code docs/error-codes.md

build-all:
	chmod +x build-all.sh && ./build-all.sh

dev: build-all
	cd runtime/wayang-runtime-standalone && ./run-dev.sh
