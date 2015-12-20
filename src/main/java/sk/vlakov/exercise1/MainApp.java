package sk.vlakov.exercise1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class MainApp {

  // ---------------------
  /**
   * @param args
   *          Vstupne nepovinne parametre:<br/>
   *          -s cesta k suboru so sumami<br/>
   *          -k cesta k suboru s kurzami<br/>
   *          -v verbose / Logovat priebeh programu<br/>
   *          -----<br/>
   *          Na vstupe zadat "stop" pre ukoncenie programu.<br/>
   *          -----
   **/
  public static void main(String args[]) {
    // ---
    boolean bVerbose = false;
    Logger logger1 = CcyLogger.getLogger();
    // ----
    String fileSums = null;
    String fileExchgRates = null;
    for (int iOrd = 0; iOrd < args.length; iOrd++) {
      String prm1;
      prm1 = (String) args[iOrd];
      if (prm1.charAt(0) != '-' || prm1.length() != 2) {
        printHelp();
        return;
      }
      if (prm1.charAt(1) == 's') {
        // fileSums
        iOrd++;
        if (iOrd >= args.length) {
          printHelp();
          return;
        }
        fileSums = args[iOrd];
      } else if (prm1.charAt(1) == 'k') {
        // fileExchgRates
        iOrd++;
        if (iOrd >= args.length) {
          printHelp();
          return;
        }
        fileExchgRates = args[iOrd];
      } else if (prm1.charAt(1) == 'v') {
        bVerbose = true;
      } else {
        printHelp();
        return;
      }
    }
    if (bVerbose) {
      CcyLogger.setLogger(Level.FINEST);
    }
    // ----------
    CcyParser sumParser = new CcyParser(2);
    CcyParser exchgParser = new CcyParser(8);
    CcyCollector collectTask = new CcyCollector();
    // --------------------
    if (fileExchgRates != null) {
      // Exchange rates from file
      collectTask.putExchgRates(fileExchgRates, exchgParser);
    }
    // ----------
    if (fileSums != null) {
      // Input from file
      collectTask.putInputSums(fileSums, sumParser);
    }
    // Input from console
    collectTask.putInputSums(CcyConstants.INP_CONSOLE, sumParser);
    logger1.log(Level.FINEST, "main thread - skoncil");
  }

  private static void printHelp() {
    System.err.println("Spustat takto:\n" + "java -jar pgm.jar -v -s file-sumy -k file-kurzy\n" + "Parametre:\n"
        + "   -s nepovinny\n" + "   -k nepovinny\n" + "   -v verbose - nepovinny\n");

  }
}
