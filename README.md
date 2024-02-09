# CTAC

> [!IMPORTANT]  
> It is with a sad heart that I need to announce that since this 9th of February 2024, the SDIS64 officially decided to decomission
> these services and thus this UI will no longer be displayed in the operational center (CTAC) of the SDIS64.
>
> It was my pleasure to maintain it for 8 years.

Monorepo of CTAC services of the SDIS64.

This project was a web interface aggregating data from multiple sources; updated in real-time and displayed to call centre operators on an operational wall to help decision making and anticipation during emergency services' interventions.
It supervised firefighting assets over 7,600 km2 of French territory, area of action of the SDIS64 Firemen departement.

## Local environment setup

Every build and test operations are handled by Gradle.

To run build and test a subproject you can run:

```
./gradlew ":name_of_the_subproject:test"
```

Example for running the test suite of `backend` subproject:

```
./gradlew ":backend:test"
```

> To know more about which command you can run on a subproject, you can use the `tasks` command.
> e.g. `./gradlew ":backend:tasks"` will show you every possible command that can run on the `backend` subproject


### Configuration encryption setup

#### Encrypt a configuration value

Run the command

    ./gradlew :backend:encrypt --value=<the value to encrypt>

For example:

    ./gradlew :backend:encrypt --value=coucou

The value must be specified in the configuration file surrounded by `ENC(<value>)`, e.g. `ctac.dragon.password=ENC(RsXot+FB+ftjPR6qXmgZy3/1+HhA==)`

## Operational documentation

SDIS64 IT department can request internal documentation to @ArchangelX360 for more internal information.
