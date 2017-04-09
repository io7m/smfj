/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.format.text;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

abstract class SMFTAbstractParser implements
  SMFParserSequentialType
{
  protected final SMFTLineReaderType reader;
  protected final SMFParserEventsType events;
  protected final AtomicReference<ParserState> state;

  SMFTAbstractParser(
    final SMFParserEventsType in_events,
    final SMFTLineReaderType in_reader,
    final AtomicReference<ParserState> in_state)
  {
    this.events = NullCheck.notNull(in_events, "Events");
    this.reader = NullCheck.notNull(in_reader, "Reader");
    this.state = NullCheck.notNull(in_state, "state");
  }

  protected abstract Logger log();

  protected final SMFParseError makeErrorExpectedGot(
    final String message,
    final String expected,
    final String received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append(message);
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return this.makeError(sb.toString(), Optional.empty());
  }

  private SMFParseError makeError(
    final String message,
    final Optional<Exception> exception)
  {
    return SMFParseError.of(this.reader.position(), message, exception);
  }

  private SMFParseError makeErrorWithLine(
    final int line,
    final String message,
    final Optional<Exception> exception)
  {
    return SMFParseError.of(
      this.reader.position().withLine(line), message, exception);
  }

  protected final void fail(
    final String message,
    final Optional<Exception> exception)
  {
    this.onFailure(this.makeError(message, exception));
  }

  protected final void failExpectedGot(
    final String message,
    final String expected,
    final String received)
  {
    this.onFailure(this.makeErrorExpectedGot(message, expected, received));
  }

  protected final void failErrors(
    final Iterable<SMFParseError> errors)
  {
    errors.forEach(this::onFailure);
  }

  private void onFailure(
    final SMFParseError error)
  {
    this.log().trace("failure: {}", error);
    this.state.set(ParserState.STATE_FAILED);
    this.events.onError(error);
  }

  protected final void failWithLineNumber(
    final int line,
    final String message,
    final Optional<Exception> exception)
  {
    this.onFailure(this.makeErrorWithLine(line, message, exception));
  }

  @Override
  public final boolean parserHasFailed()
  {
    return this.state.get() == ParserState.STATE_FAILED;
  }

  @Override
  public void close()
    throws IOException
  {

  }

  enum ParserState
  {
    STATE_INITIAL,
    STATE_HEADER_PARSING,
    STATE_HEADER_PARSED,
    STATE_FAILED,
    STATE_FINISHED
  }
}