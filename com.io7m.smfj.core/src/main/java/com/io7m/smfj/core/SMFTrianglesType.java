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

package com.io7m.smfj.core;

import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

/**
 * Information about triangles in a mesh.
 */

@Value.Immutable
@JavaslangEncodingEnabled
@SMFImmutableStyleType
public interface SMFTrianglesType
{
  /**
   * @return The number of triangles in the file
   */

  @Value.Parameter
  @Value.Default
  default long triangleCount()
  {
    return 0L;
  }

  /**
   * @return The size in bits of each triangle index
   */

  @Value.Parameter
  @Value.Default
  default long triangleIndexSizeBits()
  {
    return 32L;
  }

  /**
   * @return The size in octets of each triangle index
   */

  @Value.Derived
  default long triangleIndexSizeOctets()
  {
    return this.triangleIndexSizeBits() / 8L;
  }

  /**
   * @return The size in octets of each triangle
   */

  @Value.Derived
  default long triangleSizeOctets()
  {
    return Math.multiplyExact(this.triangleIndexSizeOctets(), 3L);
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    SMFSupportedSizes.checkIntegerUnsignedSupported(
      Math.toIntExact(this.triangleIndexSizeBits()));
  }
}