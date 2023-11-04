import fr.sdis64.supportedEnvironments

plugins {
  id("ctac-service-plugin")
}

ctacService {
  name = "prometheus"

  supportedEnvironments.forEach { envName ->
    val capitalizedEnvName = envName.replaceFirstChar { it.uppercase() }

    val prepareTask = tasks.register<Sync>("dockerPrepare$capitalizedEnvName") {
      group = "package"

      into(layout.buildDirectory.dir("docker/${envName}"))
      from(
        "prometheus/${envName}/Dockerfile",
        "prometheus/${envName}/prometheus.yml"
      )
    }

    environments {
      environment(envName) {
        dockerPrepareTask = prepareTask
        dockerSwarmServiceFile = file("prometheus/prometheus.$envName.service.yml")
      }
    }
  }
}
