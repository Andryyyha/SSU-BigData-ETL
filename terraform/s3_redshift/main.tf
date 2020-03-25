provider "aws" {
  region = "us-east-2"
}


module "data_and_other" {
  source                    = "git::https://github.com/cloudposse/terraform-aws-s3-log-storage.git?ref=tags/0.8.0"
  name                      = "data-and-other"
  region                    = var.region
  stage                     = "prod"
  acl                      = "log-delivery-write"
  enable_glacier_transition = false
  force_destroy             = true
}

module "redshift_metrics" {
  source  = "terraform-aws-modules/redshift/aws"
  version = "~> 2.0"

  cluster_identifier      = var.cluster_identifier
  cluster_node_type       = var.cluster_node_type
  cluster_number_of_nodes = var.cluster_number_of_nodes

  cluster_database_name   = var.cluster_database_name
  cluster_master_username = var.cluster_master_username
  cluster_master_password = var.cluster_master_password

  wlm_json_configuration = "[{\"query_concurrency\": 5}]"

  subnets                = var.subnets
  vpc_security_group_ids = var.security_group_ids
}

