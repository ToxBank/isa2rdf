package net.toxbank.isa;

import net.toxbank.isa2rdf.ISA;

import com.hp.hpl.jena.rdf.model.Property;


public class RowAssay extends ARow<TemplateAssay,TemplateRowAssay> {

	protected RowAssay(TemplateRowAssay template) throws Exception {
		this(null,template);
	}	
	protected RowAssay(String uri, TemplateRowAssay template) throws Exception {
		super(uri, template);
	}

	@Override
	protected Property getPartOfEntryProperty() {
		return  ISA.ISAObjectProperty.isPartOfAssayEntry.createProperty(getModel());
	}	
	protected NodeAssay addNode(String uri) throws Exception {
		NodeAssay row = new NodeAssay(uri,template);
		Property partOfEntry = ISA.ISAObjectProperty.isPartOfAssayEntry.createProperty(getModel());
		getModel().add(row.getResource(),partOfEntry,resource);
		return row;
	}
	
}
