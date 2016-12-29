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

package com.io7m.smfj.tests.processing;

import com.io7m.jfunctional.Unit;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.SMFMemoryMesh;
import com.io7m.smfj.processing.SMFMemoryMeshFilterCheck;
import com.io7m.smfj.processing.SMFMemoryMeshFilterCheckConfiguration;
import com.io7m.smfj.processing.SMFMemoryMeshFilterSchemaSet;
import com.io7m.smfj.processing.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.SMFProcessingError;
import javaslang.collection.List;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SMFMemoryMeshFilterSchemaSetTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterSchemaSetTest.class);
  }

  @Test
  public void testParseWrong0()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.empty());
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("schema-set"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("schema-set", "x", "<#@"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("schema-set", "<#@", "y"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of(
          "schema-set",
          "x",
          "y",
          "z"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong5()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of(
          "schema-set",
          "x",
          "float",
          "z",
          "32"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseOk0()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("schema-set", "696f376d", "0", "1", "2"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), "schema-set");
  }

  private static void checkMeshesSame(
    final SMFMemoryMesh mesh0,
    final SMFMemoryMesh mesh1)
  {
    Assert.assertEquals(
      mesh0.header().attributesByName(), mesh1.header().attributesByName());
    Assert.assertEquals(
      mesh0.header().attributesInOrder(), mesh1.header().attributesInOrder());
    Assert.assertEquals(
      mesh0.header().coordinateSystem(), mesh1.header().coordinateSystem());
    Assert.assertEquals(
      mesh0.header().metaCount(), mesh1.header().metaCount());
    Assert.assertEquals(
      mesh0.metadata(), mesh1.metadata());
    Assert.assertEquals(
      mesh0.arrays(), mesh1.arrays());
    Assert.assertEquals(
      mesh0.triangles(), mesh1.triangles());
  }

  @Test
  public void testSetOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setVendorID(0x696f376d)
        .setSchemaID(0)
        .setSchemaMajorVersion(2)
        .setSchemaMinorVersion(3)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaSet.create(identifier);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isValid());
    Assert.assertEquals(identifier, r.get().header().schemaIdentifier());

    checkMeshesSame(loader.mesh(), r.get());
  }
}