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

package com.io7m.smfj.processing.api;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import java.io.IOException;
import java.util.Objects;

import static com.io7m.smfj.core.SMFVoid.void_;

/**
 * A memory mesh serializer.
 */

public final class SMFMemoryMeshSerializer
{
  private SMFMemoryMeshSerializer()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Serialize the given mesh to the given serializer.
   *
   * @param mesh A mesh
   * @param s    A serializer
   *
   * @throws IOException On I/O errors
   */

  public static void serialize(
    final SMFMemoryMesh mesh,
    final SMFSerializerType s)
    throws IOException
  {
    Objects.requireNonNull(mesh, "Mesh");
    Objects.requireNonNull(s, "Serial");

    final SMFHeader header = mesh.header();
    s.serializeHeader(header);
    serializeNonInterleaved(mesh, s, header);
    serializeTriangles(mesh, s);
    serializeMetadata(mesh, s);
  }

  private static void serializeMetadata(
    final SMFMemoryMesh mesh,
    final SMFSerializerType s)
    throws IOException
  {
    for (final SMFMetadata m : mesh.metadata()) {
      s.serializeMetadata(m.schema(), m.data());
    }
  }

  private static void serializeTriangles(
    final SMFMemoryMesh mesh,
    final SMFSerializerType s)
    throws IOException
  {
    try (SMFSerializerDataTrianglesType st = s.serializeTrianglesStart()) {
      for (final Vector3L t : mesh.triangles()) {
        st.serializeTriangle(t.x(), t.y(), t.z());
      }
    }
  }

  private static void serializeNonInterleaved(
    final SMFMemoryMesh mesh,
    final SMFSerializerType s,
    final SMFHeader header)
    throws IOException
  {
    try (SMFSerializerDataAttributesNonInterleavedType sv =
           s.serializeVertexDataNonInterleavedStart()) {
      for (final SMFAttribute attribute : header.attributesInOrder()) {
        final SMFAttributeName name = attribute.name();
        try (SMFSerializerDataAttributesValuesType sav =
               sv.serializeData(name)) {
          final SMFAttributeArrayType array = mesh.arrays().get(name);
          array.matchArray(
            sav,
            SMFMemoryMeshSerializer::serializeFloat4,
            SMFMemoryMeshSerializer::serializeFloat3,
            SMFMemoryMeshSerializer::serializeFloat2,
            SMFMemoryMeshSerializer::serializeFloat1,
            SMFMemoryMeshSerializer::serializeUnsigned4,
            SMFMemoryMeshSerializer::serializeUnsigned3,
            SMFMemoryMeshSerializer::serializeUnsigned2,
            SMFMemoryMeshSerializer::serializeUnsigned1,
            SMFMemoryMeshSerializer::serializeSigned4,
            SMFMemoryMeshSerializer::serializeSigned3,
            SMFMemoryMeshSerializer::serializeSigned2,
            SMFMemoryMeshSerializer::serializeSigned1);
        }
      }
    }
  }

  private static SMFVoid serializeSigned1(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerSigned1Type y)
    throws IOException
  {
    for (final Long v : y.values()) {
      s.serializeValueIntegerSigned1(v.longValue());
    }
    return void_();
  }

  private static SMFVoid serializeSigned2(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerSigned2Type y)
    throws IOException
  {
    for (final Vector2L v : y.values()) {
      s.serializeValueIntegerSigned2(v.x(), v.y());
    }
    return void_();
  }

  private static SMFVoid serializeSigned3(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerSigned3Type y)
    throws IOException
  {
    for (final Vector3L v : y.values()) {
      s.serializeValueIntegerSigned3(v.x(), v.y(), v.z());
    }
    return void_();
  }

  private static SMFVoid serializeSigned4(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerSigned4Type y)
    throws IOException
  {
    for (final Vector4L v : y.values()) {
      s.serializeValueIntegerSigned4(v.x(), v.y(), v.z(), v.w());
    }
    return void_();
  }

  private static SMFVoid serializeUnsigned1(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerUnsigned1Type y)
    throws IOException
  {
    for (final Long v : y.values()) {
      s.serializeValueIntegerUnsigned1(v.longValue());
    }
    return void_();
  }

  private static SMFVoid serializeUnsigned2(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerUnsigned2Type y)
    throws IOException
  {
    for (final Vector2L v : y.values()) {
      s.serializeValueIntegerUnsigned2(v.x(), v.y());
    }
    return void_();
  }

  private static SMFVoid serializeUnsigned3(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerUnsigned3Type y)
    throws IOException
  {
    for (final Vector3L v : y.values()) {
      s.serializeValueIntegerUnsigned3(v.x(), v.y(), v.z());
    }
    return void_();
  }

  private static SMFVoid serializeUnsigned4(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayIntegerUnsigned4Type y)
    throws IOException
  {
    for (final Vector4L v : y.values()) {
      s.serializeValueIntegerUnsigned4(v.x(), v.y(), v.z(), v.w());
    }
    return void_();
  }

  private static SMFVoid serializeFloat1(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayFloating1Type y)
    throws IOException
  {
    for (final Double v : y.values()) {
      s.serializeValueFloat1(v.doubleValue());
    }
    return void_();
  }

  private static SMFVoid serializeFloat2(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayFloating2Type y)
    throws IOException
  {
    for (final Vector2D v : y.values()) {
      s.serializeValueFloat2(v.x(), v.y());
    }
    return void_();
  }

  private static SMFVoid serializeFloat3(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayFloating3Type y)
    throws IOException
  {
    for (final Vector3D v : y.values()) {
      s.serializeValueFloat3(v.x(), v.y(), v.z());
    }
    return void_();
  }

  private static SMFVoid serializeFloat4(
    final SMFSerializerDataAttributesValuesType s,
    final SMFAttributeArrayFloating4Type y)
    throws IOException
  {
    for (final Vector4D v : y.values()) {
      s.serializeValueFloat4(v.x(), v.y(), v.z(), v.w());
    }
    return void_();
  }
}
