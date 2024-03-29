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

package com.io7m.smfj.format.text;

import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import java.io.IOException;
import java.util.List;

/**
 * The type of body section parsers.
 */

public interface SMFTBodySectionParserType
{
  /**
   * @return The name of the body section
   */

  String name();

  /**
   * Parse the section, delivering events to {@code receiver}.
   *
   * @param receiver The event receiver
   * @param line     The current line
   *
   * @return The result of parsing
   *
   * @throws IOException On I/O errors
   */

  SMFTParsingStatus parse(
    SMFParserEventsBodyType receiver,
    List<String> line)
    throws IOException;
}
