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

package com.io7m.smfj.validation.api;

import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFImmutableStyleType;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import javaslang.collection.SortedMap;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The type of schemas.
 */

@SMFImmutableStyleType
@JavaslangEncodingEnabled
@Value.Immutable
public interface SMFSchemaType
{
  /**
   * @return The unique schema identifier
   */

  @Value.Parameter
  SMFSchemaIdentifier schemaIdentifier();

  /**
   * @return The required attributes
   */

  @Value.Parameter
  SortedMap<SMFAttributeName, SMFSchemaAttribute> requiredAttributes();

  /**
   * @return The required coordinate system, if any
   */

  @Value.Parameter
  Optional<SMFCoordinateSystem> requiredCoordinateSystem();

  /**
   * @return {@code true} iff the mesh is allowed to contain attributes that are
   * not given in {@link #requiredAttributes()}
   */

  @Value.Parameter
  boolean allowExtraAttributes();
}