package models;

public class Scenario {

    public long id;

    public String title;

    public String summary;

    public String narrative;

    public Scenario(long id, String title, String summary, String narrative) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.narrative = narrative;
    }
}
