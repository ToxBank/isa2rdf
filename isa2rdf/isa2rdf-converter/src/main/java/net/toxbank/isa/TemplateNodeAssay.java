package net.toxbank.isa;

import net.toxbank.isa2rdf.ColumnHeader;
import net.toxbank.isa2rdf.ISA;

public class TemplateNodeAssay extends TemplateNode<TemplateRowAssay, TemplateAssay> {

	public TemplateNodeAssay(ColumnHeader header,String uri, TemplateRowAssay collection)
			throws Exception {
		super(header,uri, collection,ISA.ISAClass.AssayNode);
	}

}
