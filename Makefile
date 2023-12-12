GIT_TAG := $(shell git describe --tags 2>/dev/null)

ifeq "${GIT_TAG}" ""
GIT_TAG := dev
endif

.PHONY: all
all:
	./gradlew --console plain -Pversion=${GIT_TAG}

.PHONY: deps
deps:
	./gradlew dependencies
