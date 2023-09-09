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

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.yegor256.Jaxec;
import com.yegor256.xsline.TrClasspath;
import com.yegor256.xsline.TrLogged;
import com.yegor256.xsline.Xsline;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.utils.io.FileUtils;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.IoCheckedText;
import org.cactoos.text.TextOf;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Generates EBNF.
 *
 * @since 0.0.1
 * @checkstyle VisibilityModifierCheck (500 lines)
 * @checkstyle MemberNameCheck (500 lines)
 */
@Mojo(
    name = "generate",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    threadSafe = true
)
public final class GenerateMojo extends AbstractMojo {

    /**
     * Source directory.
     */
    @Parameter(
        required = true,
        defaultValue = "${project.basedir}/src/main/antlr4"
    )
    public File sourceDir;

    /**
     * Target directory.
     */
    @Parameter(
        required = true,
        defaultValue = "${project.build.directory}/ebnf"
    )
    public File targetDir;

    /**
     * Convert location.
     */
    @Parameter(
        required = true,
        defaultValue = "${project.build.directory}/convert"
    )
    public File convertDir;

    /**
     * Do we need to skip the entire plugin execution?
     */
    @Parameter(
        required = true,
        defaultValue = "false"
    )
    public boolean skip;

    /**
     * Do we need to compile it into PDF, through the "pdflatex"?
     */
    @Parameter(
        required = true,
        defaultValue = "false"
    )
    public boolean skipLatex;

    /**
     * The binary for 'pdflatex'.
     */
    @Parameter(
        required = true,
        defaultValue = "pdflatex"
    )
    public String pdflatex;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            Logger.info(this, "Skipped because of the 'skip' configuration option");
            return;
        }
        if (!this.sourceDir.exists()) {
            throw new MojoExecutionException(
                String.format(
                    "The source directory doesn't exist: '%s'",
                    this.sourceDir
                )
            );
        }
        Logger.info(this, "Searching for .g4 files in '%s'", this.sourceDir);
        try {
            for (final File src : FileUtils.getFiles(this.sourceDir, "**/*.g4", "")) {
                this.each(src.toPath());
            }
        } catch (final IOException ex) {
            throw new MojoFailureException(ex);
        }
    }

    /**
     * Convert one ANTLR file.
     * @param antlr The path of it
     * @throws IOException If fails
     */
    private void each(final Path antlr) throws IOException {
        final String rel = antlr.toString().substring(this.sourceDir.toString().length() + 1);
        Logger.info(
            this, "Converting '%s' (%d lines)",
            rel, Files.readAllLines(antlr, StandardCharsets.UTF_8).size()
        );
        final String ebnf = this.toEbnf(antlr);
        final Path target = this.targetDir.toPath().resolve(rel.replace(".g4", ".txt"));
        target.getParent().toFile().mkdirs();
        Files.write(target, ebnf.getBytes(StandardCharsets.UTF_8));
        Logger.info(this, "EBNF saved into '%s'", target);
        Logger.debug(this, "EBNF content:%n%s", ebnf);
        if (this.skipLatex) {
            Logger.info(this, "PDF generation skipped due to 'skipLatex' flag");
        } else {
            this.toPdf(target);
        }
    }

    /**
     * Convert to PDF.
     * @param ebnf The location of the EBNF text
     * @throws IOException If fails
     */
    private void toPdf(final Path ebnf) throws IOException {
        final Path dir = ebnf.getParent();
        Files.write(
            dir.resolve("article.tex"),
            new IoCheckedText(new TextOf(new ResourceOf("com/yegor256/antlr2ebnf/ebnf.tex")))
                .asString()
                .replace("EBNF", new String(Files.readAllBytes(ebnf), StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8)
        );
        new Jaxec(
            this.pdflatex,
            "-interaction=errorstopmode",
            "-halt-on-error",
            "-no-shell-escape",
            "article"
        ).withHome(dir).exec();
        dir.resolve("article.pdf").toFile().renameTo(
            dir.resolve(ebnf.getFileName().toString().replace(".txt", ".pdf")).toFile()
        );
    }

    /**
     * Convert to EBNF.
     * @param antlr The location of ANTLR file
     * @return The EBNF as text
     * @throws IOException If fails
     */
    private String toEbnf(final Path antlr) throws IOException {
        final List<String> jars = Stream.of(this.convert())
            .filter(file -> !file.isDirectory())
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
        final String output = new Jaxec(
            "java",
            "-cp",
            String.join(File.pathSeparator, jars),
            "de.bottlecaps.convert.Convert",
            "-xml",
            antlr.toString()
        ).exec();
        if (!output.startsWith("<?xml")) {
            Logger.warn(this, "Stdout: %n%s", output);
        }
        Logger.debug(this, "XML generated by the convert:%n%s", output);
        final XML xml = new XMLDocument(output);
        final XML after = new Xsline(
            new TrLogged(
                new TrClasspath<>()
                    .with("/com/yegor256/antlr2ebnf/to-non-terminals.xsl")
                    .with("/com/yegor256/antlr2ebnf/catch-duplicates.xsl")
                    .with("/com/yegor256/antlr2ebnf/to-ebnf.xsl")
                    .back(),
                this,
                Level.FINE
            )
        ).pass(xml);
        return after.xpath("/ebnf/text()").get(0).replaceAll(" +", " ");
    }

    /**
     * Find all JAR files required by the converter.
     * @return The list of JAR files
     * @throws IOException If fails
     */
    private File[] convert() throws IOException {
        if (!this.convertDir.exists()) {
            throw new IOException(
                String.format(
                    "The JAR of the 'convert' tool is not unpacked into '%s'",
                    this.convertDir
                )
            );
        }
        final File[] jars = this.convertDir.listFiles();
        if (jars == null) {
            throw new IOException(
                String.format(
                    "The directory of the 'convert' tool is wrong: '%s'",
                    this.convertDir
                )
            );
        }
        Logger.info(
            this, "Found %d JARs of the 'convert' tool in '%s'",
            jars.length, this.convertDir
        );
        return jars;
    }
}
