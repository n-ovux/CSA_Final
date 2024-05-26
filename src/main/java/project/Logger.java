package project;

public class Logger {
  public static void log(String message) {
    System.out.println("[LOG] " + message);
  }

  public static void warning(String message) {
    System.err.println("[WARNING] " + message);
  }

  public static void error(String message) {
    throw new RuntimeException("[Error] " + message);
  }
}
