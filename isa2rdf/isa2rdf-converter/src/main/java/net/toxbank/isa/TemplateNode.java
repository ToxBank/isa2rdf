package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TemplateNode<T extends TemplateRow<CT>,CT extends TemplateCollection> extends AnyISAObject<OntClass> {

	public TemplateNode(ColumnHeader header,String uri,T collection)  throws Exception {
		this(header,uri,collection,ISAClass.ISANode);
	}
	protected TemplateNode(ColumnHeader header,String uri,T collection,ISAClass clazz) throws Exception {
		super(collection.getModel().createClass(uri));
		
		getModel().add(resource,RDFS.subClassOf,clazz.createOntClass(collection.getModel()));
		if (header!=null) header2properties(header);
	}
	protected void header2properties(ColumnHeader header) throws Exception {
		getModel().add(resource,RDFS.label,header.getTitle());
	}
	public TemplateNode(ColumnHeader header,T collection)  throws Exception  {
		this(header,null,collection);
	}
	protected TemplateNode(OntClass resource) {
		this(null,resource);
	}
	protected TemplateNode(String uri,OntClass resource) {
		super(uri,resource);
	}
	protected void setFirst(boolean value) {
		/*
		HasValueRestriction restriction = getModel().createHasValueRestriction(null, 
				ISA.ISADataProperty.isFirstNode.createProperty(getModel()),
				getModel().createTypedLiteral(value));
		getModel().add(getResource(),RDFS.subClassOf,restriction);
		*/
	}
	/*
	protected void setNextNode(TemplateNode nextNode) throws Exception {
		if (nextNode== null)  return;
	
			Property nextProperty = getModel().createObjectProperty(null,true);
			getModel().add(nextProperty,RDFS.subPropertyOf,ISA.ISAObjectProperty.hasNext.createProperty(getModel()));
			getModel().add(nextProperty,RDFS.domain,getResource());
			getModel().add(nextProperty,RDFS.range,nextNode.getResource());
		//model.createAllValuesFromRestriction(null, nextProperty, nextNode);
	}	
*/
	public Property addFactor( ColumnHeader header,String uri) throws Exception {
		Property properties = getObjectProperty(getModel(),uri,ISAObjectProperty.hasFactor.createProperty(getModel()),true);
		
		getModel().add(properties,RDFS.label,header.getTitle());
		getModel().add(properties,RDFS.domain,resource);
		getModel().add(properties,RDFS.range,ISAClass.Value.createOntClass(getModel()));
		return properties;
	}
	public Property addCharacteristics( ColumnHeader header,String uri) throws Exception {
		Property properties = getObjectProperty(getModel(),uri,ISAObjectProperty.hasCharacteristic.createProperty(getModel()),true);
		
		getModel().add(properties,RDFS.label,header.getTitle());
		getModel().add(properties,RDFS.domain,resource);
		getModel().add(properties,RDFS.range,ISAClass.Value.createOntClass(getModel()));
		return properties;
	}	
	public Property addComment( ColumnHeader header,String uri) throws Exception {
		Property properties = getObjectProperty(getModel(),uri,ISAObjectProperty.hasComment.createProperty(getModel()),true);
		
		getModel().add(properties,RDFS.label,header.getTitle());
		getModel().add(properties,RDFS.domain,resource);
		getModel().add(properties,RDFS.range,ISAClass.Value.createOntClass(getModel()));
		return properties;
	}		
	public Property addFile(ColumnHeader header,String uri) throws Exception {
		Property files = getObjectProperty(getModel(),uri,ISAObjectProperty.hasDataset.createProperty(getModel()),true);
		getModel().add(files,RDFS.label,header.getTitle());
		getModel().add(files,RDFS.domain,resource);

		return files;
	}
	private Property getObjectProperty(OntModel model, String uri, Property parent, boolean functional) {
		Property c = uri==null?null:model.getObjectProperty(uri);
		if (c==null) {
			c = model.createObjectProperty(uri,functional);
			if (parent!=null) 
				model.add(c,RDFS.subPropertyOf,parent);
		}
		return c;
	}
	private Property getDataProperty(OntModel model, String uri, Property parent, boolean functional) {
		Property c = uri==null?null:model.getObjectProperty(uri);
		if (c==null) {
			c = model.createDatatypeProperty(uri,functional);
			if (parent!=null) 
				model.add(c,RDFS.subPropertyOf,parent);

		}
		return c;
	}	
}
