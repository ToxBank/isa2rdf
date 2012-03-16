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
 
 * Consortium URI (??)
 
 * Author URI list (Investigation and Study contacts)
 
 * Organisation URI (Affiliations)
 
 * Version number //there is no version in ISA files
 
 * Date of submission  dcterms:created "29 Apr 2007 21:00:00 GMT" ; // should it be long (timestamp) instead ?
 
 * Last modified date   dcterms:issued "29 Apr 2007 21:00:00 GMT" ;  //Investigation Public Release Date - this is different than last modified
    
      
 

Example N3
`
:I2225
      a       isa:Investigation ;
      isa:hasAccessionID "BII-I-1" ;
      dcterms:title "Growth control of the eukaryote cell: a systems biology study in yeast" . 
      isa:hasStudy :S2 , :S1 ;
      dcterms:abstract """Background
Cell growth underlies many key cellular and developmental processes, yet a limited number of studies have been carried out on cell-growth regulation. Comprehensive studies at the transcriptional, proteomic and metabolic levels under defined controlled conditions are currently lacking.
Results....""" ;
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
Results......</dcterms:abstract>
  </isa:Investigation>
`
      