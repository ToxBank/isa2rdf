package net.toxbank.isa;

/**
 * Node in an {@link Assay}
 * @author nina
 *
 */
public class NodeAssay extends ANode<TemplateAssay, TemplateRowAssay>{

	protected NodeAssay(String uri, TemplateRowAssay template) throws Exception {
		super(uri, template);
	}


}
