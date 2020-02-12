.PHONY: build-jar build-dmg clean test deps

GIT_SHA ?= $(shell git rev-parse --short=7 HEAD)

build-jar:
	./gradlew --console plain -Pversion=${GIT_SHA} clean shadowJar

build-dmg:
	./gradlew --console plain -Pversion=${GIT_SHA} clean createDmg

clean:
	./gradlew clean

test:
	./gradlew test

deps:
	./gradlew dependencies
