<?xml version="1.0" encoding="UTF-8"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2023-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
