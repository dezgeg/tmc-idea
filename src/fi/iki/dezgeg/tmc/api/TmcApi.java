package fi.iki.dezgeg.tmc.api;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TmcApi {
    private static final String API_VERSION = "7";
    public static String DEFAULT_SERVER_URL = "http://tmc.mooc.fi/hy/";

    private String serverUrl;
    private Gson gson;
    private Header authHeader;

    public TmcApi() {
        gson = new GsonBuilder().registerTypeAdapter(Date.class, new CustomDateDeserializer()).create();
    }

    public static void main(String[] args) {
        TmcApi tmc = new TmcApi();
        tmc.setCredentials(DEFAULT_SERVER_URL, "dezgeg", "dezgeg");
        System.out.println(tmc.getExercises(tmc.getCourses().get("k2014-tira-paja")));
    }

    public void setCredentials(String server, String username, String password) {
        serverUrl = server.replaceFirst("/*$", "/");
        authHeader = BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), "US-ASCII", false);
    }

    private InputStreamReader makeJsonRequest(String url) {
        URI uri;
        try {
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("api_version", API_VERSION);
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new TmcException("Invalid server URL: " + e.getMessage(), e);
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(uri);
        get.addHeader(authHeader);
        InputStreamReader reader;
        HttpResponse response;
        try {
            response = httpClient.execute(get);
            reader = new InputStreamReader(response.getEntity().getContent());
        } catch (Exception e) {
            throw new TmcException("Connection error: " + e.getMessage(), e);
        }

        // Give a more informative errors for 403 & 404.
        int status = response.getStatusLine().getStatusCode();
        if (status == 403) {
            throw new TmcException("Invalid username or password");
        } else if (status == 404) {
            throw new TmcException("Server replied with HTTP status 404. Is the server URL correct?");
        } else if (status != 200) {
            String content = null;
            try {
                content = IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                content = e.toString();
            }

            Throwable cause = null;
            try {
                JsonObject json = gson.fromJson(content, JsonObject.class);
                throw new TmcException(json.get("error").getAsString());
            } catch (Exception e) {
                // Very robust exception catching
                throw new TmcException("Unknown error response: " + content, cause);
            }

        }

        return reader;
    }

    public Map<String, Course> getCourses() {
        InputStreamReader reader = makeJsonRequest(serverUrl + "courses.json");
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        HashMap<String, Course> courses = new HashMap<String, Course>();
        for (JsonElement courseJson : json.getAsJsonArray("courses")) {
            Course c = gson.fromJson(courseJson, Course.class);
            courses.put(c.getName(), c);
        }

        return courses;
    }

    public Map<String, Exercise> getExercises(Course course) {
        InputStreamReader reader = makeJsonRequest(course.getDetailsUrl());
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        System.out.println(json);

        Map<String, Exercise> exercises = new HashMap<String, Exercise>();
        for (JsonElement exerciseJson : json.getAsJsonObject("course").getAsJsonArray("exercises")) {
            Exercise e = gson.fromJson(exerciseJson, Exercise.class);
            e.setCourse(course);
            exercises.put(e.getName(), e);
        }
        return exercises;
    }
}

class CustomDateDeserializer implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        SimpleDateFormat dateTimeParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return dateTimeParser.parse(je.getAsString());
        } catch (ParseException ex) {
            throw new JsonParseException(ex);
        }
    }
}
