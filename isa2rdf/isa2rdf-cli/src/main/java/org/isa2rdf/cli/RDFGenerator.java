package org.isa2rdf.cli;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import net.toxbank.client.io.rdf.TOXBANK;

import org.isa2rdf.model.ISA;
import org.isatools.isatab.mapping.AssayGroup;
import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Accessible;
import uk.ac.ebi.bioinvindex.model.AssayResult;
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
	protected HashMap<String, ReferenceSource> references = new HashMap<String, ReferenceSource>();
	protected Hashtable<String,List<Object>> cache = new Hashtable<String, List<Object>>();
	
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
		return getCachedURI(protocol, String.format("%s/P",prefix) , protocol);
	}
	protected String getURI(Identifiable node) throws Exception {
		String p = "ISA_";
		if ( node instanceof MaterialNode ) p = "MN";
		else if ( node instanceof DataNode) p = "DN";		
		else if ( node instanceof MaterialProcessing ) p = "MN"; 
		else if ( node instanceof DataAcquisition ) p = "DAN"; 
		else if ( node instanceof DataProcessing ) p = "DPN";
		else if ( node instanceof Material ) {
			//source or sample
			String typeAcc = ((Material)node).getType().getAcc().substring(4);
			p = typeAcc;
		}else if ( node instanceof Data ) p = "D";
		else if ( node instanceof Protocol ) {
			p = "P";
			return getProtocolURI((Protocol)node);
		}
		else if ( node instanceof ProtocolApplication ) {
			ProtocolApplication papp = (ProtocolApplication) node;
			p = "PA";
			return getCachedURI(papp, String.format("%s/%s",prefix,p) , papp);
			//
		} else if ( node instanceof Study ) p = "S";
		else if ( node instanceof Assay ) {
			p = "Assay";
			return getCachedURI(node, String.format("%s/%s",prefix,p) , node);
		}
		else if ( node instanceof Investigation ) p = "I";
		else if ( node instanceof PropertyValue ) {
			if ( node instanceof ParameterValue ) p = "PMV";
			else if ( node instanceof FactorValue ) p = "FV";
			else if ( node instanceof CharacteristicValue ) p = "CV";

			PropertyValue pv = (PropertyValue) node;
			/*
			if (pv.getOntologyTerms()!=null && pv.getOntologyTerms().size()==1) {
				OntologyTerm term  =(OntologyTerm) pv.getOntologyTerms().get(0);
				String uri = mintOntologyURI(prefix,term, p);
				if (uri != null) return uri;
			} 
			*/
			return getCachedURI(pv, String.format("%s/%s",prefix,p) , pv);
		}
		else if ( node instanceof OntologyEntry ) {
			/**
    <!-- http://purl.obolibrary.org/obo/CHEBI_26523 -->
			 */
			OntologyEntry term  =(OntologyEntry) node;
			String purl = "http://purl.obolibrary.org/obo";
			String uri = null;
			try {
				String lookup = term.getSource().getAcc();
				String s_acc = term.getSource().getAcc();;
				int x = lookup.indexOf(":");
				if (x >0) { 
					s_acc = lookup.substring(x+1);
					lookup = lookup.substring(0,x);  
				}
				SupportedPurl sp = SupportedPurl.valueOf(lookup);//just for the lookup
				purl = sp.getPURL();
				return sp.getEntry(s_acc, term.getAcc()); 
			} catch (Exception x) {	
				uri = mintOntologyURI(purl,term, null);
			}
			return uri!=null?uri:String.format("%s/%s_%s",purl,term.getSource().getAcc(),term.getAcc());
		} else if ( node instanceof Property ) {
			p = "PRM";
			if ( node instanceof Factor ) p = "F";
			else if ( node instanceof Characteristic ) p = "C";
			
			Property pv = (Property) node;
			/*
			if (pv.getOntologyTerms()!=null && pv.getOntologyTerms().size()==1) {
				OntologyTerm term  =(OntologyTerm) pv.getOntologyTerms().get(0);
				String uri = mintOntologyURI(prefix, term, p);
				if (uri != null) return uri;
			} 
			*/
			return getCachedURI(pv, String.format("%s/%s",prefix,p) , pv);

		} else if ( node instanceof Property ) p = "PR";
		else if ( node instanceof ReferenceSource ) {
			p = "RS";
			String url = ((ReferenceSource) node).getUrl();
			if (url!=null) return url;
			else return String.format("%s/%s%s",prefix,p,((ReferenceSource) node).getAcc());
		} else {
			//System.err.println(node.getClass().getName());
		}
			if (node.getId()==null) { node.setId(tempIdCounter); tempIdCounter++; }
			return String.format("%s/%s%d",prefix,p,node.getId());
	}
	
	protected String mintOntologyURI(String prefix, OntologyEntry term, String p) {
		if ((term.getAcc().indexOf("NULL-")<0) && (term.getSource().getAcc().indexOf("NULL-")<0)) {
			//ISACreator 1.7.x writes prefixed accession numbers ....
			String[] split = term.getAcc().split(":");
			ReferenceSource xs = references.get(term.getSource().getAcc());
			if (xs != null && xs.getUrl()!=null && xs.getUrl().toLowerCase().indexOf("toxbank")>=0) {
				return String.format("%s%s%s%s",xs.getUrl(),
						xs.getUrl().endsWith("/")?"":"/",
						p==null?"":(p+"_"),split.length>1?split[1]:split[0]);	
			} else
			return String.format("%s/%s%s_%s",prefix,p==null?"":(p+"_"),term.getSource().getAcc(),split.length>1?split[1]:split[0]);
		}		
		return null;
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
			//resource.addProperty(ISA.hasAccessionID, ((Accessible) node).getAcc());
		}
		if (node instanceof Protocol) {
			resource.addProperty(ISA.hasAccessionID, ((Accessible) node).getAcc());
		} else if (node instanceof Property) { //factor/char/params are descendant
			Property pv = (Property) node;
			getModel().add(resource,DCTerms.title,pv.getValue());
			if (pv.getOntologyTerms()!=null)
				for (Object ot: pv.getOntologyTerms()) {
					OntologyTerm term = (OntologyTerm) ot;
					if ((term!=null) && (term.getAcc()!=null) && (term.getSource().getAcc()!=null) && (term.getAcc().indexOf("NULL-")<0) && (term.getSource().getAcc().indexOf("NULL-")<0)) {
					Resource xot = getResource(term, ISA.OntologyTerm);
					if (xot!=null)
						getModel().add(resource,ISA.HASONTOLOGYTERM,xot);
					}
				}
		} else if (node instanceof PropertyValue) {
			PropertyValue pv = (PropertyValue) node;
			if (pv.getOntologyTerms()!=null)
			for (Object ot: pv.getOntologyTerms()) {
				OntologyTerm term = (OntologyTerm) ot;
				if ((term.getAcc().indexOf("NULL-")<0) && (term.getSource().getAcc().indexOf("NULL-")<0)) {
					Resource xot = getResource(term, ISA.OntologyTerm);
					if (xot!=null)
						getModel().add(resource,ISA.HASONTOLOGYTERM,xot);
				}
			}
			
			Resource r = node instanceof FactorValue?ISA.Factor:node instanceof ParameterValue?ISA.Parameter:ISA.Property;
			com.hp.hpl.jena.rdf.model.Property p = node instanceof FactorValue?ISA.HASFACTOR:node instanceof ParameterValue?ISA.HASPARAMETER:ISA.HASPROPERTY;
			
			addScalarValue(resource, ISA.HASVALUE,pv.getValue());

			if (pv.getType()!=null) {
				Resource xt = getResource(pv.getType(),r);
				getModel().add(resource,p,xt);
			}
			
		} else if (node instanceof OntologyTerm) {
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
			
		} else if (node instanceof OntologyEntry) {
			OntologyEntry term = (OntologyEntry) node;
			//resource.addProperty(ISA.,getResource(term.getSource(),ISA.ReferenceSources));
			resource.addProperty(RDFS.label,term.getName());
			resource.addProperty(ISA.hasAccessionID,term.getAcc());
			
		} else if (node instanceof Data) {
			/**
			 * Data
			 */
			Data data = (Data) node;
			resource.addProperty(ISA.hasAccessionID, data.getName());
			if (data.getType()!=null) { //ontlogy entry
				Resource oe = getResourceID(data.getType(), ISA.OntologyTerm);
				getModel().add(resource,ISA.HASONTOLOGYTERM,oe);
			}			
			//if (data.getName()!=null) resource.addProperty(DCTerms.title, data.getName());
			if (data.getDataMatrixUrl()!=null && !"".equals(data.getDataMatrixUrl())) 
				resource.addProperty(RDFS.seeAlso, data.getDataMatrixUrl());
			if (data.getType()!=null) { //ontlogy entry
				Resource oe = getResourceID(data.getType(), ISA.OntologyTerm);
			}
			if (data.getFactorValues()!=null)
				for (FactorValue fv : data.getFactorValues()) {
					Resource xfv = getResource(fv, ISA.FactorValue);
					getModel().add(resource,ISA.HASFACTORVALUE,xfv);
				}

			
		} if (node instanceof Material) {
			/**
			 * material
			 */
			Material data = (Material) node;
			//resource.addProperty(ISA.hasAccessionID, ((Accessible) node).getAcc());
			resource.addProperty(ISA.hasAccessionID, data.getName());
			//if (data.getName()!=null) resource.addProperty(DCTerms.title, data.getName());
			if (data.getType()!=null) { //ontlogy entry
				Resource oe = getResourceID(data.getType(), ISA.OntologyTerm);
				getModel().add(resource,ISA.HASONTOLOGYTERM,oe);
			}
			if (data.getFactorValues()!=null)
				for (FactorValue fv : data.getFactorValues()) {
					Resource xfv = getResource(fv, ISA.FactorValue);
					getModel().add(resource,ISA.HASFACTORVALUE,xfv);
				}
			if (data.getCharacteristicValues()!=null)
				for (CharacteristicValue fv : data.getCharacteristicValues()) {
					Resource xfv = getResource(fv, ISA.CharacteristicValue);
					getModel().add(resource,ISA.HASCHARACTERISTICVALUE,xfv);
				}			
		} if (node instanceof Study) {
			Study study = (Study) node;
			if (study.getTitle()!=null)
			resource.addProperty(DCTerms.title,study.getTitle());
			if (study.getDescription()!=null)
			resource.addProperty(DCTerms.description,study.getDescription());
			if (study.getSubmissionDate()!=null)
			resource.addProperty(DCTerms.created,study.getSubmissionDate().toGMTString());
			if (study.getObjective()!=null)
			resource.addProperty(DCTerms.abstract_,study.getObjective());
			
			for (AssayResult ar : study.getAssayResults()) {
				Resource rData = getResourceID(ar.getData(), ISA.Data);
				rData.addProperty(RDF.type, ISA.AssayResult);
				
		
				
				//System.out.println(ar);
				//System.out.println(ar.getData());
				/*
				for (BioEntity bioentity : ar.getBioEntities()) {
					System.out.println("Bioentity" + bioentity.getDescription());
				}
				*/
				/*
				for (FactorValue fv : ar.getFactorValues()) {
					System.out.println(fv.getType().getValue() + "=" + fv.getValue());
				}
				*/
				
				for (PropertyValue fv : ar.getCascadedPropertyValues()) {
					if (fv==null) continue;
					Property property = fv.getType();
					StringBuilder a = new StringBuilder();
					for (Assay assay : ar.getAssays()) {
						a.append(assay.getAcc());
						a.append("|");
					}
					/*
					System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
							ar.getStudy().getAcc(),a,
							(ar.getData()==null?"":ar.getData().getName()),
							(ar.getData()==null?"":ar.getData().getType().getAcc()),
							(ar.getData()==null?"":ar.getData().getProcessingNode()),
							(property==null?"":fv.getType().getOrder()), 
							(property==null?"":fv.getType().getRole()), 
							(property==null?"":fv.getType().getValue()), 
							fv.getValue(), 
							(fv.getUnit()==null?"":fv.getUnit().getValue()))
							);
							*/
				}				
/*
				for (Assay fv : ar.getAssays()) {
					System.out.println("Assay\t" + fv.getAcc() + " : " + fv + "\t" +  fv.getMaterial());
				}
				*/				
			}

		} else	if (node instanceof Contact) {
			/**
			 * Persons , defined in the investigation file
			 */
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
		/*
		} else if (node instanceof Assay) {
			Assay assay = (Assay) node;
			resource.addProperty(DCTerms.title,assay.getTechnologyName());
			resource.addProperty(ISA.hasAccessionID,assay.getAcc());
*/
		} else if (node instanceof AssayGroup) {
			AssayGroup assaygroup = (AssayGroup) node;
			if (assaygroup.getTechnology()!=null) {
				Resource technology = getResource(assaygroup.getTechnology(),ISA.OntologyTerm);
				resource.addProperty(ISA.USESTECHNOLOGY,technology);
			}
			if (assaygroup.getPlatform()!=null)
				resource.addProperty(ISA.USESPLATFORM,assaygroup.getPlatform());
			if (assaygroup.getFilePath()!=null && !"".equals(assaygroup.getFilePath()))
				resource.addProperty(RDFS.seeAlso,assaygroup.getFilePath());
			Resource endpoint = getResource(assaygroup.getMeasurement(),ISA.OntologyTerm);
			resource.addProperty(ISA.HASENDPOINT,endpoint);
			for (Assay assay : assaygroup.getAssays()) {
				Material material = assay.getMaterial();
				Resource mresource = getResourceID(material,ISA.Material);
				resource.addProperty(ISA.USES,mresource);
			}
		} else if (node instanceof Investigation) {
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
					if ((protocol.getUri()!=null) && !"".equals(protocol.getUri()))
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
		
		//GraphElement
		if (node instanceof GraphElement) {
			if (((GraphElement) node).getStudy()!=null)
				resource.addProperty(ISA.HASSTUDY, getResourceID(((GraphElement) node).getStudy(),ISA.Study));
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
	

	protected void addScalarValue(Resource resource,com.hp.hpl.jena.rdf.model.Property property,Object value) {
		if (value==null) return;
		try { 
			double v = Double.parseDouble(value.toString());
			try {
				int i = Integer.parseInt(value.toString());	
				getModel().addLiteral(resource,property,i);
			} catch (Exception x) {
				getModel().addLiteral(resource,property,v);	
			}
		} catch (Exception x) {
			getModel().add(resource,property,value.toString());	
		}

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
	
	protected String getCachedURI(Identifiable object,String prefix, Object value) {
		if (value==null) return null;
		value = (value instanceof String)?value.toString().trim():value;
		List<Object> ocache = cache.get(object.getClass().getName());
		int index = -1;
		if (ocache==null) {
			ocache = new ArrayList<Object>();
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
