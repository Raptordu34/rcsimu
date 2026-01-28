.PHONY: all build run

JAVA_CMD = java --enable-native-access=ALL-UNNAMED
MAIN_CLASS = fr.ensma.a3.ia.rcservice.RCServiceLauncher
TEST_JAR = target/rcservice-1.0-SNAPSHOT.jar
DEPS = target/dependency/*

REPO_API = https://gitlab-ia.ensma.fr/be2025/rcsimu/rc/servocontrolbusiness.git
REPO_DRIVER = https://gitlab-ia.ensma.fr/be2025/rcsimu/rc/servohatdriver.git

ROOT_DIR = ..

all:  build

build:
	@cd .. set -e; \
	cd servohatdriver && mvn clean install && \
	cd ../servocontrolbusiness && mvn clean install && \
	cd ../javacvwebcamframestream && mvn clean install && \
	cd ../webcamframestreambusiness && mvn clean install && \
	cd ../servocamerabusiness && mvn clean install && \
	cd ../serviceapi && mvn clean install && \
	cd ../rcservice && mvn clean install

run:
	sudo $(JAVA_CMD) -cp $(TEST_JAR):$(DEPS) $(MAIN_CLASS)
