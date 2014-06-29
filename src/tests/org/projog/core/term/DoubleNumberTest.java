package org.projog.core.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.doubleNumber;

import org.junit.Test;

/**
 * @see TermTest
 */
public class DoubleNumberTest {
   private static final double DELTA = 0;

   @Test
   public void testGetName() {
      assertEquals("0.0", new DoubleNumber(0).getName());
      assertEquals(Double.toString(Double.MAX_VALUE), new DoubleNumber(Double.MAX_VALUE).getName());
      assertEquals("-7.0", new DoubleNumber(-7).getName());
   }

   @Test
   public void testToString() {
      assertEquals("0.0", new DoubleNumber(0).toString());
      assertEquals(Double.toString(Double.MAX_VALUE), new DoubleNumber(Double.MAX_VALUE).toString());
      assertEquals("-7.0", new DoubleNumber(-7).toString());
   }

   @Test
   public void testGetTerm() {
      DoubleNumber d1 = new DoubleNumber(0);
      DoubleNumber d2 = d1.getTerm();
      assertSame(d1, d2);
   }

   @Test
   public void testGetLong() {
      assertEquals(0, new DoubleNumber(0).getLong());
      assertEquals(Long.MAX_VALUE, new DoubleNumber(Long.MAX_VALUE).getLong());
      assertEquals(-7, new DoubleNumber(-7.01).getLong());
      assertEquals(-1, new DoubleNumber(-1.01).getLong());
   }

   @Test
   public void testGetDouble() {
      assertEquals(0.0, new DoubleNumber(0).getDouble(), DELTA);
      assertEquals(Double.MAX_VALUE, new DoubleNumber(Double.MAX_VALUE).getDouble(), DELTA);
      assertEquals(-7.01, new DoubleNumber(-7.01).getDouble(), DELTA);
   }

   @Test
   public void testGetType() {
      DoubleNumber d = doubleNumber();
      assertSame(TermType.DOUBLE, d.getType());
   }

   @Test
   public void testGetNumberOfArguments() {
      DoubleNumber d = doubleNumber();
      assertEquals(0, d.getNumberOfArguments());
   }

   @Test
   public void testGetArgument() {
      try {
         DoubleNumber d = doubleNumber();
         d.getArgument(0);
         fail();
      } catch (UnsupportedOperationException e) {
         // expected
      }
   }

   @Test
   public void testGetArgs() {
      DoubleNumber d = doubleNumber();
      assertSame(TermUtils.EMPTY_ARRAY, d.getArgs());
   }
}