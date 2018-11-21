package memory;

public class Stats {

  public UnitStats reads = new UnitStats();
  public UnitStats writes = new UnitStats();

  @Override
  public String toString() {
    String result = "\n";
    result += "reads: " + reads;
    result += "\nwrites: " + writes;

    long count = reads.getCount() + writes.getCount();
    long hits = reads.getHits() + writes.getHits();
    long time = reads.getTime() + writes.getTime();
    boolean hasHits = reads.hasHits();

    result += "\ntotal: " + count;
    if (hasHits && count != 0) {
      result += " (" + ((hits * 100) / count) + "% hits)";
    }
    result += ", time = " + time;

    return result;
  }

}
