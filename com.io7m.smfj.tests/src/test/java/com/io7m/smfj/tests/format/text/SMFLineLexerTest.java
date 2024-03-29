/*
 * Copyright © 2016 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.format.text.SMFTLineLexer;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public final class SMFLineLexerTest
{
  @Test
  public void testLexEmpty()
  {
    Assertions.assertEquals(
      List.of(), new SMFTLineLexer().lex(""));
  }

  @Test
  public void testLexABC()
  {
    Assertions.assertEquals(
      List.of("a", "b", "c"), new SMFTLineLexer().lex("a b c"));
  }

  @Test
  public void testLexAAA()
  {
    Assertions.assertEquals(
      List.of("aaa"), new SMFTLineLexer().lex("aaa"));
  }

  @Test
  public void testLexqAAA()
  {
    Assertions.assertEquals(
      List.of("aaa"), new SMFTLineLexer().lex("\"aaa\""));
  }

  @Test
  public void testLexAqBC()
  {
    Assertions.assertEquals(
      List.of("a", "b", "c"), new SMFTLineLexer().lex("a \"b\" c"));
  }

  @Test
  public void testLexAqB()
  {
    Assertions.assertEquals(
      List.of("a", "b"), new SMFTLineLexer().lex("a\"b\""));
  }

  @Test
  public void testLexQEscape0()
  {
    Assertions.assertEquals(
      List.of("a", "\\b"), new SMFTLineLexer().lex("a \"\\\\b\""));
  }

  @Test
  public void testLexQEscape1()
  {
    Assertions.assertEquals(
      List.of("a", "\"b"), new SMFTLineLexer().lex("a \"\\\"b\""));
  }

}
