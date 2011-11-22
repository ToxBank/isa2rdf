package net.toxbank.isa;

import com.hp.hpl.jena.rdf.model.Property;

public class AStudy extends ACollection<TemplateStudy> {

	public AStudy(TemplateStudy template) throws Exception {
		this(null,template);
	}
	public AStudy(String uri, TemplateStudy template) throws Exception {
		super(uri, template);
	}
	
	/**
	 * Study row
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	@Override
	public RowStudy addRow(String uri) throws Exception {
		RowStudy row = new RowStudy(uri,(TemplateRowStudy)template.rowTemplate);
		Property partOfCollection = ISAObjectProperty.isPartOfStudy.createProperty(getModel());
		getModel().add(row.getResource(),partOfCollection,resource);
	//	row.parse(row,uri, headers, tabs);
		return row;
	}

	/**
	 * Assay
	 * @param uri
	 * @return
	 * @throws Exception
	 */

	public AnAssay addAssay(String uri, TemplateAssay assayTemplate) throws Exception {
		return addAssay(new AnAssay(uri,assayTemplate));
	}
	public AnAssay addAssay(AnAssay assay) throws Exception {
		Property p = ISAObjectProperty.hasAssay.createProperty(getModel());
		getModel().add(resource,p,assay.getResource());		
		return assay;
	}	

}
