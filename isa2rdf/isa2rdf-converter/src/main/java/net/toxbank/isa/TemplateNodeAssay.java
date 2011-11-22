package net.toxbank.isa;

import net.toxbank.isa2rdf.ColumnHeader;

public class TemplateNodeAssay extends TemplateNode<TemplateRowAssay, TemplateAssay> {

	public TemplateNodeAssay(ColumnHeader header,String uri, TemplateRowAssay collection)
			throws Exception {
		super(header,uri, collection,ISAClass.AssayNode);
	}

}
