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
  type        = list(object({
    name = string
  }))
  default = [
    {
      name = var.fines_db_name # Default database
    }
  ]
}

variable "fines_db_name" {
  description = "Name of the fines database"
  default     = "opal-fines-db"
}

