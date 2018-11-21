package memory;

public class UnitStats {

  private long count;
  private long hits;
  private long time;

  private boolean hasHits;

  public long getCount() {
    return count;
  }

  public long getHits() {
    return hits;
  }

  public long getTime() {
    return time;
  }

  public boolean hasHits() {
    return hasHits;
  }

  @Override
  public String toString() {
    String result = count + "";
    if (hasHits && count != 0) {
      result += " (" + ((hits * 100) / count) + "% hits)";
    }
    result += ", time = " + time;
    return result;
  }

  public void add(int time) {
    count += 1;
    this.time += time;
  }

  public void add(boolean isHit, int time) {
    hasHits = true;
    count += 1;
    if (isHit) {
      hits += 1;
    }
    this.time += time;
  }

}
