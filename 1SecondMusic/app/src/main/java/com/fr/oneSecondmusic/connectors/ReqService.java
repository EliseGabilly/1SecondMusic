package com.fr.oneSecondmusic.connectors;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fr.oneSecondmusic.Utils;
import com.fr.oneSecondmusic.object.Album;
import com.fr.oneSecondmusic.object.Playlist;
import com.fr.oneSecondmusic.object.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReqService {
    private ArrayList<Song> songs = new ArrayList<>();
    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<Album> albums = new ArrayList<>();
    private Playlist createdPlaylist;
    private final SharedPreferences sharedPreferences;
    private final RequestQueue queue;
    private String availableDeviceId;
    private boolean isPlayerPlaying;
    private String imgURL = "";

    public ReqService(Context context) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(context);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public String getDeviceId() {
        return availableDeviceId;
    }

    public void getAvailableDevice(final IVolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/player/devices";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("devices");
                    try {
                        assert jsonArray != null;
                        JSONObject object = jsonArray.getJSONObject(0);
                        availableDeviceId = object.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public void getUserPlaylists(final IVolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/playlists";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int n = 0; n < Objects.requireNonNull(jsonArray).length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            String playlistID = object.getString("name");
                            if(playlistID.contains("1Sec_")){
                                Playlist playlist = new Playlist(object.getString("id"), playlistID,object.getString("description"));
                                playlists.add(playlist);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public Playlist getCreatedPlaylist() {
        return createdPlaylist;
    }

    public void createNewPlaylist(String userID, String playlistName, String playlistDescription, final IVolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/users/" + userID + "/playlists";

        Map<String, String> params = new HashMap<>();
        params.put("name", playlistName);
        params.put("description", playlistDescription);
        params.put("public", "false");

        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, endpoint, parameters, response -> {
                    try {
                        createdPlaylist = new Playlist(response.getString("id"), response.getString("name"), response.getString("description"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void getAlbumsFromPlaylist(String playlistId, final IVolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/playlists/" + playlistId;
        //create all albums
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONObject tracks = response.optJSONObject("tracks");
                    JSONArray jsonArray = Objects.requireNonNull(tracks).optJSONArray("items");
                    for (int n = 0; n < Objects.requireNonNull(jsonArray).length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            JSONObject trackObj = object.optJSONObject("track");
                            JSONObject albumObj = Objects.requireNonNull(trackObj).optJSONObject("album");
                            String albumName = Objects.requireNonNull(albumObj).getString("name");
                            String albumId = albumObj.getString("id");
                            List<String> albumArtists = Utils.getListOfItemFromJSONArray(albumObj.getJSONArray("artists"), "name");
                            Album album = getAlbumInListIfExist(albumId);
                            if (album == null) {
                                album = new Album(albumId, albumName, albumArtists);
                                albums.add(album);
                                checkSelectedSongInAlbum(playlistId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);

    }

    private void checkSelectedSongInAlbum(String playlistId) {
        String endpoint = "https://api.spotify.com/v1/playlists/" + playlistId;
        //check selected song in albums
        //not in the same request as the first one need to end so the albums are populated
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONObject tracks = response.optJSONObject("tracks");
                    JSONArray jsonArray = Objects.requireNonNull(tracks).optJSONArray("items");
                    for (int n = 0; n < Objects.requireNonNull(jsonArray).length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            JSONObject trackObj = object.optJSONObject("track");
                            String trackId = Objects.requireNonNull(trackObj).getString("id");
                            JSONObject albumObj = Objects.requireNonNull(trackObj).optJSONObject("album");
                            assert albumObj != null;
                            String albumId = albumObj.getString("id");
                            Album album = getAlbumInListIfExist(albumId);
                            if (album == null) {
                                System.out.println("Album " + albumId + " not found");
                            } else {
                                album.checkSong(trackId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private Album getAlbumInListIfExist(String albumId) {
        for (Album album : albums) {
            if (album.getId().equals(albumId)) {
                return album;
            }
        }
        return null;
    }

    public void getTracksFromAlbum(Album album, final IVolleyCallBack callBack) {
        songs = new ArrayList<>();
        String endpoint = "https://api.spotify.com/v1/albums/" + album.getId() + "/tracks";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int n = 0; n < Objects.requireNonNull(jsonArray).length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            String trackId = object.getString("id");
                            String trackName = object.getString("name");
                            Song song = new Song(album, trackId, trackName, false);
                            songs.add(song);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void putPlayPlaylist(String playlistId) {
        //preparePutPayload
        JSONObject offset = new JSONObject();
        try {
            offset.put("position", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject payload = new JSONObject();
        try {
            payload.put("context_uri", "spotify:playlist:" + playlistId);
            payload.put("offset", offset);
            payload.put("position_ms:", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //request
        String url = "https://api.spotify.com/v1/me/player/play?device_id=" + getDeviceId();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, payload, response -> {
        }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public Boolean getIsPlayerPlaying() {
        String endpoint = "https://api.spotify.com/v1/me/player";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    try {
                        isPlayerPlaying = response.getBoolean("is_playing");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
        return isPlayerPlaying;
    }

    public void putPausePlayback() {
        //request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, "https://api.spotify.com/v1/me/player/pause?device_id=" + getDeviceId(), null, response -> {
        }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public String getImgURL() {
        return imgURL;
    }

    public void getAlbumIgmCover(String albumId, final IVolleyCallBack callBack) {
        imgURL = "";
        String endpoint = "https://api.spotify.com/v1/albums/" + albumId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("images");
                    try {
                        assert jsonArray != null;
                        JSONObject object = jsonArray.getJSONObject(jsonArray.length() - 1);
                        imgURL = object.getString("url");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void getPlaylistIgmCover(String playlistId, final IVolleyCallBack callBack) {
        imgURL="";
        String endpoint = "https://api.spotify.com/v1/playlists/" + playlistId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    JSONArray jsonArray = response.optJSONArray("images");
                    try {
                        assert jsonArray != null;
                        JSONObject object = jsonArray.getJSONObject(jsonArray.length() - 1);
                        imgURL = object.getString("url");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void deleteUnfollowPlaylist(String playlistId) {
        String endpoint = "https://api.spotify.com/v1/playlists/" + playlistId + "/followers";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, endpoint, null, response -> {
        }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public void addTracksToPlaylist(ArrayList<Song> songsToAdd, String playlistId, final IVolleyCallBack callBack) {
        StringBuilder uris = new StringBuilder();
        for (Song s : songsToAdd) {
            uris.append("spotify:track:").append(s.getId()).append(",");
        }
        String endpoint = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?uris="+uris;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, endpoint, null, response -> callBack.onSuccess(), this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void handleError(VolleyError error) {
        if (error == null || error.networkResponse == null) {
            return;
        }
        String body;
        //get status code here
        final String statusCode = String.valueOf(error.networkResponse.statusCode);
        //get response body and parse with appropriate encoding
        body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
        System.out.println("Error " + statusCode);
        System.out.println(body);
    }

    public void getSearch(String search, final IVolleyCallBack callBack) {
        albums = new ArrayList<>();
        String endpoint = "https://api.spotify.com/v1/search?q="+search+"&type=album&limit=3";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest (Request.Method.GET, endpoint, null, response -> {
            JSONObject jsonObject = response.optJSONObject("albums");
            JSONArray jsonArray = Objects.requireNonNull(jsonObject).optJSONArray("items");
                    for (int n = 0; n < Objects.requireNonNull(jsonArray).length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            List<String> albumArtists = Utils.getListOfItemFromJSONArray(object.getJSONArray("artists"), "name");
                            Album album = new Album(object.getString("id"), object.getString("name"),albumArtists);
                            albums.add(album);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }
}