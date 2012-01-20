package net.toxbank.isa.parser;

import java.io.Reader;

import net.toxbank.isa.AnAssay;
import net.toxbank.isa.ColumnHeader;
import net.toxbank.isa.RowAssay;
import net.toxbank.isa.TemplateAssay;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class AssayParser extends TabsParser<RowAssay> {
	private TemplateAssay ta;
	private AnAssay assay;
	
	public AssayParser(TemplateAssay templateAssay, Reader in) {
		super(in);
		this.ta = templateAssay;
	}
	public AssayParser(String prefixDir,String name, Reader in, OntModel model) {
		this(new TemplateAssay(prefixDir,name,model),in);
	}

	@Override
	protected ColumnHeader<Resource>[] readHeader() throws Exception {
		ColumnHeader<Resource>[] h = super.readHeader();
		if (h!=null) {
			ta.parseHeader(h, String.format("%s/Entry",ta.getResource().getURI()));	
			assay = new AnAssay(String.format("%s/Assay",ta.getResource().getURI()),ta);
		}
		return h;
	}
	@Override
	protected RowAssay transform(String[] tabs) throws Exception {
		String uri = String.format("%s/R%d", assay.getResource().getURI(),count);
		return ta.parse(assay, header, tabs,uri);
	}
	public AnAssay getAssay() {
		return assay;
	}
}
