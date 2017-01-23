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

package com.io7m.smfj.core;

import com.io7m.jcoords.core.conversion.CAxisSystem;
import org.immutables.value.Value;

/**
 * The specification of a coordinate system and face winding order.
 */

@Value.Immutable
@SMFImmutableStyleType
public interface SMFCoordinateSystemType
{
  /**
   * @return The coordinate system axes
   */

  @Value.Parameter
  CAxisSystem axes();

  /**
   * @return The winding order for triangles
   */

  @Value.Parameter
  SMFFaceWindingOrder windingOrder();

  /**
   * @return A humanly-readable string such as "+x +y -z counter-clockwise"
   */

  @Value.Lazy
  default String toHumanString()
  {
    return String.format(
      "%s %s %s %s",
      this.axes().right().axisSigned(),
      this.axes().up().axisSigned(),
      this.axes().forward().axisSigned(),
      this.windingOrder().toName());
  }
}
