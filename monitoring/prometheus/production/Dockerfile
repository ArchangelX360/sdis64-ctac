FROM prom/prometheus:v2.41.0

ADD prometheus.yml /etc/prometheus/

CMD [\
"--config.file=/etc/prometheus/prometheus.yml",\
# "--storage.tsdb.path=/prometheus",\ # not available in `agent` mode
"--web.console.libraries=/usr/share/prometheus/console_libraries",\
"--web.console.templates=/usr/share/prometheus/consoles",\
# Above flags are defaults of the `prom/prometheus` image that we don't want to override
"--enable-feature=agent",\
"--web.external-url=https://api-ctac.sdis64.fr/prometheus/"]
