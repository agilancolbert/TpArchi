package test;

import java.util.Random;

import memory.DirectMappedCache;
import memory.AssociativeCache;
import memory.MainMemory;
import memory.Memory;

public class Test {

  //
  // UTILITIES
  //
  static int[] randomArray(int size) {
    Random random = new Random(System.currentTimeMillis());
    int[] array = new int[size];
    for (int i = 0; i < array.length - 1; i++) {
      array[i] = random.nextInt(100);
    }
    return array;
  }

  static boolean isSorted(int[] array) {
    for (int i = 0; i < array.length - 1; i++) {
      if (array[i + 1] < array[i]) {
        return false;
      }
    }
    return true;
  }

  //
  // SORT ALGORITHMS
  //

  // Selection sort - array version
  static void selectionSort(int[] array) {

    for (int i = 0; i < array.length - 1; i++) {

      int k = i;
      int min = array[i];
      for (int j = i + 1; j < array.length; j++) {
        if (array[j] < min) {
          k = j;
          min = array[j];
        }
      }

      int tmp = array[i];
      array[i] = array[k];
      array[k] = tmp;

    }

  }

  // Selection sort - memory version
  static void selectionSort(Memory memory, int start, int end) {

    int length = end - start;

    for (int i = 0; i < length - 1; i++) {

      int k = i;
      int min = memory.read(start + i);
      for (int j = i + 1; j < length; j++) {
        if (memory.read(start + j) < min) {
          k = j;
          min = memory.read(start + j);
        }
      }

      int tmp = memory.read(start + i);
      memory.write(start + i, memory.read(start + k));
      memory.write(start + k, tmp);

    }
  }

  // Bubble sort - array version
  static void bubbleSort(int[] array) {

    int unsortedLength = array.length;
    do {

      int lastIndex = 0;

      for (int i = 0; i < unsortedLength - 1; i++) {
        if (array[i] >= array[i + 1]) {
          int tmp = array[i];
          array[i] = array[i + 1];
          array[i + 1] = tmp;
          lastIndex = i + 1;
        }
      }

      unsortedLength = lastIndex;

    } while (unsortedLength != 0);

  }

  // Bubble sort - memory version
  static void bubbleSort(Memory memory, int start, int end) {

    int unsortedLength = end - start;
    do {

      int lastIndex = 0;

      for (int i = 0; i < unsortedLength - 1; i++) {
        if (memory.read(i) >= memory.read(i + 1)) {
          int tmp = memory.read(i);
          memory.write(i, memory.read(i + 1));
          memory.write(i + 1, tmp);
          lastIndex = i + 1;
        }
      }

      unsortedLength = lastIndex;

    } while (unsortedLength != 0);

  }

  //
  // RANDOM ACCESSES
  //
  static void randomAccesses(Memory memory, int start, int end) {
    Random random = new Random();
    for (int i = 0; i < 1000 * 1000; i++) {
      int address = start + random.nextInt(end - start);
      memory.read(address);
    }
  }

  //
  // TEST PROGRAM
  //
  public static void main(String[] args) {

    MainMemory memory;
    //DirectMappedCache cache;
    AssociativeCache cache;

    // Ex�cution d'acc�s al�atoires en m�moire avec cache
    System.out.println("Execution d'acc�s al�atoire avec cache");

    memory = new MainMemory(1024 * 1024, 50);
    //cache = new DirectMappedCache(1024, 10, memory);
    cache = new AssociativeCache(1024,10,memory);
    randomAccesses(cache, 0, 1024 * 1024);

    System.out.println("cache  stats: " + cache.getStats());
    System.out.println("memory stats: " + memory.getStats());

    // Ex�cution du tri s�lection en m�moire sans cache
    System.out.println("\nExecution du tri s�lection sans cache");

    memory = new MainMemory(1024 * 1024, 50);

    int[] array = new int[] { 10, 9, 8, 7, 6, 5, 4, 2, 3, 1 };
    memory.write(0, array);

    selectionSort(memory, 0, array.length);

    System.out.println("array=" + memory.dump(0, array.length));
    System.out.println("memory stats: " + memory.getStats());

    // Ex�cution du tri s�lection en m�moire avec cache
    System.out.println("\nExecution du tri s�lection avec cache");

    memory = new MainMemory(1024 * 1024, 50);
    //cache = new DirectMappedCache(1024, 10, memory);
    cache = new AssociativeCache(1024, 10, memory);

    memory.write(0, array);

    selectionSort(cache, 0, array.length);
    //bubbleSort(cache, 0, array.length);

    System.out.println("array=" + memory.dump(0, array.length));
    System.out.println("cache  stats: " + cache.getStats());
    System.out.println("memory stats: " + memory.getStats());

  }

}
