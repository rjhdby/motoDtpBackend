FROM bellsoft/liberica-openjdk-alpine-musl:15

COPY ./build/libs/motodtp-0.0.1-SNAPSHOT.jar motodtp.jar

EXPOSE 8080

CMD ["java", \
 "-XX:MaxRAMPercentage=80", \
 "-XX:+UseContainerSupport", \
 "-XX:+UnlockExperimentalVMOptions", \
 "-XX:+EnableJVMCI",  \
 "-XX:+UseJVMCICompiler", \
 "-jar",  \
 "motodtp.jar" \
 ]
