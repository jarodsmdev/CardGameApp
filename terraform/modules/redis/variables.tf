variable "name_prefix" {
  type        = string
  description = "Prefijo para los nombres"
}

variable "engine_version" {
  type        = string
  default     = "7.1"
  description = "Versión de Redis"
}

variable "node_type" {
  type        = string
  default     = "cache.t3.micro"
  description = "Tipo de nodo"
}

variable "num_cache_clusters" {
  type        = number
  default     = 1
  description = "Nº de nodos (1 = sin replicación, 2+ = multi-AZ con failover)"
}

variable "parameter_group_name" {
  type        = string
  default     = null
  description = "Grupo de parámetros (por defecto el de la versión)"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnets privadas de Redis"
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups que permiten acceder a Redis"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags adicionales"
}
