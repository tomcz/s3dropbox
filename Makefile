GIT_TAG := $(shell git describe --tags 2>/dev/null)

.PHONY: all
all: clean test build

.PHONY: build
build: clean
	./gradlew --console plain -Pversion=${GIT_TAG} shadowJar

.PHONY: clean
clean:
	./gradlew --console plain clean

.PHONY: test
test:
	./gradlew --console plain test -Dignore.integration.tests='true'

.PHONY: deps
deps:
	./gradlew dependencies
