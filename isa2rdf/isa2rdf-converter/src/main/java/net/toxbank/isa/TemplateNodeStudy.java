package net.toxbank.isa;


public class TemplateNodeStudy extends TemplateNode<TemplateRowStudy, TemplateStudy> {

	public TemplateNodeStudy(ColumnHeader header,String uri, TemplateRowStudy collection)
			throws Exception {
		super(header,uri, collection,ISAClass.StudyNode);
	}

}
