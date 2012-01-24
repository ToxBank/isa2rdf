ISA-TAB to RDF conversion

Build:
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


Use 
isa2rdf>java -jar isa2rdf-converter\target\isa2rdf-0.0.1-SNAPSHOT.jar
ISAParser filename
Filename: An ISA-TAB investigation file or i_*.txt file or a directory with ISA-TAB file
