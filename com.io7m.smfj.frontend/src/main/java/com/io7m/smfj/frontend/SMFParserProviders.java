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

import com.io7m.smfj.parser.api.SMFParserProviderType;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience functions to locate parser providers via {@link ServiceLoader}.
 */

public final class SMFParserProviders
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFParserProviders.class);
  }

  private SMFParserProviders()
  {

  }

  /**
   * Try to find a parser provider. If a format name is not specified, the
   * format is inferred from the suffix of the given file.
   *
   * @param format_opt An optional format name.
   * @param file       A file name
   *
   * @return A parser provider, if a suitable one is available
   */

  public static Optional<SMFParserProviderType> findParserProvider(
    final Optional<String> format_opt,
    final String file)
  {
    final ServiceLoader<SMFParserProviderType> loader =
      ServiceLoader.load(SMFParserProviderType.class);

    if (!format_opt.isPresent()) {
      LOG.debug("attempting to infer format from file suffix");
      final int index = file.lastIndexOf('.');
      if (index != -1) {
        final String suffix = file.substring(index + 1);

        final var providersWithFormat =
          loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.parserFormat().suffix().equals(suffix))
            .collect(Collectors.toList());

        return findBestProviderFromList(providersWithFormat);
      }

      LOG.error("File {} does not have a recognized suffix", file);
    } else {
      final String format = format_opt.get();
      LOG.debug("attempting to find provider for {}", format);

      final var providersWithFormat =
        loader.stream()
          .map(ServiceLoader.Provider::get)
          .filter(p -> p.parserFormat().name().equals(format))
          .collect(Collectors.toList());

      return findBestProviderFromList(providersWithFormat);
    }

    return Optional.empty();
  }

  private static Optional<SMFParserProviderType> findBestProviderFromList(
    final List<SMFParserProviderType> providersWithFormat)
  {
    if (providersWithFormat.isEmpty()) {
      LOG.error("no provider has the specified format");
      return Optional.empty();
    }

    SMFParserProviderType providerBest = providersWithFormat.get(0);
    for (final var providerNow : providersWithFormat) {
      final var versionBest =
        providerBest.parserSupportedVersions().last();
      final var versionNow =
        providerNow.parserSupportedVersions().last();
      if (versionNow.compareTo(versionBest) > 0) {
        providerBest = providerNow;
      }
    }

    LOG.debug("using provider: {}", providerBest);
    return Optional.of(providerBest);
  }
}
