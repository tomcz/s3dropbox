.PHONY: build-jar build-dmg clean test deps

GIT_SHA ?= $(shell git rev-parse --short HEAD)

build-jar:
	./gradlew --console plain -Pversion=${GIT_SHA} clean fatJar

build-dmg:
	./gradlew --console plain -Pversion=${GIT_SHA} clean createDmg

clean:
	./gradlew clean

test:
	./gradlew test

deps:
	./gradlew dependencies
