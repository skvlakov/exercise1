package sk.vlakov.exercise1;

import java.math.BigDecimal;

public class CcyExHolder {
  public String mCcy;
  public BigDecimal mSum;
  public BigDecimal mDomSum;
  CcyExHolder(String aCcy, BigDecimal aSum, BigDecimal aUSDSum) {
  	mCcy = aCcy;
  	mSum = aSum;
  	mDomSum = aUSDSum;
  }
}
