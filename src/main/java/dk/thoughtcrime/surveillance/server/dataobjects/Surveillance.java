package dk.thoughtcrime.surveillance.server.dataobjects;

import org.springframework.stereotype.Component;

import javax.persistence.*;

/**
 * Created by jimmy on 19/07/15.
 */
/*        'CREATE TABLE Surveillance(id INTEGER PRIMARY KEY, host TEXT NOT NULL, timestamp TEXT NOT NULL, imageLink TEXT NOT NULL)',
*/
@Component
@Entity(name="Surveillance")
@Table(name="Surveillance")
public class Surveillance {
    @Id
    @SequenceGenerator(name="pk_sequence",sequenceName="reading_id_seq", allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE,generator="pk_sequence")
    private long id;
    private String host;
    private String timestamp;
    private String imageLink;

    public Surveillance() {
    }

    public Surveillance(long id, String host, String timestamp, String imageLink) {
        this.id = id;
        this.host = host;
        this.timestamp = timestamp;
        this.imageLink = imageLink;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
