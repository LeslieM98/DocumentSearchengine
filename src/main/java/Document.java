import java.util.Arrays;
import java.sql.*;

/**
 * Encapsulates a parsed & normalized document ready to be indexed.
 *
 * @author Klaus Berberich (klaus.berberich@htwsaar.de)
 */
public class Document {

    // Document identifier
    private long id;

    // Document title
    private String title;

    // URL where the document can be looked up
    private String url;

    // Document content as a sequence of tokens
    private String[] content;

    public Document() {

    }

    public Document(long id, String titel, String url) {
        this.id = id;
        this.title = titel;
        this.url = url;
    }

    public Document(long id, String titel, String url, String[] content) {
        this.id = id;
        this.title = titel;
        this.url = url;
        this.content = content;
    }

    public Document(long id){
        setId(id);
        try {
            final String sql = "SELECT title, url FROM docs WHERE did = ?";
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement query = conn.prepareStatement(sql);
            query.setLong(1, id);

            ResultSet doc = query.executeQuery();
            setTitle(doc.getString("title"));
            setURL(doc.getString("url"));
            doc.close();
        } catch (Exception e) {
            System.err.println("Unexpected error while creating Document data");
            e.printStackTrace();
        }
        
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return id + " " + (title == null ? "" : title) + " @ " + (url == null ? "" : url) + " [" + (content == null ? "" : Arrays.toString(content)) + "]";
    }

}
