<?xml version="1.0" encoding="UTF-8"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:g="http://www.w3.org/2001/03/XPath/grammar" id="catch-duplicates" version="2.0">
  <xsl:output method="xml" encoding="UTF-8"/>
  <xsl:template match="g:production">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
    <xsl:variable name="name" select="@name"/>
    <xsl:if test="count(//g:production[@name=$name]) &gt; 1">
      <xsl:message terminate="yes">
        <xsl:text>Duplicate production rule '</xsl:text>
        <xsl:value-of select="$name"/>
        <xsl:text>'</xsl:text>
      </xsl:message>
    </xsl:if>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
