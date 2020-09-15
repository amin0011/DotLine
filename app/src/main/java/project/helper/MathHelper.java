package project.helper;

public class MathHelper {
  public static float computeDiff(float x1, float y1, float x2, float y2) {
    return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
  }


  public static int getRandom(int min, int max) {
    return (int) Math.floor(Math.random() * (max - min + 1)) + min;
  }
}
