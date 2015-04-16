package models;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import com.google.common.base.MoreObjects;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@IndexType(name = "scenario")
public class Scenario extends Index {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String SUMMARY = "summary";
    public static final String NARRATIVE = "narrative";
    public static final String CREATOR = "creator";

    public String title;

    public String summary;

    public String narrative;

    public User creator;

    @Override
    public Map toIndex() {
        Map<String, Object> map = newHashMap();
        map.put(ID, id);
        map.put(TITLE, title);
        map.put(SUMMARY, summary);
        map.put(NARRATIVE, narrative);
        map.put(CREATOR, creator.toIndex());
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        if (map == null) {
            return this;
        }
        this.id = (String) map.get(ID);
        this.title = (String) map.get(TITLE);
        this.summary = (String) map.get(SUMMARY);
        this.narrative = (String) map.get(NARRATIVE);
        this.creator = IndexUtils.getIndexable(map, CREATOR, User.class);
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(ID, id)
                .add(TITLE, title)
                .add(SUMMARY, summary)
                .add(NARRATIVE, narrative)
                .add(CREATOR, creator)
                .toString();
    }
}
