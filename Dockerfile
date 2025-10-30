# ========== STAGE 1: BUILD ==========
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml e baixa dependências primeiro (cache)
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copia o código-fonte e compila
COPY src ./src
RUN mvn -B -DskipTests package

# ========== STAGE 2: RUNTIME ==========
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia o .jar gerado (nome exato do seu arquivo)
COPY --from=build /app/target/fixi-0.0.1-SNAPSHOT.jar /app/app.jar

# Render fornece a variável PORT automaticamente
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
