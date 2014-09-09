package fi.iki.dezgeg.tmc.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class Exercise implements Serializable {
    private int id;
    private String name;
    private transient Course course;

    @SerializedName("zip_url")
    private String downloadUrl;
    @SerializedName("solution_zip_url")
    private String solutionDownloadUrl;
    @SerializedName("return_url")
    private String returnUrl;

    private Date deadline;
    private boolean returnable;
    private boolean attempted;
    private boolean completed;
    private String checksum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSolutionDownloadUrl() {
        return solutionDownloadUrl;
    }

    public void setSolutionDownloadUrl(String solutionDownloadUrl) {
        this.solutionDownloadUrl = solutionDownloadUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public boolean isReturnable() {
        return returnable;
    }

    public void setReturnable(boolean returnable) {
        this.returnable = returnable;
    }

    public boolean isAttempted() {
        return attempted;
    }

    public void setAttempted(boolean attempted) {
        this.attempted = attempted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", course='" + course + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", solutionDownloadUrl='" + solutionDownloadUrl + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", deadline=" + deadline +
                ", returnable=" + returnable +
                ", attempted=" + attempted +
                ", completed=" + completed +
                ", checksum='" + checksum + '\'' +
                '}';
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
