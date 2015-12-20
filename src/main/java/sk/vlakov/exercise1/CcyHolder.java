package sk.vlakov.exercise1;

import java.math.BigDecimal;

public class CcyHolder {
  public String mCcy;
  public BigDecimal mSum;
  CcyHolder(String aCcy, BigDecimal aSum) {
  	mCcy = aCcy;
  	mSum = aSum;
  }
}
