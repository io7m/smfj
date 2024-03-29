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

package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSReaderType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartial;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.format.support.SMFTriangleTracker;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * An 'end' section.
 */

public final class SMFB2ParsingSectionTriangles
  implements SMFB2StructureParserType<SMFPartial<SMFVoid>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionTriangles.class);

  private final SMFB2Section sectionHeader;
  private final SMFHeader smfHeader;
  private final SMFTriangleTracker triangleTracker;
  private final SMFParserEventsDataTrianglesType triangles;

  /**
   * Construct a parser.
   *
   * @param inSMFHeader       The SMF header
   * @param inTriangles       The triangle receiver
   * @param inTriangleTracker The triangle tracker
   * @param inSectionHeader   The section header for this section
   */

  public SMFB2ParsingSectionTriangles(
    final SMFB2Section inSectionHeader,
    final SMFHeader inSMFHeader,
    final SMFTriangleTracker inTriangleTracker,
    final SMFParserEventsDataTrianglesType inTriangles)
  {
    this.sectionHeader =
      Objects.requireNonNull(inSectionHeader, "sectionHeader");
    this.smfHeader =
      Objects.requireNonNull(inSMFHeader, "inSMFHeader");
    this.triangleTracker =
      Objects.requireNonNull(inTriangleTracker, "inTriangleTracker");
    this.triangles =
      Objects.requireNonNull(inTriangles, "inTriangles");
  }

  /**
   * @return The magic number identifying the section.
   */

  public static long magic()
  {
    return 0x534D_465F_5452_4953L;
  }

  @Override
  public SMFPartial<SMFVoid> parse(
    final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader(
      "triangles",
      this.sectionHeader.sizeOfData(),
      reader -> this.parseWithReader(context, reader));
  }

  private SMFPartial<SMFVoid> parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    if (!SMFB2ParsingSectionHeader.checkHeader(
      context,
      reader,
      this.sectionHeader,
      magic(),
      "triangles")) {
      return SMFPartial.failed();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "section '{}' @ 0x{}",
        "triangles",
        Long.toUnsignedString(reader.offsetCurrentAbsolute(), 16));
    }

    final var headerTriangles = this.smfHeader.triangles();
    final var byteOrder = this.smfHeader.dataByteOrder();
    if (Objects.equals(byteOrder, BIG_ENDIAN)) {
      this.parseTrianglesBE(reader, headerTriangles);
    } else if (Objects.equals(byteOrder, LITTLE_ENDIAN)) {
      this.parseTrianglesLE(reader, headerTriangles);
    }

    this.triangles.onDataTrianglesFinish();
    return SMFPartial.succeeded(SMFVoid.void_());
  }

  private void parseTrianglesBE(
    final BSSReaderType reader,
    final SMFTriangles headerTriangles)
    throws IOException
  {
    final var triangleCount = headerTriangles.triangleCount();
    switch (headerTriangles.triangleIndexSizeBits()) {
      case 8: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU8("v0");
          final var v1 = reader.readU8("v1");
          final var v2 = reader.readU8("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU16BE("v0");
          final var v1 = reader.readU16BE("v1");
          final var v2 = reader.readU16BE("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU32BE("v0");
          final var v1 = reader.readU32BE("v1");
          final var v2 = reader.readU32BE("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU64BE("v0");
          final var v1 = reader.readU64BE("v1");
          final var v2 = reader.readU64BE("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseTrianglesLE(
    final BSSReaderType reader,
    final SMFTriangles headerTriangles)
    throws IOException
  {
    final var triangleCount = headerTriangles.triangleCount();
    switch (headerTriangles.triangleIndexSizeBits()) {
      case 8: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU8("v0");
          final var v1 = reader.readU8("v1");
          final var v2 = reader.readU8("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU16LE("v0");
          final var v1 = reader.readU16LE("v1");
          final var v2 = reader.readU16LE("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU32LE("v0");
          final var v1 = reader.readU32LE("v1");
          final var v2 = reader.readU32LE("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, triangleCount) < 0;
             ++index) {
          final var v0 = reader.readU64LE("v0");
          final var v1 = reader.readU64LE("v1");
          final var v2 = reader.readU64LE("v2");
          this.triangleTracker.addTriangle(
            SMFB2Lexical.ofReader(reader), v0, v1, v2);
          this.triangles.onDataTriangle(v0, v1, v2);
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }
}
