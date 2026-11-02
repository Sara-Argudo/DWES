# Imagen de despliegue de la tienda: solo necesita el JRE, no el JDK,
# porque el jar ya está compilado (mvnw package -DskipTests).
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/shop-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
