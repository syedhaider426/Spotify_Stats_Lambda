# Spotify Stats Lambda Function
This AWS Lambda function is responsible for finding the artist in Spotify's API and then finding the artist's whole discography and the audio features for each track. Audio features are metrics that Spotify stores for each song, such as danceability, acousticness, and loudness. The lambda function is triggered upon an artist being created in the Artist table.

# Installation and Deployment
1) Setup an S3 Bucket, DynamoDB database, and AWS Lambda function.
2) Fork the repository.
3) Setup the secrets in the 'Settings -> Secrets' tab in Github. These are used in the deployment to S3. Values are used in maven.yml.
<ul>
  <li>AWS_ACCESS_KEY_ID - Access key for user in AWS</li>
    <li>AWS_SECRET_ACCESS_KEY - Secret key for user in AWS</li>
  <li>AWS_PRODUCTION_BUCKET_NAME - Name of bucket in Amazon S3</li>
  <li>AWS_REGION - Region where the S3 Bucket is stored</li>
  </ul>


# Built With
<ul>
<li>Java</li>
<li>DynamoDB</li>
<li>AWS Lambda - Serverless function used to run the bot periodically</li>
<li>AWS S3 - File storage for jar file</li>
</ul>

# Authors
Syed Haider
