package net.toxbank.isa;


public class TemplateNodeAssay extends TemplateNode<TemplateRowAssay, TemplateAssay> {

	public TemplateNodeAssay(ColumnHeader header,String uri, TemplateRowAssay collection)
			throws Exception {
		super(header,uri, collection,ISAClass.AssayNode);
	}

}
