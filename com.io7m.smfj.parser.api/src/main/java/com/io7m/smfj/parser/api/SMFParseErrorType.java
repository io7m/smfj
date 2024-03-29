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

package com.io7m.smfj.parser.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFErrorType;
import java.net.URI;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The type of parse errors.
 */

@Value.Immutable
@ImmutablesStyleType
public interface SMFParseErrorType extends SMFErrorType
{
  /**
   * @return Lexical information
   */

  @Value.Parameter
  LexicalPosition<URI> lexical();

  @Override
  @Value.Parameter
  String message();

  @Override
  @Value.Parameter
  Optional<Exception> exception();

  @Override
  @Value.Lazy
  default String fullMessage()
  {
    final LexicalPosition<URI> lex = this.lexical();

    final StringBuilder sb = new StringBuilder(128);
    if (lex.file().isPresent()) {
      final URI file = lex.file().get();
      sb.append(file);
      sb.append(":");
    }
    sb.append(lex.line());
    sb.append(":");
    sb.append(lex.column());
    sb.append(": ");
    sb.append(this.message());

    if (this.exception().isPresent()) {
      final Exception ex = this.exception().get();
      sb.append(" (");
      sb.append(ex.getClass().getCanonicalName());
      sb.append(": ");
      sb.append(ex.getMessage());
      sb.append(")");
    }

    return sb.toString();
  }
}
