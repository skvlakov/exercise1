package sk.vlakov.exercise1;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class Test1 {
	CcyParser proc1 = new CcyParser(2);

	@Test
	public void test1() {
		String line1 = "USD -455";
		// ----
		line1 = "USD -10";
		CcyHolder ccyHld1 = proc1.parse(line1);
		CcyHolder ccyHld2 = new CcyHolder("USD", new BigDecimal("-10.00"));
		System.out.println("Sum1 = " + ccyHld1.mSum);
		System.out.println("Sum2 = " + ccyHld2.mSum);
		assertTrue(ccyHld1.mSum.compareTo(ccyHld2.mSum) == 0);
		// ----
		line1 = "USD 455";
		assertTrue(proc1.parse(line1).mSum.compareTo(new BigDecimal("455.00")) == 0);
		// ----
		line1 = "USD 455.23";
		assertTrue(proc1.parse(line1).mSum.compareTo(new BigDecimal("455.23")) == 0);
		// ----
		line1 = "USD -455,78";
		assertTrue(proc1.parse(line1).mSum.compareTo(new BigDecimal("-455.78")) == 0);
		// ----
		line1 = "USDS -455.78";
		assertTrue(proc1.parse(line1) == null);
		//---
		// fail("Not yet implemented");
	}
	// -----
}
