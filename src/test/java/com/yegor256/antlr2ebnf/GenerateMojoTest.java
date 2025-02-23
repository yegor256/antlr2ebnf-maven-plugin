/*
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.antlr2ebnf;

import com.yegor256.farea.Farea;
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
    void generatesEbnfViaRealMaven(@TempDir final Path temp) throws Exception {
        new Farea(temp).together(
            f -> {
                f.files().file("src/main/antlr4/Program.g4").write(
                    String.join(
                        System.lineSeparator(),
                        "grammar Program;",
                        "program: ONE | TWO;",
                        "ONE: '1';",
                        "TWO: '2';"
                    ).getBytes(StandardCharsets.UTF_8)
                );
                f.build()
                    .plugins()
                    .appendItself()
                    .execution()
                    .goals("generate")
                    .phase("compile")
                    .configuration()
                    .set("convertDir", new File("target/convert").getAbsolutePath());
                f.exec("compile");
                MatcherAssert.assertThat(
                    "translates to PDF as expected",
                    temp.resolve("target/ebnf/Program.pdf").toFile().exists(),
                    Matchers.is(true)
                );
            }
        );
    }

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
                "program: alpha | zeta | gamma | delta | sigma | lambda;",
                "alpha: BOOL | BAR;",
                "beta: 'test';",
                "BAR: '\\n';",
                "BOOL: 'TRUE' | 'FALSE';"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final GenerateMojo mojo = new GenerateMojo();
        mojo.convertDir = new File("target/convert");
        mojo.sourceDir = temp.toFile();
        mojo.include = "**/*.g4";
        mojo.margin = 16;
        mojo.targetDir = temp.toFile();
        mojo.pdflatex = "pdflatex";
        mojo.specials = "bar,boom,hello";
        mojo.latexDir = temp.resolve("latex-dir").toFile();
        mojo.execute();
        final Path target = temp.resolve("a/b/c/Simple.txt");
        MatcherAssert.assertThat(
            "the file is there",
            target.toFile().exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            "all terms are there",
            new String(Files.readAllBytes(target), StandardCharsets.UTF_8),
            Matchers.allOf(
                Matchers.containsString("<bool> := \"TRUE\" | \"FALSE\" \\\\"),
                Matchers.containsString("<alpha> := <bool> | 'BAR' \\\\"),
                Matchers.containsString("<program> := <alpha> | <zeta> | <gamma> \\\\"),
                Matchers.containsString("|| <delta> | <sigma> | <lambda> \\\\")
            )
        );
        final Path pdf = temp.resolve("a/b/c/Simple.pdf");
        MatcherAssert.assertThat(
            "this file is also there",
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
            "the PDF has been generated",
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
            "the text file is there",
            target.toFile().exists(),
            Matchers.is(true)
        );
        final Path pdf = temp.resolve("Foo.pdf");
        MatcherAssert.assertThat(
            "the PDF is also there",
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
