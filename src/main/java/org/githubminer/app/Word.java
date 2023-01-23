package org.githubminer.app;
import org.codehaus.jackson.annotate.*;

@JsonWriteNullProperties(false)
//@JsonIgnoreProperties({"id", "revision"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Word {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String revision;

    private long count;
    private WordType type;

    public Word(String id, String revision, long count, WordType type) {
        this.id = id;
        this.revision = revision;
        this.count = count;
        this.type = type;
    }

    public Word() {
    }

    public Word(String id, WordType type, String revision) {
        this(id, null, 1, type);
    }

    public void set_id(String id) {
        this.id = id;
    }

    public void set_rev(String rev) {
        this.revision = rev;
    }
    public void setId(String s) {
        id = s;
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

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setType(WordType type) {
        this.type = type;
    }

    public WordType getType() {
        return type;
    }
}
//
// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import com.fasterxml.jackson.annotation.JsonProperty;
// import org.codehaus.jackson.annotate.JsonWriteNullProperties;
//
// @JsonWriteNullProperties(false)
// public class Word {
//     @JsonProperty("_id")
//     public String id;
//     public WordType wordType;
//     public String repoName;
//     public long count;
//
//     public Word(String word, WordType wordType, String repoName, long count) {
//         this.id = word;
//         this.wordType = wordType;
//         this.repoName = repoName;
//         this.count = count;
//     }
//
//     public Word(String word, WordType wordType, String repoName) {
//         this(word, wordType, repoName, 1);
//     }
//
// }
