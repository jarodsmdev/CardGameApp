# Red
module "vpc" {
  source = "../../modules/vpc"

  name_prefix          = "card-game-${var.environment}"
  cidr_block           = "10.0.0.0/16"
  azs                  = ["us-east-1a", "us-east-1b"]
  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnet_cidrs = ["10.0.11.0/24", "10.0.12.0/24"]
}

# Security groups
module "sg_alb" {
  source = "../../modules/sg"

  name_prefix = "card-game-${var.environment}"
  name        = "alb"
  vpc_id      = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port   = 80
      to_port     = 80
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "HTTP público"
    },
    {
      from_port   = 443
      to_port     = 443
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "HTTPS público"
    }
  ]
}

module "sg_backend" {
  source = "../../modules/sg"

  name_prefix = "card-game-${var.environment}"
  name        = "backend"
  vpc_id      = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port       = 8080
      to_port         = 8080
      protocol        = "tcp"
      security_groups = [module.sg_alb.sg_id]
      description     = "Solo desde el ALB"
    }
  ]
}

module "sg_db" {
  source = "../../modules/sg"

  name_prefix = "card-game-${var.environment}"
  name        = "db"
  vpc_id      = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port       = 5432
      to_port         = 5432
      protocol        = "tcp"
      security_groups = [module.sg_backend.sg_id]
      description     = "PostgreSQL solo desde el backend"
    }
  ]
}

module "sg_redis" {
  source = "../../modules/sg"

  name_prefix = "card-game-${var.environment}"
  name        = "redis"
  vpc_id      = module.vpc.vpc_id

  ingress_rules = [
    {
      from_port       = 6379
      to_port         = 6379
      protocol        = "tcp"
      security_groups = [module.sg_backend.sg_id]
      description     = "Redis solo desde el backend"
    }
  ]
}

# Balanceador
module "alb" {
  source = "../../modules/alb"

  name_prefix        = "card-game-${var.environment}"
  vpc_id             = module.vpc.vpc_id
  subnet_ids         = module.vpc.public_subnet_ids
  security_group_ids = [module.sg_alb.sg_id]
}

# Backend (Spring Boot)
module "ec2" {
  source = "../../modules/ec2"

  name_prefix        = "card-game-${var.environment}"
  security_group_ids = [module.sg_backend.sg_id]
  subnet_ids         = module.vpc.private_subnet_ids
  target_group_arns  = [module.alb.target_group_arn]
  user_data          = <<-EOT
    #!/bin/bash
    # Arrancar el backend (Docker/Java) — completar
  EOT
}

# Base de datos
module "rds" {
  source = "../../modules/rds"

  name_prefix        = "card-game-${var.environment}"
  db_name            = var.db_name
  db_username        = var.db_username
  db_password        = var.db_password
  subnet_ids         = module.vpc.private_subnet_ids
  security_group_ids = [module.sg_db.sg_id]
}

# Caché / sesiones
module "redis" {
  source = "../../modules/redis"

  name_prefix        = "card-game-${var.environment}"
  subnet_ids         = module.vpc.private_subnet_ids
  security_group_ids = [module.sg_redis.sg_id]
}
