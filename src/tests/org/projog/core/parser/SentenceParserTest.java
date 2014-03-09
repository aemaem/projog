/*
 * Copyright 2013 S Webber
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projog.core.parser;

import static org.projog.TestUtils.parseSentence;
import static org.projog.TestUtils.write;
import junit.framework.TestCase;

import org.projog.TestUtils;
import org.projog.core.Operands;
import org.projog.core.term.Term;

public class SentenceParserTest extends TestCase {
   public void testIncompleteSentences() {
      error(":-");
      error(":- .");
      error("a :-");
      error("a :- .");
      error(":- X is");
      error(":- X is 1"); // no '.' character at end of sentence
      error(":- X is p(a, b, c)"); // no '.' character at end of sentence
      error(":- X is [a, b, c | d]"); // no '.' character at end of sentence
   }

   public void testIncompletePredicateSyntax() {
      error(":- X is p("); // no )
      error(":- X is p(a, b"); // no )
      error(":- X is p(a b"); // no ,
   }

   public void testInvalidListSyntax() {
      error(":- X is ["); // no ]
      error(":- X is [a b"); // no , or |
      error(":- X is [a, b"); // no ]
      error(":- X is [a, b |"); // no tail
      error(":- X is [a, b | ]"); // no tail
      error(":- X is [a, b | c, d]"); // 2 args after |
   }

   public void testInvalidOperators() {
      error("a xyz b.");
      error("a $ b.");
      error("a b.");
      error("$ b.");
   }

   public void testInvalidOperatorOrder() {
      error("1 :- 2 :- 3.");
      error(":- X = 1 + 2 + 3 + 4 = 5.");
      error("a ?- b.");
      error("?- a ?- b.");
      error("?- p(X) :- x(Y), y(X,Y).");
      error("?- :- X.");
      error("?- ?- true.");
   }

   public void testEquationPrecedence() {
      checkEquation("1+2-3*4/5", "-(+(1, 2), /(*(3, 4), 5))");
      checkEquation("1+2-3/4*5", "-(+(1, 2), *(/(3, 4), 5))");
      checkEquation("1+2/3-4*5", "-(+(1, /(2, 3)), *(4, 5))");
      checkEquation("1+2/3*4-5", "-(+(1, *(/(2, 3), 4)), 5)");
      checkEquation("1/2+3*4-5", "-(+(/(1, 2), *(3, 4)), 5)");
      checkEquation("1/2*3+4-5", "-(+(*(/(1, 2), 3), 4), 5)");

      checkEquation("1+2+3+4+5+6+7+8+9+0", "+(+(+(+(+(+(+(+(+(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1*2+3+4+5+6+7+8+9+0", "+(+(+(+(+(+(+(+(*(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1+2+3+4+5*6+7+8+9+0", "+(+(+(+(+(+(+(+(1, 2), 3), 4), *(5, 6)), 7), 8), 9), 0)");
      checkEquation("1+2+3+4+5+6+7+8+9*0", "+(+(+(+(+(+(+(+(1, 2), 3), 4), 5), 6), 7), 8), *(9, 0))");
      checkEquation("1*2+3+4+5*6+7+8+9+0", "+(+(+(+(+(+(+(*(1, 2), 3), 4), *(5, 6)), 7), 8), 9), 0)");
      checkEquation("1*2+3+4+5+6+7+8+9*0", "+(+(+(+(+(+(+(*(1, 2), 3), 4), 5), 6), 7), 8), *(9, 0))");
      checkEquation("1+2+3+4+5*6+7+8+9*0", "+(+(+(+(+(+(+(1, 2), 3), 4), *(5, 6)), 7), 8), *(9, 0))");
      checkEquation("1*2+3+4+5*6+7+8+9*0", "+(+(+(+(+(+(*(1, 2), 3), 4), *(5, 6)), 7), 8), *(9, 0))");

      checkEquation("1*2*3*4*5*6*7*8*9*0", "*(*(*(*(*(*(*(*(*(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1+2*3*4*5*6*7*8*9*0", "+(1, *(*(*(*(*(*(*(*(2, 3), 4), 5), 6), 7), 8), 9), 0))");
      checkEquation("1*2*3*4*5+6*7*8*9*0", "+(*(*(*(*(1, 2), 3), 4), 5), *(*(*(*(6, 7), 8), 9), 0))");
      checkEquation("1*2*3*4*5*6*7*8*9+0", "+(*(*(*(*(*(*(*(*(1, 2), 3), 4), 5), 6), 7), 8), 9), 0)");
      checkEquation("1+2*3*4*5+6*7*8*9*0", "+(+(1, *(*(*(2, 3), 4), 5)), *(*(*(*(6, 7), 8), 9), 0))");
      checkEquation("1+2*3*4*5*6*7*8*9+0", "+(+(1, *(*(*(*(*(*(*(2, 3), 4), 5), 6), 7), 8), 9)), 0)");
      checkEquation("1*2*3*4*5+6*7*8*9+0", "+(+(*(*(*(*(1, 2), 3), 4), 5), *(*(*(6, 7), 8), 9)), 0)");
      checkEquation("1+2*3*4*5+6*7*8*9+0", "+(+(+(1, *(*(*(2, 3), 4), 5)), *(*(*(6, 7), 8), 9)), 0)");
   }

   public void testMultiTerm() {
      String[] sentences = {"p(A, B, C) :- A = 1 , B = 2 , C = 3", "p(X, Y, Z) :- X = 1 , Y = 2 , Z = 3", "p(Q, W, E) :- Q = 1 ; W = 2 ; E = 3"};
      String source = sentences[0] + ".\n" + sentences[1] + ". " + sentences[2] + ".";
      SentenceParser sp = getSentenceParser(source);
      for (String sentence : sentences) {
         Term t = sp.parseSentence();
         assertNotNull(t);
         assertEquals(sentence, write(t));
      }
   }

   public void testBrackets() {
      Term t1 = parseSentence("?- fail, fail ; true.");
      assertEquals("?- fail , fail ; true", write(t1));
      assertEquals("?-(;(,(fail, fail), true))", t1.toString());

      Term t2 = parseSentence("?- fail, (fail;true).");
      assertEquals("?- fail , (fail ; true)", write(t2));
      assertEquals("?-(,(fail, ;(fail, true)))", t2.toString());

      Term t3 = parseSentence("?- X is 4*(2+3).");
      assertEquals("?- X is 4 * (2 + 3)", write(t3));
      assertEquals("?-(is(X, *(4, +(2, 3))))", t3.toString());

      // TODO The following is successfully parsed - but I'm not sure if it should.
      // As "?-" and ":-" both have "fx" associativity *and* the same priority I don't think this is valid syntax.
      // The only reason the parser currently allows it is because, in this example, the ":-" is in brackets.
      parseSentence("?- ( p :- A = 1 , B = 2 , C = 3).");
   }

   public void testExtraTextAfterFullStop() {
      SentenceParser sp = getSentenceParser("?- consult(\'bench.pl\'). jkhkj");
      Term t = sp.parseSentence();
      assertEquals("?-(consult(bench.pl))", t.toString());
      try {
         sp.parseSentence();
         fail();
      } catch (ParserException pe) {
         // expected
      }
   }

   public void testMixtureOfPrefixInfixAndPostfixOperands() {
      Term t = parseSentence("a --> { 1 + -2 }.");
      assertEquals("-->(a, {(}(+(1, -2))))", t.toString());
      assertEquals("a --> { 1 + -2 }", TestUtils.write(t));
   }

   /**
    * Test "xf" (postfix) associativity.
    * <p>
    * A "x" means that the argument can contain operators of <i>only</i> a lower level of priority than the operator
    * represented by "f".
    */
   public void testParseOperandXF() {
      Operands o = new Operands();
      o.addOperand("~", "xf", 900);
      SentenceParser sp = SentenceParser.getInstance("a ~.", o);
      Term t = sp.parseSentence();
      assertEquals("~(a)", t.toString());
      try {
         sp = SentenceParser.getInstance("a ~ ~.", o);
         sp.parseSentence();
         fail();
      } catch (ParserException e) {
         // expected
      }
   }

   /**
    * Test "yf" (postfix) associativity.
    * <p>
    * A "y" means that the argument can contain operators of <i>the same</i> or lower level of priority than the
    * operator represented by "f".
    */
   public void testParseOperandYF() {
      Operands o = new Operands();
      o.addOperand(":", "yf", 900);
      SentenceParser sp = SentenceParser.getInstance("a : :.", o);
      Term t = sp.parseSentence();
      assertEquals(":(:(a))", t.toString());
   }

   public void testBuiltInPredicateNamesAsAtomArguments() {
      check("[=]", ".(=, [])");
      check("[=, = | =]", ".(=, .(=, =))");

      check("[:-]", ".(:-, [])");
      check("[:-, :- | :-]", ".(:-, .(:-, :-))");

      check("p(?-)", "p(?-)");
      check("p(:-)", "p(:-)");
      check("p(<)", "p(<)");

      check("p(1<1,is)", "p(<(1, 1), is)");
      check("p(;, ',', :-, ?-)", "p(;, ,, :-, ?-)");

      check("?- write(p(1, :-, 1))", "?-(write(p(1, :-, 1)))");
      check("?- write(p(1, ',', 1))", "?-(write(p(1, ,, 1)))");
      check("?- write(p(<,>,=))", "?-(write(p(<, >, =)))");

      // following fails as '\\+' prefix operand has higher precedence than '/' infix operand
      error("?- test('\\+'/1, 'abc').");
      // following works as explictly specifying '/' as the functor of a structure
      check("?- test('/'('\\+', 1), 'abc')", "?-(test(/(\\+, 1), abc))");

      error("p(a :- b).");
      parseSentence("p(':-'(a, b)).");
   }

   private void checkEquation(String input, String expected) {
      // apply same extra tests cos is easy to do
      for (int i = 0; i < 2; i++) {
         check(input, expected);
         check("X is " + input, "is(X, " + expected + ")");
         String conjunction = "X is " + input + ", Y is " + input + ", Z is " + input;
         String expectedConjunctionResult = ",(,(is(X, " + expected + "), is(Y, " + expected + ")), is(Z, " + expected + "))";
         check(conjunction, expectedConjunctionResult);
         check("?- " + conjunction, "?-(" + expectedConjunctionResult + ")");
         check("test(X, Y, Z) :- " + conjunction, ":-(test(X, Y, Z), " + expectedConjunctionResult + ")");

         for (int n = 0; n < 10; n++) {
            input = input.replace("" + n, "p(" + n + ")");
            expected = expected.replace("" + n, "p(" + n + ")");
         }
      }
   }

   private void error(String input) {
      try {
         Term term = parseSentence(input);
         fail("parsing: " + input + " produced: " + term + " whan expected an exception");
      } catch (ParserException pe) {
         // expected
      } catch (Exception e) {
         e.printStackTrace();
         fail("parsing: " + input + " produced: " + e + " whan expected a ParserException");
      }
   }

   /**
    * @param input syntax (not including trailing .) to attempt to produce term for
    * @param expected what toString method of Term should look like
    */
   private Term check(String input, String expected) {
      error(input);
      try {
         input += ".";
         Term t = parseSentence(input);
         if (!expected.equals(t.toString())) {
            throw new Exception("got: " + t + " instead of: " + expected);
         }
         return t;
      } catch (Exception e) {
         throw new RuntimeException("Exception parsing: " + input + " " + e.getClass() + " " + e.getMessage(), e);
      }
   }

   private SentenceParser getSentenceParser(String source) {
      return TestUtils.createSentenceParser(source);
   }
}