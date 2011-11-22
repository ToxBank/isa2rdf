package net.toxbank.isa;

import net.toxbank.isa2rdf.ColumnHeader;
import net.toxbank.isa2rdf.ISA;

public class TemplateNodeStudy extends TemplateNode<TemplateRowStudy, TemplateStudy> {

	public TemplateNodeStudy(ColumnHeader header,String uri, TemplateRowStudy collection)
			throws Exception {
		super(header,uri, collection,ISA.ISAClass.StudyNode);
	}

}
