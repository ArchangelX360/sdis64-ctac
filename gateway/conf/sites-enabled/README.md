# How `sites-enabled` works in the CTAC nGinx?

The `Dockerfile` will select the right configuration file in `sites-available` based on the `environment` build argument
(`--build-args environment=<name_of_the_env>`) passed to it.
