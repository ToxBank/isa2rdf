package org.isa2rdf.cli;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.toxbank.client.io.rdf.TOXBANK;

import org.isa2rdf.model.ISA;
import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Accessible;
import uk.ac.ebi.bioinvindex.model.Contact;
import uk.ac.ebi.bioinvindex.model.Data;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Material;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.DataAcquisition;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.DataProcessing;
import uk.ac.ebi.bioinvindex.model.processing.GraphElement;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.Characteristic;
import uk.ac.ebi.bioinvindex.model.term.CharacteristicValue;
import uk.ac.ebi.bioinvindex.model.term.Factor;
import uk.ac.ebi.bioinvindex.model.term.FactorValue;
import uk.ac.ebi.bioinvindex.model.term.OntologyEntry;
import uk.ac.ebi.bioinvindex.model.term.OntologyTerm;
import uk.ac.ebi.bioinvindex.model.term.Parameter;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import uk.ac.ebi.bioinvindex.model.term.Property;
import uk.ac.ebi.bioinvindex.model.term.PropertyValue;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public abstract class RDFGenerator<NODE extends Identifiable,MODEL extends Model> {
	protected Hashtable<String,Resource> affiliations = new Hashtable<String,Resource>();
	protected BIIObjectStore store;
	private MODEL model;
	protected String prefix;
	protected long tempIdCounter=1;
	
	protected Hashtable<String,List<String>> cache = new Hashtable<String, List<String>>();
	
	public long getTempIdCounter() {
		return tempIdCounter;
	}

	public void setTempIdCounter(long tempIdCounter) {
		this.tempIdCounter = tempIdCounter;
	}

	public MODEL getModel() {
		return model;
	}

	public void setModel(MODEL model) {
		this.model = model;
	}

	public RDFGenerator (  String prefix, BIIObjectStore store , MODEL model) {	
		this.store = store;
		this.prefix = prefix;
		setModel(model);
	}

	public abstract MODEL createGraph () throws Exception;
	public MODEL createGraph ( String fileName ) throws Exception {
		return createGraph();
	}
	
	protected String getProtocolURI(Protocol protocol) throws Exception {
		if (protocol.getType()==null) return null;
		if (protocol.getType().getSource()!=null) {
			if (protocol.getType().getSource().getUrl()!=null) {
				return String.format("%s%s",protocol.getType().getSource().getUrl(),protocol.getType().getAcc());
			}
		}
		return null;
	}
	protected String getURI(Identifiable node) throws Exception {
		String p = "ISA_";
		if ( node instanceof MaterialNode ) p = "MN";
		else if ( node instanceof DataNode) p = "DN";		
		else if ( node instanceof MaterialProcessing ) p = "MN"; 
		else if ( node instanceof DataAcquisition ) p = "DAN"; 
		else if ( node instanceof DataProcessing ) p = "DPN";
		else if ( node instanceof Material ) p = "M";
		else if ( node instanceof Data ) p = "D";
		else if ( node instanceof Protocol ) {
			p = getProtocolURI((Protocol)node);
			if (p!=null) return p; else p="P_";
		}
		else if ( node instanceof ProtocolApplication ) p = "PA";
		else if ( node instanceof Study ) p = "S";
		else if ( node instanceof Assay ) p = "A";
		else if ( node instanceof Investigation ) p = "I";
		else if ( node instanceof ParameterValue ) {
			ParameterValue pv = (ParameterValue) node;
			if (pv.getOntologyTerms()!=null && pv.getOntologyTerms().size()==1) {
				OntologyTerm term  =(OntologyTerm) pv.getOntologyTerms().get(0);
				if (!"NULL-ACCESSION".equals(term.getAcc()))
					return String.format("%s/PMV_%s_%s",prefix,term.getSource().getAcc(),term.getAcc());	
			} 
			cache.get(ParameterValue.class.getName());
			return getCachedURI(pv, String.format("%s/PMV",prefix) , pv.getValue());
		}
		else if ( node instanceof FactorValue ) p = "FV";
		else if ( node instanceof CharacteristicValue ) p = "CV";
		else if ( node instanceof PropertyValue ) p = "PV";
		else if ( node instanceof OntologyTerm ) {
			/**
    <!-- http://purl.obolibrary.org/obo/CHEBI_26523 -->
			 */
			OntologyTerm term  =(OntologyTerm) node;
			return String.format("http://purl.obolibrary.org/obo/%s_%s",term.getSource().getAcc(),term.getAcc());
		}
		else if ( node instanceof OntologyEntry ) p = "OE";
		else if ( node instanceof Factor ) p = "F";
		else if ( node instanceof Characteristic ) p = "C";
		else if ( node instanceof Parameter ) {
			Parameter pv = (Parameter) node;
			if (pv.getOntologyTerms()!=null && pv.getOntologyTerms().size()==1) {
				OntologyTerm term  =(OntologyTerm) pv.getOntologyTerms().get(0);
				if (!"NULL-ACCESSION".equals(term.getAcc()))
						return String.format("%s/PRM_%s_%s",prefix,term.getSource().getAcc(),term.getAcc());	
			} 
			cache.get(Parameter.class.getName());
			return getCachedURI(pv, String.format("%s/PRM",prefix) , pv.getValue());

		} else if ( node instanceof Property ) p = "PR";
		else if ( node instanceof ReferenceSource ) {
			String url = ((ReferenceSource) node).getUrl();
			if (url!=null) return url;
			else p = "RS";
		} else {
			//System.err.println(node.getClass().getName());
		}
			if (node.getId()==null) { node.setId(tempIdCounter); tempIdCounter++; }
			return String.format("%s/%s%d",prefix,p,node.getId());
	}
	protected Resource getResourceID(Identifiable node,Resource clazz)  throws Exception {

		if (node==null) return null;
		
		return model.createResource(getURI(node), clazz);

	}
	protected Resource getResource(Identifiable node,Resource clazz)  throws Exception {
		
		Resource resource = getResourceID(node, clazz);
		if (node==null) return null;
		//Accessible
		if ((node instanceof Accessible) && ((Accessible) node).getAcc()!=null) {
			resource.addProperty(ISA.hasAccessionID, ((Accessible) node).getAcc());
		}
		if (node instanceof Property) { //factor/char/params are descendant
			Property pv = (Property) node;
			getModel().add(resource,DCTerms.title,pv.getValue());
			if (pv.getOntologyTerms()!=null)
				for (Object ot: pv.getOntologyTerms()) {
					Resource xot = getResource((OntologyTerm)ot, ISA.OntologyTerm);
					if (xot!=null)
						getModel().add(resource,ISA.HASONTOLOGYTERM,xot);
				}
		}
		
		if (node instanceof PropertyValue) {
			PropertyValue pv = (PropertyValue) node;
			if (pv.getOntologyTerms()!=null)
			for (Object ot: pv.getOntologyTerms()) {
				OntologyTerm term = (OntologyTerm) ot;
				if (!"NULL-ACCESSION".equals(term.getAcc())) {
					Resource xot = getResource(term, ISA.OntologyTerm);
					if (xot!=null)
						getModel().add(resource,ISA.HASONTOLOGYTERM,xot);
				}
			}
			
			Resource r = node instanceof FactorValue?ISA.Factor:node instanceof ParameterValue?ISA.Parameter:ISA.Property;
			com.hp.hpl.jena.rdf.model.Property p = node instanceof FactorValue?ISA.HASFACTOR:node instanceof ParameterValue?ISA.HASPARAMETER:ISA.HASPROPERTY;
			
			if (pv.getValue()!=null) getModel().add(resource,ISA.HASVALUE,pv.getValue());
			if (pv.getType()!=null) {
				Resource xt = getResource(pv.getType(),r);
				getModel().add(resource,p,xt);
			}
			
		}

		if (node instanceof OntologyTerm) {
			OntologyTerm term = (OntologyTerm) node;
			//resource.addProperty(ISA.,getResource(term.getSource(),ISA.ReferenceSources));
			resource.addProperty(RDFS.label,term.getName());
			resource.addProperty(ISA.hasAccessionID,term.getAcc());
			/*

    <owl:Class rdf:about="http://purl.obolibrary.org/obo/CHEBI_26523">
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">reactive oxygen species</rdfs:label>
        <rdfs:subClassOf rdf:resource="http://purl.obolibrary.org/obo/CHEBI_25806"/>
        <obo2:Definition rdf:datatype="http://www.w3.org/2001/XMLSchema#string">Molecules or ions formed by the incomplete one-electron reduction of oxygen. They contribute to the microbicidal activity of phagocytes, regulation of signal transduction and gene expression, and the oxidative damage to biopolymers.</obo2:Definition>
        <obo2:Synonym rdf:datatype="http://www.w3.org/2001/XMLSchema#string">ROS</obo2:Synonym>
    </owl:Class>
			 */
			
		}
		if (node instanceof OntologyEntry) {
			//TODO
		}
		//Data

		if (node instanceof Data) {
			//System.out.println(node);
			Data data = (Data) node;
			//if (data.getUrl()!=null) resource.addProperty(RDFS.isDefinedBy, data.getUrl());
			
			if (data.getName()!=null) resource.addProperty(DCTerms.title, data.getName());
			if (data.getDataMatrixUrl()!=null) resource.addProperty(RDFS.seeAlso, data.getUrl());
			if (data.getType()!=null) { //ontlogy entry
				Resource oe = getResourceID(data.getType(), ISA.OntologyEntry);
			}
			
			//if (data.getSubmissionTs()!=null) resource.addProperty(DCTerms.created, data.getSubmissionTs().toGMTString());
			if (data.getFactorValues()!=null)
				for (FactorValue fv : data.getFactorValues()) {
					Resource xfv = getResourceID(fv, ISA.FactorValue);
					logger(xfv);
					getModel().add(resource,ISA.HASFACTORVALUE,xfv);
				}
		}
		
		//GraphElement
		if (node instanceof GraphElement) {
			if (((GraphElement) node).getStudy()!=null)
				resource.addProperty(ISA.HASSTUDY, getResourceID(((GraphElement) node).getStudy(),ISA.Study));
		}
		
		//GraphElement
		if (node instanceof Study) {
			Study study = (Study) node;
			if (study.getTitle()!=null)
			resource.addProperty(DCTerms.title,study.getTitle());
			if (study.getDescription()!=null)
			resource.addProperty(DCTerms.description,study.getDescription());
			if (study.getSubmissionDate()!=null)
			resource.addProperty(DCTerms.created,study.getSubmissionDate().toGMTString());
			if (study.getObjective()!=null)
			resource.addProperty(DCTerms.abstract_,study.getObjective());
			
			/*
			for (AssayResult ar : study.getAssayResults()) {
				System.out.println(ar);
			}
			*/
		}


		/**
		 * Persons , defined in the investigation file
		 */
		if (node instanceof Contact) {
			Contact contact = (Contact) node;
			getModel().add(resource, RDF.type, FOAF.Person); 
			if (contact.getFirstName() != null)
				resource.addLiteral(FOAF.givenname, contact.getFirstName());
			if (contact.getLastName() != null)
				resource.addLiteral(FOAF.family_name, contact.getLastName());		
			//TODO affiliations
			
			if (contact.getAffiliation()!=null) {
				Resource affiliation = affiliations.get(contact.getAffiliation());
				if (affiliation==null) { //don't want to assign URI
	 				affiliation = getModel().createResource();
					getModel().add(affiliation, RDF.type, TOXBANK.ORGANIZATION);
					affiliation.addLiteral(DCTerms.title, contact.getAffiliation());
					affiliations.put(contact.getAffiliation(), affiliation);
				}
				affiliation.addProperty(TOXBANK.HASMEMBER, resource);

			}
			processRoles(contact,resource);

		}
		
		//GraphElement
		if (node instanceof Investigation) {
			Investigation inv = (Investigation) node;
			processAnnotations(inv,resource);
			if (inv.getTitle()!=null)
			resource.addProperty(DCTerms.title,inv.getTitle());
			if (inv.getDescription()!=null)
			resource.addProperty(DCTerms.abstract_,inv.getDescription());
			if (inv.getSubmissionDate()!=null)
			resource.addProperty(DCTerms.created,inv.getSubmissionDate().toGMTString());
			if (inv.getReleaseDate()!=null)
				resource.addProperty(DCTerms.issued,inv.getReleaseDate().toGMTString());			
			//if (study.getPublications()!=null)
			//resource.addProperty(DCTerms.abstract_,study.getObjective());
			//if (study.getContacts()!=null)
				//resource.addProperty(DCTerms.abstract_,study.getObjective());
			
			for (Contact contact: inv.getContacts()) {

				Resource contactResource = getResource(contact,ISA.Contact);
				getModel().add(resource,ISA.HASOWNER,contactResource);
			}
			
			if (inv.getStudies()!=null) for (Study study : inv.getStudies()) {
				Resource studyResource = getResourceID(study,ISA.Study);
				//Studies are already added, but not their details
				for (Protocol protocol : study.getProtocols()) {
					
					Resource protocolResource = getResourceID(protocol,ISA.Protocol);
					if (protocol.getUri()!=null)
						getModel().add(protocolResource,RDFS.seeAlso,protocol.getUri());
					if (protocol.getDescription()!=null)
						getModel().add(protocolResource,DCTerms.description,protocol.getDescription());
					
					if (protocol.getType()!=null) processProtocolType(protocol,protocolResource);
					
					getModel().add(studyResource,ISA.HASPROTOCOL,protocolResource);
				}
				for (Contact contact: study.getContacts()) {
					Resource contactResource = getResource(contact,ISA.Contact);
					getModel().add(studyResource,ISA.HASOWNER,contactResource);
				}
			}
				
			/*
			for (AssayResult ar : study.getAssayResults()) {
				System.out.println(ar);
			}
			*/
		}		
		//Node
		/**FIXME something is wrong, owl gets broken ... perhaps consider assayfields as resources not literals
		if (node instanceof Node) {

			if (((Node) node).getSampleFileId()!=null)
				resource.addProperty(ISA.HASSAMPLEFIELD,((Node) node).getSampleFileId() );
	
			
			if (((Node) node).getAssayFileIds()!=null) {
				Iterator assayFields = ((Node) node).getAssayFileIds().iterator();
				while (assayFields.hasNext()) {
					String assayField = assayFields.next().toString();
					resource.addLiteral(ISA.HASASSAYFIELD,assayField);
					System.out.println(node.getClass().getName() + " " + resource.getURI() + " "+ assayField);
				}
			}
			
		}		
	*/
	
		return resource;
	}
	

	public void logger(Object object) {
		//System.err.println(object!=null?object.toString():"");
	}
	
	public void processAnnotations(Investigation investigation, Resource investigationResource) throws Exception {
		
	}

	
	public void processRoles(Contact contact, Resource contactResource)	throws Exception {
		
	}

	
	public void processProtocolType(Protocol protocol, Resource protocolResource)	throws Exception {
		//TODO
	}
	
	protected String getCachedURI(Identifiable object,String prefix, String value) {
		if (value==null) return null;
		value = value.trim();
		List<String> ocache = cache.get(object.getClass().getName());
		int index = -1;
		if (ocache==null) {
			ocache = new ArrayList<String>();
			cache.put(object.getClass().getName(),ocache);
		} else 
			index = ocache.indexOf(value);
		if (index<0) {
			ocache.add(value);
			index = ocache.size()-1;
		}
		return String.format("%s%d",prefix,(index+1));
	}
}
