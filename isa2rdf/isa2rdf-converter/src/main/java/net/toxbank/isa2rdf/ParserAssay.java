package net.toxbank.isa2rdf;

import java.io.Reader;

import net.toxbank.isa2rdf.ISA.ISAClass;

import com.hp.hpl.jena.ontology.OntModel;

public class ParserAssay extends EntryParser {

	public ParserAssay(Reader in, String prefix, String prefixURI, OntModel model) {
		super(in,prefix,prefixURI,model);
	}
	protected ISA.ISAClass getEntryClass() {
		return ISAClass.Assay;
	}
	@Override
	protected ISAClass getNodeClass() {
		return ISAClass.AssayNode;
	}
}
