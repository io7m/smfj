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

package com.io7m.smfj.frontend;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositionType;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFTLineLexer;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolverType;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleType;
import com.io7m.smfj.processing.api.SMFFilterCommandParserType;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenient functions to parse sequences of filter commands from files.
 */

public final class SMFFilterCommandFile
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFFilterCommandFile.class);

  private SMFFilterCommandFile()
  {

  }

  /**
   * Parse a command file.
   *
   * @param path_opt The path for error reporting
   * @param stream   An input stream
   * @param resolver A filter command module resolver
   *
   * @return A sequence of filters, or a list of errors encountered during parsing
   *
   * @throws IOException On I/O errors
   */

  public static SMFPartialLogged<List<SMFMemoryMeshFilterType>>
  parseFromStream(
    final SMFFilterCommandModuleResolverType resolver,
    final Optional<URI> path_opt,
    final InputStream stream)
    throws IOException
  {
    Objects.requireNonNull(resolver, "Resolver");
    Objects.requireNonNull(path_opt, "Path");
    Objects.requireNonNull(stream, "Stream");

    final SMFTLineLexer lexer = new SMFTLineLexer();
    final Map<String, SMFFilterCommandModuleType> modules =
      resolver.available();

    final List<SMFWarningType> warnings = new ArrayList<>();
    final List<SMFErrorType> errors = new ArrayList<>();
    final List<SMFMemoryMeshFilterType> filters = new ArrayList<>();

    LexicalPosition<URI> position =
      LexicalPosition.of(1, 0, path_opt);

    try (BufferedReader reader =
           new BufferedReader(new InputStreamReader(
             stream,
             StandardCharsets.UTF_8))) {

      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        final List<String> text = lexer.lex(line);
        if (text.isEmpty()) {
          continue;
        }

        final String qualified = text.get(0);
        final String[] segments = qualified.split(":");
        if (segments.length == 2) {
          final String module_name = segments[0];
          final String command_name = segments[1];

          final var result =
            resolveCommand(
              modules,
              position,
              path_opt,
              module_name,
              command_name,
              text);

          errors.addAll(result.errors());
          warnings.addAll(result.warnings());

          if (result.isSucceeded()) {
            filters.add(result.get());
          }
        } else {
          errors.add(notFullyQualified(position, path_opt, qualified));
        }
        position = position.withLine(position.line() + 1).withColumn(0);
      }
    }

    if (errors.isEmpty()) {
      return SMFPartialLogged.succeeded(filters);
    }
    return SMFPartialLogged.failed(errors);
  }

  private static SMFPartialLogged<SMFMemoryMeshFilterType>
  resolveCommand(
    final Map<String, SMFFilterCommandModuleType> modules,
    final LexicalPosition<URI> position,
    final Optional<URI> path_opt,
    final String module_name,
    final String command_name,
    final List<String> text)
  {
    if (modules.containsKey(module_name)) {
      final SMFFilterCommandModuleType module = modules.get(module_name);

      if (module.parsers().containsKey(command_name)) {
        final SMFFilterCommandParserType parser =
          module.parsers().get(command_name);

        LOG.debug(
          "resolved {}:{} -> {}",
          module_name,
          command_name,
          parser);

        final var lineTail = new ArrayList<>(text);
        if (!lineTail.isEmpty()) {
          lineTail.remove(0);
        }

        return parser.parse(position.file(), position.line(), lineTail);
      }
      return SMFPartialLogged.failed(
        noSuchCommand(position, path_opt, module, command_name));
    }
    return SMFPartialLogged.failed(
      noSuchModule(position, path_opt, modules, module_name));
  }

  private static SMFParseError notFullyQualified(
    final LexicalPositionType<URI> position,
    final Optional<URI> path_opt,
    final String received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Expected a fully qualified command name.");
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return SMFParseError.of(
      LexicalPosition.of(
        position.line(), position.column(), path_opt),
      sb.toString(),
      Optional.empty());
  }

  private static SMFParseError noSuchCommand(
    final LexicalPositionType<URI> position,
    final Optional<URI> path_opt,
    final SMFFilterCommandModuleType module,
    final String command_name)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Unknown command.");
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(command_name);
    sb.append(System.lineSeparator());
    sb.append("  Module: ");
    sb.append(module.name());
    sb.append(System.lineSeparator());
    sb.append("  Available: ");
    sb.append(System.lineSeparator());
    for (final String available : module.parsers().keySet()) {
      sb.append("    ");
      sb.append(available);
      sb.append(System.lineSeparator());
    }
    return SMFParseError.of(
      LexicalPosition.of(position.line(), position.column(), path_opt),
      sb.toString(),
      Optional.empty());
  }

  private static SMFParseError noSuchModule(
    final LexicalPositionType<URI> position,
    final Optional<URI> path_opt,
    final Map<String, SMFFilterCommandModuleType> modules,
    final String module_name)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Unknown module.");
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(module_name);
    sb.append(System.lineSeparator());
    sb.append("  Available: ");
    sb.append(System.lineSeparator());
    for (final String available : modules.keySet()) {
      sb.append("    ");
      sb.append(available);
      sb.append(System.lineSeparator());
    }
    return SMFParseError.of(
      LexicalPosition.of(position.line(), position.column(), path_opt),
      sb.toString(),
      Optional.empty());
  }
}
