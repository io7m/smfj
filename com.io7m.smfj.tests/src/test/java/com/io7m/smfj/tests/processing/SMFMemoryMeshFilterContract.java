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

package com.io7m.smfj.tests.processing;

import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * The type of memory mesh filter contracts.
 */

public abstract class SMFMemoryMeshFilterContract
{
  protected FileSystem filesystem;

  @BeforeEach
  public final void setUp()
  {
    this.filesystem = SMFTestFilesystems.makeEmptyUnixFilesystem();
  }

  @AfterEach
  public final void tearDown()
    throws Exception
  {
    this.filesystem.close();
  }

  protected final SMFFilterCommandContext createContext()
  {
    final Path root = this.filesystem.getRootDirectories().iterator().next();
    return SMFFilterCommandContext.of(
      root.resolve("x/y/z"),
      root.resolve("x/y/z"));
  }
}
