package net.toxbank.isa;

/**
 * A node in a {@link TemplateStudy}
 * @author nina
 *
 */
public class TemplateNodeStudy extends TemplateNode<TemplateRowStudy, TemplateStudy> {

	public TemplateNodeStudy(ColumnHeader header,String uri, TemplateRowStudy collection)
			throws Exception {
		super(header,uri, collection,ISAClass.StudyNode);
	}

}
