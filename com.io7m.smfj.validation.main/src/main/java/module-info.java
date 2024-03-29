/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.smfj.validation.api.SMFSchemaParserProviderType;
import com.io7m.smfj.validation.api.SMFSchemaSerializerProviderType;
import com.io7m.smfj.validation.main.SMFSchemaParserProvider;
import com.io7m.smfj.validation.main.SMFSchemaSerializerProvider;

/**
 * Main validation implementation.
 */

module com.io7m.smfj.validation.main
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;
  requires static org.osgi.service.component.annotations;

  requires com.io7m.jcoords.core;
  requires com.io7m.jlexing.core;
  requires com.io7m.junreachable.core;
  requires com.io7m.smfj.core;
  requires com.io7m.smfj.parser.api;
  requires com.io7m.smfj.validation.api;
  requires org.apache.commons.io;
  requires com.io7m.smfj.format.text;

  provides SMFSchemaParserProviderType with SMFSchemaParserProvider;
  provides SMFSchemaSerializerProviderType with SMFSchemaSerializerProvider;

  exports com.io7m.smfj.validation.main;
}