variable "name_prefix" {
  type        = string
  description = "Prefijo para los nombres"
}

variable "ami_id" {
  type        = string
  default     = null
  description = "AMI a usar (por defecto: Amazon Linux 2023)"
}

variable "instance_type" {
  type        = string
  default     = "t3.micro"
  description = "Tipo de instancia"
}

variable "key_name" {
  type        = string
  default     = null
  description = "Key pair SSH (opcional)"
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups de las instancias"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnets privadas donde se lanzan las instancias"
}

variable "user_data" {
  type        = string
  default     = ""
  description = "Script de arranque (instala/arranca el backend)"
}

variable "instance_profile_name" {
  type        = string
  default     = null
  description = "IAM instance profile (acceso a secrets, logs, etc.)"
}

variable "min_size" {
  type    = number
  default = 1
}

variable "max_size" {
  type    = number
  default = 3
}

variable "desired_capacity" {
  type    = number
  default = 1
}

variable "target_group_arns" {
  type        = list(string)
  default     = []
  description = "Target groups del ALB a los que registrar el ASG"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags adicionales"
}
