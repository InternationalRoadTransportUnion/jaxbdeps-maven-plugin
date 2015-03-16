<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="xsl">
    
    <xsl:output method="xml" />
    
    <xsl:template match="node()|@*">
        <xsl:copy>
	    <xsl:if test="local-name() = 'bindings' and starts-with(@scd, 'x-schema::')">
		<xsl:attribute name="if-exists">true</xsl:attribute>
	    </xsl:if>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>

