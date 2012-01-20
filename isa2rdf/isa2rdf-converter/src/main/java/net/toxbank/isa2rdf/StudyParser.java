package net.toxbank.isa2rdf;

import java.io.Reader;

import net.toxbank.isa.AStudy;
import net.toxbank.isa.ColumnHeader;
import net.toxbank.isa.RowStudy;
import net.toxbank.isa.TemplateStudy;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class StudyParser extends TabsParser<RowStudy> {
	private TemplateStudy ts;
	private AStudy study;
	
	public StudyParser(String prefixDir,String name, Reader in, OntModel model) {
		super(in);
		ts = new TemplateStudy(prefixDir,name,model);
	}

	@Override
	protected ColumnHeader<Resource>[] readHeader() throws Exception {
		ColumnHeader<Resource>[] h = super.readHeader();
		if (h!=null) {
			ts.parseHeader(h, String.format("%s/Entry",ts.getResource().getURI()));
			study = new AStudy(String.format("%s/Study",ts.getResource().getURI()),ts);
		}
		return h;
	}
	@Override
	protected RowStudy transform(String[] tabs) throws Exception {
		String uri = String.format("%s/R%d", study.getResource().getURI(),count);
		return ts.parse(study,header, tabs,uri);
	}

}
