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

package com.io7m.smfj.parser.api;

/**
 * A receiver of parse events for mesh attribute data.
 */

public interface SMFParserEventsDataAttributeValuesType
  extends SMFParserEventsErrorType
{
  /**
   * A data value has been received.
   *
   * @param x The x value
   */

  void onDataAttributeValueIntegerSigned1(
    long x);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   */

  void onDataAttributeValueIntegerSigned2(
    long x,
    long y);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   */

  void onDataAttributeValueIntegerSigned3(
    long x,
    long y,
    long z);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   */

  void onDataAttributeValueIntegerSigned4(
    long x,
    long y,
    long z,
    long w);

  /**
   * A data value has been received.
   *
   * @param x The x value
   */

  void onDataAttributeValueIntegerUnsigned1(
    long x);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   */

  void onDataAttributeValueIntegerUnsigned2(
    long x,
    long y);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   */

  void onDataAttributeValueIntegerUnsigned3(
    long x,
    long y,
    long z);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   */

  void onDataAttributeValueIntegerUnsigned4(
    long x,
    long y,
    long z,
    long w);

  /**
   * A data value has been received.
   *
   * @param x The x value
   */

  void onDataAttributeValueFloat1(
    double x);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   */

  void onDataAttributeValueFloat2(
    double x,
    double y);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   */

  void onDataAttributeValueFloat3(
    double x,
    double y,
    double z);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   */

  void onDataAttributeValueFloat4(
    double x,
    double y,
    double z,
    double w);

  /**
   * Parsing of data for the attribute has finished.
   */

  void onDataAttributeValueFinish();
}
