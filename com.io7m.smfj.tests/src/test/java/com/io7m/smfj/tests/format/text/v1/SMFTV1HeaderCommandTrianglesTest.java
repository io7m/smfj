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

package com.io7m.smfj.tests.format.text.v1;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1HeaderCommandTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1HeaderCommandTrianglesTest
{
  private SMFParserEventsHeaderType events;
  private SMFTLineReaderType reader;
  private ArgumentCaptor<SMFErrorType> captor;

  @BeforeEach
  public void testSetup()
  {
    this.events = Mockito.mock(SMFParserEventsHeaderType.class);
    this.reader = Mockito.mock(SMFTLineReaderType.class);
    this.captor = ArgumentCaptor.forClass(SMFErrorType.class);

    Mockito.when(this.reader.position())
      .thenReturn(LexicalPosition.of(0, 0, Optional.empty()));
  }

  @Test
  public void testOK_0()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("triangles", "100", "32"));
    Assertions.assertEquals(SUCCESS, r);

    final SMFHeader result = header.build();
    Assertions.assertEquals(100L, result.triangles().triangleCount());
    Assertions.assertEquals(32L, result.triangles().triangleIndexSizeBits());
  }

  @Test
  public void testFailure_0()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(SMFTV1HeaderCommandTriangles.SYNTAX));
  }

  @Test
  public void testFailure_1()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("triangles", "z", "32"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(SMFTV1HeaderCommandTriangles.SYNTAX));
  }

  @Test
  public void testFailure_2()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("triangles", "100", "z"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(SMFTV1HeaderCommandTriangles.SYNTAX));
  }
}
