import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
/**
 * Setup the connection to DynamoDB
 */

public class DynamoDBConnection {
    private static AmazonDynamoDB client;

    public static AmazonDynamoDB getConnection(){
        if(client == null || client.equals(null))
            configDBClientBuilder();
        return client;
    }

    /**
     * This will create the connection the DynamoDB endpoint
     * @return AmazonDynamoDB Client
     */
    private static void configDBClientBuilder() {
        client = AmazonDynamoDBClientBuilder
                .standard()
                .withRegion("us-west-2")
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(System.getenv("accessKey"),System.getenv("secretKey"))))
                .build();
    }


}
