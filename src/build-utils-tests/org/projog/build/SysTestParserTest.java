/*
 * Copyright 2013-2014 S. Webber
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
package org.projog.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.build.BuildUtilsConstants.BUILD_DIR;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Test;

public class SysTestParserTest {
   @Test
   public void testTrue() throws IOException {
      SysTestQuery query = parseSingleItem("%TRUE X = 9");

      assertEquals("X = 9", query.getQueryStr());
      assertNull(query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertFalse(query.isContinuesUntilFails());
      assertSingleEmptyAnswer(query);
   }

   @Test
   public void testTrueNo() throws IOException {
      SysTestQuery query = parseSingleItem("%TRUE_NO X = 9");

      assertEquals("X = 9", query.getQueryStr());
      assertNull(query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertTrue(query.isContinuesUntilFails());
      assertSingleEmptyAnswer(query);
   }

   @Test
   public void testFalse() throws IOException {
      SysTestQuery query = parseSingleItem("%FALSE p(x,y,z) >= [X|Y]");

      assertEquals("p(x,y,z) >= [X|Y]", query.getQueryStr());
      assertNull(query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertTrue(query.isContinuesUntilFails());
      assertTrue(query.getAnswers().isEmpty());
   }

   @Test
   public void testAnswersNoVariables() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%ANSWER/", "%ANSWER/", "%ANSWER/", "%ANSWER/", "%ANSWER/");

      assertEquals("X = 9", query.getQueryStr());
      assertNull(query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertFalse(query.isContinuesUntilFails());
      assertEmptyAnswers(query, 5);
   }

   @Test
   public void testAnswersNoVariablesContinueUntilFails() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%ANSWER/", "%ANSWER/", "%ANSWER/", "%NO");

      assertEquals("X = 9", query.getQueryStr());
      assertNull(query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertTrue(query.isContinuesUntilFails());
      assertEmptyAnswers(query, 3);
   }

   @Test
   public void testLink() throws IOException {
      SysTestLink link = parseSingleItem("%LINK prolog-test.pl");

      assertLink("prolog-test.pl", link);
   }

   @Test
   public void testException() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%ERROR error text");

      assertEquals("X = 9", query.getQueryStr());
      assertEquals("error text", query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertFalse(query.isContinuesUntilFails());
      assertTrue(query.getAnswers().isEmpty());
   }

   @Test
   public void testErrorOnRetry() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%ANSWER/", "%ERROR exception on retry");

      assertEquals("X = 9", query.getQueryStr());
      assertEquals("exception on retry", query.getExpectedExceptionMessage());
      assertEquals("", query.getExpectedOutput());
      assertFalse(query.isContinuesUntilFails());
      assertSingleEmptyAnswer(query);
   }

   @Test
   public void testMultipleVariableAssignment() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%ANSWER", "% X=4", "% Y=[X|Y]", "%ANSWER");

      List<SysTestAnswer> answers = query.getAnswers();
      assertEquals(1, answers.size());
      SysTestAnswer answer = answers.get(0);
      assertEquals("", answer.getExpectedOutput());
      assertEquals(2, answer.getAssignments().size());
      assertAssignment(answer, "X", "4");
      assertAssignment(answer, "Y", "[X|Y]");
   }

   @Test
   public void testSingleVariableAssignment() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%ANSWER X=453", "%ANSWER X=[X|Y]");

      List<SysTestAnswer> answers = query.getAnswers();
      assertEquals(2, answers.size());
      assertSingleAssignment(answers.get(0), "X", "453");
      assertSingleAssignment(answers.get(1), "X", "[X|Y]");
   }

   @Test
   public void testSingleLineOutput() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%OUTPUT example of single line output", "%ANSWER/");

      List<SysTestAnswer> answers = query.getAnswers();
      assertEquals(1, answers.size());
      assertEquals("example of single line output", answers.get(0).getExpectedOutput());
   }

   @Test
   public void testMultiLineOutput() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%OUTPUT", "% example of", "% multi line output", "%OUTPUT", "%ANSWER/");

      List<SysTestAnswer> answers = query.getAnswers();
      assertEquals(1, answers.size());
      assertEquals("example of" + System.lineSeparator() + "multi line output", answers.get(0).getExpectedOutput());
   }

   @Test
   public void testOutputAndError() throws IOException {
      SysTestQuery query = parseSingleItem("%QUERY X = 9", "%OUTPUT output from first attempt", "%ANSWER/", "%OUTPUT output from retry", "%ERROR an error message");

      List<SysTestAnswer> answers = query.getAnswers();
      assertEquals(1, answers.size());
      assertEquals("output from first attempt", answers.get(0).getExpectedOutput());
      assertEquals("output from retry", query.getExpectedOutput());
      assertEquals("an error message", query.getExpectedExceptionMessage());
   }

   @Test
   public void testCombinations() throws IOException {
      SysTestParser parser = parse("% hello", "% world", "%TRUE true", "a line of code", "another line of code", "%LINK xyz", "final line of code", "% end");

      assertComment("hello world", parser);
      assertQueryString("true", parser);
      assertCode("a line of code", parser);
      assertCode("another line of code", parser);
      assertLink("xyz", parser);
      assertCode("final line of code", parser);
      assertComment("end", parser);
      assertNull(parser.getNext());
   }

   @Test
   public void testUnknownMarkup() throws IOException {
      SysTestParser p = parse("%FAIL");

      try {
         p.getNext();
         fail();
      } catch (RuntimeException e) {
         assertEquals("Unknown sys-test markup: %FAIL", e.getMessage());
      }
   }

   @Test
   public void testGetQueries() throws IOException {
      File scriptFile = createScriptFile("% a comment", "some code.", "%TRUE query 1", "%FALSE query 2", "% another comment", "%TRUE_NO query 3", "%QUERY query 4", "ANSWER/",
                  "%LINK xyz.pl");

      List<SysTestQuery> contents = SysTestParser.getQueries(scriptFile);
      assertEquals(4, contents.size());
      assertEquals("query 1", contents.get(0).getQueryStr());
      assertEquals("query 2", contents.get(1).getQueryStr());
      assertEquals("query 3", contents.get(2).getQueryStr());
      assertEquals("query 4", contents.get(3).getQueryStr());
   }

   @Test
   public void testNoQueries() throws IOException {
      File scriptFile = createScriptFile("% a comment", "some code.");

      try {
         SysTestParser.getQueries(scriptFile);
         fail();
      } catch (RuntimeException e) {
         assertEquals("could not find any tests or links in: " + scriptFile, e.getMessage());
      }
   }

   @SuppressWarnings("unchecked")
   private <T extends SysTestContent> T parseSingleItem(String... lines) throws IOException {
      SysTestParser parser = parse(lines);
      SysTestContent content = parser.getNext();
      assertNull(parser.getNext());
      return (T) content;
   }

   private SysTestParser parse(String... lines) throws IOException {
      File testScript = createScriptFile(lines);
      return new SysTestParser(testScript);
   }

   private File createScriptFile(String... lines) throws IOException {
      File f = File.createTempFile("SysTestParserTest", ".tmp", BUILD_DIR);
      try (PrintWriter pw = new PrintWriter(f)) {
         for (String line : lines) {
            pw.println(line);
         }
      }
      f.deleteOnExit();
      return f;
   }

   private void assertComment(String expected, SysTestParser parser) throws IOException {
      SysTestComment comment = (SysTestComment) parser.getNext();
      assertEquals(expected, comment.comment);
   }

   private void assertLink(String expected, SysTestParser parser) throws IOException {
      SysTestLink link = (SysTestLink) parser.getNext();
      assertLink(expected, link);
   }

   private void assertLink(String expected, SysTestLink link) {
      assertEquals(expected, link.target);
   }

   private void assertSingleAssignment(SysTestAnswer answer, String id, String value) {
      assertEquals(1, answer.getAssignments().size());
      assertAssignment(answer, id, value);
   }

   private void assertAssignment(SysTestAnswer answer, String id, String value) {
      assertEquals(value, answer.getAssignedValue(id));
   }

   private void assertCode(String expected, SysTestParser parser) throws IOException {
      SysTestCode code = (SysTestCode) parser.getNext();
      assertEquals(code.code, expected);
   }

   private void assertQueryString(String expectedQueryString, SysTestParser parser) throws IOException {
      SysTestQuery query = (SysTestQuery) parser.getNext();
      assertEquals(expectedQueryString, query.getQueryStr());
   }

   private void assertSingleEmptyAnswer(SysTestQuery query) {
      assertEmptyAnswers(query, 1);
   }

   private void assertEmptyAnswers(SysTestQuery query, int numAnswers) {
      List<SysTestAnswer> answers = query.getAnswers();
      assertEquals(numAnswers, answers.size());
      for (SysTestAnswer answer : answers) {
         assertEmptyAnswer(answer);
      }
   }

   private void assertEmptyAnswer(SysTestAnswer answer) {
      assertEquals("", answer.getExpectedOutput());
      assertEquals(0, answer.getAssignmentsCount());
      assertTrue(answer.getAssignments().isEmpty());
   }
}
