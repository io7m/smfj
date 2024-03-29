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

package com.io7m.smfj.tests.processing;

import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterTrianglesOptimize;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterTrianglesOptimizeConfiguration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.tests.processing.SMFMemoryMeshFilterTesting.WarningsAllowed.WARNINGS_DISALLOWED;

public final class SMFMemoryMeshFilterTrianglesOptimizeTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterTrianglesOptimizeTest.class);
  }

  private static void checkMeshesSame(
    final SMFMemoryMesh mesh0,
    final SMFMemoryMesh mesh1)
  {
    Assertions.assertEquals(
      mesh0.header().attributesByName(), mesh1.header().attributesByName());
    Assertions.assertEquals(
      mesh0.header().attributesInOrder(), mesh1.header().attributesInOrder());
    Assertions.assertEquals(
      mesh0.header().coordinateSystem(), mesh1.header().coordinateSystem());
    Assertions.assertEquals(
      mesh0.header().schemaIdentifier(), mesh1.header().schemaIdentifier());
    Assertions.assertEquals(
      mesh0.metadata(), mesh1.metadata());
    Assertions.assertEquals(
      mesh0.arrays(), mesh1.arrays());
    Assertions.assertEquals(
      mesh0.triangles(), mesh1.triangles());
  }

  private static void checkTriangles(
    final SMFMemoryMesh mesh,
    final int bits)
  {
    final long max = (long) (Math.pow(2.0, bits) - 1.0);

    final List<Vector3L> triangles = mesh.triangles();
    for (int index = 0; index < triangles.size(); ++index) {
      final Vector3L triangle = triangles.get(index);
      Assertions.assertTrue(Long.compareUnsigned(triangle.x(), max) <= 0);
      Assertions.assertTrue(Long.compareUnsigned(triangle.y(), max) <= 0);
      Assertions.assertTrue(Long.compareUnsigned(triangle.z(), max) <= 0);
    }
  }

  @Test
  public void testParseWrong1()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong2()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong3()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "x"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong4()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "validate", "x"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseOk0()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "validate"));
    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public void testParseOk1()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "no-validate"));
    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public void testParseOk2()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("-", "no-validate"));
    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public void testValidateBadTriangle()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "bad_triangle.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(true)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isFailed());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e);
    });
  }

  @Test
  public void testValidateSize8_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      8L, mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      8L, mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize8_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      8L, mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      16L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize8_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      8L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      32L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize8_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      8L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      64L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }

  @Test
  public void testValidateSize16_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      16L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      8L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize16_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      16L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      16L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize16_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      16L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      32L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize16_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      16L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      64L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }

  @Test
  public void testValidateSize32_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      32L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      8L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize32_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      32L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      16L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize32_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      32L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      32L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize32_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      32L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      64L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }

  @Test
  public void testValidateSize64_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      64L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      8L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize64_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      64L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      16L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize64_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      64L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      32L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize64_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assertions.assertEquals(
      64L,
      mesh0.header().triangles().triangleIndexSizeBits());
    Assertions.assertEquals(
      64L,
      mesh1.header().triangles().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }
}
