provider "google" {
  credentials = file(var.path_to_account_key)
  project = "ssu-bigdata-etl"
  region = "europe-west3"
}

# module "air_pollution" {
#   source  = "terraform-google-modules/pubsub/google"
#   version = "~>1.0"

#   topic      = var.air
#   project_id = var.project_id

#   pull_subscriptions = [
#     {
#       name                 = "air_pollution_pull"
#       ack_deadline_seconds = 30
#     }
#   ]
# }


module "stations" {
  source = "terraform-google-modules/pubsub/google"
  version = "~>1.0"
  
  topic = var.stations
  project_id = var.project_id

  pull_subscriptions = [
    {
      name = "stations_pull"
      ack_deadline_seconds = 600
    }
  ]
}
