# RDF example

namespace: isa 'http://onto.toxbank.net/isa/'

## Investigation

 Class isa:Investigation

 * Title  dcterms:title "Growth control of the eukaryote cell: a systems biology study in yeast" .
 
 * Abstract dcterms:abstract
 
 * Keyword URI list (linked to keyword hierarchy)

 * Protocol URI list (linked to Protocol service)
 
 * File URL list (linked to the Investigation Service) - i.e. links for the user to download any of the specific ISA-Tab files
   (these might be just URI of the investigation/study/assays )
 
 * Consortium URI (linked to Project service)
 
 * Author URI list (linked to User service)
 
 * Organisation URI (linked to Organisation service)
 
 * Version number //there is no version in ISA files
 
 * Date of submission  dcterms:created "29 Apr 2007 21:00:00 GMT" ; // should it be long?
 
 * Last modified date   dcterms:issued "29 Apr 2007 21:00:00 GMT" ;  //Investigation Public Release Date - this is differnt than last modified
    
      
 

Example N3
`
:I2225
      a       isa:Investigation ;
      isa:hasAccessionID "BII-I-1" ;
      dcterms:title "Growth control of the eukaryote cell: a systems biology study in yeast" . 
      isa:hasStudy :S2 , :S1 ;
      dcterms:abstract """Background
Cell growth underlies many key cellular and developmental processes, yet a limited number of studies have been carried out on cell-growth regulation. Comprehensive studies at the transcriptional, proteomic and metabolic levels under defined controlled conditions are currently lacking.
Results
Metabolic control analysis is being exploited in a systems biology study of the eukaryotic cell. Using chemostat culture, we have measured the impact of changes in flux (growth rate) on the transcriptome, proteome, endometabolome and exometabolome of the yeast Saccharomyces cerevisiae. Each functional genomic level shows clear growth-rate-associated trends and discriminates between carbon-sufficient and carbon-limited conditions. Genes consistently and significantly upregulated with increasing growth rate are frequently essential and encode evolutionarily conserved proteins of known function that participate in many protein-protein interactions. In contrast, more unknown, and fewer essential, genes are downregulated with increasing growth rate; their protein products rarely interact with one another. A large proportion of yeast genes under positive growth-rate control share orthologs with other eukaryotes, including humans. Significantly, transcription of genes encoding components of the TOR complex (a major controller of eukaryotic cell growth) is not subject to growth-rate regulation. Moreover, integrative studies reveal the extent and importance of post-transcriptional control, patterns of control of metabolic fluxes at the level of enzyme synthesis, and the relevance of specific enzymatic reactions in the control of metabolic fluxes during cell growth.
Conclusion
This work constitutes a first comprehensive systems biology study on growth-rate control in the eukaryotic cell. The results have direct implications for advanced studies on cell growth, in vivo regulation of metabolic fluxes for comprehensive metabolic engineering, and for the design of genome-scale systems biology models of the eukaryotic cell.""" ;
      dcterms:created "29 Apr 2007 21:00:00 GMT" ;
      dcterms:issued "29 Apr 2007 21:00:00 GMT" ;
`      

Example RDF/XML

`
  <isa:Investigation rdf:about="I2225">
    <isa:hasAccessionID>BII-I-1</isa:hasAccessionID>
    <dcterms:title>Growth control of the eukaryote cell: a systems biology study in yeast</dcterms:title>    
    <isa:hasStudy rdf:resource="S1"/>
    <isa:hasStudy rdf:resource="S2"/>
    <dcterms:issued>29 Apr 2007 21:00:00 GMT</dcterms:issued>
    <dcterms:created>29 Apr 2007 21:00:00 GMT</dcterms:created>
    <dcterms:abstract>Background
Cell growth underlies many key cellular and developmental processes, yet a limited number of studies have been carried out on cell-growth regulation. Comprehensive studies at the transcriptional, proteomic and metabolic levels under defined controlled conditions are currently lacking.
Results
Metabolic control analysis is being exploited in a systems biology study of the eukaryotic cell. Using chemostat culture, we have measured the impact of changes in flux (growth rate) on the transcriptome, proteome, endometabolome and exometabolome of the yeast Saccharomyces cerevisiae. Each functional genomic level shows clear growth-rate-associated trends and discriminates between carbon-sufficient and carbon-limited conditions. Genes consistently and significantly upregulated with increasing growth rate are frequently essential and encode evolutionarily conserved proteins of known function that participate in many protein-protein interactions. In contrast, more unknown, and fewer essential, genes are downregulated with increasing growth rate; their protein products rarely interact with one another. A large proportion of yeast genes under positive growth-rate control share orthologs with other eukaryotes, including humans. Significantly, transcription of genes encoding components of the TOR complex (a major controller of eukaryotic cell growth) is not subject to growth-rate regulation. Moreover, integrative studies reveal the extent and importance of post-transcriptional control, patterns of control of metabolic fluxes at the level of enzyme synthesis, and the relevance of specific enzymatic reactions in the control of metabolic fluxes during cell growth.
Conclusion
This work constitutes a first comprehensive systems biology study on growth-rate control in the eukaryotic cell. The results have direct implications for advanced studies on cell growth, in vivo regulation of metabolic fluxes for comprehensive metabolic engineering, and for the design of genome-scale systems biology models of the eukaryotic cell.</dcterms:abstract>
  </isa:Investigation>
`
      