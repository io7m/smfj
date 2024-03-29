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

package com.io7m.smfj.processing.api;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.parser.api.SMFParseError;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Useful combinators for parsing.
 */

public final class SMFFilterCommandParsing
{
  private SMFFilterCommandParsing()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct an error message that indicates that one sort of input was expected but another was
   * received.
   *
   * @param uri      The URI, if any
   * @param line     The current line number
   * @param expected The expected input
   * @param text     The received input
   *
   * @return An error message
   */

  public static SMFPartialLogged<SMFMemoryMeshFilterType>
  errorExpectedGotValidation(
    final Optional<URI> uri,
    final int line,
    final String expected,
    final List<String> text)
  {
    return SMFPartialLogged.failed(errorExpectedGot(uri, line, expected, text));
  }

  /**
   * Construct an error message that indicates that one sort of input was expected but another was
   * received.
   *
   * @param uri      The URI, if any
   * @param line     The current line number
   * @param expected The expected input
   * @param text     The received input
   *
   * @return An error message
   */

  public static SMFParseError
  errorExpectedGot(
    final Optional<URI> uri,
    final int line,
    final String expected,
    final List<String> text)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Incorrect command syntax.");
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(String.join(" ", text));
    sb.append(System.lineSeparator());

    return SMFParseError.of(
      LexicalPosition.of(line, 0, uri), sb.toString(), Optional.empty());
  }
}
