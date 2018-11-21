package memory;

public interface Memory {

  int read(int address);

  void write(int address, int value);

  int getOperationTime();

  Stats getStats();

}
