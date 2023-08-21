FROM dgandalcio/java-otel-agent:17.0.7_7-1.29.0

ADD ./target/javalin-template.jar javalin-template.jar
ADD ./target/lib lib

EXPOSE 10000

CMD java $JAVA_OPTS -Dlogback.configurationFile=logbackConfig.xml -jar javalin-template.jar