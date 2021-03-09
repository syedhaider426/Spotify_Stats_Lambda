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
        logger.log("----STARTING THE PROCESS------");
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
                    results = songActions.populateSongs(artist, spotifyId, logger);
                    logger.log(results);
                }
            }
        }
        return ddbEvent.getRecords().size();
    }
}


