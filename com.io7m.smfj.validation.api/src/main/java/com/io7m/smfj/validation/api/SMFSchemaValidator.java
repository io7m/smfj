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

package com.io7m.smfj.validation.api;

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Component;

import static com.io7m.smfj.validation.api.SMFSchemaAllowExtraAttributes.SMF_EXTRA_ATTRIBUTES_DISALLOWED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireTriangles.SMF_TRIANGLES_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireVertices.SMF_VERTICES_REQUIRED;

/**
 * The default implementation of the {@link SMFSchemaValidatorType} interface.
 */

@Component
public final class SMFSchemaValidator implements SMFSchemaValidatorType
{
  /**
   * Construct a validator.
   */

  public SMFSchemaValidator()
  {

  }

  private static SMFSchemaValidationError errorWrongSchemaID(
    final SMFSchemaIdentifier schema_id,
    final SMFSchemaIdentifier file_id)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append(
      "The mesh schema identifier does not match the identifier in the schema.");
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(schema_id.toHumanString());
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(file_id.toHumanString());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorWrongCoordinateSystem(
    final SMFCoordinateSystem expected,
    final SMFCoordinateSystem received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("The mesh has an unexpected coordinate system.");
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(expected.toHumanString());
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(received.toHumanString());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorExtraAttribute(
    final SMFAttributeName name)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append(
      "The mesh contains an extra attribute but the schema does not permit them.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorMissingAttribute(
    final SMFAttributeName name)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("A required attribute is missing.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static void checkComponentCount(
    final List<SMFErrorType> errors,
    final SMFAttributeName name,
    final SMFSchemaAttribute attr_schema,
    final SMFAttribute attr)
  {
    final OptionalInt req_count_opt = attr_schema.requiredComponentCount();
    if (req_count_opt.isPresent()) {
      final int req_count = req_count_opt.getAsInt();
      if (attr.componentCount() != req_count) {
        errors.add(errorWrongComponentCount(
          name, req_count, attr.componentCount()));
        return;
      }
    }
  }

  private static void checkComponentSize(
    final List<SMFErrorType> errors,
    final SMFAttributeName name,
    final SMFSchemaAttribute attr_schema,
    final SMFAttribute attr)
  {
    final OptionalInt req_size_opt = attr_schema.requiredComponentSize();
    if (req_size_opt.isPresent()) {
      final int req_size = req_size_opt.getAsInt();
      if (attr.componentSizeBits() != req_size) {
        errors.add(errorWrongComponentSize(
          name, req_size, attr.componentSizeBits()));
        return;
      }
    }
  }

  private static void checkComponentType(
    final List<SMFErrorType> errors,
    final SMFAttributeName name,
    final SMFSchemaAttribute attr_schema,
    final SMFAttribute attr)
  {
    final Optional<SMFComponentType> req_type_opt =
      attr_schema.requiredComponentType();
    if (req_type_opt.isPresent()) {
      final SMFComponentType req_type = req_type_opt.get();
      if (attr.componentType() != req_type) {
        errors.add(errorWrongComponentType(
          name, req_type, attr.componentType()));
        return;
      }
    }
  }

  private static SMFSchemaValidationError errorWrongComponentCount(
    final SMFAttributeName name,
    final int expected,
    final int received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Attribute component count is not the expected count.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    sb.append("  Expected:  ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorWrongComponentSize(
    final SMFAttributeName name,
    final int expected,
    final int received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Attribute component size is not the expected size.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    sb.append("  Expected:  ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorWrongComponentType(
    final SMFAttributeName name,
    final SMFComponentType expected,
    final SMFComponentType received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Attribute is not of the expected type.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    sb.append("  Expected:  ");
    sb.append(expected.getName());
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(received.getName());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorVerticesRequiredButEmpty()
  {
    return SMFSchemaValidationError.of(
      "The model contains no vertices, but a non-zero vertex count is required.",
      Optional.empty());
  }

  private static SMFSchemaValidationError errorTrianglesRequiredButEmpty()
  {
    return SMFSchemaValidationError.of(
      "The model contains no triangles, but a non-zero triangle count is required.",
      Optional.empty());
  }

  private static void checkVerticesAndTriangles(
    final SMFHeader header,
    final SMFSchema schema,
    final List<SMFErrorType> errors)
  {
    if (schema.requireTriangles() == SMF_TRIANGLES_REQUIRED) {
      if (header.triangles().triangleCount() == 0L) {
        errors.add(errorTrianglesRequiredButEmpty());
      }
    }

    if (schema.requireVertices() == SMF_VERTICES_REQUIRED) {
      if (header.vertexCount() == 0L) {
        errors.add(errorVerticesRequiredButEmpty());
      }
    }
  }

  private static void checkCoordinateSystem(
    final SMFHeader header,
    final SMFSchema schema,
    final List<SMFErrorType> errors)
  {
    final Optional<SMFCoordinateSystem> coords_opt =
      schema.requiredCoordinateSystem();
    if (coords_opt.isPresent()) {
      final SMFCoordinateSystem req_coords = coords_opt.get();
      if (!Objects.equals(req_coords, header.coordinateSystem())) {
        errors.add(errorWrongCoordinateSystem(
          req_coords,
          header.coordinateSystem()));
      }
    }
  }

  private static void checkAttributes(
    final SMFHeader header,
    final SMFSchema schema,
    final List<SMFErrorType> errors)
  {
    final Map<SMFAttributeName, SMFSchemaAttribute> optional_by_name =
      schema.optionalAttributes();
    final Map<SMFAttributeName, SMFSchemaAttribute> required_by_name =
      schema.requiredAttributes();
    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      header.attributesByName();

    for (final Map.Entry<SMFAttributeName, SMFAttribute> p : by_name.entrySet()) {
      final SMFAttributeName name = p.getKey();
      final SMFAttribute attribute = p.getValue();

      if (required_by_name.containsKey(name)) {
        final SMFSchemaAttribute attr_schema = required_by_name.get(name);
        checkComponentType(errors, name, attr_schema, attribute);
        checkComponentSize(errors, name, attr_schema, attribute);
        checkComponentCount(errors, name, attr_schema, attribute);
      } else if (optional_by_name.containsKey(name)) {
        final SMFSchemaAttribute attr_schema = optional_by_name.get(name);
        checkComponentType(errors, name, attr_schema, attribute);
        checkComponentSize(errors, name, attr_schema, attribute);
        checkComponentCount(errors, name, attr_schema, attribute);
      } else if (schema.allowExtraAttributes() == SMF_EXTRA_ATTRIBUTES_DISALLOWED) {
        errors.add(errorExtraAttribute(name));
      }
    }

    final var namesPresent = new TreeSet<>(by_name.keySet());
    final var namesMissing = new TreeSet<>(required_by_name.keySet());
    namesMissing.removeAll(namesPresent);

    errors.addAll(
      namesMissing.stream()
        .map(SMFSchemaValidator::errorMissingAttribute)
        .collect(Collectors.toList()));
  }

  @Override
  public SMFPartialLogged<SMFHeader> validate(
    final SMFHeader header,
    final SMFSchema schema)
  {
    Objects.requireNonNull(header, "Header");
    Objects.requireNonNull(schema, "Schema");

    final List<SMFErrorType> errors = new ArrayList<>();
    final Optional<SMFSchemaIdentifier> file_id_opt = header.schemaIdentifier();
    if (file_id_opt.isPresent()) {
      final SMFSchemaIdentifier file_id = file_id_opt.get();
      final SMFSchemaIdentifier schema_id = schema.schemaIdentifier();
      if (!Objects.equals(schema_id, file_id)) {
        errors.add(errorWrongSchemaID(schema_id, file_id));
      }
    }

    checkVerticesAndTriangles(header, schema, errors);
    checkAttributes(header, schema, errors);
    checkCoordinateSystem(header, schema, errors);

    if (errors.isEmpty()) {
      return SMFPartialLogged.succeeded(header);
    }

    return SMFPartialLogged.failed(errors);
  }
}
