variable "name_prefix" {
  type        = string
  description = "Prefijo para los nombres"
}

variable "engine_version" {
  type        = string
  default     = "16.4"
  description = "Versión de PostgreSQL"
}

variable "instance_class" {
  type        = string
  default     = "db.t3.micro"
  description = "Clase de instancia RDS"
}

variable "allocated_storage" {
  type        = number
  default     = 20
  description = "Almacenamiento en GB"
}

variable "multi_az" {
  type        = bool
  default     = false
  description = "Réplica multi-AZ"
}

variable "db_name" {
  type        = string
  description = "Nombre de la base de datos"
}

variable "db_username" {
  type        = string
  description = "Usuario maestro"
}

variable "db_password" {
  type        = string
  sensitive   = true
  description = "Contraseña del usuario maestro"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnets privadas de la BD"
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups que permiten acceder a la BD"
}

variable "backup_retention_period" {
  type        = number
  default     = 7
  description = "Días de retención de backups"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags adicionales"
}
