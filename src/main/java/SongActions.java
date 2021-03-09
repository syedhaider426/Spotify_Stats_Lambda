import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all the business logic for the Song model
 */
public class SongActions {

    private final DynamoDBMapper mapper;
    private final SpotifyActions spotify;
    public SongActions(){
        mapper = new DynamoDBMapper(DynamoDBConnection.getConnection());
        spotify = new SpotifyActions();
    }

    /**
     * Queries for the songs the artist has created.
     * @param artist name of artist
     * @return list of songs the Artist has created
     */
    public PaginatedQueryList<Song> getSongsForArtist(String artist){
        final Song song = new Song();
        song.setArtist(artist);
        final DynamoDBQueryExpression<Song> queryExpression = new DynamoDBQueryExpression<Song>()
                .withHashKeyValues(song);
        final PaginatedQueryList<Song> results = mapper.query(Song.class, queryExpression);
        return results;
    }

    /**
     * Determines whether or not the Song table has a track with specific artist
     * @param artist name of artist
     * @return true if artist has songs, false if not
     */
    public boolean isArtistHasSongs(String artist) {
        return getSongsForArtist(artist).size() > 0;
    }

    // Create a song based off the provided parameters
    public Song create(String artist, String track, String releaseDate, String externalUrl, AudioFeatures audioFeatures) {
        Song song = new Song();
        song.setAcousticness(audioFeatures.getAcousticness());
        song.setArtist(artist);
        song.setDanceability(audioFeatures.getDanceability());
        song.setEnergy(audioFeatures.getEnergy());
        song.setInstrumentalness(audioFeatures.getInstrumentalness());
        song.setLink(externalUrl);
        song.setLiveness(audioFeatures.getLiveness());
        song.setLoudness(audioFeatures.getLoudness());
        song.setReleaseDate(releaseDate + " -- " + artist + " -- " + audioFeatures.getId());
        song.setSong(track);
        song.setSpeechiness(audioFeatures.getSpeechiness());
        song.setTempo(audioFeatures.getTempo());
        song.setId(audioFeatures.getId());
        song.setValence(audioFeatures.getValence());
        song.setHidden(false);
        return song;
    }

    /**
     * Insert a list of songs
     * @param songs list of songs to insert
     */
    public void save(List<Song> songs) {
        mapper.batchSave(songs);
    }



    /**
     * Method used by the Lambda function to get the songs and the song attributes for a specific artist
     * @param name of artist
     * @param spotifyId of artist
     * @return success/failure message
     */
    public String populateSongs(String name, String spotifyId, LambdaLogger logger) {
        logger.log("Starting to populate songs");
        // Get albums created by artist - albums can represent a one or more songs
        List<String> albumReleases = spotify.getReleases(spotifyId);
        if (albumReleases.size() == 0) {
            deleteArtist(name);
            return "No album releases found";
        }

        // Get tracks for each album
        List<String> tracks = spotify.getAlbumTracks(name, albumReleases);
        if (tracks.size() == 0) {
            deleteArtist(name);
            return "No tracks found";
        }
        logger.log("Found album tracks for artist");

        // Get audio features of each song
        List<AudioFeatures> audioFeatureList = spotify.getSeveralTrackFeatures(tracks);
        if (audioFeatureList.size() == 0) {
            deleteArtist(name);
            return "No audio features found";
        }
        List<String> trackList = new ArrayList<>();

        /**
         * A new list of tracks is returned from getSeveralTrackFeatures because
         * some songs do not have audio features
         **/
        for (AudioFeatures track : audioFeatureList) {
            trackList.add(track.getId());
        }

        // Get song name, releaseDate, and external url (the playable Spotify link)
        List<Track> songInfoList = spotify.getSongInfo(trackList);  //batch
        if (songInfoList.size() == 0) {
            deleteArtist(name);
            return "No song information found";
        }
        int x = 0;
        List<Song> songs = new ArrayList<>();
        logger.log("Adding tracks to list");
        for (AudioFeatures audioFeature : audioFeatureList) {
            String songName = songInfoList.get(x).getName();
            String releaseDate = songInfoList.get(x).getAlbum().getReleaseDate();
            String externalUrl = songInfoList.get(x).getExternalUrls().get("spotify");
            Song newSong = create(name, songName, releaseDate, externalUrl, audioFeature);
            songs.add(newSong);
            x++;
        }
        save(songs);
        if (songs.size() == 0) {
            deleteArtist(name);
            return "No songs added";
        }
        return "Artist and Songs added";
    }

    public void deleteArtist(String name){
        Artist artist = mapper.load(Artist.class, name);
        mapper.delete(artist);
    }

}
