variable "region" {
  type        = string
  default     = "us-east-1"
  description = "Región de AWS"
}

variable "environment" {
  type        = string
  default     = "dev"
  description = "Nombre del entorno (dev/staging/prod)"
}

variable "db_name" {
  type        = string
  default     = "cardgame"
  description = "Nombre de la base de datos"
}

variable "db_username" {
  type        = string
  default     = "cardgame"
  description = "Usuario maestro de PostgreSQL"
}

variable "db_password" {
  type        = string
  sensitive   = true
  description = "Contraseña del usuario maestro (inyectarla por variable de entorno: TF_VAR_db_password)"
}
