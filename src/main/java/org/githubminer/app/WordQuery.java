package org.githubminer.app;
import org.codehaus.jackson.annotate.*;

@JsonWriteNullProperties(false)
//@JsonIgnoreProperties({"id", "revision"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordQuery {

    private String _id;

    private String _rev;
    private String id;
    private String revision;

    private long count;
    private WordType type;

    public WordQuery() {
    }

    public WordQuery(String _id, String _rev, String id, String revision, long count, WordType type) {
        this._id = _id;
        this._rev = _rev;
        this.id = id;
        this.revision = revision;
        this.count = count;
        this.type = type;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setType(WordType type) {
        this.type = type;
    }

    public String get_id() {
        return _id;
    }

    public String get_rev() {
        return _rev;
    }

    public String getId() {
        return id;
    }

    public String getRevision() {
        return revision;
    }

    public long getCount() {
        return count;
    }

    public WordType getType() {
        return type;
    }
}
