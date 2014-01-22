package fi.iki.dezgeg.tmc.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Course {
    private int id;
    private String name;
    @SerializedName("details_url")
    private String detailsUrl;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", detailsUrl='" + detailsUrl + '\'' +
                '}';
    }

}
