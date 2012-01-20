package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A template row
 * @author nina
 *
 * @param <T>
 */
public class TemplateRow<T extends TemplateCollection> extends AnyISAObject<OntClass> {

	public TemplateRow(String uri,T collection)  throws Exception {
		this(uri,collection,ISAClass.ISAEntry);
	}
	public TemplateRow(String uri,T collection,ISAClass clazz) throws Exception {
		super(collection.getModel().createClass(uri));
		getModel().add(resource,RDFS.subClassOf,clazz.createOntClass(collection.getModel()));
		
		//Property partOfCollection = ISA.ISAObjectProperty.isPartOfCollection.createProperty(getModel());
		//getModel().add(partOfCollection,RDFS.domain,resource);
		//getModel().add(partOfCollection,RDFS.range,collection.getResource());
		
	}
	public TemplateRow(T collection)  throws Exception  {
		this(null,collection);
	}
	protected TemplateRow(OntClass resource) {
		this(null,resource);
	}
	protected TemplateRow(String uri,OntClass resource) {
		super(uri,resource);
	}	
	
}
