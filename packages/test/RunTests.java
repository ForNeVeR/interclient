package test;

public class RunTests {

  public RunTests() {
  }

  public static void main(String[] args) {
    ReadOnly.RunReadOnlyTest();
//    Catalog1.RunGetColumnTest();
  }
  private boolean invokedStandalone = false;
}