# Description: This file contains the configuration for the staging environment.

local_db_name = "opal-fines-db"

# PostgreSQL Database Configuration
pgsql_databases = [
  {
    name = "opal-fines-db" # Main database
  },
  {
    name = "test-gob-fines-db" # Test database for ETL
  },
  {
    name = "test-opal-fines-db" # Test database for ETL
  }
]
