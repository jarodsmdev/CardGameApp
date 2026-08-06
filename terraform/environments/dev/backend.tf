terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # State remoto en S3 (bootstrap con terraform/global/s3-backend)
  backend "s3" {
    bucket         = "card-game-terraform-state"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "card-game-terraform-lock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "card-game"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
