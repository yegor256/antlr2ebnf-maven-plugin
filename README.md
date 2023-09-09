[![mvn](https://github.com/yegor256/antlr2ebnf-maven-plugin/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/antlr2ebnf-maven-plugin/actions/workflows/mvn.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.yegor256/antlr2ebnf-maven-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/com.yegor256/antlr2ebnf-maven-plugin)
[![Javadoc](http://www.javadoc.io/badge/com.yegor256/antlr2ebnf-maven-plugin.svg)](http://www.javadoc.io/doc/com.yegor256/antlr2ebnf-maven-plugin)
[![codecov](https://codecov.io/gh/yegor256/antlr2ebnf-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/antlr2ebnf-maven-plugin)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/antlr2ebnf-maven-plugin)](https://hitsofcode.com/view/github/yegor256/antlr2ebnf-maven-plugin)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/yegor256/antlr2ebnf-maven-plugin/blob/master/LICENSE.txt)

This Maven plugin takes your 
[ANTLR4](https://github.com/antlr/antlr4) grammar `.g4` files 
and generates [EBNF](https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form)
in the format expected by the 
[naive-ebnf](https://ctan.org/pkg/naive-ebnf) LaTeX package.
Then, using `pdflatex` installed on your computer, 
the plugin renders the generated EBNF as a PDF document 
(you can skip that with the `skipLatex` configuration option).
Then, you can transform this PDF to SVG or PNG formats, 
using the tools explained below.

The plugin expects you to have ANTLR-to-XML converter made by 
[Gunther Rademacher](https://www.bottlecaps.de/convert/), in the `target/convert`
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
        <version>0.0.2</version>
        <executions>
          <execution>
            <phase>initialize</phase>
            <goals>
              <goal>generate</goal>
            </goals>
            <configuration>
              <skip>false</skip>
              <skipLatex>false</skipLatex>
              <sourceDir>src/main/antlr4</sourceDir>
              <targetDir>target/ebnf</targetDir>
              <convertDir>${project.build.directory}/convert</convertDir>
              <pdflatex>/opt/homebrew/bin/pdflatex</pdflatex>
            </configuration>
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
(Maven is still used, but no project is required, just a directory with `.g4` files):

```bash
$ mvn com.yegor256:antlr2ebnf-maven-plugin:generate \
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
$ pdfcrop --margins '10 10 10 10' Sample.pdf crop.pdf
$ pdf2svg crop.pdf Sample.svg
$ convert -density 300 -quality 100 -transparent white -colorspace RGB crop.pdf Sample.png
```

Should work. If it doesn't, submit a ticket, I will try to help.