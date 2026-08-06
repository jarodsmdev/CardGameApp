variable "name_prefix" {
  type        = string
  description = "Prefijo para los nombres de los recursos"
}

variable "cidr_block" {
  type        = string
  default     = "10.0.0.0/16"
  description = "CIDR de la VPC"
}

variable "azs" {
  type        = list(string)
  description = "Zonas de disponibilidad (una por subnet)"
}

variable "public_subnet_cidrs" {
  type        = list(string)
  description = "CIDRs de las subnets públicas (uno por AZ)"
}

variable "private_subnet_cidrs" {
  type        = list(string)
  description = "CIDRs de las subnets privadas (uno por AZ)"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags adicionales para todos los recursos"
}
