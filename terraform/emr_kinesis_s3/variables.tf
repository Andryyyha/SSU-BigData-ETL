variable "vpc_id" {
  type        = string
  description = "ID of default VPC in AWS"
  default     = "vpc-a71a06cf"
}

variable "subnets" {
  type        = string
  description = "ID of default subnet (us-east2c) in AWS"
  default     = "subnet-f2cd68be"
}

variable "region" {
  type        = string
  description = "Region name where clustter will be created"
  default     = "us-east-2"
}

variable "ebs_root_volume_size" {
  type    = number
  default = 10
}

variable "name" {
  type        = string
  description = "Name of cluster"
  default     = "SSU_BigData_ETL"
}

variable "applications" {
  type        = list(string)
  description = "List of applications that will be installed in cluster"
  default     = ["Spark"]
}

variable "core_instance_group_ebs_size" {
  type    = number
  default = 10
}

variable "core_instance_group_instance_type" {
  type    = string
  default = "m4.large"
}

variable "master_instance_group_ebs_size" {
  type    = number
  default = 10
}

variable "master_instance_group_instance_type" {
  type    = string
  default = "m4.large"
}


