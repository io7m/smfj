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

package com.io7m.smfj.parser.api;

import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.SortedSet;
import org.osgi.annotation.versioning.ProviderType;

/**
 * The type of parser providers.
 */

@ProviderType
public interface SMFParserProviderType
{
  /**
   * @return The format that this provider supports
   */

  SMFFormatDescription parserFormat();

  /**
   * @return The supported versions of the format
   */

  SortedSet<SMFFormatVersion> parserSupportedVersions();

  /**
   * @param events The event receiver
   * @param uri    The URI referred to by the input stream, for diagnostic
   *               messages
   * @param stream An input stream
   *
   * @return A new parser for the format
   *
   * @throws UnsupportedOperationException If sequential parsing is not
   *                                       supported
   * @see SMFFormatDescription#randomAccess()
   */

  SMFParserSequentialType parserCreateSequential(
    SMFParserEventsType events,
    URI uri,
    InputStream stream)
    throws UnsupportedOperationException;

  /**
   * @param events The event receiver
   * @param uri    The URI referred to by the input stream, for diagnostic
   *               messages
   * @param file   A file channel
   *
   * @return A new parser for the format
   *
   * @throws UnsupportedOperationException If random-access parsing is not
   *                                       supported
   * @see SMFFormatDescription#randomAccess()
   */

  SMFParserRandomAccessType parserCreateRandomAccess(
    SMFParserEventsType events,
    URI uri,
    FileChannel file)
    throws UnsupportedOperationException;
}
