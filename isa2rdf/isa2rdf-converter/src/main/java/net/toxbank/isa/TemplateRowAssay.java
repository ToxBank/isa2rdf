package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * An assay row
 * @author nina
 *
 */
public class TemplateRowAssay extends TemplateRow<TemplateAssay> {

	public TemplateRowAssay(String uri, TemplateAssay collection)  throws Exception  {
		super(uri, collection, ISAClass.AssayEntry);
	}
	public TemplateRowAssay(TemplateAssay collection)  throws Exception {
		this(null,collection);
	}
	protected TemplateRowAssay(String uri,OntClass resource) {
		super(uri,resource);
	}		
	/*
	public TemplateRowAssay createSubclass(String uri) throws Exception {
		OntModel model = ((OntModel)resource.getModel());
		OntClass subclass = uri==null?model.createClass():model.createClass(uri);
		model.add(subclass,RDFS.subClassOf,resource);
		return new TemplateRowAssay(uri,subclass);
	}
	*/
	
}
