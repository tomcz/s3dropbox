GIT_SHA ?= $(shell git rev-parse --short=7 HEAD)

.PHONY: build-jar
build-jar: clean
	./gradlew --console plain -Pversion=${GIT_SHA} shadowJar

.PHONY: clean
clean:
	./gradlew --console plain clean

.PHONY: test
test:
	./gradlew --console plain test -Dignore.integration.tests='true'

.PHONY: deps
deps:
	./gradlew dependencies
