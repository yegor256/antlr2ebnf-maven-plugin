<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright (c) 2023-2025 Yegor Bugayenko
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
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:g="http://www.w3.org/2001/03/XPath/grammar" id="to-non-terminals" version="2.0">
  <xsl:output method="xml" encoding="UTF-8"/>
  <xsl:template match="g:production">
    <xsl:copy>
      <xsl:attribute name="name">
        <xsl:value-of select="lower-case(@name)"/>
      </xsl:attribute>
      <xsl:apply-templates select="node()|@* except @name"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="g:ref">
    <xsl:copy>
      <xsl:attribute name="name">
        <xsl:value-of select="lower-case(@name)"/>
      </xsl:attribute>
      <xsl:apply-templates select="node()|@* except @name"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
