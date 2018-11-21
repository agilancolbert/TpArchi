package util;

public class Utils {

  public static int log(int value) {
    if (value <= 0) {
      return -1;
    }
    int log = 0;
    while (value % 2 == 0) {
      log += 1;
      value /= 2;
    }
    if (value != 1) {
      return -1;
    }
    return log;
  }

  public static int mask(int width) {
    int mask = 0;
    for (int i = 0; i < width; i++) {
      mask |= (1 << i);
    }
    return mask;
  }

}
