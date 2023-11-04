import fr.sdis64.supportedEnvironments

plugins {
  id("ctac-service-plugin")
}

ctacService {
  name = project.name

  supportedEnvironments.forEach { envName ->
    val capitalizedEnvName = envName.replaceFirstChar { it.uppercase() }

    val prepareTask = tasks.register<Sync>("dockerPrepare${capitalizedEnvName}") {
      group = "package"

      destinationDir = layout.buildDirectory.dir("docker/${envName}").get().asFile
      from("Dockerfile")
      into("conf") {
        from("conf/nginx.conf")
      }
      into("conf/conf.d") {
        from("conf/conf.d")
      }
      into("conf/sites-enabled") {
        from("conf/sites-available/${envName}")
      }
    }

    environments {
      environment(envName) {
        dockerPrepareTask = prepareTask
        dockerSwarmServiceFile = file("${project.name}.$envName.service.yml")
      }
    }
  }
}
