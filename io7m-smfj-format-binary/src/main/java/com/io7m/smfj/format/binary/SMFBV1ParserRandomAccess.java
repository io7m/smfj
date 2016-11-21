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

package com.io7m.smfj.format.binary;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVendorSchemaIdentifier;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

final class SMFBV1ParserRandomAccess extends SMFBAbstractParserRandomAccess
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBV1ParserRandomAccess.class);
  }

  private final byte[] attribute_buffer;
  private Map<SMFAttributeName, SMFAttribute> attributes_named;
  private List<SMFAttribute> attributes;
  private long vertices_count;
  private long triangles_count;
  private long triangles_size_bits;
  private long attributes_count;
  private SMFHeader header;
  private SMFBV1Offsets offsets;
  private long vendor_id;
  private long vendor_schema_id;
  private long vendor_schema_version_major;
  private long vendor_schema_version_minor;

  SMFBV1ParserRandomAccess(
    final SMFParserEventsType in_events,
    final SMFBDataFileChannelReader in_reader,
    final AtomicReference<ParserState> in_state)
  {
    super(in_events, in_reader, in_state);
    this.attribute_buffer =
      new byte[SMFBV1AttributeByteBuffered.sizeInOctets()];
    this.attributes = List.empty();
    this.attributes_named = HashMap.empty();
  }

  @Override
  protected Logger log()
  {
    return LOG;
  }

  @Override
  public void parseHeader()
  {
    LOG.debug("parsing header");

    try {
      this.vendor_id =
        super.reader.readUnsigned32(
          Optional.of("vendor id"),
          SMFBV1Offsets.offsetSchemaVendorId());
      this.vendor_schema_id =
        super.reader.readUnsigned32(
          Optional.of("vendor schema id"),
          SMFBV1Offsets.offsetSchemaVendorSchemaId());
      this.vendor_schema_version_major =
        super.reader.readUnsigned32(
          Optional.of("vendor schema version major"),
          SMFBV1Offsets.offsetSchemaVendorSchemaVersionMajor());
      this.vendor_schema_version_minor =
        super.reader.readUnsigned32(
          Optional.of("vendor schema version minor"),
          SMFBV1Offsets.offsetSchemaVendorSchemaVersionMinor());

      this.vertices_count =
        super.reader.readUnsigned64(
          Optional.of("vertex count"),
          SMFBV1Offsets.offsetHeaderVerticesCount());

      this.triangles_count =
        super.reader.readUnsigned64(
          Optional.of("triangle count"),
          SMFBV1Offsets.offsetHeaderTrianglesCount());

      this.triangles_size_bits =
        super.reader.readUnsigned32(
          Optional.of("triangle size"),
          SMFBV1Offsets.offsetHeaderTrianglesSize());

      this.attributes_count =
        super.reader.readUnsigned32(
          Optional.of("attribute count"),
          SMFBV1Offsets.offsetHeaderAttributesCount());

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "expecting {} vertices",
          Long.valueOf(this.vertices_count));
        LOG.debug(
          "expecting {} triangles of size {}",
          Long.valueOf(this.triangles_count),
          Long.valueOf(this.triangles_size_bits));
        LOG.debug(
          "expecting {} attributes",
          Long.valueOf(this.attributes_count));
      }

      this.parseHeaderAttributes();
      this.checkHeaderAttributes();

      if (super.state.get() != ParserState.STATE_FAILED) {
        super.events.onHeaderParsed(this.header);
        super.state.set(ParserState.STATE_PARSED_HEADER);
      }
    } catch (final IOException e) {
      super.fail("I/O error: " + e.getMessage());
    } catch (final Exception e) {
      super.fail(e.getMessage());
    }
  }

  @Override
  public void parseAttributeData(
    final SMFAttributeName name)
  {
    NullCheck.notNull(this.offsets, "Offsets");

    final Map<SMFAttributeName, SMFBOctetRange> ao = this.offsets.attributeOffsets();
    final Option<SMFBOctetRange> range_opt = ao.get(name);
    if (range_opt.isEmpty()) {
      throw new NoSuchElementException("No such attribute: " + name.value());
    }

    final SMFBOctetRange range = range_opt.get();
    long offset = range.octetStart();

    final SMFAttribute attribute = this.attributes_named.get(name).get();
    final long size = Math.multiplyExact(
      (long) attribute.componentSizeOctets(),
      (long) attribute.componentCount());

    try {
      super.events.onDataAttributeStart(attribute);

      final Optional<String> name_opt = Optional.of(name.value());
      for (long index = 0L;
           Long.compareUnsigned(index, this.vertices_count) < 0;
           index = Math.addExact(index, 1L)) {

        switch (attribute.componentType()) {
          case ELEMENT_TYPE_INTEGER_SIGNED: {
            this.parseAttributeDataIntegerSigned(attribute, name_opt, offset);
            break;
          }
          case ELEMENT_TYPE_INTEGER_UNSIGNED: {
            this.parseAttributeDataIntegerUnsigned(attribute, name_opt, offset);
            break;
          }
          case ELEMENT_TYPE_FLOATING: {
            this.parseAttributeDataFloating(attribute, name_opt, offset);
          }
        }

        offset = Math.addExact(offset, size);
      }

    } catch (final IOException e) {
      super.fail(e.getMessage());
    } finally {
      super.events.onDataAttributeFinish(attribute);
    }
  }

  @Override
  public void parseTriangles()
  {
    try {
      super.events.onDataTrianglesStart();

      final Optional<String> name = Optional.of("triangle");

      long offset =
        this.offsets.trianglesDataOffset();

      final long size =
        Math.multiplyExact(3L, this.triangles_size_bits / 8L);
      Invariants.checkInvariant(size != 0L, "Triangle size is nonzero");

      for (long index = 0L;
           Long.compareUnsigned(index, this.triangles_count) < 0;
           index = Math.addExact(index, 1L)) {

        switch (Math.toIntExact(this.triangles_size_bits)) {
          case 8: {
            super.events.onDataTriangle(
              super.reader.readUnsigned8(name, offset),
              super.reader.readUnsigned8(name, Math.addExact(offset, 1L)),
              super.reader.readUnsigned8(name, Math.addExact(offset, 2L)));
            break;
          }
          case 16: {
            super.events.onDataTriangle(
              super.reader.readUnsigned16(name, offset),
              super.reader.readUnsigned16(name, Math.addExact(offset, 2L)),
              super.reader.readUnsigned16(name, Math.addExact(offset, 4L)));
            break;
          }
          case 32: {
            super.events.onDataTriangle(
              super.reader.readUnsigned32(name, offset),
              super.reader.readUnsigned32(name, Math.addExact(offset, 4L)),
              super.reader.readUnsigned32(name, Math.addExact(offset, 8L)));
            break;
          }
        }

        offset = Math.addExact(offset, size);
      }

    } catch (final IOException e) {
      super.fail(e.getMessage());
    } finally {
      super.events.onDataTrianglesFinish();
    }
  }

  private void parseAttributeDataFloating(
    final SMFAttribute attribute,
    final Optional<String> name_opt,
    final long offset)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        this.parseAttributeDataFloating1(attribute, name_opt, offset);
        return;
      }
      case 2: {
        this.parseAttributeDataFloating2(attribute, name_opt, offset);
        return;
      }
      case 3: {
        this.parseAttributeDataFloating3(attribute, name_opt, offset);
        return;
      }
      case 4: {
        this.parseAttributeDataFloating4(attribute, name_opt, offset);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataFloating1(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        super.events.onDataAttributeValueFloat1(
          this.reader.readFloat16(name, offset));
        return;
      }
      case 32: {
        super.events.onDataAttributeValueFloat1(
          this.reader.readFloat32(name, offset));
        return;
      }
      case 64: {
        super.events.onDataAttributeValueFloat1(
          this.reader.readFloat64(name, offset));
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataFloating2(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        final double x =
          this.reader.readFloat16(name, offset);
        final double y =
          this.reader.readFloat16(name, Math.addExact(offset, 2L));
        super.events.onDataAttributeValueFloat2(x, y);
        return;
      }
      case 32: {
        final double x =
          this.reader.readFloat32(name, offset);
        final double y =
          this.reader.readFloat32(name, Math.addExact(offset, 4L));
        super.events.onDataAttributeValueFloat2(x, y);
        return;
      }
      case 64: {
        final double x =
          this.reader.readFloat64(name, offset);
        final double y =
          this.reader.readFloat64(name, Math.addExact(offset, 8L));
        super.events.onDataAttributeValueFloat2(x, y);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataFloating3(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        final double x =
          this.reader.readFloat16(name, offset);
        final double y =
          this.reader.readFloat16(name, Math.addExact(offset, 2L));
        final double z =
          this.reader.readFloat16(name, Math.addExact(offset, 4L));
        super.events.onDataAttributeValueFloat3(x, y, z);
        return;
      }
      case 32: {
        final double x =
          this.reader.readFloat32(name, offset);
        final double y =
          this.reader.readFloat32(name, Math.addExact(offset, 4L));
        final double z =
          this.reader.readFloat32(name, Math.addExact(offset, 8L));
        super.events.onDataAttributeValueFloat3(x, y, z);
        return;
      }
      case 64: {
        final double x =
          this.reader.readFloat64(name, offset);
        final double y =
          this.reader.readFloat64(name, Math.addExact(offset, 8L));
        final double z =
          this.reader.readFloat64(name, Math.addExact(offset, 16L));
        super.events.onDataAttributeValueFloat3(x, y, z);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataFloating4(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        final double x =
          this.reader.readFloat16(name, offset);
        final double y =
          this.reader.readFloat16(name, Math.addExact(offset, 2L));
        final double z =
          this.reader.readFloat16(name, Math.addExact(offset, 4L));
        final double w =
          this.reader.readFloat16(name, Math.addExact(offset, 6L));
        super.events.onDataAttributeValueFloat4(x, y, z, w);
        return;
      }
      case 32: {
        final double x =
          this.reader.readFloat32(name, offset);
        final double y =
          this.reader.readFloat32(name, Math.addExact(offset, 4L));
        final double z =
          this.reader.readFloat32(name, Math.addExact(offset, 8L));
        final double w =
          this.reader.readFloat32(name, Math.addExact(offset, 12L));
        super.events.onDataAttributeValueFloat4(x, y, z, w);
        return;
      }
      case 64: {
        final double x =
          this.reader.readFloat64(name, offset);
        final double y =
          this.reader.readFloat64(name, Math.addExact(offset, 8L));
        final double z =
          this.reader.readFloat64(name, Math.addExact(offset, 16L));
        final double w =
          this.reader.readFloat64(name, Math.addExact(offset, 24L));
        super.events.onDataAttributeValueFloat4(x, y, z, w);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerSigned(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        this.parseAttributeDataIntegerSigned1(attribute, name, offset);
        return;
      }
      case 2: {
        this.parseAttributeDataIntegerSigned2(attribute, name, offset);
        return;
      }
      case 3: {
        this.parseAttributeDataIntegerSigned3(attribute, name, offset);
        return;
      }
      case 4: {
        this.parseAttributeDataIntegerSigned4(attribute, name, offset);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerSigned4(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        final long x =
          this.reader.readSigned8(name, offset);
        final long y =
          this.reader.readSigned8(name, Math.addExact(offset, 1L));
        final long z =
          this.reader.readSigned8(name, Math.addExact(offset, 2L));
        final long w =
          this.reader.readSigned8(name, Math.addExact(offset, 3L));
        super.events.onDataAttributeValueIntegerSigned4(x, y, z, w);
        return;
      }
      case 16: {
        final long x =
          this.reader.readSigned16(name, offset);
        final long y =
          this.reader.readSigned16(name, Math.addExact(offset, 2L));
        final long z =
          this.reader.readSigned16(name, Math.addExact(offset, 4L));
        final long w =
          this.reader.readSigned16(name, Math.addExact(offset, 6L));
        super.events.onDataAttributeValueIntegerSigned4(x, y, z, w);
        return;
      }
      case 32: {
        final long x =
          this.reader.readSigned32(name, offset);
        final long y =
          this.reader.readSigned32(name, Math.addExact(offset, 4L));
        final long z =
          this.reader.readSigned32(name, Math.addExact(offset, 8L));
        final long w =
          this.reader.readSigned32(name, Math.addExact(offset, 12L));
        super.events.onDataAttributeValueIntegerSigned4(x, y, z, w);
        return;
      }
      case 64: {
        final long x =
          this.reader.readSigned64(name, offset);
        final long y =
          this.reader.readSigned64(name, Math.addExact(offset, 8L));
        final long z =
          this.reader.readSigned64(name, Math.addExact(offset, 16L));
        final long w =
          this.reader.readSigned64(name, Math.addExact(offset, 24L));
        super.events.onDataAttributeValueIntegerSigned4(x, y, z, w);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerSigned3(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        final long x =
          this.reader.readSigned8(name, offset);
        final long y =
          this.reader.readSigned8(name, Math.addExact(offset, 1L));
        final long z =
          this.reader.readSigned8(name, Math.addExact(offset, 2L));
        super.events.onDataAttributeValueIntegerSigned3(x, y, z);
        return;
      }
      case 16: {
        final long x =
          this.reader.readSigned16(name, offset);
        final long y =
          this.reader.readSigned16(name, Math.addExact(offset, 2L));
        final long z =
          this.reader.readSigned16(name, Math.addExact(offset, 4L));
        super.events.onDataAttributeValueIntegerSigned3(x, y, z);
        return;
      }
      case 32: {
        final long x =
          this.reader.readSigned32(name, offset);
        final long y =
          this.reader.readSigned32(name, Math.addExact(offset, 4L));
        final long z =
          this.reader.readSigned32(name, Math.addExact(offset, 8L));
        super.events.onDataAttributeValueIntegerSigned3(x, y, z);
        return;
      }
      case 64: {
        final long x =
          this.reader.readSigned64(name, offset);
        final long y =
          this.reader.readSigned64(name, Math.addExact(offset, 8L));
        final long z =
          this.reader.readSigned64(name, Math.addExact(offset, 16L));
        super.events.onDataAttributeValueIntegerSigned3(x, y, z);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerSigned2(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        final long x =
          this.reader.readSigned8(name, offset);
        final long y =
          this.reader.readSigned8(name, Math.addExact(offset, 1L));
        super.events.onDataAttributeValueIntegerSigned2(x, y);
        return;
      }
      case 16: {
        final long x =
          this.reader.readSigned16(name, offset);
        final long y =
          this.reader.readSigned16(name, Math.addExact(offset, 2L));
        super.events.onDataAttributeValueIntegerSigned2(x, y);
        return;
      }
      case 32: {
        final long x =
          this.reader.readSigned32(name, offset);
        final long y =
          this.reader.readSigned32(name, Math.addExact(offset, 4L));
        super.events.onDataAttributeValueIntegerSigned2(x, y);
        return;
      }
      case 64: {
        final long x =
          this.reader.readSigned64(name, offset);
        final long y =
          this.reader.readSigned64(name, Math.addExact(offset, 8L));
        super.events.onDataAttributeValueIntegerSigned2(x, y);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerSigned1(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        super.events.onDataAttributeValueIntegerSigned1(
          this.reader.readSigned8(name, offset));
        return;
      }
      case 16: {
        super.events.onDataAttributeValueIntegerSigned1(
          this.reader.readSigned16(name, offset));
        return;
      }
      case 32: {
        super.events.onDataAttributeValueIntegerSigned1(
          this.reader.readSigned32(name, offset));
        return;
      }
      case 64: {
        super.events.onDataAttributeValueIntegerSigned1(
          this.reader.readSigned64(name, offset));
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerUnsigned(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        this.parseAttributeDataIntegerUnsigned1(attribute, name, offset);
        return;
      }
      case 2: {
        this.parseAttributeDataIntegerUnsigned2(attribute, name, offset);
        return;
      }
      case 3: {
        this.parseAttributeDataIntegerUnsigned3(attribute, name, offset);
        return;
      }
      case 4: {
        this.parseAttributeDataIntegerUnsigned4(attribute, name, offset);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerUnsigned4(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        final long x =
          this.reader.readUnsigned8(name, offset);
        final long y =
          this.reader.readUnsigned8(name, Math.addExact(offset, 1L));
        final long z =
          this.reader.readUnsigned8(name, Math.addExact(offset, 2L));
        final long w =
          this.reader.readUnsigned8(name, Math.addExact(offset, 3L));
        super.events.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        return;
      }
      case 16: {
        final long x =
          this.reader.readUnsigned16(name, offset);
        final long y =
          this.reader.readUnsigned16(name, Math.addExact(offset, 2L));
        final long z =
          this.reader.readUnsigned16(name, Math.addExact(offset, 4L));
        final long w =
          this.reader.readUnsigned16(name, Math.addExact(offset, 6L));
        super.events.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        return;
      }
      case 32: {
        final long x =
          this.reader.readUnsigned32(name, offset);
        final long y =
          this.reader.readUnsigned32(name, Math.addExact(offset, 4L));
        final long z =
          this.reader.readUnsigned32(name, Math.addExact(offset, 8L));
        final long w =
          this.reader.readUnsigned32(name, Math.addExact(offset, 12L));
        super.events.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        return;
      }
      case 64: {
        final long x =
          this.reader.readUnsigned64(name, offset);
        final long y =
          this.reader.readUnsigned64(name, Math.addExact(offset, 8L));
        final long z =
          this.reader.readUnsigned64(name, Math.addExact(offset, 16L));
        final long w =
          this.reader.readUnsigned64(name, Math.addExact(offset, 24L));
        super.events.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerUnsigned3(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        final long x =
          this.reader.readUnsigned8(name, offset);
        final long y =
          this.reader.readUnsigned8(name, Math.addExact(offset, 1L));
        final long z =
          this.reader.readUnsigned8(name, Math.addExact(offset, 2L));
        super.events.onDataAttributeValueIntegerUnsigned3(x, y, z);
        return;
      }
      case 16: {
        final long x =
          this.reader.readUnsigned16(name, offset);
        final long y =
          this.reader.readUnsigned16(name, Math.addExact(offset, 2L));
        final long z =
          this.reader.readUnsigned16(name, Math.addExact(offset, 4L));
        super.events.onDataAttributeValueIntegerUnsigned3(x, y, z);
        return;
      }
      case 32: {
        final long x =
          this.reader.readUnsigned32(name, offset);
        final long y =
          this.reader.readUnsigned32(name, Math.addExact(offset, 4L));
        final long z =
          this.reader.readUnsigned32(name, Math.addExact(offset, 8L));
        super.events.onDataAttributeValueIntegerUnsigned3(x, y, z);
        return;
      }
      case 64: {
        final long x =
          this.reader.readUnsigned64(name, offset);
        final long y =
          this.reader.readUnsigned64(name, Math.addExact(offset, 8L));
        final long z =
          this.reader.readUnsigned64(name, Math.addExact(offset, 16L));
        super.events.onDataAttributeValueIntegerUnsigned3(x, y, z);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerUnsigned2(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        final long x =
          this.reader.readUnsigned8(name, offset);
        final long y =
          this.reader.readUnsigned8(name, Math.addExact(offset, 1L));
        super.events.onDataAttributeValueIntegerUnsigned2(x, y);
        return;
      }
      case 16: {
        final long x =
          this.reader.readUnsigned16(name, offset);
        final long y =
          this.reader.readUnsigned16(name, Math.addExact(offset, 2L));
        super.events.onDataAttributeValueIntegerUnsigned2(x, y);
        return;
      }
      case 32: {
        final long x =
          this.reader.readUnsigned32(name, offset);
        final long y =
          this.reader.readUnsigned32(name, Math.addExact(offset, 4L));
        super.events.onDataAttributeValueIntegerUnsigned2(x, y);
        return;
      }
      case 64: {
        final long x =
          this.reader.readUnsigned64(name, offset);
        final long y =
          this.reader.readUnsigned64(name, Math.addExact(offset, 8L));
        super.events.onDataAttributeValueIntegerUnsigned2(x, y);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void parseAttributeDataIntegerUnsigned1(
    final SMFAttribute attribute,
    final Optional<String> name,
    final long offset)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        super.events.onDataAttributeValueIntegerUnsigned1(
          this.reader.readUnsigned8(name, offset));
        return;
      }
      case 16: {
        super.events.onDataAttributeValueIntegerUnsigned1(
          this.reader.readUnsigned16(name, offset));
        return;
      }
      case 32: {
        super.events.onDataAttributeValueIntegerUnsigned1(
          this.reader.readUnsigned32(name, offset));
        return;
      }
      case 64: {
        super.events.onDataAttributeValueIntegerUnsigned1(
          this.reader.readUnsigned64(name, offset));
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void checkHeaderAttributes()
  {
    this.attributes.forEach(attribute -> {
      final SMFAttributeName name = attribute.name();
      if (this.attributes_named.containsKey(name)) {
        super.fail("Duplicate attribute name: " + name.value());
      }
      this.attributes_named = this.attributes_named.put(name, attribute);
    });

    if (this.state.get() == ParserState.STATE_FAILED) {
      return;
    }

    final SMFVendorSchemaIdentifier.Builder vb =
      SMFVendorSchemaIdentifier.builder();
    vb.setVendorID((int) this.vendor_id);
    vb.setSchemaID((int) this.vendor_schema_id);
    vb.setSchemaMajorVersion((int) this.vendor_schema_version_major);
    vb.setSchemaMinorVersion((int) this.vendor_schema_version_minor);

    final SMFHeader.Builder hb = SMFHeader.builder();
    hb.setVertexCount(this.vertices_count);
    hb.setTriangleCount(this.triangles_count);
    hb.setTriangleIndexSizeBits(this.triangles_size_bits);
    hb.setAttributesInOrder(this.attributes);
    hb.setAttributesByName(this.attributes_named);
    hb.setSchemaIdentifier(vb.build());
    this.header = hb.build();
    this.offsets = SMFBV1Offsets.fromHeader(this.header);
  }

  private void parseHeaderAttributes()
    throws IOException
  {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "attribute size in octets: {}",
        Integer.valueOf(SMFBV1AttributeByteBuffered.sizeInOctets()));
    }

    final ByteBuffer buffer =
      ByteBuffer.wrap(this.attribute_buffer);
    final JPRACursor1DType<SMFBV1AttributeType> cursor =
      JPRACursor1DByteBufferedChecked.newCursor(
        buffer, SMFBV1AttributeByteBuffered::newValueWithOffset);
    final SMFBV1AttributeType view =
      cursor.getElementView();

    long offset = SMFBV1Offsets.offsetHeaderAttributesData();
    for (long index = 0L;
         Long.compareUnsigned(index, this.attributes_count) < 0;
         index = Math.addExact(index, 1L)) {

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "reading attribute {} from offset {}",
          Long.valueOf(index),
          Long.valueOf(offset));
      }

      Invariants.checkInvariant(
        offset % 8L == 0L, "Attribute offset must be divisible by 8");

      super.reader.readBytes(
        Optional.of("Attribute"), this.attribute_buffer, offset);

      try {
        final SMFAttributeName name =
          SMFAttributeName.of(view.getNameReadable().getNewValue());
        final int component_count =
          view.getComponentCount();
        final int component_size =
          view.getComponentSize();
        final SMFComponentType component_type =
          SMFComponentType.ofInteger(view.getComponentKind());

        if (LOG.isDebugEnabled()) {
          LOG.debug(
            "attribute name: {}", name);
          LOG.debug(
            "attribute component count: {}",
            Integer.valueOf(component_count));
          LOG.debug(
            "attribute component size: {}",
            Integer.valueOf(component_size));
          LOG.debug(
            "attribute component type: {}",
            component_type);
        }

        this.attributes = this.attributes.append(
          SMFAttribute.of(
            name,
            component_type,
            component_count,
            component_size));
      } catch (final IllegalArgumentException e) {
        super.fail(e.getMessage());
      }

      offset = Math.addExact(
        offset, (long) SMFBV1AttributeByteBuffered.sizeInOctets());
    }
  }

  @Override
  public void close()
    throws IOException
  {

  }
}