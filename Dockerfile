 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.18
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/opal-account-enquiry.jar /opt/app/

EXPOSE 4550
CMD [ "opal-account-enquiry.jar" ]
