resource "aws_key_pair" "emr_key" {
  key_name   = "emr_key"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC059pG2HYz8LiqY17b5I0o48+E/6BNUsmmTJMirw7rKoKGsCn7pXphwX8mcJQpN2xE7FzSYPc1st2qvN58sBgqpKAQKF6x2Ax+gICAVSGPkc4p8tIIJeYipFCytadrArCVlBrgJ/JDrecOAxLZEArIdjUzI+okleibF0f8dnXovnlXcDKCf9tFyCq9eizongPjfCxkjfzEqbZIpEiVGvXMVJ/k/YxA4XBLvCtBcUgOd+3sPY3gdvBfp05vlGGBUNU9+JSbKmJpTix7nOty1XLcoiFZdfHRNEWPeXByyjOZYWeAiEsk4NU55dD87rf1qalpsjxHYDwD8qtEGSkSvH0F avyazkov@C6689"
}

module "s3_logs" {
  source                    = "git::https://github.com/cloudposse/terraform-aws-s3-log-storage.git?ref=tags/0.8.0"
  name                      = "emr_cluster_logs"
  region                    = var.region
  stage                     = "prod"
  enable_glacier_transition = false
  force_destroy             = true
  attributes                = ["emr_logs"]
}

resource "aws_kinesis_stream" "input_data_stream" {
  name             = "input_data_stream"
  shard_count      = 1
  retention_period = 24
  shard_level_metrics = [
    "IncomingBytes",
    "OutgoingBytes",
    "IncomingRecords",
    "OutgoingRecords"
  ]

  tags = {
    Environment = "prod"
  }
}

module "emr_cluster" {
  source                              = "git::https://github.com/cloudposse/terraform-aws-emr-cluster.git?ref=tags/0.3.0"
  stage                               = "prod"
  name                                = var.name
  region                              = var.region
  vpc_id                              = var.vpc_id
  subnet_id                           = var.subnet_id
  subnet_type                         = "public"
  ebs_root_volume_size                = var.ebs_root_volume_size
  core_instance_group_ebs_size        = var.core_instance_group_ebs_size
  core_instance_group_instance_type   = var.core_instance_group_instance_type
  master_instance_group_ebs_size      = var.master_instance_group_ebs_size
  master_instance_group_instance_type = var.master_instance_group_instance_type
  key_name                            = aws_key_pair.emr_key.key_name
}



