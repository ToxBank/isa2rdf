package net.toxbank.isa;

import net.toxbank.isa2rdf.ISA;

import com.hp.hpl.jena.rdf.model.Property;

public class AnAssay extends ACollection<TemplateAssay> {

	protected AnAssay(TemplateAssay template) throws Exception {
		this(null,template);
	}	
	protected AnAssay(String uri, TemplateAssay template) throws Exception {
		super(uri, template);
	}
	/**
	 * 
	 */
	@Override
	public RowAssay addRow(String uri) throws Exception {
		RowAssay row = new RowAssay(uri,(TemplateRowAssay)template.rowTemplate);
		Property partOfCollection = ISA.ISAObjectProperty.isPartOfAssay.createProperty(getModel());
		getModel().add(row.getResource(),partOfCollection,resource);
	//	row.parse(row,uri, headers, tabs);
		return row;
	}	
}
