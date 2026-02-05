#!/bin/bash

# To set the secrets in your shell, source this file ie. source ./bin/secrets-stg.sh
# Ensure you already have the Azure CLI installed - `brew install azure-cli`

echo "Exporting secrets from Azure keyvault (opal-stg), please ensure you have \"az\" installed and you have logged in, using \"az login\"."

echo "AAD_CLIENT_ID=$(az keyvault secret show --vault-name opal-stg --name AzureADClientId | jq .value -r)"
echo "AAD_CLIENT_SECRET=$(az keyvault secret show --vault-name opal-stg --name AzureADClientSecret | jq .value -r)"
echo "AAD_TENANT_ID=$(az keyvault secret show --vault-name opal-stg --name AzureADTenantId | jq .value -r)"
echo "OPAL_TEST_USER_PASSWORD=$(az keyvault secret show --vault-name opal-stg --name OpalTestUserPassword | jq .value -r)"
echo "LAUNCH_DARKLY_SDK_KEY=$(az keyvault secret show --vault-name opal-stg --name launch-darkly-sdk-key | jq .value -r)"

#Some local database variables to help gradle understand flyway
echo "FLYWAY_URL=jdbc:postgresql://localhost:5432/opal-fines-db"
echo "FLYWAY_USER=opal-fines"
echo "FLYWAY_PASSWORD=opal-fines"
