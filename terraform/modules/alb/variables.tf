variable "name_prefix" {
  type        = string
  description = "Prefijo para los nombres"
}

variable "vpc_id" {
  type        = string
  description = "ID de la VPC"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnets públicas del ALB"
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups del ALB"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags adicionales"
}
