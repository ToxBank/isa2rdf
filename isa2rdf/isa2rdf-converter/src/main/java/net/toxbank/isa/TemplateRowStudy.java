package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * A study row
 * @author nina
 *
 */
public class TemplateRowStudy extends TemplateRow<TemplateStudy> {

	protected TemplateRowStudy(String uri, TemplateStudy collection)  throws Exception  {
		super(uri, collection,ISAClass.StudyEntry);
	}
	protected TemplateRowStudy(TemplateStudy collection)  throws Exception  {
		this(null, collection);
	}
	protected TemplateRowStudy(String uri,OntClass resource) {
		super(uri,resource);
	}		
	/*
	public TemplateRowStudy createSubclass(String uri) throws Exception {
		OntModel model = ((OntModel)resource.getModel());
		OntClass subclass = uri==null?model.createClass():model.createClass(uri);
		model.add(subclass,RDFS.subClassOf,resource);
		return new TemplateRowStudy(uri,subclass);
	}
	
	*/
}
