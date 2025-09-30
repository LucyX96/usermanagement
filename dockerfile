FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

# comandi per avviare docker da bash

# vai al percorso del docker file:

# cd /c/Users/lucia/Desktop/IntelliJ/projects/usermanagement

# Costruire l'immagine docker con nome a piacere

# docker build -t usermanagement . <- il punto è importante per far partire la build dalla cartella corrente

# avviare il container

# docker run -d -p 8081:8081 --name usermanagement-container usermanagement

# controllare lo status e log

# docker ps
# docker logs -f usermanagement-container


