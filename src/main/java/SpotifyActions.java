import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetSeveralTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all the api requests to the Spotify
 * and processes the data
 */

public class SpotifyActions {


    private SpotifyApi spotifyApi;

    public SpotifyActions() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(System.getenv("spotifyClientId"))
                .setClientSecret(System.getenv("spotifyClientSecret"))
                .build();

    }

    /**
     * Get album releases from a specific artist
     *
     * @param artistId from spotify
     * @return list of album ids
     */
    public List<String> getReleases(String artistId) {
        System.out.println("Getting album releases");
        List<String> albumsList = new ArrayList<>();
        boolean counter = false;
        int offset = 0;
        // Artists can have more than 50 albums, so we may need to do multiple requests to spotify
        while (!counter) {
            spotifyApi = setToken();
            GetArtistsAlbumsRequest getArtistsAlbumsRequest = spotifyApi
                    .getArtistsAlbums(artistId)
                    .limit(50)
                    .market(CountryCode.US)
                    .offset(offset)
                    .build();
            try {
                Paging<AlbumSimplified> albums = getArtistsAlbumsRequest.execute();
                AlbumSimplified[] items = albums.getItems();

                // All albums have been found
                if (items.length == 0) {
                    counter = true;
                } else {
                    for (AlbumSimplified item : items) {
                        albumsList.add(item.getId());
                    }
                    offset += 50;
                }
            } catch (SpotifyWebApiException | ParseException | IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Got all album releases");
        return albumsList;
    }


    /**
     * Get tracks from each album
     *
     * @param artistName    name of artist
     * @param albumReleases ids of the albums
     * @return list of songs from each album
     */
    public List<String> getAlbumTracks(String artistName, List<String> albumReleases) {
        System.out.println("Loading tracks");
        String name = artistName.toLowerCase();
        Map<String, String> originalList = new HashMap<>();
        List<String> tracksList = new ArrayList<>();
        try {
            for (String albumRelease : albumReleases) {
                GetAlbumsTracksRequest getAlbumsTracksRequest = spotifyApi.getAlbumsTracks(albumRelease).build();
                Paging<TrackSimplified> trackSimplifiedPaging = getAlbumsTracksRequest.execute();
                TrackSimplified[] items = trackSimplifiedPaging.getItems();
                // Each album can have 1 or more songs
                for (TrackSimplified item : items) {
                    ArtistSimplified[] artistsSimplified = item.getArtists();
                    // Each track can have 1 or more artists collaborating on it
                    for (ArtistSimplified artist : artistsSimplified) {
                        if (artist.getName().toLowerCase().equals(name)) {
                            String song = item.getName().toLowerCase();
                            if (!song.contains("remix") || song.contains(name)) {
                                System.out.println(song);
                                originalList.put(item.getName(), item.getId());
                                break;
                            }
                        }
                    }
                }
            }
            System.out.println("All tracks found");
            // Duplicates removed
            for (Map.Entry<String, String> entry : originalList.entrySet()) {
                tracksList.add(entry.getValue());
            }
            System.out.println("Ending tracks");
            return tracksList;
        } catch (SpotifyWebApiException | IOException | ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get audio features of each track
     *
     * @param tracks list of ids
     * @return list of audio features for each track (audio features contain track id + song features)
     */
    public List<AudioFeatures> getSeveralTrackFeatures(List<String> tracks) {
        System.out.println("Loading track features");
        String[] tracksArray = tracks.toArray(new String[0]);
        List<AudioFeatures> trackList = new ArrayList<>();
        int startRange = 0;
        int endRange;
        try {
            // Only 90 songs can be included in the batch request
            while (startRange < tracksArray.length) {
                endRange = startRange + 90;
                // If the endRange is greater than total length of array, set endRange to trackLength
                if (endRange > tracksArray.length) {
                    endRange = tracksArray.length;
                }

                // Get a slice of the array
                String[] tempList = Arrays.copyOfRange(tracksArray, startRange, endRange);
                GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest = spotifyApi
                        .getAudioFeaturesForSeveralTracks(tempList)
                        .build();
                // Execute request to get audiofeatures for tracks
                AudioFeatures[] audioFeatures = getAudioFeaturesForSeveralTracksRequest.execute();
                trackList.addAll(Arrays.asList(audioFeatures));
                startRange += 90;
            }
            // Some tracks do not have audio features, so we filter them from the list
            List<AudioFeatures> audioFeaturesList = trackList.stream()
                    .filter(p -> p != null)
                    .collect(Collectors.toList());
            System.out.println("Ending track features");
            return audioFeaturesList;
        } catch (SpotifyWebApiException | IOException | ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get song information about for each track
     *
     * @param tracks list of ids for each track
     * @return list of tracks
     */
    public List<Track> getSongInfo(List<String> tracks) {
        System.out.println("Loading track information");
        spotifyApi = setToken();
        String[] tracksArray = tracks.toArray(new String[0]);
        List<Track> trackList = new ArrayList<>();
        int startRange = 0;
        int endRange;
        try {
            while (startRange < tracksArray.length) {
                endRange = startRange + 45;
                if (endRange > tracksArray.length) {
                    endRange = tracksArray.length;
                }
                String[] tempList = Arrays.copyOfRange(tracksArray, startRange, endRange);
                GetSeveralTracksRequest getSeveralTracksRequest = spotifyApi.getSeveralTracks(tempList).build();
                Track[] tList = getSeveralTracksRequest.execute();
                trackList.addAll(Arrays.asList(tList));
                startRange += 45;
            }
            System.out.println("Ending track information");
            return trackList;
        } catch (SpotifyWebApiException | IOException | ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Access token is required to make a request
     *
     * @return the spotifyApi object
     */
    public SpotifyApi setToken() {
        try {
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException ex) {
            ex.printStackTrace();
        }
        return this.spotifyApi;
    }

}
