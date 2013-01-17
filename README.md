ISA-TAB to RDF conversion

```Build:
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
```

Use: 
1) Download [0.0.3-SNAPSHOT] (https://github.com/ToxBank/isa2rdf/downloads)  or newer version  [isa2rdf-0.0.4-SNAPSHOT.jar] (https://www.ideaconsult.net/downloads/ISAcreator.SEURAT/isa2rdf-0.0.4-SNAPSHOT.jar)

2)

```
>java -jar isa2rdf-0.0.4-SNAPSHOT.jar
ISA-TAB to RDF conversion
usage: org.isa2rdf.cli.IsaClient
 -d,--dir <dir>                                         Directory with
                                                        ISA-TAB files
 -h,--help                                              ISA2RDF client
 -o,--output <output file>                              Output file
                                                        .n3|.rdf
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
