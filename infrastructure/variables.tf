variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}

variable "aks_subscription_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "pgsql_databases" {
  description = "List of PostgreSQL databases to create"
  type = list(object({
    name = string
  }))
  default = [
    {
      name = "opal-fines-db"
    }
  ]

}

variable "db_name" {
  description = "Name of the app database"
  default     = "opal-fines-db"
}

variable "pgsql_server_configuration" {
  description = "Postgres server configuration"
  type        = list(object({ name : string, value : string }))
  default = [
    {
      name  = "azure.extensions"
      value = "POSTGRES_FDW"
    },
    {
      name  = "azure.enable_temp_tablespaces_on_local_ssd"
      value = "off"
    }
  ]
}
