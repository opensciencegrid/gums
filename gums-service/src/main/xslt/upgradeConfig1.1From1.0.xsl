<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : gums.xsl
    Created on : June 22, 2005, 3:05 PM
    Author     : carcassi
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" />

        <xsl:template match="/|*|@*|text()|comment()">
            <xsl:copy>
                <xsl:apply-templates select="*|@*|text()|comment()"/>
            </xsl:copy>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@*">
                <xsl:attribute name="hibernate.connection.{name()}">
                        <xsl:value-of select="."/>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@user">
                <xsl:attribute name="hibernate.connection.username">
                        <xsl:value-of select="."/>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@jdbcDriver">
                <xsl:attribute name="hibernate.connection.driver_class">
                        <xsl:value-of select="."/>
                </xsl:attribute>
                <xsl:attribute name="hibernate.dialect">
                        <xsl:text>net.sf.hibernate.dialect.MySQLDialect</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="hibernate.c3p0.min_size">
                        <xsl:text>3</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="hibernate.c3p0.max_size">
                        <xsl:text>20</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="hibernate.c3p0.timeout">
                        <xsl:text>180</xsl:text>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@jdbcUrl">
                <xsl:attribute name="hibernate.connection.url"><xsl:value-of select="substring(., 1, string-length()-1)"/>1</xsl:attribute>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@user">
                <xsl:attribute name="hibernate.connection.username">
                        <xsl:value-of select="."/>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@className">
                <xsl:attribute name="className">
                        <xsl:text>gov.bnl.gums.hibernate.HibernatePersistenceFactory</xsl:text>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="persistenceFactory[@className='gov.bnl.gums.MySQLPersistenceFactory']/@name">
            <xsl:copy>
                <xsl:apply-templates select="*|@*|text()|comment()"/>
            </xsl:copy>
        </xsl:template>
        
        <xsl:template match="userGroup[@className='gov.bnl.gums.VOMSGroup'and@ignoreFQAN='true']/@ignoreFQAN">
                <xsl:attribute name="matchFQAN">
                        <xsl:text>ignore</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="acceptProxyWithoutFQAN">
                        <xsl:text>true</xsl:text>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="userGroup[@className='gov.bnl.gums.VOMSGroup'and@ignoreFQAN='false']/@ignoreFQAN">
                <xsl:attribute name="matchFQAN">
                        <xsl:text>exact</xsl:text>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="hostGroup[@className='gov.bnl.gums.WildcardHostGroup']/@className">
                <xsl:attribute name="className">
                        <xsl:text>gov.bnl.gums.CertificateHostGroup</xsl:text>
                </xsl:attribute>
        </xsl:template>
        
        <xsl:template match="hostGroup[@className='gov.bnl.gums.WildcardHostGroup']/@wildcard">
                <xsl:attribute name="cn">
                        <xsl:value-of select="."/>
                </xsl:attribute>
        </xsl:template>
</xsl:stylesheet>