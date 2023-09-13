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
import com.jcabi.xml.XSLDocument;
import com.yegor256.Jaxec;
import com.yegor256.xsline.StXSL;
import com.yegor256.xsline.TrClasspath;
import com.yegor256.xsline.TrDefault;
import com.yegor256.xsline.TrJoined;
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
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
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
     * Source directory, with ".g4" files, either directly in it
     * or in sub-directories.
     */
    @Parameter(
        property = "antlr2ebnf.sourceDir",
        required = true,
        defaultValue = "${project.basedir}/src/main/antlr4"
    )
    public File sourceDir;

    /**
     * Glob mask for ".g4" files to find in the source directory.
     *
     * @since 0.0.2
     */
    @Parameter(
        property = "antlr2ebnf.include",
        defaultValue = "**/*.g4"
    )
    public String include;

    /**
     * Glob mask for ".g4" files to exclude in the source directory.
     *
     * @since 0.0.2
     */
    @Parameter(
        property = "antlr2ebnf.exclude"
    )
    public String exclude;

    /**
     * Target directory, where ".txt" and ".pdf" files must be generated,
     * preserving the same subdirectory hierarchy as in the source directory.
     */
    @Parameter(
        property = "antlr2ebnf.targetDir",
        required = true,
        defaultValue = "${project.build.directory}/ebnf"
    )
    public File targetDir;

    /**
     * The path to the directory with ".jar" files of the "convert" tool,
     * created by <a href="https://www.bottlecaps.de/convert/">Gunther Rademacher</a>.
     */
    @Parameter(
        property = "antlr2ebnf.convertDir",
        required = true,
        defaultValue = "${project.build.directory}/convert"
    )
    public File convertDir;

    /**
     * Fit the entire content into one PDF page.
     *
     * @since 0.0.5
     */
    @Parameter(
        property = "antlr2ebnf.fitToPage",
        required = true,
        defaultValue = "true"
    )
    public boolean fitToPage;

    /**
     * Comma-separated list of terms that must be converted to "specials",
     * like "EOL" or "TAB".
     *
     * <p>The formatting of specials is shorter and they don't show up
     * in the list of terms. This will make your final document more compact
     * and more readable.</p>
     *
     * @since 0.0.6
     */
    @Parameter(
        property = "antlr2ebnf.specials",
        required = true,
        defaultValue = "eol"
    )
    public String specials = "";

    /**
     * Do we need to skip the entire plugin execution?
     *
     * @since 0.0.2
     */
    @Parameter(
        property = "antlr2ebnf.skip",
        required = true,
        defaultValue = "false"
    )
    public boolean skip;

    /**
     * Do we need to skip the compilation of ".txt" files
     * into PDF files, through the "pdflatex" system?
     */
    @Parameter(
        property = "antlr2ebnf.skipLatex",
        required = true,
        defaultValue = "false"
    )
    public boolean skipLatex;

    /**
     * The directory where ".pdf" files will be compiled by pdflatex.
     *
     * <p>Before every next compilation all files and subdirectories in this
     * directory will be deleted.</p>
     *
     * @since 0.0.3
     */
    @Parameter(
        property = "antlr2ebnf.latexDir",
        required = true,
        defaultValue = "${project.build.directory}/ebnf-latex"
    )
    public File latexDir;

    /**
     * The path of the "pdflatex" executable binary.
     */
    @Parameter(
        property = "antlr2ebnf.pdflatex",
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
            for (final File src
                : FileUtils.getFiles(this.sourceDir, this.include, this.exclude)) {
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
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void toPdf(final Path ebnf) throws IOException {
        final Path dir = this.latexDir.toPath();
        if (ebnf.getParent().startsWith(dir)) {
            throw new IOException(
                String.format(
                    "The 'targetDir' directory '%s' is inside of the 'latexDir' directory '%s'",
                    ebnf.getParent(), dir
                )
            );
        }
        if (dir.toFile().mkdirs()) {
            Logger.debug(this, "The 'latexDir' directory created: '%s'", dir);
        }
        org.apache.commons.io.FileUtils.cleanDirectory(dir.toFile());
        Files.write(
            dir.resolve("ebnf.cls"),
            new IoCheckedText(new TextOf(new ResourceOf("com/yegor256/antlr2ebnf/ebnf.cls")))
                .asString()
                .getBytes(StandardCharsets.UTF_8)
        );
        String suffix = "";
        if (this.fitToPage) {
            suffix = "-fitToPage";
        }
        final String bnf = new String(Files.readAllBytes(ebnf), StandardCharsets.UTF_8);
        final String tex = new IoCheckedText(
            new TextOf(
                new ResourceOf(
                    String.format("com/yegor256/antlr2ebnf/ebnf%s.tex", suffix)
                )
            )
        ).asString().replace("EBNF", bnf).trim();
        Files.write(dir.resolve("article.tex"), tex.getBytes(StandardCharsets.UTF_8));
        Logger.debug(
            this, "LaTeX before processing (%d lines):%n%s",
            tex.split("\n").length, tex
        );
        Logger.debug(
            this,
            "The document class for LaTeX is here: https://github.com/yegor256/antlr2ebnf-maven-plugin/blob/master/src/main/resources/com/yegor256/antlr2ebnf/ebnf.cls"
        );
        final long start = System.currentTimeMillis();
        new Jaxec(
            this.pdflatex,
            "-interaction=errorstopmode",
            "-halt-on-error",
            "-no-shell-escape",
            "article"
        ).withHome(dir).exec();
        final File raw = dir.resolve("article.pdf").toFile();
        final File pdf = ebnf.getParent().resolve(
            ebnf.getFileName().toString().replace(".txt", ".pdf")
        ).toFile();
        if (!raw.renameTo(pdf)) {
            throw new IOException(
                String.format("Failed to move '%s' to '%s'", raw, pdf)
            );
        }
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            if (Logger.isInfoEnabled(this)) {
                Logger.info(
                    this, "PDF generated (%d pages) and saved to '%s' (%d bytes), in %[ms]s",
                    doc.getPages().getCount(),
                    pdf, pdf.length(), System.currentTimeMillis() - start
                );
            }
        }
    }

    /**
     * Convert to EBNF.
     * @param antlr The location of the ANTLR file
     * @return The EBNF as text
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private String toEbnf(final Path antlr) throws IOException {
        final List<String> jars = Stream.of(this.convert())
            .filter(file -> !file.isDirectory())
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
        final long start = System.currentTimeMillis();
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
            throw new IOException("The output of the 'convert' tool doesn't look like XML");
        }
        final XML xml = new XMLDocument(output);
        Logger.debug(this, "XML generated by the convert:%n%s", xml);
        final XML after = new Xsline(
            new TrLogged(
                new TrJoined<>(
                    new TrClasspath<>()
                        .with("/com/yegor256/antlr2ebnf/to-non-terminals.xsl")
                        .with("/com/yegor256/antlr2ebnf/catch-duplicates.xsl")
                        .back(),
                    new TrDefault<>(
                        new StXSL(
                            new XSLDocument(
                                GenerateMojo.class.getResource(
                                    "/com/yegor256/antlr2ebnf/to-ebnf.xsl"
                                )
                            ).with("specials", this.specials)
                        )
                    )
                ),
                this,
                Level.FINE
            )
        ).pass(xml);
        final String ebnf = after.xpath("/ebnf/text()").get(0).replaceAll(" +", " ").trim();
        if (Logger.isInfoEnabled(this)) {
            Logger.info(
                this,
                "EBNF generated (%d lines) by the 'convert' and XSLT transformations in %[ms]s",
                ebnf.split("\n").length, System.currentTimeMillis() - start
            );
        }
        return ebnf;
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
                    "The JAR of the 'convert' tool is not unpacked into '%s' (the directory is absent)",
                    this.convertDir
                )
            );
        }
        final File[] jars = this.convertDir.listFiles();
        if (jars == null) {
            throw new IOException(
                String.format(
                    "The directory of the 'convert' tool is wrong (can't list files in it): '%s'",
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
