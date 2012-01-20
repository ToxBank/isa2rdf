package net.toxbank.isa;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A top class for ISA-TAB, holding Jena {@link Resource}
 * @author nina
 *
 * @param <T>
 */
public class AnyISAObject<T extends Resource>  {
	protected T resource;
	
	public T getResource() {
		return resource;
	}
	public OntModel getModel() {
		return (OntModel)resource.getModel();
	}
	public AnyISAObject(String uri,T resource) {
		this.resource = resource; 
	}
	public AnyISAObject(T resource) {
		this(null,resource);
	}
	/**
	 * Creates a subclass of the resource, if it is a class.
	 * It could be considered a template to create new objects (individuals)
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public OntClass createTemplate(String uri) throws Exception {
		if (resource instanceof OntClass) {
			OntModel model = ((OntModel)resource.getModel());
			OntClass template = uri==null?model.createClass():model.createClass(uri);
			model.add(template,RDFS.subClassOf,resource);
			return template;
		} else return null;
	}
	public Individual createInstance(String uri) throws Exception {
		if (resource instanceof OntClass) {
			OntModel model = ((OntModel)resource.getModel());
			Individual instance = uri==null?model.createIndividual(resource):model.createIndividual(uri,resource);
			return instance;
		} else return null;
	}	

	public void setLabel(String label) {
		resource.getModel().add(resource,RDFS.label,label);
	}
	public String getLabel() {
		return resource.getProperty(RDFS.label).getString();
	}
	@Override
	public String toString() {
		return String.format("[%s\t%s]",getClass().getName(),getLabel());
	}
}
