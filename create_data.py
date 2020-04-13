import argparse
import csv
import os
from oauth2client import client
from google.cloud import pubsub_v1


def parse():
    parser = argparse.ArgumentParser()
    parser.add_argument('--project_id', help='project name', dest='project_id', default='ssu-bigdata-etl')
    parser.add_argument('--path_to_data', help='path to csv file', dest='path_to_data', default='./stations.csv')
    parser.add_argument('--repeats', help='how repeats to publish the same file', dest='repeats', default=3, type=int)
    parser.add_argument('--topic_name', help='pub/sub topic name', dest='topic_name', default='stations')
    parser.add_argument('--path_to_creds', help='path to JSON with credentials', dest='creds', default='/Users/andryyyha/SSU-BigData-ETL-3b758a590d47.json')
    return parser.parse_args()
    

def create_client():
    return pubsub_v1.PublisherClient()


def publish_data(path_to_data, topic_name, repeats, client):
  for i in range(1, repeats):
    firstline = True
    with open(path_to_data) as data:
      print("Publishing data iteration {}".format(i))
      reader = csv.reader(data)
      for line in reader:
        if firstline:
          firstline = False
          continue
        line[1] = '"' + line[1] + '"'
        line[2] = '"' + line[2] + '"'
        # print(len(line))
        encoded_line = ','.join(line).encode('utf-8')
        print(encoded_line, '\n')
        future = client.publish(topic_name, encoded_line)
        print(future.result())


def main():
    args = parse()
    print(args)
    project_id = args.project_id
    path_to_data = args.path_to_data
    repeats = args.repeats
    name = args.topic_name
    topic_name = 'projects/{}/topics/{}'.format(project_id, name)
    creds = args.creds
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = creds
    client = create_client()
    publish_data(path_to_data, topic_name, repeats, client)

if __name__ == "__main__":
    main()