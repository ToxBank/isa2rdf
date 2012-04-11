CHANGELOG

isa2rdf 0.0.2

-Fixed NPE (appears when investigation relese date is empty)

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
    
      
### Examples 

####Example N3


:I2213
      a       isa:Investigation ;
      isa:hasAccessionID "BII-I-1" ;
      isa:hasOwner :ISA_3977 , :ISA_3975 , :ISA_3976 ;
      isa:hasStudy :S2212 , :S2211 ;
      dcterms:abstract """Background
Cell growth underlies many key cellular and developmental processes, .......""" ;
      dcterms:created "29 Apr 2007 21:00:00 GMT" ;
      dcterms:issued "29 Apr 2007 21:00:00 GMT" ;
      dcterms:title "Growth control of the eukaryote cell: a systems biology study in yeast" .

      
[]    a       tb:Organization ;
      tb:hasMember :ISA_3977 , :ISA_3975 , :ISA_3981 , :ISA_3978 , :ISA_3982 , :ISA_3980 , :ISA_3979 , :ISA_3976 , :ISA_2214 ;
      dcterms:title "Faculty of Life Sciences, Michael Smith Building, University of Manchester"^^xsd:string .
      
:ISA_3977
      a       foaf:Person , isa:Contact , tb:User ;
      foaf:family_name "Leo"^^xsd:string ;
      foaf:givenname "Zeef"^^xsd:string .
            
:ISA_3975
      a       foaf:Person , isa:Contact , tb:User ;
      foaf:family_name "Oliver"^^xsd:string ;
      foaf:givenname "Stephen"^^xsd:string .
      
:S2212
      a       isa:Study ;
      isa:hasAccessionID "BII-S-1" ;
      isa:hasAssay :A13 , :A48 , :A9 , :A168 , :A74 , :A188 , :A178 , :A116  ;
      isa:hasOwner :ISA_3977 , :ISA_3980 , :ISA_3979 ;
      isa:hasProtocol :P_2224 , :P_2225 , :P_2216 , :P_2222 , :P_2217 , :P_2218 , :P_2220 ;
      dcterms:created "29 Apr 2007 21:00:00 GMT" ;
      dcterms:description "We wished to study the impact of growth rate ....." ;
      dcterms:title "Study of the impact of changes in flux on the transcriptome, proteome, endometabolome and ....." .
      
:P_2218
      a       isa:Protocol ;
      rdfs:label "biotin labeling"^^xsd:string ;
      isa:hasAccessionID "BII-S-1\\biotin labeling" .
      
:P_2216
      a       isa:Protocol ;
      rdfs:label "EukGE-WS4"^^xsd:string ;
      isa:hasAccessionID "BII-S-1\\EukGE-WS4" .      
                        
Example RDF/XML

"
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
"
      