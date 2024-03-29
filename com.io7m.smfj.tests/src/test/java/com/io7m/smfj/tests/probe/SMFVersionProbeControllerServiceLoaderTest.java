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

package com.io7m.smfj.tests.probe;

import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.probe.api.SMFVersionProbeControllerServiceLoader;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFVersionProbeControllerServiceLoaderTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFVersionProbeControllerServiceLoaderTest.class);
  }

  private static InputStream resource(
    final String name)
  {
    return SMFVersionProbeControllerServiceLoader.class.getResourceAsStream(name);
  }

  private static void dumpValidation(
    final SMFPartialLogged<?> r)
  {
    r.errors().forEach(c -> LOG.error("{}", c));
    r.warnings().forEach(c -> LOG.warn("{}", c));

    if (r.isSucceeded()) {
      LOG.debug("{}", r.get());
    }
  }

  @Test
  public void testEmptyFile()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> new ByteArrayInputStream(new byte[0]));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testOneText()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/one.smft"));

    dumpValidation(r);
    Assertions.assertTrue(r.isSucceeded());

    final SMFVersionProbed v = r.get();
    Assertions.assertEquals(1L, v.version().major());
    Assertions.assertEquals(0L, v.version().minor());
  }

  @Test
  public void testBadText()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/bad.smft"));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testOneBinary()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/smfFull_validBasic0.smfb"));

    dumpValidation(r);
    Assertions.assertTrue(r.isSucceeded());

    final SMFVersionProbed v = r.get();
    Assertions.assertEquals(2L, v.version().major());
    Assertions.assertEquals(0L, v.version().minor());
  }

  @Test
  public void testBadBinary()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/bad.smfb"));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testGarbage()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/garbage.smfb"));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testFailingStreams()
  {
    final SMFVersionProbeControllerServiceLoader c =
      new SMFVersionProbeControllerServiceLoader();

    final var r =
      c.probe(() -> new BrokenInputStream());

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }
}
