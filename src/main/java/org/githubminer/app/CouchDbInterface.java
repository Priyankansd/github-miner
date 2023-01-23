package org.githubminer.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ektorp.http.StdHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.CouchDbInstance;
import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.impl.StreamingJsonSerializer;

public class CouchDbInterface {

    private final String DB_USER = "admin";
    private final String DB_PASSWORD = "admin";
    private final String DB_TABLE = "github-miner";
    private final String DB_DOCUMENT = "words";
    private CouchDbConnector db;

    public CouchDbInterface(String host, int port) {
        HttpClient httpClient = new StdHttpClient.Builder()
                .host(host)
                .port(port)
                .username(DB_USER)
                .password(DB_PASSWORD)
                .build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        this.db = new StdCouchDbConnector(DB_TABLE, dbInstance);
        this.db.createDatabaseIfNotExists();
    }

    public void addWord(Word word) {
        createOrUpdateWord(word);
    }

    private void createOrUpdateWord(Word word) {
        WordQuery result = this.db.find(WordQuery.class, word.getId());
        ObjectMapper mapper = new ObjectMapper();
        if (result != null) {
            result.setCount(result.getCount() + 1);
            //result.setRevision(null);
            try {
                String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                System.out.println("Update: " + result.getId() + ", count:" + result.getCount() + ", jsonStr:" + jsonStr);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            this.db.update(result);
        } else {
            try {
                String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(word);
                System.out.println("Create: " + word.getId() + ", jsonStr:" + jsonStr);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            this.db.create(word);
        }
    }

    public void addWords(List<Word> words) {
        for (Word word : words) {
            //this.db.addToBulkBuffer(word);
            try{
                createOrUpdateWord(word);
            } catch (Exception e) {
                System.out.println("error : ");
                e.printStackTrace();
            }
        }
        //this.db.flushBulkBuffer();
        this.db.ensureFullCommit();
    }

}
