package net.toxbank.isa2rdf;

import java.io.Reader;

import net.toxbank.isa.ISAClass;

public class ParserAssay extends EntryParser {

	public ParserAssay(Reader in, String prefix, String prefixURI, ISAObject model) throws Exception {
		super(in,prefix,prefixURI,model);
	}
	protected ISAClass getEntryClass() {
		return ISAClass.Assay;
	}
	@Override
	protected ISAClass getNodeClass() {
		return ISAClass.AssayNode;
	}
}
