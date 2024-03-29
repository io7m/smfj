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

package com.io7m.smfj.format.text.v1;

import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTHeaderCommandParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorCommandExpectedGotWithException;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorMalformedCommand;

/**
 * A parser for the "endianness" command.
 */

public final class SMFTV1HeaderCommandEndianness
  implements SMFTHeaderCommandParserType
{
  /**
   * The command syntax.
   */

  public static final String SYNTAX =
    "endianness <byte-order>";

  private final SMFTLineReaderType reader;
  private final SMFHeader.Builder header;

  /**
   * Construct a parser.
   *
   * @param in_reader The current line reader
   * @param in_header A header builder
   */

  public SMFTV1HeaderCommandEndianness(
    final SMFTLineReaderType in_reader,
    final SMFHeader.Builder in_header)
  {
    this.reader = Objects.requireNonNull(in_reader, "Reader");
    this.header = Objects.requireNonNull(in_header, "Header");
  }

  @Override
  public String name()
  {
    return "endianness";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsHeaderType receiver,
    final List<String> line)
    throws IOException
  {
    if (line.size() == 2) {
      try {
        switch (line.get(1)) {
          case "big": {
            this.header.setDataByteOrder(ByteOrder.BIG_ENDIAN);
            return SUCCESS;
          }
          case "little": {
            this.header.setDataByteOrder(ByteOrder.LITTLE_ENDIAN);
            return SUCCESS;
          }
          default: {
            throw new IllegalArgumentException(
              "Byte order must be 'big' | 'little'");
          }
        }
      } catch (final IllegalArgumentException e) {
        receiver.onError(errorCommandExpectedGotWithException(
          "endianness", SYNTAX, line, this.reader.position(), e));
        return FAILURE;
      }
    }

    receiver.onError(errorMalformedCommand(
      "endianness", SYNTAX, line, this.reader.position()));
    return FAILURE;
  }
}
