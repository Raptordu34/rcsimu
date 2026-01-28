# Build env
FROM maven:3.9.12-eclipse-temurin-21 AS build-java-stage
LABEL maintainer="Mickael BARON"

WORKDIR /simurcserver
COPY src src
COPY pom.xml .

RUN mvn -f pom.xml clean package

# Run env
FROM openliberty/open-liberty:kernel-slim-java21-openj9-ubi-minimal

ARG VERSION=1.0
ARG REVISION=SNAPSHOT

LABEL \
    org.opencontainers.image.authors="Mickael BARON" \
    org.opencontainers.image.url="local" \
    org.opencontainers.image.source="https://gitlab-ia.ensma.fr/be2025/rcsimu/simurcserver" \
    org.opencontainers.image.version="$VERSION" \
    org.opencontainers.image.revision="$REVISION" \
    vendor="Mickael BARON" \
    name="rest" \
    version="$VERSION-$REVISION" \
    summary="Ce projet sert de relais entre RC et Simu, en gérant indépendamment chaque flux et en autorisant un seul émetteur pour plusieurs récepteurs." \
    description="Ce projet agit comme un relais afin que le composant RC et Simu puisse communiquer car la communication directe est impossible. Chaque fonctionnalité (capteurs, contrôle, webcam, données) sont indépendants afin que les fréquences de rafraichissement puissent être différentes. SimuRCServer joue le rôle de passe-plat avec comme fonctionnement de n'autoriser qu'un émetteur et plusieurs récepteurs."

COPY --chown=1001:0 --from=build-java-stage /simurcserver/src/main/liberty/config/server.xml /config/server.xml
COPY --chown=1001:0 --from=build-java-stage /simurcserver/target/simurcserver.war /config/apps/
    
RUN features.sh

RUN configure.sh