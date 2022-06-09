terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.17.1"
    }
  }
  backend "s3" {}
}

provider "aws" {
  region = var.aws_region
}
