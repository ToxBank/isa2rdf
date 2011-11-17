package net.toxbank.isa2rdf;

import java.io.Reader;
import java.util.Hashtable;

import net.toxbank.isa2rdf.ISA.ISAClass;
import net.toxbank.isa2rdf.ISA.ISADataProperty;
import net.toxbank.isa2rdf.ISA.ISAObjectProperty;

import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class EntryParser extends TabsParser<Resource> {
	protected OntModel model;
	protected String prefixURI;

    protected Individual collection;
	
	protected Hashtable<String, String>[] attributes = new Hashtable[ISA.ISAClass.values().length];
	
	public OntModel getModel() {
		return model;
	}

	public EntryParser(Reader in, String prefix, String prefixURI, OntModel model) {
		super(in);
		attributes = new Hashtable[ISA.ISAClass.values().length];
		this.prefixURI = prefixURI;
		this.model = model;
		model.setNsPrefix(prefix,String.format("%s/",prefixURI));
		
		initModel(prefix);

	}
	
	protected ISA.ISAClass getEntryClass() {
		return ISAClass.ISACollection;
	}
	protected ISA.ISAClass getNodeClass() {
		return ISAClass.NamedNode;
	}	
	
	protected void initModel(String prefix) {
		collection = model.createIndividual(String.format("%s/%s",prefixURI,prefix),
												getEntryClass().createOntClass(model));
		model.add(collection,RDFS.label,prefix);
		
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
		model.add(ISAObjectProperty.hasFile.createProperty(model),RDFS.domain,ISAClass.AssayNode.createOntClass(model));
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
	
	/**
    <owl:Restriction>
        <owl:onProperty rdf:resource="http://www.owl-ontologies.com/toxbank.owl#hasNext"/>
        <owl:allValuesFrom rdf:resource="&study1;SampleName"/>
    </owl:Restriction>
		 */
	protected void setNext(String uri, OntClass node,OntClass nextNode) {
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
	protected void setFirst(OntClass node, boolean value) {
		HasValueRestriction restriction = model.createHasValueRestriction(null, 
							ISA.ISADataProperty.isFirstNode.createProperty(model),
							model.createTypedLiteral(value));
		model.add(node,RDFS.subClassOf,restriction);
	}
	@Override
	protected void readHeader() throws Exception {
		super.readHeader();

		int lastIndex = -1;
		OntClass node = null;
		Resource protocol = null;
		
		for (int i=0; i < header.length; i++) {
			//System.out.println(header[i]);
			String uriPrefix = String.format("%s/%s", prefixURI,header[i]); 
			if (header[i].isNamedNode()) {
				OntClass clazz = getOntClass(model,uriPrefix,getNodeClass().createOntClass(model));
				if (node!=null) {
					setNext(String.format("%s/nextEntry%d",prefixURI,i+1),node,clazz);
					setFirst(clazz,false);
				} else 
					setFirst(clazz,true);
				node = clazz;
				header[i].setResource(clazz);
				lastIndex = i;
			} else if (header[i].isProtocol()) {
				uriPrefix = String.format("%s/P%d",prefixURI,i+1);
				OntClass protocolClass = getOntClass(model,uriPrefix,ISA.ISAClass.Protocol.createOntClass(model));
				model.add(protocolClass,RDFS.label,header[i].getTitle());
				if (node!=null) setNext(String.format("%s/nextProtocol%d",prefixURI,i+1),node, protocolClass);
				node = protocolClass;
				header[i].setResource(protocolClass);
				setFirst(node,false);
				protocol = node;
				lastIndex = i;
			} else if (header[i].isFactor()) {
				uriPrefix = String.format("%s/F%s",prefixURI,i+1);
				Property properties = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasFactor.createProperty(model),header[i],true);
				model.add(properties,RDFS.label,header[i].getTitle());
				model.add(properties,RDFS.domain,node);
				model.add(properties,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
				header[i].setResource(properties);
				lastIndex = i;
			} else if (header[i].isCharacteristic()) {
				uriPrefix = String.format("%s/CH%s",prefixURI,i+1);
				Property properties = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasCharacteristic.createProperty(model),header[i],true);
				model.add(properties,RDFS.domain,node);
				model.add(properties,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
				header[i].setResource(properties);
				lastIndex = i;
			} else if (header[i].isComment()) {
				uriPrefix = String.format("%s/CM%s",prefixURI,i+1);
				Property property = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasComment.createProperty(model),header[i],false);
				model.add(property,RDFS.domain,node);
				model.add(property,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
			//	model.add(dataProperty[i],RDFS.range,XSDBaseStringType.XSD); for some reason this makes the ontology insocsistent...
				header[i].setResource(property);
				lastIndex = i;
			} else if (header[i].isPerformer()) {
				uriPrefix = String.format("%s/PF%s",prefixURI,i+1);
				Property dataProperty = getDataProperty(model,uriPrefix,ISA.ISADataProperty.hasPerformer.createProperty(model),header[i],false);
				model.add(dataProperty,RDFS.domain,node);
			//	model.add(dataProperty[i],RDFS.range,XSDBaseStringType.XSD); for some reason this makes the ontology insocsistent...
				header[i].setResource(dataProperty);
			} else if (header[i].isParameter()) {
				uriPrefix = String.format("%s/PRM%s",prefixURI,i+1);
				Property properties  = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasParameter.createProperty(model),header[i],true);
				model.add(properties,RDFS.domain,node);				
				model.add(properties,RDFS.range,ISA.ISAClass.Value.createOntClass(model));
				header[i].setResource(properties);
				lastIndex = i;
			} else if (header[i].isDate()) {
				uriPrefix = String.format("%s/DATE%s",prefixURI,i+1);
				Property dataProperty  = getDataProperty(model,uriPrefix,ISA.ISADataProperty.hasDate.createProperty(model),header[i],false);
				model.add(dataProperty,RDFS.domain,node);		
				header[i].setResource(dataProperty);
			} else if (header[i].isFile()) {
				uriPrefix = String.format("%s/FILE%s",prefixURI,i+1);
				Property files = getObjectProperty(model,uriPrefix,ISA.ISAObjectProperty.hasFile.createProperty(model),header[i],true);
				model.add(files,RDFS.domain,node);
				model.add(files,RDFS.range,ISA.ISAClass.ValueFile.createOntClass(model));
				model.add(files,RDFS.domain,node);	
				header[i].setResource(files);
			} else if (header[i].isUnit()) {
				if (lastIndex>=0) header[lastIndex].setUnit(header[i]);
				lastIndex = i; //units can have terms!
			} else if (header[i].isTermREF()) {
				if (lastIndex>=0) header[lastIndex].setTermREF(header[i]);			
			} else if (header[i].isTermNo()) {
				if (lastIndex>=0) header[lastIndex].setTermNo(header[i]);						
			} else {
				System.err.println("What we don't support yet???\t"+header[i]+"\t"+header[lastIndex]);
			}
			
			
		}
/*
		for (ColumnHeader column:header) { 
			System.out.println(
					String.format("%s\t%s\t%s\t%s",
					column.index,column.title,column.termREF==null?"-":column.termREF.index, 
							column.termNo==null?"-":column.termNo.index )
							);
		}
*/		
	}
	
	@Override
	protected Resource transform(String[] tabs) throws Exception {
		
		Individual row = model.createIndividual(String.format("%s/R%d",prefixURI,count), ISA.ISAClass.ISAEntry.createOntClass(model));
		model.add(row,ISA.ISAObjectProperty.isPartOfCollection.createProperty(model),collection);

		Individual node = null;
		Property partOf = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);

		for (int i=0; i < tabs.length; i++) tabs[i] = tabs[i].replace("\"","");
		for (int i=0; i < tabs.length; i++) {
			
			String unit = null;
			String term = null;
			try {
			 unit = header[i].getUnitValue(tabs); 
			 term = header[i].getTerm(tabs); 
			} catch (Exception x) {
				System.out.println(header[i]);
				x.printStackTrace();
			}
	
			if (header[i].getNamedNodeClass()!=null) { //named node
				//String uri = String.format("%s/%s",prefix,tabs[i]);
				String uri = generateURI(model, prefixURI, ISAClass.NamedNode, header[i], tabs[i]);
				node = model.createIndividual(uri,header[i].getNamedNodeClass());
				model.add(node,partOf,row);
				
			} else if (header[i].getProtocolClass() != null) {
				String uri = generateURI(model, prefixURI, ISAClass.Protocol, header[i], tabs[i]);
				//String uri = String.format("%s/%s",prefix,tabs[i]);
				node = model.createIndividual(uri,header[i].getProtocolClass());
				model.add(node,RDFS.label,tabs[i]);
				model.add(node,partOf,row);
			} else if (header[i].getFileProperty() != null) {
				if (node!=null) {
				
					String uri = generateURI(model, prefixURI, ISAClass.ValueFile, null, tabs[i]);
					Individual value = model.getIndividual(uri);
					model.add(node, header[i].getFileProperty(), value);
				}				
			} else if (header[i].getObjectProperty() != null) {
				if (node!=null) {
				
					String uri = generateURI(model, prefixURI, ISAClass.Value, null, tabs[i]);
					Individual value = model.getIndividual(uri);
					model.add(node, header[i].getObjectProperty(), value);
					if (unit!=null) {
						model.add(value,ISA.ISADataProperty.hasUnit.createProperty(model),model.createTypedLiteral(unit));
					}
					if (term!=null) {
						model.add(value,RDFS.comment,model.createTypedLiteral(term));
					}
				}

			} else if (header[i].getDataProperty() != null) {
				if (node!=null) {
					model.add(node, header[i].getDataProperty(), model.createTypedLiteral(tabs[i]));
				}
			}
		}
	
		return null;
	}
	
	public OntClass getOntClass(OntModel model, String uri, OntClass parent) {
		OntClass c = model.getOntClass(uri);
		if (c==null) {
			c = model.createClass(uri);
			if (parent!=null) {
				model.add(c,RDFS.subClassOf,parent);
			}
		}
		return c;
	}
	public Property getObjectProperty(OntModel model, String uri, Property parent, ColumnHeader label, boolean functional) {
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
	public Property getDataProperty(OntModel model, String uri, Property parent, ColumnHeader label,boolean functional) {
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
	

}



/**
SELECT ?study ?nl ?fl ?l
WHERE { 
?node <http://www.owl-ontologies.com/toxbank.owl#isPartOfEntry>  ?study.
?node rdfs:label ?nl.
?node  ?f ?z.
{
{?f rdfs:subPropertyOf <http://www.owl-ontologies.com/toxbank.owl#hasCharacteristic>.
?f rdfs:label ?fl.
?z rdfs:label ?l.}
union
{?f rdfs:subPropertyOf <http://www.owl-ontologies.com/toxbank.owl#hasFactor>.
?f rdfs:label ?fl.
?z rdfs:label ?l.}
}
}
order by ?study ?node
 */

