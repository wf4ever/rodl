<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:oxds="http://vocab.ox.ac.uk/dataset/schema#">
	<xsl:output method="xml" encoding="utf-8" indent="yes" />
	<xsl:template match="/ | node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates select="node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates
				select="*[name() != 'ore:aggregates' and name() != 'dcterms:hasVersion' and name() != 'oxds:currentVersion' and name() != 'dcterms:modified' and name() != 'rdf:type']">
				<xsl:sort select="name()" />
			</xsl:apply-templates>
			<xsl:comment>
				RO SRS generated tags below
			</xsl:comment>
			<xsl:apply-templates select="ore:aggregates">
				<xsl:sort select="@rdf:resource" />
			</xsl:apply-templates>
			<xsl:apply-templates select="dcterms:hasVersion">
				<xsl:sort select="text()" />
			</xsl:apply-templates>
			<xsl:apply-templates select="oxds:currentVersion" />
			<xsl:apply-templates select="dcterms:modified" />
			<xsl:apply-templates select="rdf:type" />


		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>