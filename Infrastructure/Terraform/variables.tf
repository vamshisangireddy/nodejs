variable "aws_region" {
  description = "The AWS region to create resources in."
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr_block" {
  description = "The CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr_block" {
  description = "The CIDR block for the public subnet."
  type        = string
  default     = "10.0.1.0/24"
}

variable "instance_type" {
  description = "The EC2 instance type for our servers."
  type        = string
  default     = "t2.medium"
}

variable "ami_id" {
  description = "The Amazon Machine Image (AMI) ID for the EC2 instances. This is an Ubuntu 20.04 AMI for us-east-1."
  type        = string
  default     = "ami-0c55b159cbfafe1f0"
}

variable "key_name" {
  description = "The name of your AWS EC2 key pair for SSH access."
  type        = string
  default     = "my-aws-key"
}