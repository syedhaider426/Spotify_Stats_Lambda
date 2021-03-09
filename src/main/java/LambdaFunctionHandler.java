import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import org.json.JSONObject;

import java.util.Map;

public class LambdaFunctionHandler implements RequestHandler<DynamodbEvent, Integer> {
    public Integer handleRequest(DynamodbEvent ddbEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Starting the process......");
        logger.log("Received event: " + ddbEvent);
        JSONObject jo;
        SongActions songActions = new SongActions();
        String spotifyId, artist, results = "";
        Map<String, AttributeValue> map;
        for (DynamodbStreamRecord record : ddbEvent.getRecords()) {
            if(record.getEventName().equals("INSERT")) {
                map = record.getDynamodb().getNewImage();
                String strMap = ItemUtils.toItem(map).toJSON();
                jo = new JSONObject(strMap);
                artist = jo.getString("artist");
                spotifyId = jo.getString("spotifyId");
                // If the artist does not have any songs in the Song table, execute
                if(!songActions.isArtistHasSongs(artist)) {
                    logger.log("No songs found. Let's find them...");
                    results = songActions.populateSongs(artist, spotifyId, logger);
                    logger.log(results);
                }
            }
        }
        return ddbEvent.getRecords().size();
    }

//    /**
//     * Lambda function is triggered upon DynamoDB inserting/updating/deleting a record into the Artist table.
//     * The function will get the artist and populate the Songs table with the songs the artist
//     * has created. These songs are returned from making a request to Spotify's API.
//     * @param event DML (insert,update,delete)
//     * @param context (logger)
//     * @return number of records processed
//     */
//    @Override
//    public Integer handleRequest(DynamodbEvent event, Context context) {
//        for (DynamodbStreamRecord record : event.getRecords()) {
//            logger.log(record.getEventID());
//            logger.log(record.getEventName());
//            logger.log("Checking if record is an INSERT");
//            if(record.getEventName().equals("INSERT")) {
//                // SuccessRecord will be a JSON that contains the new artist and their spotifyId
//                String successRecord = record.getDynamodb().toString();
//                logger.log(record.toString());
//                logger.log(record.getDynamodb().toString());
//                logger.log(successRecord);
//                jo = new JSONObject(successRecord);
//                artist = jo.getString("artist");
//                spotifyId = jo.getString("spotifyId");
//                // If the artist does not have any songs in the Song table, execute
//                if(!songActions.isArtistHasSongs(artist)) {
//                    logger.log("No songs found. Let's find them...");
//                    results = db.populateSongs(artist, spotifyId, logger);
//                    logger.log(results);
//                }
//            }
//        }
//        return event.getRecords().size();
//    }
//
//
}


