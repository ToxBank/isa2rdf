package net.toxbank.isa2rdf;

import java.io.Reader;

import net.toxbank.isa2rdf.ISA.ISAClass;

import com.hp.hpl.jena.ontology.OntModel;

public class ParserStudy extends EntryParser {
	public ParserStudy(Reader in, String prefix, String prefixURI, ISAObject model) throws Exception {
		super(in,prefix,prefixURI,model);
	}
	protected ISA.ISAClass getEntryClass() {
		return ISAClass.Study;
	}
	@Override
	protected ISAClass getNodeClass() {
		return ISAClass.StudyNode;
	}
}
