public class TestCommDiag1 {

  public TestCommDiag1() {
  }

  public static void main(String[] args) {
    TestCommDiag1 testCommDiag1 = new TestCommDiag1();
    testCommDiag1.invokedStandalone = true;
    try {
      interbase.interclient.utils.CommDiag.main(args);
    }
    catch (java.sql.SQLException eSQL) {
      eSQL.printStackTrace();
    }
  }
  private boolean invokedStandalone = false;
} 