package net.toxbank.isa;

import com.hp.hpl.jena.rdf.model.Property;


/**
 * A row in an {@link Study}
 * @author nina
 *
 */
public class RowStudy extends ARow<TemplateStudy,TemplateRowStudy> {

	protected RowStudy(TemplateRowStudy template) throws Exception {
		this(null,template);
	}	
	protected RowStudy(String uri, TemplateRowStudy template) throws Exception {
		super(uri, template);
	}
	

	@Override
	protected Property getPartOfEntryProperty() {
		return  ISAObjectProperty.isPartOfStudyEntry.createProperty(getModel());
	}	

	protected NodeStudy addNode(String uri) throws Exception {
		NodeStudy row = new NodeStudy(uri,template);
		Property partOfEntry = ISAObjectProperty.isPartOfStudyEntry.createProperty(getModel());
		getModel().add(row.getResource(),partOfEntry,resource);
		return row;
	}
	


}
