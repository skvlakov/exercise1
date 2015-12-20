package sk.vlakov.exercise1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class CcyCollector implements Runnable {
  private static final long sleepMillis = 10000; // * 60;
  private Thread mThread;
  private Map<String, CcyExHolder> mCcyExRepository = new HashMap<String, CcyExHolder>();
  private Map<String, BigDecimal> mExchgRates;
  private static final Logger mLogger = CcyLogger.getLogger();
  // ---------------
  // volatile boolean mBStop = false;
  // ---
  // Visibility + atomicity: I would rather rely on "synchronized"
  // than on "volatile":
  boolean mBStop = false;

  CcyCollector() {
    mThread = new Thread(this, "CcyCollector thread");
    System.out.println("CcyCollector thread - vytvoreny " + mThread);
    mThread.start();
  }

  /**
   * @param aLine
   *          line to add to the collector
   * @return true - line was added<br/>
   *         false - line was ignored
   */
  public synchronized boolean addLine(String aLine, CcyParser aParser) {
    CcyHolder ccyHld = aParser.parse(aLine);
    if (ccyHld == null)
      // invalid line was ignored
      return false;
    // ---------
    BigDecimal exRate = getExchgRate(ccyHld.mCcy);
    boolean exRateOK = exRate.compareTo(BigDecimal.ZERO) != 0;
    if (ccyHld.mSum.compareTo(BigDecimal.ZERO) != 0) {
      CcyExHolder storedExCcy = mCcyExRepository.get(ccyHld.mCcy);
      if (storedExCcy == null) {
        // New record
        storedExCcy = new CcyExHolder(ccyHld.mCcy, ccyHld.mSum, BigDecimal.ZERO);
        mCcyExRepository.put(ccyHld.mCcy, storedExCcy);
      } else {
        // Update existing record
        storedExCcy.mSum = storedExCcy.mSum.add(ccyHld.mSum);
        if (storedExCcy.mSum.compareTo(BigDecimal.ZERO) == 0) {
          // Delete record
          mCcyExRepository.remove(ccyHld.mCcy);
          storedExCcy = null;
        }
      }
      if (storedExCcy != null) {
        // Apply exchg rate
        storedExCcy.mDomSum = exRateOK ? storedExCcy.mSum.divide(exRate, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
      }
    }
    return true;
  }

  /**
   * 
   * @param ccy
   *          Currency
   * @return Exchange rate != 0 - rate exists<br/>
   *         = 0 - rate does not exist
   */
  BigDecimal getExchgRate(String ccy) {
    if (mExchgRates != null) {
      BigDecimal exchgRate = mExchgRates.get(ccy);
      if (exchgRate != null)
        return exchgRate;
    }
    return BigDecimal.ZERO;
  }

  /**
   * Report all sums and stop the thread
   */
  public synchronized void stop() {
    mBStop = true;
    mThread.interrupt();
  }

  /**
   * Report all the sums now.
   */
  public synchronized void now() {
    mThread.interrupt();
  }

  public void run() {
    boolean bStop = false;
    while (true) {
      try {
        Thread.sleep(sleepMillis);
      } catch (InterruptedException e) {
        mLogger.log(Level.FINEST, "CcyCollector thread sleep interrupted");
      }
      synchronized (this) {
        if (mBStop) {
          mLogger.log(Level.FINEST, "CcyCollector thread - stop");
          bStop = true;
          // Yet, before the end of PGM report all the sums.
        }
        System.out.println("----------------------------------");
        System.out.println("Riadky: ");
        // Sort by currency:
        List<String> sortedList = new ArrayList<String>(mCcyExRepository.keySet());
        Collections.sort(sortedList);
        // Report:
        for (String key : sortedList) {
          CcyExHolder ccyExHld = mCcyExRepository.get(key);
          String sPart1 = String.format("%s %s", ccyExHld.mCcy,
              ccyExHld.mSum.setScale(2, RoundingMode.HALF_UP).toString());
          String sPart2 = "";
          if (getExchgRate(ccyExHld.mCcy).compareTo(BigDecimal.ZERO) != 0) {
            sPart2 = String.format("(" + CcyConstants.DOMESTIC_CCY + " %s)",
                ccyExHld.mDomSum.setScale(2, RoundingMode.HALF_UP).toString());
          }
          System.out.println(sPart1 + " " + sPart2);
        }
      }
      if (bStop)
        break;
    }
    CcyLogger.getLogger().log(Level.FINEST, "CcyCollector run() - skoncil");
  }

  /**
   * @param fileSums
   *          input file path<br/>
   *          <code>Constants.INP_CONSOLE</code> - input from console
   * @see CcyParser
   **/
  public void putInputSums(String fileSums, CcyParser aCcyParser) {
    boolean isConsole = (fileSums.equals(CcyConstants.INP_CONSOLE)) ? true : false;
    System.out.println("------------------------------------------");
    if (isConsole) {
      System.out.println("Import sum z konzoly: ");
    } else {
      System.out.println("Import sum zo vstupneho suboru: ");
    }
    // ------------------------------------------
    // Read input file
    BufferedReader bufRd1;
    bufRd1 = null;
    try {
      if (isConsole) {
        bufRd1 = new BufferedReader(new InputStreamReader(System.in));
      } else {
        bufRd1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileSums))));
      }
      while (true) {
        if (!isConsole)
          if (!bufRd1.ready())
            break;
        String line1 = bufRd1.readLine();
        if (line1.equals("teraz")) {
          // Report all the sums now
          now();
        } else if (line1.equals("stop")) {
          stop();
          break;
        } else if (!addLine(line1, aCcyParser)) {
          System.err.println("Ignorovany vstup: " + line1);
        }
      }
    } catch (FileNotFoundException e) {
      System.err.println("vstup File Error: " + e.getMessage());
      if (isConsole)
        stop();
      else
        System.err.println(CcyConstants.S_DONT_WORRY);
    } catch (IOException e) {
      System.err.println("vstup File IO Error: " + e.getMessage());
      if (isConsole)
        stop();
      else
        System.err.println(CcyConstants.S_DONT_WORRY);
    } finally {
      if (isConsole) {
        if (bufRd1 != null) {
          try {
            bufRd1.close();
          } catch (IOException e) {
            System.err.println("vstup close IO Error: " + e.getMessage());
            System.err.println(CcyConstants.S_DONT_WORRY);
          }
        }
      }
    }
    System.err.flush();
    System.out.println("------------------------------------------");
    if (isConsole)
      mLogger.log(Level.FINEST, "Koniec: Import sum z konzoly");
    else
      mLogger.log(Level.FINEST, "Koniec: Import sum zo vstupneho suboru");
  }

  /**
   * @param fileRates
   *          input file path
   * @param aCcyParser
   *          Sum line parser
   **/
  public void putExchgRates(String fileRates, CcyParser aCcyParser) {
    System.out.println("------------------------------------------");
    System.out.println("Import kurzov: ");
    // Read exchange rates:
    BufferedReader bufRdexchg = null;
    try {
      bufRdexchg = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileRates))));
      while (bufRdexchg.ready()) {
        String line1 = bufRdexchg.readLine();
        CcyHolder ccyHld = aCcyParser.parse(line1);
        if (ccyHld == null || ccyHld.mSum.compareTo(BigDecimal.ZERO) < 0) {
          System.err.println("Ignorovany kurz: " + line1);
        } else {
          synchronized (this) {
            if (mExchgRates == null) {
              mExchgRates = new HashMap<String, BigDecimal>();
            }
            if (ccyHld.mCcy.equals(CcyConstants.DOMESTIC_CCY)) {
              System.err.println("Mena sa musi lisit od " + CcyConstants.DOMESTIC_CCY + ": " + line1);
            } else if (ccyHld.mSum.compareTo(BigDecimal.ZERO) == 0) {
              System.err.println("Nulovy kurz: " + line1);
            } else {
              mExchgRates.put(ccyHld.mCcy, ccyHld.mSum);
            }
          }
        }
      }
    } catch (FileNotFoundException e) {
      System.err.println("kurzy File Error: " + e.getMessage());
      System.err.println(CcyConstants.S_DONT_WORRY);
    } catch (IOException e) {
      System.err.println("kurzy File IO Error: " + e.getMessage());
      System.err.println(CcyConstants.S_DONT_WORRY);
    } finally {
      if (bufRdexchg != null) {
        try {
          bufRdexchg.close();
        } catch (IOException e) {
          System.err.println("kurzy close IO Error: " + e.getMessage());
          System.err.println(CcyConstants.S_DONT_WORRY);
        }
      }
    }
    System.err.flush();
    System.out.println("------------------------------------------");
    System.out.println("Koniec: Import kurzov");
  }
}
