package models;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@IndexType(name = "role")
public class Role extends Index {

    public static final String NAME = "name";

    public String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    @Override
    public Map toIndex() {
        HashMap<String, Object> map = newHashMap();
        map.put(NAME, name);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        if (map == null) {
            return null;
        }
        this.name = (String) map.get(NAME);
        return this;
    }
}
