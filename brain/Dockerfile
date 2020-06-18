FROM amazoncorretto:17.0.6-alpine3.17
LABEL maintainer="Titouan Bion <titouan.bion@gmail.com>"

ENV DEPLOY_ENV=default

# this will not work if you are using the Dockerfile directly with `docker build .`
# Instead you should rely on `./gradlew ":brain:docker" task and similar
# If you *really* need to run that manually, you should change `brain.jar` to `build/libs/brain.jar` and it will work
COPY brain.jar app.jar

EXPOSE 8081

ENTRYPOINT [ "java", \
  "-Duser.timezone=Europe/Paris", \
  "-jar", "/app.jar", \
  "--spring.profiles.active=${DEPLOY_ENV}" \
]
