variable "name_prefix" {
  type        = string
  description = "Prefijo para los nombres"
}

variable "name" {
  type        = string
  description = "Nombre del security group (p. ej. backend, db, redis, alb)"
}

variable "description" {
  type    = string
  default = "Security group gestionado por Terraform"
}

variable "vpc_id" {
  type        = string
  description = "ID de la VPC"
}

variable "ingress_rules" {
  type = list(object({
    from_port       = number
    to_port         = number
    protocol        = string
    cidr_blocks     = optional(list(string))
    security_groups = optional(list(string))
    description     = optional(string)
  }))
  default     = []
  description = "Reglas de entrada"
}

variable "egress_rules" {
  type = list(object({
    from_port   = number
    to_port     = number
    protocol    = string
    cidr_blocks = optional(list(string))
    description = optional(string)
  }))
  default = [
    {
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }
  ]
  description = "Reglas de salida (por defecto: todo)"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags adicionales"
}
