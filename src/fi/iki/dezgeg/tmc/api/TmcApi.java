package fi.iki.dezgeg.tmc.api;

import com.google.gson.*;
import com.intellij.openapi.util.Pair;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TmcApi {
    public static final String DEFAULT_SERVER_URL = "http://tmc.mooc.fi/hy/";
    public static final List<Pair<String, String>> DEFAULT_SERVERS = Arrays.asList(
            new Pair<String, String>("University of Helsinki", "http://tmc.mooc.fi/hy/"),
            new Pair<String, String>("MOOC", "http://tmc.mooc.fi/mooc/")
    );

    private static final String API_VERSION = "7";

    private String serverUrl;
    private Gson gson;
    private UsernamePasswordCredentials credentials;

    public TmcApi() {
        gson = new GsonBuilder().registerTypeAdapter(Date.class, new CustomDateDeserializer()).create();
    }

    public static void main(String[] args) {
        TmcApi tmc = new TmcApi();
        tmc.setCredentials(DEFAULT_SERVER_URL, "dezgeg", "dezgeg");
        Course course = tmc.getCourses().get("k2014-tira-paja");
        Exercise exercise = tmc.getExercises(course).get("viikko1-01.4.PieninSuurin");

        System.out.println(exercise);
        tmc.downloadExercise(exercise, "/tmp/foo/");
    }

    public void setCredentials(String server, String username, String password) {
        serverUrl = server.replaceFirst("/*$", "/");
        credentials = new UsernamePasswordCredentials(username, password);
    }

    private InputStreamReader makeTextRequest(String url) {
        return new InputStreamReader(makeBinaryRequest(url), StandardCharsets.UTF_8);
    }

    private InputStream makeBinaryRequest(String url) {
        URI uri;
        try {
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("api_version", API_VERSION);
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new TmcException("Invalid server URL: " + e.getMessage(), e);
        }

        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credProvider).build();
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), "http");
        HttpGet get = new HttpGet(uri);

        HttpResponse response;
        try {
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicScheme = new BasicScheme();
            authCache.put(host, basicScheme);

            HttpClientContext context = HttpClientContext.create();
            context.setAuthCache(authCache);

            response = httpClient.execute(get, context);
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
        try {
            return response.getEntity().getContent();
        } catch (Exception e) {
            throw new TmcException("Connection error: " + e.getMessage(), e);
        }
    }

    public Map<String, Course> getCourses() {
        InputStreamReader reader = makeTextRequest(serverUrl + "courses.json");
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        Map<String, Course> courses = new TreeMap<String, Course>();
        for (JsonElement courseJson : json.getAsJsonArray("courses")) {
            Course c = gson.fromJson(courseJson, Course.class);
            courses.put(c.getName(), c);
        }

        return courses;
    }

    public Map<String, Exercise> getExercises(Course course) {
        InputStreamReader reader = makeTextRequest(course.getDetailsUrl());
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        System.out.println(json);

        Map<String, Exercise> exercises = new TreeMap<String, Exercise>();
        for (JsonElement exerciseJson : json.getAsJsonObject("course").getAsJsonArray("exercises")) {
            Exercise e = gson.fromJson(exerciseJson, Exercise.class);
            e.setCourse(course);
            exercises.put(e.getName(), e);
        }
        return exercises;
    }

    public void downloadExercise(Exercise exercise, String courseDir) {
        try {
            InputStream zipIn = makeBinaryRequest(exercise.getDownloadUrl());
            File tempFile = File.createTempFile("tmc-exercise-" + exercise.getName(), ".zip");
            tempFile.deleteOnExit();
            BufferedOutputStream zipOut = new BufferedOutputStream(new FileOutputStream(tempFile));
            IOUtils.copy(zipIn, zipOut);
            zipOut.close();
            zipIn.close();

            ZipFile zipFile = new ZipFile(tempFile);
            Iterator<? extends ZipEntry> it = IteratorUtils.asIterator(zipFile.entries());
            while (it.hasNext()) {
                ZipEntry zipEntry = it.next();
                if (zipEntry.isDirectory())
                    continue;

                File f = new File(courseDir, zipEntry.getName());
                f.getParentFile().mkdirs();
                InputStream fileIn = zipFile.getInputStream(zipEntry);
                BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(f));
                IOUtils.copy(fileIn, fileOut);
                fileOut.close();
                fileIn.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
