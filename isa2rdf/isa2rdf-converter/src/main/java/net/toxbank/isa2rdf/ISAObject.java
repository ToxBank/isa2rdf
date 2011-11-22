package net.toxbank.isa2rdf;


import java.util.Hashtable;

import net.toxbank.isa2rdf.ISA.ISAClass;
import net.toxbank.isa2rdf.ISA.ISADataProperty;
import net.toxbank.isa2rdf.ISA.ISAObjectProperty;

import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ISAObject {
	protected String prefixURI;
	protected OntModel model;
	public OntModel getModel() {
		return model;
	}
	protected Hashtable<String, String>[] attributes = new Hashtable[ISA.ISAClass.values().length];
	
	
	public String getPrefixURI() {
		return prefixURI;
	}
	public void setPrefixURI(String prefixURI) {
		this.prefixURI = prefixURI;
	}
	public ISAObject(String prefix, String prefixURI) throws Exception {
		this(ISA.createModel(),prefix,prefixURI);
	}
	public ISAObject(OntModel model,String prefix, String prefixURI) {
		this.model = model;
		model.setNsPrefix("",ISA.NS);
		this.prefixURI = prefixURI;
		attributes = new Hashtable[ISA.ISAClass.values().length];
		initModel(prefix);
	}	
	
	private void initModel(String prefix) {
		model.setNsPrefix(prefix,String.format("%s/",prefixURI));
		
		//top objects, correspond to named nodes and protocols in ISA-TAB
		OntClass isaNodeClass = ISAClass.ISANode.createOntClass(model);
		OntClass namedNodeClass = ISAClass.NamedNode.createOntClass(model);
		OntClass protocolNodeClass = ISAClass.Protocol.createOntClass(model);
		model.add(namedNodeClass,RDFS.subClassOf,isaNodeClass);
		model.add(protocolNodeClass,RDFS.subClassOf,isaNodeClass);
		
		//File-level objects - study or assay file
		OntClass fileClass = ISA.ISAClass.ISACollection.createOntClass(model);
		//study file
		model.add(ISA.ISAClass.Study.createOntClass(model),RDFS.subClassOf,fileClass);
		//assay file
		model.add(ISA.ISAClass.Assay.createOntClass(model),RDFS.subClassOf,fileClass);
		
		//Row-level objects - a row of study or assay file
		OntClass rowClass = ISA.ISAClass.ISAEntry.createOntClass(model);
		
		//a row is part of a collection (study/assay)
		Property isPartOf = ISA.ISAObjectProperty.isPartOf.createProperty(model);
		model.add(isPartOf,RDF.type,OWL.TransitiveProperty); //an entry (row) is only part a of single collection

		//and nodes are part of the row
		Property isPartOfEntry = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		Property isPartOfCollection = ISA.ISAObjectProperty.isPartOfCollection.createProperty(model);
		model.add(isPartOfCollection,RDFS.subPropertyOf,isPartOf);
		model.add(isPartOfEntry,RDFS.subPropertyOf,isPartOf);
		//first node in a row
		Property isFirstNode = ISA.ISADataProperty.isFirstNode.createProperty(model);
		model.add(isFirstNode,RDFS.domain,namedNodeClass);
		

		//a node is part of a row
		model.add(isPartOfEntry,RDFS.domain,namedNodeClass);
		model.add(isPartOfEntry,RDFS.range,rowClass);

		//and we have study nodes and assay nodes
		model.add(ISAClass.StudyNode.createOntClass(model),RDFS.subClassOf,namedNodeClass);
		model.add(ISAClass.AssayNode.createOntClass(model),RDFS.subClassOf,namedNodeClass);

		//properties - factors, etc.
		model.add(ISAObjectProperty.hasFactor.createProperty(model),RDFS.domain,namedNodeClass);
		model.add(ISAObjectProperty.hasCharacteristic.createProperty(model),RDFS.domain,namedNodeClass);
		model.add(ISAObjectProperty.hasComment.createProperty(model),RDFS.domain,namedNodeClass);
		model.add(ISADataProperty.hasPerformer.createProperty(model),RDFS.domain,protocolNodeClass);
		model.add(ISADataProperty.hasDate.createProperty(model),RDFS.domain,protocolNodeClass);
		model.add(ISAObjectProperty.hasDataset.createProperty(model),RDFS.domain,ISAClass.AssayNode.createOntClass(model));
		model.add(ISAObjectProperty.hasParameter.createProperty(model),RDFS.domain,protocolNodeClass);

		OntClass valueClass = ISAClass.Value.createOntClass(model);
		model.add(ISAObjectProperty.hasFactor.createProperty(model),RDFS.range,valueClass);
		model.add(ISAObjectProperty.hasCharacteristic.createProperty(model),RDFS.range,valueClass);
		model.add(ISAObjectProperty.hasComment.createProperty(model),RDFS.range,valueClass);
		model.add(ISAObjectProperty.hasParameter.createProperty(model),RDFS.range,valueClass);
		
		//and values have units
		model.add(ISADataProperty.hasUnit.createProperty(model),RDFS.domain,valueClass);
		/**
    <owl:DatatypeProperty rdf:about="http://www.owl-ontologies.com/toxbank.owl#hasComment">
        <rdf:type rdf:resource="&owl;FunctionalProperty"/>
        <rdfs:domain rdf:resource="http://www.owl-ontologies.com/toxbank.owl#ISANode"/>
        <rdfs:range rdf:resource="&xsd;string"/>
    </owl:DatatypeProperty>
		 */
		
		//and we have some internal chained structure
		Property next = ISA.ISAObjectProperty.hasNext.createProperty(model);
		model.add(next,RDFS.domain,ISA.ISAClass.ISANode.createOntClass(model));
		model.add(next,RDFS.range,ISA.ISAClass.ISANode.createOntClass(model));
/*
SELECT *
WHERE { 

?f rdfs:subPropertyOf tb:hasNext.
?f rdfs:domain ?a.
?f rdfs:range ?b.
?i rdf:type ?a.
?i tb:isPartOfEntry <http://toxbank.net/assay/a_metabolome/R2>
}
order by ?a ?b

===

SELECT *
WHERE { 

?f rdfs:subPropertyOf tb:hasNext.
?f rdfs:domain ?a.
?f rdfs:range ?b.
?i1 rdf:type ?a.
?i2 rdf:type ?b.
?i1 tb:isPartOfEntry ?entry.
?i2 tb:isPartOfEntry ?entry.
}
order by ?entry ?a ?b
*/
	}
	
	protected String generateURI(OntModel model,String prefix,ISAClass classType,ColumnHeader header,String value) {
		return generateURI(model, prefix, classType, header, value,true);
	}
	protected String generateURI(OntModel model,String prefix,ISAClass classType,ColumnHeader header,String value,boolean addLabel) {
		if (attributes[classType.ordinal()]==null) attributes[classType.ordinal()] = new Hashtable<String, String>();
		String key = String.format("%s/%s/%s", prefix,header,value);
		String uri = attributes[classType.ordinal()].get(key);
		Individual node = null;
		if (uri==null) {
			uri = String.format("%s/%s",prefix,
					classType.getURI(attributes[classType.ordinal()].size()));						
			attributes[classType.ordinal()].put(key,uri);
			node = model.createIndividual(uri, classType.createOntClass(model));
			
		} else {
			node = model.getIndividual(uri);
		}
		if (addLabel)
			if (node != null) {
				if ("".equals(value)) {
					model.add(node,RDFS.label,header==null?"":header.getTitle());
				} else {
					model.add(node,RDFS.label,value);
					//model.add(node,RDFS.comment,header);
				}
				//System.out.println(String.format("%s\t%s\t%s\t%s",uri,header,value,key));
			}
		return uri;
	}
	/**
    <owl:Restriction>
        <owl:onProperty rdf:resource="http://www.owl-ontologies.com/toxbank.owl#hasNext"/>
        <owl:allValuesFrom rdf:resource="&study1;SampleName"/>
    </owl:Restriction>
		 */
	private void setNext(String uri, OntClass node,OntClass nextNode) {
		Property nextProperty = model.createObjectProperty(uri);
		model.add(nextProperty,RDFS.subPropertyOf,ISA.ISAObjectProperty.hasNext.createProperty(model));
		model.add(nextProperty,RDFS.domain,node);
		model.add(nextProperty,RDFS.range,nextNode);
		//model.createAllValuesFromRestriction(null, nextProperty, nextNode);
	}

	/**
    <rdfs:subClassOf>
    <owl:Restriction>
        <owl:onProperty rdf:resource="http://www.owl-ontologies.com/toxbank.owl#isFirstNode"/>
        <owl:hasValue rdf:datatype="&xsd;boolean">true</owl:hasValue>
    </owl:Restriction>
</rdfs:subClassOf>		
	 **/	
	private void setFirst(OntClass node, boolean value) {
		HasValueRestriction restriction = model.createHasValueRestriction(null, 
							ISA.ISADataProperty.isFirstNode.createProperty(model),
							model.createTypedLiteral(value));
		model.add(node,RDFS.subClassOf,restriction);
	}
	
	private OntClass getOntClass(OntModel model, String uri, OntClass parent) {
		OntClass c = model.getOntClass(uri);
		if (c==null) {
			c = model.createClass(uri);
			if (parent!=null) {
				model.add(c,RDFS.subClassOf,parent);
			}
		}
		return c;
	}

	private Property getObjectProperty(OntModel model, String uri, Property parent, ColumnHeader label, boolean functional) {
		Property c = model.getObjectProperty(uri);
		if (c==null) {
			c = model.createObjectProperty(uri,functional);
			if (parent!=null) 
				model.add(c,RDFS.subPropertyOf,parent);
			if (label !=null) {
				model.add(c,RDFS.label,label.getTitle());
			}
		}
		return c;
	}
	private Property getDataProperty(OntModel model, String uri, Property parent, ColumnHeader label,boolean functional) {
		Property c = model.getObjectProperty(uri);
		if (c==null) {
			c = model.createDatatypeProperty(uri,functional);
			if (parent!=null) 
				model.add(c,RDFS.subPropertyOf,parent);
			if (label !=null) {
				model.add(c,RDFS.label,label.getTitle());
			}
		}
		return c;
	}	
	public Property addFactor(OntClass node,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/F%s",prefixURI,i);
		Property properties = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasFactor.createProperty(model),header,true);
		model.add(properties,RDFS.label,header.getTitle());
		model.add(properties,RDFS.domain,node);
		model.add(properties,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
		return properties;
	}
	
	public Property addCharacteristic(OntClass node,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/CH%s",prefixURI,i);
		Property properties = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasCharacteristic.createProperty(model),header,true);
		model.add(properties,RDFS.label,header.getTitle());
		model.add(properties,RDFS.domain,node);
		model.add(properties,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
		return properties;
	}	
	public Property addComment(OntClass node,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/CM%s",prefixURI,i);
		Property property = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasComment.createProperty(model),header,false);
		model.add(property,RDFS.label,header.getTitle());
		model.add(property,RDFS.domain,node);
		model.add(property,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
		return property;
	}		
	public Property addPerformer(OntClass node,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/PF%s",prefixURI,i);
		Property dataProperty = getDataProperty(model,uriPrefix,ISA.ISADataProperty.hasPerformer.createProperty(model),header,false);
		model.add(dataProperty,RDFS.domain,node);
		return dataProperty;
	}	
	public Property addParameter(OntClass node,  ColumnHeader header,int i) throws Exception {
		String uriPrefix = String.format("%s/PRM%s",prefixURI,i);
		Property properties  = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasParameter.createProperty(model),header,true);
		model.add(properties,RDFS.domain,node);				
		model.add(properties,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
		return properties;
	}		
	public Property addDate(OntClass node,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/DATE%s",prefixURI,i);
		Property dataProperty  = getDataProperty(model,uriPrefix,ISA.ISADataProperty.hasDate.createProperty(model),header,false);
		model.add(dataProperty,RDFS.domain,node);		
		return dataProperty;
	}	
	public Property addFile(OntClass node,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/FILE%s",prefixURI,i);
		Property files = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasDataset.createProperty(model),header,true);
		model.add(files,RDFS.domain,node);
		model.add(files,RDFS.range,ISA.ISAClass.Dataset.createOntClass(model));
		model.add(files,RDFS.domain,node);	
		return files;
	}	
	
	public OntClass addNamedNodeClass(ISAClass parent, OntClass prevNode,  ColumnHeader header,  int i) throws Exception {
		String uriPrefix = String.format("%s/%s", prefixURI,header); 
		OntClass clazz = getOntClass(model,uriPrefix,parent.createOntClass(model));
		if (prevNode!=null) {
			setNext(String.format("%s/nextEntry%d",prefixURI,i+1),prevNode,clazz);
			setFirst(clazz,false);
		} else 
			setFirst(clazz,true);
		return clazz;
	}	
	
	public OntClass addProtocolNodeClass(OntClass prevNode,  ColumnHeader header, int i) throws Exception {
		String uriPrefix = String.format("%s/P%d",prefixURI,i+1);
		OntClass protocolClass = getOntClass(model,uriPrefix,ISA.ISAClass.Protocol.createOntClass(model));
		model.add(protocolClass,RDFS.label,header.getTitle());
		if (prevNode!=null) setNext(String.format("%s/nextProtocol%d",prefixURI,i+1),prevNode, protocolClass);
		setFirst(protocolClass,false);
		return  protocolClass;
	}			
	public Individual addCollection(ISAClass clazz,String name) throws Exception {
		Individual collection = model.createIndividual(String.format("%s#%s",prefixURI,name),
											clazz.createOntClass(model));
		model.add(collection,RDFS.label,name);
		return collection;

	}
	public Individual addRow( int count, Individual collection) throws Exception {
		Individual row = model.createIndividual(String.format("%s/R%d",prefixURI,count), ISA.ISAClass.ISAEntry.createOntClass(model));
		model.add(row,ISA.ISAObjectProperty.isPartOfCollection.createProperty(model),collection);
		return row;
	}
	
	public Individual addNamedNode(ColumnHeader header, String name, Individual row) throws Exception {
		//String uri = String.format("%s/%s",prefix,tabs[i]);
		String uri = generateURI(model, prefixURI, ISAClass.NamedNode, header, name);
		Individual node = model.createIndividual(uri,header.getNamedNodeClass());
		Property partOf = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		model.add(node,partOf,row);
		return node;
	}	
	
	public Individual addProtocolNode(ColumnHeader header,String value, Individual row ) throws Exception {
		String uri = generateURI(model, prefixURI, ISAClass.Protocol, header,value);
		Individual node = model.createIndividual(uri,header.getProtocolClass());
		model.add(node,RDFS.label,value);
		Property partOf = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		model.add(node,partOf,row);
		return node;
	}			
	public Individual addFileNode(String prefixURI,ColumnHeader header,String value, Individual node ) throws Exception {
		String uri = generateURI(model, prefixURI, ISAClass.Dataset, null,value);
		Individual vi = model.getIndividual(uri);
		model.add(node, header.getFileProperty(), vi);
		return vi;
	}
	/**
	 * Factor,characteristic,comment
	 * @param prefixURI
	 * @param header
	 * @param value
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public Individual addNodeAttribute(ColumnHeader header,String value,
						String unit, String term,
						Individual node ) throws Exception {
		String uri = generateURI(model, prefixURI, ISAClass.Value, null, value);
		Individual vi = model.getIndividual(uri);
		model.add(node, header.getObjectProperty(), vi);

		if (unit!=null) {
			model.add(vi,ISA.ISADataProperty.hasUnit.createProperty(model),model.createTypedLiteral(unit));
		}
		if (term!=null) {
			model.add(vi,RDFS.comment,model.createTypedLiteral(term));
		}
		return vi;
	}	
	public Literal addNodeDataAttribute(String prefixURI,ColumnHeader header,String value,Individual node ) throws Exception {
		Literal l =  model.createTypedLiteral(value);
		model.add(node, header.getDataProperty(),l);
		return l;
	}
}
