import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Imports files and directories into our index.
 *
 * @author Klaus Berberich (klaus.berberich@htwsaar.de)
 */
public class Importer {

    // path to SQLite database file
    private String db;

    public Importer(String db) {
        this.db = db;
    }

    /**
     * Traverses the given root recursively, collects all files with suffix
     * .xml, and appends them to the given list.
     *
     * @param root Root to be traversed.
     * @param files List to which found files are appended.
     */
    private void traverse(File root, List<File> files) {
        if (root.isFile()) {
            if (root.getAbsolutePath().endsWith(".xml")) {
                files.add(root);
            }
        } else {
            for (File file : root.listFiles()) {
                traverse(file, files);
            }
        }
    }

    /**
     * Convenience method accepting a String instead of a File.
     *
     * @param dir Directory to be imported.
     */
    public void importDirectory(String dir) {
        importDirectory(new File(dir));
    }

    /**
     * Imports all files in the given directory into the database.
     *
     * @param dir Directory to be imported.
     */
    public void importDirectory(File dir) {
        Connection conn = null;
        PreparedStatement insertDoc = null;
        PreparedStatement insertTfs = null;

        try {
            // load JDBC driver
            Class.forName("org.sqlite.JDBC");

            // open connection
            conn = DriverManager.getConnection("jdbc:sqlite:" + db);
            conn.setAutoCommit(false);

            // prepare statements
            insertDoc = conn.prepareStatement("INSERT INTO docs (did, title, url) VALUES (?, ?, ?)");
            insertTfs = conn.prepareStatement("INSERT INTO tfs (did, term, tf) VALUES (?, ?, ?)");

            // recursively traverse given directory
            LinkedList<File> files = new LinkedList<>();
            traverse(dir, files);

            // parse documents and insert them into the database
            int batchCount = 0;
            Parser parser = new Parser();
            for (File file : files) {
                Document doc = parser.parse(file);

                // add document meta-data to batch
                insertDoc.setLong(1, doc.getId());
                insertDoc.setString(2, doc.getTitle());
                insertDoc.setString(3, doc.getURL());
                insertDoc.addBatch();

                // determine term frequencies
                HashMap<String, Integer> tfs = new HashMap<String, Integer>();
                for (String term : doc.getContent()) {
                    tfs.putIfAbsent(term, 0);
                    tfs.put(term, tfs.get(term) + 1);
                }

                // add term frequencies to batch
                for (String term : tfs.keySet()) {
                    insertTfs.setLong(1, doc.getId());
                    insertTfs.setString(2, term);
                    insertTfs.setInt(3, tfs.get(term));
                    insertTfs.addBatch();
                }

                // execute current batch
                if (++batchCount % 100 == 0) {
                    System.out.println(batchCount);
                    insertDoc.executeBatch();
                    insertTfs.executeBatch();
                }
            }

            // execute last batch
            if (batchCount % 100 != 0) {
                insertDoc.executeBatch();
                insertTfs.executeBatch();
            }

            // commit transaction
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close all statements and the connection
            if (insertDoc != null) {
                try {
                    insertDoc.close();
                } catch (SQLException sqle) {
                }
            }
            if (insertTfs != null) {
                try {
                    insertTfs.close();
                } catch (SQLException sqle) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqle) {
                }
            }
        }
    }

    public static void main(String[] args) {
        String dbPath = "nyt.db";
        String nytPath = "C:\\Users\\Leslie\\Desktop\\nyt\\data";

        Importer importer = new Importer(dbPath);
        importer.importDirectory(nytPath);
    }
}
