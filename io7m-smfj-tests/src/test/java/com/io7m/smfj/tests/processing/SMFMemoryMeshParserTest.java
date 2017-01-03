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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshParser;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import javaslang.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFMemoryMeshParserTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshParserTest.class);
  }

  @Test
  public void testRoundTripRandom()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader0 = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader0, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMesh mesh0 = loader0.mesh();
    final SMFMemoryMeshProducerType loader1 = SMFMemoryMeshProducer.create();

    try (final SMFParserRandomAccessType parser =
           SMFMemoryMeshParser.createRandomAccess(mesh0, loader1)) {
      parser.parseHeader();
      for (final SMFAttribute attr : mesh0.header().attributesInOrder()) {
        parser.parseAttributeData(attr.name());
      }
      parser.parseTriangles();
      parser.parseMetadata();
    }

    final SMFMemoryMesh mesh1 = loader1.mesh();
    Assert.assertEquals(mesh0.header(), mesh1.header());
    Assert.assertEquals(mesh0.metadata(), mesh1.metadata());

    for (final Tuple2<SMFAttributeName, SMFAttributeArrayType> pair : mesh0.arrays()) {
      final SMFAttributeArrayType array0 = pair._2;
      final SMFAttributeArrayType array1 = mesh1.arrays().get(pair._1).get();
      Assert.assertEquals(array0, array1);
    }

    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
  }

  @Test
  public void testRoundTripSequential()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader0 = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader0, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMesh mesh0 = loader0.mesh();
    final SMFMemoryMeshProducerType loader1 = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFMemoryMeshParser.createSequential(mesh0, loader1)) {
      parser.parseHeader();
      parser.parseData();
    }

    final SMFMemoryMesh mesh1 = loader1.mesh();
    Assert.assertEquals(mesh0.header(), mesh1.header());
    Assert.assertEquals(mesh0.metadata(), mesh1.metadata());

    for (final Tuple2<SMFAttributeName, SMFAttributeArrayType> pair : mesh0.arrays()) {
      final SMFAttributeArrayType array0 = pair._2;
      final SMFAttributeArrayType array1 = mesh1.arrays().get(pair._1).get();
      Assert.assertEquals(array0, array1);
    }

    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
  }
}
