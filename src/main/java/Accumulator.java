/**
 * A Class that represent a accmulator for the search engine 
 * 
 * @author Leslie Marxen
 *
 */
public class Accumulator implements Comparable<Accumulator>{
	private long did;
	private double score;

	public Accumulator(long did, double score) {

		this.did = did;
		this.score = score;
	}

	public long getDid() {
		return did;
	}

	public void setDid(long did) {
		this.did = did;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "Accumulator [did=" + did + ", score=" + score + "]";
	}

	@Override
	public int compareTo(Accumulator o) {
		if(o.score == score){
			return 0;
		} else {
			return (o.score > score) ? 1 : -1;
		}
	}

}
