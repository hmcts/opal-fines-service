 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.9
FROM hmctsprod.azurecr.io/base/java:25-distroless

COPY bin/utils/launchdarkly-flags.json /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/opal-fines-service.jar /opt/app/

EXPOSE 4550
CMD [ "opal-fines-service.jar" ]
