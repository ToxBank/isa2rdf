package net.toxbank.isa;

/**
 * Node in an {@link Study}
 * @author nina
 *
 */
public class NodeStudy  extends ANode<TemplateStudy, TemplateRowStudy>{

	protected NodeStudy(String uri, TemplateRowStudy template) throws Exception {
		super(uri, template);
	}

}
