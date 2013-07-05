<a href='http://toxbank.net'><img src='http://toxbank.github.io/isa2rdf/images/toxbank_rgb-72.png' alt = 'toxbank' title='ToxBank'></a>

ISA-TAB to RDF conversion
================

##Build
````
   isa2rdf>mvn clean package

   [INFO]
   [INFO] ------------------------------------------------------------------------
   [INFO] Reactor Summary:
   [INFO] ------------------------------------------------------------------------
   [INFO] isa2rdf ............................................... SUCCESS [2.362s]
   [INFO] isa2rdf converter ..................................... SUCCESS [17.080s]
   [INFO] isa2rdf-cli ........................................... SUCCESS [2.051s]
   [INFO] ------------------------------------------------------------------------
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESSFUL
````   


##Download 

### [isa2rdf-0.0.6-SNAPSHOT.jar] (http://ambit.uni-plovdiv.bg:8083/nexus/index.html#nexus-search;gav~~isa2rdf-cli~0.0.6-SNAPSHOT~~)
   
   * Dependency upgraded to Apache [jena 2.10.1](http://jena.apache.org/download/index.html) (faster RDF input/output)
   * Multiple optimisations in RDF generation 

### [isa2rdf-0.0.5-SNAPSHOT.jar] (http://ambit.uni-plovdiv.bg:8083/nexus/index.html#nexus-search;gav~~isa2rdf-cli~0.0.5-SNAPSHOT~~)

   * The [configuration templates](/isa2rdf/isa2rdf-cli/src/main/resources/toxbank-config) synchronised with the latest ISACreator 1.7.0
   * Added new study template default fields 
   * New template for [qHTS](/isa2rdf/isa2rdf-cli/src/main/resources/toxbank-config/qHTS.xml) assays
   * Upgraded isa-tools [import_layer](https://github.com/ISA-tools/ISAvalidator-ISAconverter-BIImanager/tree/master/import_layer) dependency to 1.6
   * Fixed to handle multiple entries in the investigation page comments

### [isa2rdf-0.0.4.jar] (https://www.ideaconsult.net/downloads/ISAcreator.SEURAT/isa2rdf-0.0.4.jar)
   
   * Keywords namespace is now http://www.owl-ontologies.com/toxbank.owl/
   * Support for N-TRIPLE output (file extension .nt)
   * Expects 'Comment[Owner URI]' instead of 'Comment[Principal Investigator URI]' in i_*.txt as defined in [investigation.xml](https://github.com/ToxBank/isa2rdf/blob/master/isa2rdf/isa2rdf-cli/src/main/resources/toxbank-config/investigation.xml). 
This configuration is used in [ISACreator.SEURAT](https://github.com/ToxBank/toxbank-isa-plugin).
   * Added TG-GATES example
   * Added toxbank-config as used in ISACreator.SEURAT

   
### [isa2rdf-0.0.4-SNAPSHOT.jar] (https://www.ideaconsult.net/downloads/ISAcreator.SEURAT/isa2rdf-0.0.4-SNAPSHOT.jar)

   * Added option -t toxbank protocol service URI, to be able to use specific instance of Toxbank protocol user,organisation,project services.
   * The deleted by mistake ISA.java class is restored and added to the isa2rdf-converter package
   * ISA tools upgraded to import-layer 1.5 (since May 2012)
   * Support for investigation authors (since May 2012)

   
### [isa2rdf-0.0.3-SNAPSHOT.jar] (https://github.com/ToxBank/isa2rdf/downloads)  
   
   * Fix for investigaton keywords not serialized (capital K).
   * More junit tests
   
### [Maven repository ] (http://ambit.uni-plovdiv.bg:8083/nexus/index.html#nexus-search;gav~~isa2rdf-cli~~~)

##Use


```
>java -jar isa2rdf-0.0.4-SNAPSHOT.jar
ISA-TAB to RDF conversion
usage: org.isa2rdf.cli.IsaClient
 -d,--dir <dir>                                         Directory with
                                                        ISA-TAB files
 -h,--help                                              ISA2RDF client
 -o,--output <output file>                              Output file
                                                        .n3|.rdf|.nt
 -t,--toxbankuri <ToxBank protocol service root URI >   ToxBank protocol
                                                        service root URI 
                                                        e.g. http://toxbanktest1.opentox.org:8080/toxbank
                                                        or https://services.toxbank.net/toxbank 
 
```

Example:

```
>java -jar isa2rdf-0.0.4-SNAPSHOT.jar -d /home/myself/sa2rdf/BII-S-11 -o /home/myself/BII-S-11/isatab.rdf -t https://services.toxbank.net/toxbank
```

Example: without -o argument, will write RDF/N3 to console

```
>java -jar isa2rdf-0.0.1-SNAPSHOT.jar -d /home/myself/sa2rdf/BII-S-11 -t https://services.toxbank.net/toxbank

@prefix :        <http://onto.toxbank.net/isa/TEST/> .
@prefix dc:      <http://purl.org/dc/elements/1.1/> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix isa:     <http://onto.toxbank.net/isa/> .
@prefix owl:     <http://www.w3.org/2002/07/owl#> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix dcterms:  <http://purl.org/dc/terms/> .

:M13303
      a       isa:Material ;
      isa:hasAccessionID "BII-S-11:labelled_extract:a_FP004BA-DNACHIP-transcriptome.992-biotin-labeled" .

:MN4186
      a       isa:MaterialNode ;
      isa:hasAccessionID "BII-S-11:proc:a_FP004BA-DNACHIP-transcriptome.2.4.47:out:0" ;
      isa:hasMaterial :M16232 ;
      isa:hasStudy :S4854 .

:PA20601
      a       isa:MaterialProcessing ;
      isa:hasProtocol :P_10571 .

:MN10215
      a       isa:MaterialProcessing ;
      isa:hasAccessionID "BII-S-11:proc:a_FP004BA-SELDI-MS-proteome.4.6.847" ;
      isa:hasInputNode :MN826 ;
      isa:hasOutputNode :MN3760 ;
      isa:hasProtocolApplication
              :PA21039 ;
      isa:hasStudy :S4854 .

```
<output skipped>
