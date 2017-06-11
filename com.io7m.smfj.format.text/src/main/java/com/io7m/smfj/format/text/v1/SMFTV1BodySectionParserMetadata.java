/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFBase64Lines;
import com.io7m.smfj.format.text.SMFTBodySectionParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import javaslang.collection.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorMalformedCommand;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorExpectedGot;

/**
 * A parser for the "metadata" body section.
 */

public final class SMFTV1BodySectionParserMetadata
  implements SMFTBodySectionParserType
{
  private final SMFTLineReaderType reader;
  private final Supplier<SMFHeader> header_get;

  /**
   * Construct a parser.
   *
   * @param in_header_get A function that yields a header
   * @param in_reader     A line reader
   */

  public SMFTV1BodySectionParserMetadata(
    final Supplier<SMFHeader> in_header_get,
    final SMFTLineReaderType in_reader)
  {
    this.header_get = NullCheck.notNull(in_header_get, "Header");
    this.reader = NullCheck.notNull(in_reader, "Reader");
  }

  private static SMFParserEventsDataMetaType makeMetadataReceiver(
    final SMFParserEventsBodyType receiver,
    final long vendor,
    final long schema)
  {
    final Optional<SMFParserEventsDataMetaType> r_opt =
      receiver.onMeta(vendor, schema);
    return r_opt.orElseGet(() -> new IgnoringMetaReceiver(receiver));
  }

  @Override
  public String name()
  {
    return "metadata";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsBodyType receiver,
    final List<String> line_start)
    throws IOException
  {
    if (line_start.size() != 1) {
      receiver.onError(errorMalformedCommand(
        "metadata",
        "metadata",
        line_start,
        this.reader.position()));
      return FAILURE;
    }

    final SMFHeader header = this.header_get.get();
    final long meta_count = header.metaCount();
    long meta_remaining = meta_count;

    boolean encountered_end = false;
    while (!encountered_end) {
      final Optional<List<String>> line_opt = this.reader.line();
      if (!line_opt.isPresent()) {
        receiver.onError(SMFParseError.of(
          this.reader.position(),
          "Unexpected EOF",
          Optional.empty()));
        return FAILURE;
      }

      final List<String> line = line_opt.get();
      if (line.isEmpty()) {
        continue;
      }

      final String command_name = line.get(0);
      switch (command_name) {
        case "meta": {
          switch (this.parseMeta(line, receiver)) {
            case SUCCESS:
              meta_remaining = Math.subtractExact(meta_remaining, 1L);
              continue;
            case FAILURE:
              return FAILURE;
          }
          break;
        }
        case "end": {
          encountered_end = true;
          break;
        }
        default: {
          throw new UnimplementedCodeException();
        }
      }
    }

    if (meta_remaining < 0L) {
      receiver.onError(errorExpectedGot(
        "Too many metadata elements were provided.",
        meta_count + " triangles",
        (meta_count - meta_remaining) + " metadata elements",
        this.reader.position()));
      return FAILURE;
    }

    if (meta_remaining > 0L) {
      receiver.onError(errorExpectedGot(
        "Too few metadata elements were provided.",
        meta_count + " triangles",
        (meta_count - meta_remaining) + " metadata elements",
        this.reader.position()));
      return FAILURE;
    }

    return SUCCESS;
  }

  private SMFTParsingStatus parseMeta(
    final List<String> line,
    final SMFParserEventsBodyType receiver)
    throws IOException
  {
    if (line.length() == 4) {
      try {
        final long vendor = Long.parseUnsignedLong(line.get(1), 16);
        final long schema = Long.parseUnsignedLong(line.get(2), 16);
        final long lines = Long.parseUnsignedLong(line.get(3));

        final SMFParserEventsDataMetaType meta_receiver =
          makeMetadataReceiver(receiver, vendor, schema);
        switch (this.readDataLines(meta_receiver, lines)) {
          case SUCCESS:
            return SUCCESS;
          case FAILURE:
            return FAILURE;
        }
        throw new UnreachableCodeException();
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse meta command: " + e.getMessage(),
          "meta <vendor> <schema> <integer-unsigned>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }

    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse meta command.",
      "meta <vendor> <schema> <integer-unsigned>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus readDataLines(
    final SMFParserEventsDataMetaType receiver,
    final long lines)
    throws IOException
  {
    final ArrayList<String> lines_saved = new ArrayList<>();
    for (long index = 0L; index < lines; ++index) {
      final Optional<List<String>> data_line_opt = this.reader.line();
      if (!data_line_opt.isPresent()) {
        receiver.onError(SMFParseError.of(
          this.reader.position(),
          "Unexpected EOF",
          Optional.empty()));
        return FAILURE;
      }

      final List<String> data_line = data_line_opt.get();
      if (data_line.length() == 1) {
        lines_saved.add(data_line.get(0));
      } else {
        receiver.onError(SMFTErrors.errorExpectedGot(
          "Cannot parse base64 encoded data.",
          "Base64 encoded data",
          data_line,
          this.reader.position()));
        return FAILURE;
      }
    }

    try {
      final byte[] data = SMFBase64Lines.fromBase64Lines(lines_saved);
      receiver.onMetaData(data);
      return SUCCESS;
    } catch (final Exception e) {
      receiver.onError(SMFTErrors.errorExpectedGotWithException(
        "Cannot parse base64 encoded data.",
        "Base64 encoded data",
        List.ofAll(lines_saved),
        this.reader.position(),
        e));
      return FAILURE;
    }
  }

  private static final class IgnoringMetaReceiver
    implements SMFParserEventsDataMetaType
  {
    private final SMFParserEventsBodyType receiver;

    IgnoringMetaReceiver(
      final SMFParserEventsBodyType in_receiver)
    {
      this.receiver = NullCheck.notNull(in_receiver, "Receiver");
    }

    @Override
    public void onMetaData(
      final byte[] data)
    {

    }

    @Override
    public void onError(final SMFErrorType e)
    {
      this.receiver.onError(e);
    }

    @Override
    public void onWarning(final SMFWarningType w)
    {
      this.receiver.onWarning(w);
    }
  }
}
