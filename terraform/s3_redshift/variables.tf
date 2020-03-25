variable "region" {
  type    = string
  default = "us-east-2"
}

variable "cluster_identifier" {
  type    = string
  default = "metrics"
}

variable "cluster_node_type" {
  type    = string
  default = "dc1.large"
}

variable "cluster_number_of_nodes" {
  type    = number
  default = 1
}

variable "cluster_database_name" {
  type    = string
  default = "metrics"
}

variable "cluster_master_username" {
  type    = string
  default = "ssu2020"
}

variable "cluster_master_password" {
  type    = string
  default = "Passw0rd"
}

variable "subnets" {
  type    = list(string)
  default = ["subnet-08363260", "subnet-3582f04f", "subnet-f2cd68be"]
}

variable "security_group_ids" {
  type    = list(string)
  default = ["sg-5d707a33"]
}

variable "applications" {
  type = list(string)
  default = ["Spark"]
}









