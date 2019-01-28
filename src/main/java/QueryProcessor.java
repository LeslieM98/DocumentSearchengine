
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Set;
import java.sql.*;

/**
 * A Query Builder for the search engine
 * 
 * @author Mahan Karimi Zaree && Leslie Marxen 
 *
 */
public class QueryProcessor {
	private final ExecutorService threadManager = Executors.newCachedThreadPool();
	private final ExecutorCompletionService<Map<Long, Accumulator>> threadQueue = new ExecutorCompletionService<>(threadManager);
	private static final InvertedIndex ix = new InvertedIndex();

	private final long newestDate;
	private final long oldestDate;
	private final double dateRelevanceBoostWeight = 1;

	private final double titleRelevanceBoostWeight = 1;

	

	/**
	 * Default constructor
	 */
	public QueryProcessor() {
		final String newestDateSQL = "SELECT date FROM docs ORDER BY date DESC LIMIT 1";
		final String oldestDateSQL = "SELECT date FROM docs ORDER BY date ASC LIMIT 1;";
		try{
			Connection conn = DatabaseConnection.getConnection();

			PreparedStatement query = conn.prepareStatement(newestDateSQL);
			ResultSet rs = query.executeQuery();
			newestDate = rs.getLong("date");
			rs.close();

			query = conn.prepareStatement(oldestDateSQL);
			rs = query.executeQuery();
			oldestDate = rs.getLong("date");
			rs.close();
		} catch (Exception e){
			throw new RuntimeException(e.getMessage());
		}
	}


	/**
	 * Processes a single term on the DB according to the given formula.
	 * This Method spawns a new thread that processes the data. It instantly 
	 * returns a Future object containing the processed data.
	 * @param term A single term to process.
	 * @return A Future object containing the solution data.
	 */
	private Future<Map<Long, Accumulator>> processTerm(String term){
		return threadQueue.submit(() -> {
			List<Posting> postingList = ix.getIndexList(term);
			Map<Long, Accumulator> map = new HashMap<>();

			final double documentCollSize = ix.getSize();
			final double df = ix.getDF(term);
			for (Posting p : postingList) {
				double score = p.getTf() * Math.log(documentCollSize / df);
				Accumulator ac = map.putIfAbsent(p.getDid(), new Accumulator(p.getDid(), score));
				if(ac != null){
					ac.setScore(ac.getScore() + score);
				}
			}

			return map;
		});
	}


	/**
	 * Merges the data fron newData into oldData according to the given formula.
	 * @param oldData Old data that gets changed.
	 * @param newData New data that gets merged with old Data.
	 * @return oldData
	 */
	private Map<Long, Accumulator> mergeProcessedData(Map<Long, Accumulator> oldData, Map<Long, Accumulator> newData){
		if(oldData == null){
			return newData;
		}
		if(newData == null){
			return oldData;
		}

		Set<Long> newKeys = newData.keySet();

		for(long k : newKeys){
			Accumulator acNew = newData.get(k);
			Accumulator acOld = oldData.putIfAbsent(k, acNew);
			
			if(acOld != null){
				acOld.setScore(acOld.getScore() + acNew.getScore());
			}
		}
		return oldData;
	}
	/**
	 * gives complete result descending sorted by the score attribute back
	 * 
	 * @param query - the query which the engine has reply
	 * @return the result of the query
	 * @throws SQLException - if something goes wrong with the query 
	 */
	public List<Accumulator> process(String query) throws SQLException {
		// a list to safe the result of the query
		List<Accumulator> result = new ArrayList<>();

		String[] queryNormalizedAndToknized = normalizeAndTokennize(query);


		// Spawn new processing threads
		int threadCount = 0;
		for (String term : queryNormalizedAndToknized) {

			processTerm(term);
			threadCount++;

		}

		// Wait for the threads to finish and merge their data.
		Map<Long, Accumulator> map = new HashMap<>();
		while(threadCount > 0){
			try{
				Map<Long, Accumulator> tmp = threadQueue.take().get();
				map = mergeProcessedData(map, tmp);
			} catch (Exception e){
				System.err.println("Thread exited unexpectedly");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			threadCount--;
		}

		threadManager.shutdown();

		result = map.values().stream()
			.map(x -> boostByTitle(x, queryNormalizedAndToknized))
			.map(this::boostByDate)
			.sorted()
			.collect(Collectors.toList());
		return result;
	}

	/**
	 * gives the best k documents sorted in descending order by the score attribute
	 * back
	 * 
	 * @param query - the query which the engine has reply
	 * @param k - the best  k documents  
	 * @return the query but only the best k documents will returned 
	 * @throws SQLException - if something goes wrong with the query 
	 */
	public List<Accumulator> process(String query, int k) throws SQLException {
		List<Accumulator> result = process(query);
		if(result.size() < k){
			return result;
		}
		return result.subList(0, k);
	}

	/**
	 * Boosts the score of a Document based on how recent it was published
	 * @param in The document that will be boosted
	 * @return A reference to the boosted document
	 */
	private Accumulator boostByDate(Accumulator in) {

		final String sql = "SELECT date FROM docs WHERE did = ?";

		Connection conn;
		PreparedStatement query;
		try {	
			conn = DatabaseConnection.getConnection();
			query = conn.prepareStatement(sql);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		long time = oldestDate;
		try{
			query.setLong(1, in.getDid());
			ResultSet rs = query.executeQuery();
			time = rs.getLong("date");
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
			return in;
		}

		long tmp = time - oldestDate;
		double boost = (double) tmp / (newestDate - oldestDate);
		boost = 1 + dateRelevanceBoostWeight * boost;
		in.setScore(in.getScore() * boost);

		return in;
	}


	private Accumulator boostByTitle(Accumulator in, String[] query) {
		final String sql = "SELECT title FROM docs WHERE did = ?";

		Connection conn;
		PreparedStatement sqlQuery;
		try {	
			conn = DatabaseConnection.getConnection();
			sqlQuery = conn.prepareStatement(sql);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		String title;
		try{
			sqlQuery.setLong(1, in.getDid());
			ResultSet rs = sqlQuery.executeQuery();
			title = rs.getString("title");
			rs.close();
		} catch(Exception e){
			e.printStackTrace();
			return in;
		}

		if (title == null) {
			return in;
		}

        // remove HTML tags
        title = title.replaceAll("<[^<>]+>", " ");

        // remove all characters that are neither a letter, a number or a full stop
        title = title.replaceAll("[^a-zA-Z0-9\\.]", " ");

        // remove all full stops before a white space that are not preceded by another full stop in the same word
        char[] titleChars = title.toCharArray();
        int stopSeen = 0;
        for (int i = 0; i < titleChars.length; i++) {
            if(titleChars[i] == ' ') {
                if (i > 0 && titleChars[i-1] == '.') {
                    if (stopSeen < 2) {
                        titleChars[i-1] = ' ';
                    }
                }
                stopSeen = 0;
            } else if (titleChars[i] == '.') {
                stopSeen++;
            }
        }

        // convert to lower case
        title = (new String(titleChars)).toLowerCase();

        // split at sequences of white spaces
		List<String> titleContent = Arrays.asList(title.split("[\\s]+"));
		int occurances = 0;
		for (String x : query) {
			occurances = (titleContent.contains(x)) ? occurances + 1 : occurances;
		}

		double boost = 1 + titleRelevanceBoostWeight * (occurances / query.length);
		in.setScore(in.getScore() * boost);

		return in;
	}

	/**
	 * A function that normalized and tokenize a string and gives the content in a
	 * string array back (The logic of the function would be taken from the first
	 * programming assignment taken over by Prof. Dr.-Ing. Klaus Berberich)
	 * 
	 * @param query - is the query for the IR Engine
	 * @return content - a normalized and tokenize string
	 */

	private String[] normalizeAndTokennize(String query) {
		query = query.replaceAll("<[^<>]+>", " ");

		// remove all characters that are neither a letter, a number or a full stop
		query = query.replaceAll("[^a-zA-Z0-9\\.]", " ");

		// remove all full stops before a white space that are not preceded by another
		// full stop in the same word
		char[] queryChars = query.toCharArray();
		int stopSeen = 0;
		for (int i = 0; i < queryChars.length; i++) {
			if (queryChars[i] == ' ') {
				if (i > 0 && queryChars[i - 1] == '.') {
					if (stopSeen < 2) {
						queryChars[i - 1] = ' ';
					}
				}
				stopSeen = 0;
			} else if (queryChars[i] == '.') {
				stopSeen++;
			}
		}
		query = (new String(queryChars)).toLowerCase();
		// split at sequences of white spaces
		String[] content = query.split("[\\s]+");

		return content;

	}


	public static void main(String[] args) {
		try{
			final String query = "tokyo train disaster";
			System.out.println("Query: " + query);

			final long begin = System.nanoTime();
			final List<Accumulator> tmp = new QueryProcessor().process(query, 5);
			final long end = System.nanoTime();

			System.out.println("Executed in: " + (end - begin) / 1000000000.0 + "s");
			tmp.forEach(System.out::println);
		} catch (SQLException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
