<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : gums.xsl
    Created on : June 22, 2005, 3:05 PM
    Author     : carcassi
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="text" />

        <xsl:template match="/|*|@*|text()|comment()">
                <xsl:apply-templates select="*|@*|text()|comment()"/>
        </xsl:template>

        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']">
<xsl:value-of select="@user"/><xsl:text>
</xsl:text><xsl:value-of select="@password"/><xsl:text>
</xsl:text><xsl:value-of select="substring-before(substring-after(@jdbcUrl, 'jdbc:mysql://'), '/GUMS_1_0')"/>
        </xsl:template>
</xsl:stylesheet>