# ANTLR4 to EBNF in PDF Converter (Maven Plugin)

[![mvn](https://github.com/yegor256/antlr2ebnf-maven-plugin/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/antlr2ebnf-maven-plugin/actions/workflows/mvn.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.yegor256/antlr2ebnf-maven-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/com.yegor256/antlr2ebnf-maven-plugin)
[![Javadoc](https://www.javadoc.io/badge/com.yegor256/antlr2ebnf-maven-plugin.svg)](https://www.javadoc.io/doc/com.yegor256/antlr2ebnf-maven-plugin)
[![codecov](https://codecov.io/gh/yegor256/antlr2ebnf-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/antlr2ebnf-maven-plugin)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/antlr2ebnf-maven-plugin)](https://hitsofcode.com/view/github/yegor256/antlr2ebnf-maven-plugin)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/yegor256/antlr2ebnf-maven-plugin/blob/master/LICENSE.txt)

This Maven plugin takes your
[ANTLR4](https://github.com/antlr/antlr4) grammar `.g4` files
and generates
[EBNF](https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form)
in the format expected by the
[naive-ebnf](https://ctan.org/pkg/naive-ebnf) LaTeX package.
Then, using `pdflatex` installed on your computer,
the plugin renders the generated EBNF as a PDF document
(you can skip that with the `skipLatex` configuration option).
Then, you can transform this PDF to SVG or PNG formats,
using the tools explained below.

The plugin expects you to have ANTLR-to-XML converter made by
[Gunther Rademacher](https://www.bottlecaps.de/convert/),
in the `target/convert`
directory (normally, there should be five `.jar` files).

Just add it to `pom.xml`:

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>com.yegor256</groupId>
        <artifactId>antlr2ebnf-maven-plugin</artifactId>
        <version>0.0.7</version>
        <executions>
          <execution>
            <phase>initialize</phase>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

If your ANTLR4 grammar file is in the `src/main/antlr4/foo/bar/Sample.g4` path,
the EBNF will be generated in the `target/ebnf/foo/bar/Sample.txt` path and
the PDF will be in the `target/ebnf/foo/bar/Sample.pdf` file.

You can also run this plugin in one line, without adding it to any `pom.xml`
(Maven is still used, but no project is required, just a directory
with `.g4` files):

```bash
mvn com.yegor256:antlr2ebnf-maven-plugin:generate \
  -Dantlr2ebnf.sourceDir=/tmp/antlr4 \
  -Dantlr2ebnf.targetDir=/tmp/ebnf \
  -Dantlr2ebnf.convertDir=/tmp/convert-jars
```

Then, if you need PNG and SVG, use
[pdfcrop](https://ctan.org/pkg/pdfcrop),
[pdf2svg](https://manpages.ubuntu.com/manpages/xenial/man1/pdf2svg.1.html),
and
[convert](https://imagemagick.org/script/convert.php):

```bash
pdfcrop --margins '10 10 10 10' Sample.pdf crop.pdf
pdf2svg crop.pdf Sample.svg
convert -density 300 -quality 100 -transparent white -colorspace RGB crop.pdf Sample.png
```

Should work. If it doesn't, submit a ticket, I will try to help.

See how this plugin generates PDF diagrams in
[objectionary/eo](https://github.com/objectionary/eo).

## Options

Here is the full list of options that you may use in the `<configuration>`
of the plugin:

* `skip` — disables the execution of the plugin
* `skipLatex` — skips PDF generation step, just generates the `.txt` file
* `sourceDir` — the directory with `.g4` files
* `targetDir` — the directory where `.txt` and `.pdf` files will be generated
* `convertDir` — the directory with `.jar` files of the "convert" tool
* `pdflatex` - the name of the `pdflatex` binary
* `specials` — the list of terms that will be converted to ENBF specials
* `latexDir` — the directory with temporary LaTeX files

More of them you can find in [`GenerateMojo.java`][mojo].

[mojo]: https://github.com/yegor256/antlr2ebnf-maven-plugin/blob/master/src/main/java/com/yegor256/antlr2ebnf/GenerateMojo.java
