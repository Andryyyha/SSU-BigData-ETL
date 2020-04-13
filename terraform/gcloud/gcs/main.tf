provider "google" {
  credentials = file(var.path_to_account_key)
  project = "ssu-bigdata-etl"
  region = "us-central1"
}

module "data_and_other" {
  source = "terraform-google-modules/cloud-storage/google"
  version = "~>1.4"

  location = "US"
  project_id = var.project_id
  names = ["data_and_other"]
  prefix = "prod"
}
