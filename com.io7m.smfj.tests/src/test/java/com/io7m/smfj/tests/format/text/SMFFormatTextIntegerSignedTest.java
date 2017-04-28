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


package com.io7m.smfj.tests.format.text;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFFormatTextIntegerSignedTest extends SMFTextTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatTextIntegerSignedTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  private static SMFHeader header(
    final SMFAttribute attr)
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setAttributesInOrder(List.of(attr));
    header_b.setVertexCount(3L);
    header_b.setTriangles(SMFTriangles.of(0L, 32L));
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    return header_b.build();
  }

  @Test
  public void testDataAttributesIntegerSigned64_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_4";
    final long component_count = 4L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned64_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_3";
    final long component_count = 3L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned64_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_2";
    final long component_count = 2L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned64_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_1";
    final long component_count = 1L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned32_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_4";
    final long component_count = 4L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned32_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_3";
    final long component_count = 3L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned32_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_2";
    final long component_count = 2L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned32_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_1";
    final long component_count = 1L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned16_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_4";
    final long component_count = 4L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned16_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_3";
    final long component_count = 3L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned16_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_2";
    final long component_count = 2L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned16_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_1";
    final long component_count = 1L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned8_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_4";
    final long component_count = 4L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned8_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_3";
    final long component_count = 3L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned8_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_2";
    final long component_count = 2L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testDataAttributesIntegerSigned8_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_1";
    final long component_count = 1L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < 3; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parseHeader();
    p.parseData();
  }

  @Test
  public void testSerializerAttributeTooFew()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        64),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(2L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);

    this.expected.expect(IllegalStateException.class);
    serializer.serializeData(SMFAttributeName.of("y"));
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_4()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned3(0, 1, 2);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_3()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        3,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_2()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        2,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_1()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        1,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_4()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned3(0, 1, 2);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_3()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        3,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_2()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        2,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_1()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        1,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }


  @Test
  public void testSerializerAttributeWrongTypeF16_4()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned3(0, 1, 2);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_3()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        3,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_2()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        2,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_1()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        1,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
  }

  @Test
  public void testSerializerAttributeAll()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path.toUri(), out);

    List<SMFAttribute> attributes = List.empty();
    for (final int size : List.of(
      Integer.valueOf(16),
      Integer.valueOf(32),
      Integer.valueOf(64))) {
      for (final int count : List.of(
        Integer.valueOf(1),
        Integer.valueOf(2),
        Integer.valueOf(3),
        Integer.valueOf(4))) {
        attributes = attributes.append(SMFAttribute.of(
          SMFAttributeName.of(String.format(
            "%s-%d-%d", SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.name(),
            Integer.valueOf(size),
            Integer.valueOf(count))),
          SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
          count,
          size));
      }
    }

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();

    for (final SMFAttribute attribute : attributes) {
      serializer.serializeData(attribute.name());
      switch (attribute.componentCount()) {
        case 1: {
          serializer.serializeValueIntegerSigned1(0);
          break;
        }
        case 2: {
          serializer.serializeValueIntegerSigned2(0, 1);
          break;
        }
        case 3: {
          serializer.serializeValueIntegerSigned3(0, 1, 2);
          break;
        }
        case 4: {
          serializer.serializeValueIntegerSigned4(0, 1, 2, 3);
          break;
        }
        default: {
          throw new UnreachableCodeException();
        }
      }
    }
  }
}
