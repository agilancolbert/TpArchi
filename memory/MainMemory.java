package memory;

import java.util.Arrays;

public class MainMemory implements Memory {

  //
  // ATTRIBUTES
  //
  private final int[] memory;
  private final int accessTime;

  private final Stats stats;

  //
  // CONSTRUCTORS
  //
  public MainMemory(int size, int accessTime) {
    if (size <= 0) {
      throw new IllegalArgumentException("size");
    }
    if (accessTime <= 0) {
      throw new IllegalArgumentException("accessTime");
    }

    this.memory = new int[size];
    this.accessTime = accessTime;

    this.stats = new Stats();
  }

  //
  // Memory INTERFACE
  //
  @Override
  public int read(int address) {
    stats.reads.add(accessTime);
    return memory[address];
  }

  @Override
  public void write(int address, int value) {
    stats.writes.add(accessTime);
    memory[address] = value;
  }

  @Override
  public int getOperationTime() {
    return accessTime;
  }

  @Override
  public Stats getStats() {
    return stats;
  }

  //
  // MainMemory-SPECIFIC METHODS
  //
  public void write(int address, int[] array) {
    System.arraycopy(array, 0, memory, address, array.length);
  }

  public int[] read(int address, int size) {
    return Arrays.copyOfRange(memory, address, size);
  }

  public String dump(int address, int size) {
    return Arrays.toString(read(address, size));
  }

}
