/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.yegor256.antlr2ebnf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link GenerateMojo}.
 *
 * @since 0.0.1
 */
final class GenerateMojoTest {
    @Test
    void generatesEbnf(@TempDir final Path temp) throws Exception {
        final Path src = temp.resolve("a/b/c/Simple.g4");
        final Path dir = src.getParent();
        dir.toFile().mkdirs();
        Files.write(
            src,
            String.join(
                System.lineSeparator(),
                "grammar Simple;",
                "program: alpha | beta;",
                "alpha: BOOL;",
                "beta: 'test';",
                "BOOL: 'TRUE' | 'FALSE';"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final GenerateMojo mojo = new GenerateMojo();
        mojo.convertDir = new File("target/convert");
        mojo.sourceDir = temp.toFile();
        mojo.include = "**/*.g4";
        mojo.targetDir = temp.toFile();
        mojo.pdflatex = "pdflatex";
        mojo.latexDir = temp.resolve("latex-dir").toFile();
        mojo.execute();
        final Path target = temp.resolve("a/b/c/Simple.txt");
        MatcherAssert.assertThat(
            target.toFile().exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new String(Files.readAllBytes(target), StandardCharsets.UTF_8),
            Matchers.allOf(
                Matchers.containsString("<bool> := \"TRUE\" | \"FALSE\" \\\\"),
                Matchers.containsString("<alpha> := <bool> \\\\")
            )
        );
        final Path pdf = temp.resolve("a/b/c/Simple.pdf");
        MatcherAssert.assertThat(
            pdf.toFile().exists(),
            Matchers.is(true)
        );
    }

    @Test
    void generatesEbnfFitToPage(@TempDir final Path temp) throws Exception {
        final Path src = temp.resolve("Bar.g4");
        final Path dir = src.getParent();
        dir.toFile().mkdirs();
        Files.write(
            src,
            String.join(
                System.lineSeparator(),
                "grammar Bar;",
                "program: alpha | beta | gamma;",
                "alpha: INT;",
                "beta: 'bar';",
                "INT: [0-9]+;"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final GenerateMojo mojo = new GenerateMojo();
        mojo.convertDir = new File("target/convert");
        mojo.sourceDir = temp.toFile();
        mojo.include = "**/*.g4";
        mojo.targetDir = temp.toFile();
        mojo.pdflatex = "pdflatex";
        mojo.latexDir = temp.resolve("latex-dir").toFile();
        mojo.fitToPage = true;
        mojo.execute();
        final Path pdf = temp.resolve("Bar.pdf");
        MatcherAssert.assertThat(
            pdf.toFile().exists(),
            Matchers.is(true)
        );
    }

    @Test
    void skipsLatex(@TempDir final Path temp) throws Exception {
        final Path src = temp.resolve("Foo.g4");
        final Path dir = src.getParent();
        dir.toFile().mkdirs();
        Files.write(
            src,
            String.join(
                System.lineSeparator(),
                "grammar Foo;",
                "foo: 'HELLO';"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final GenerateMojo mojo = new GenerateMojo();
        mojo.convertDir = new File("target/convert");
        mojo.sourceDir = temp.toFile();
        mojo.include = "**/*.g4";
        mojo.targetDir = temp.toFile();
        mojo.pdflatex = "/wrong-path-should-not-be-used";
        mojo.skipLatex = true;
        mojo.execute();
        final Path target = temp.resolve("Foo.txt");
        MatcherAssert.assertThat(
            target.toFile().exists(),
            Matchers.is(true)
        );
        final Path pdf = temp.resolve("Foo.pdf");
        MatcherAssert.assertThat(
            pdf.toFile().exists(),
            Matchers.is(false)
        );
    }

    @Test
    void failsIfCovertIsAbsent(@TempDir final Path temp) throws IOException {
        final GenerateMojo mojo = new GenerateMojo();
        mojo.sourceDir = temp.toFile();
        Files.write(
            temp.resolve("foo.g4"),
            "some wrong grammar here".getBytes(StandardCharsets.UTF_8)
        );
        mojo.convertDir = new File("/this-path-is-absent");
        Assertions.assertThrows(
            MojoFailureException.class,
            () -> mojo.execute()
        );
    }

    @Test
    void skipsExecution() throws Exception {
        final GenerateMojo mojo = new GenerateMojo();
        mojo.skip = true;
        mojo.execute();
    }

    @Test
    void failsIfSourceDirIsAbsent() {
        final GenerateMojo mojo = new GenerateMojo();
        mojo.sourceDir = new File("/file-is-absent");
        Assertions.assertThrows(
            MojoExecutionException.class,
            () -> mojo.execute()
        );
    }
}
