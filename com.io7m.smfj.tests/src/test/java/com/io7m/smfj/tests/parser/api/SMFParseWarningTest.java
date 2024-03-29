/*
 * Copyright © 2017 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.smfj.tests.parser.api;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.parser.api.SMFParseWarning;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFParseWarningTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFParseWarningTest.class);
  }

  @Test
  public void testFullMessage0()
  {
    final SMFParseWarning e =
      SMFParseWarning.of(
        LexicalPosition.of(
          23,
          127,
          Optional.of(Paths.get("/x/y.txt").toUri())),
        "Failed",
        Optional.empty());

    LOG.error(e.fullMessage());
    Assertions.assertTrue(e.fullMessage().endsWith("y.txt:23:127: Failed"));
  }

  @Test
  public void testFullMessage1()
  {
    final SMFParseWarning e =
      SMFParseWarning.of(
        LexicalPosition.of(
          23,
          127,
          Optional.of(Paths.get("/x/y.txt").toUri())),
        "Failed",
        Optional.of(new IOException("Printer on fire")));

    LOG.error(e.fullMessage());
    Assertions.assertTrue(
      e.fullMessage().endsWith("y.txt:23:127: Failed (java.io.IOException: Printer on fire)"));
  }

  @Test
  public void testFullMessage2()
  {
    final SMFParseWarning e =
      SMFParseWarning.of(
        LexicalPosition.of(
          23,
          127,
          Optional.empty()),
        "Failed",
        Optional.of(new IOException("Printer on fire")));

    LOG.error(e.fullMessage());
    Assertions.assertTrue(
      e.fullMessage().endsWith("23:127: Failed (java.io.IOException: Printer on fire)"));
  }

  @Test
  public void testFullMessage3()
  {
    final SMFParseWarning e =
      SMFParseWarning.of(
        LexicalPosition.of(
          23,
          127,
          Optional.empty()),
        "Failed",
        Optional.empty());

    LOG.error(e.fullMessage());
    Assertions.assertEquals("23:127: Failed", e.fullMessage());
  }
}
