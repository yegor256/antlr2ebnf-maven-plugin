[![Maven Central](https://img.shields.io/maven-central/v/com.yegor256/antlr2ebnf-maven-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/com.yegor256/antlr2ebnf-maven-plugin)
[![Javadoc](http://www.javadoc.io/badge/com.yegor256/antlr2ebnf-maven-plugin.svg)](http://www.javadoc.io/doc/com.yegor256/antlr2ebnf-maven-plugin)

This Maven plugin generates EBNF grammar from ANTLR grammar files and then
renders it as PDF.
The plugin expects you to have ANTLR-to-XML converter made by Gunther Rademacher.

Just add it to `pom.xml`:

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>com.yegor256</groupId>
        <artifactId>antlr2ebnf-maven-plugin</artifactId>
        <version>0.0.0</version>
      </plugin>
    </plugins>
  </build>
</project>
```

If your ANTLR4 grammar file is in `src/main/antlr4/Sample.g4`,
the PDF will be generated in `target/ebnf/Sample.txt` and.

If you need PNG and SVG, use these tools:

```bash
$ pdfcrop --margins '10 10 10 10' Sample.pdf crop.pdf
$ pdf2svg crop.pdf Sample.svg
$ convert -density 300 -quality 100 -transparent white -colorspace RGB crop.pdf Sample.png
```
