package org.isa2rdf.cli;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.toxbank.client.Resources;
import net.toxbank.client.io.rdf.OrganisationIO;
import net.toxbank.client.io.rdf.ProjectIO;
import net.toxbank.client.io.rdf.TOXBANK;
import net.toxbank.client.io.rdf.UserIO;
import net.toxbank.client.resource.Organisation;
import net.toxbank.client.resource.Project;
import net.toxbank.client.resource.User;

import org.isa2rdf.model.ISA;
import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Annotation;
import uk.ac.ebi.bioinvindex.model.Contact;
import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Protocol;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;
import uk.ac.ebi.bioinvindex.model.processing.DataAcquisition;
import uk.ac.ebi.bioinvindex.model.processing.DataNode;
import uk.ac.ebi.bioinvindex.model.processing.DataProcessing;
import uk.ac.ebi.bioinvindex.model.processing.MaterialNode;
import uk.ac.ebi.bioinvindex.model.processing.MaterialProcessing;
import uk.ac.ebi.bioinvindex.model.processing.Node;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.model.processing.ProtocolApplication;
import uk.ac.ebi.bioinvindex.model.term.ParameterValue;
import uk.ac.ebi.bioinvindex.model.xref.ReferenceSource;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ProcessingPipelineRDFGenerator<NODE extends Identifiable>  extends RDFGenerator<NODE,Model>{
	//e.g. "http://toxbanktest1.opentox.org:8080/toxbank";
	protected final String TB_URI ;
	protected final String TBPROTOCOL_URI;

	
	/**
	 * I will graph all the instances of {@link #objects} which are {@link Processing} (and nodes, materials, etc.).
	 * WARNING: I need to assign a temporary IDs to the objecs, via {@link Identifiable#setId(Long)}, BE AWARE that
	 * the objects are changed if their ID is null. Otherwise ENSURE the IDs returned by {@link Identifiable#getId()}
	 * are distinct.
	 *
	 */
	public ProcessingPipelineRDFGenerator ( String toxbankURI, String prefix,  BIIObjectStore store , Model model) {
		super(prefix,store,model);
		getModel().setNsPrefix( "", prefix+"/" );
		getModel().setNsPrefix( "isa", ISA.URI );
		getModel().setNsPrefix( "tb", TOXBANK.URI );
		getModel().setNsPrefix( "foaf", FOAF.NS );
		getModel().setNsPrefix( "owl", OWL.NS );
		getModel().setNsPrefix( "dc", DC.NS );
		getModel().setNsPrefix( "dcterms", DCTerms.NS );
		getModel().setNsPrefix( "rdfs", RDFS.getURI() );
		getModel().setNsPrefix( "rdf", RDF.getURI() );
		getModel().setNsPrefix("xsd", XSDDatatype.XSD+"#");
		getModel().setNsPrefix("kw", ISA.TBKeywordsNS);
		this.TB_URI = toxbankURI;
		this.TBPROTOCOL_URI = String.format("%s%s/",TB_URI,Resources.protocol);
		ISA.init(getModel());
	}
	
	public ProcessingPipelineRDFGenerator (  String toxbankURI, String prefix,  BIIObjectStore store ) {
		this(toxbankURI,prefix, store,ModelFactory.createDefaultModel());
	}

	/**
	 * Creates the DOT string corresponding to the graph of {@link Processing} objects in the collection I manage
	 *
	 */
	public Model createGraph () throws Exception {
		//lookup for references
        for (Identifiable object: store.values(ReferenceSource.class)) {
	        	ReferenceSource xs = ((ReferenceSource)object);
	        	Resource xref = getResource(object, ISA.ReferenceSources);
	        	getModel().add(xref,DCTerms.title,xs.getName());
	        	if (xs.getDescription()!=null)
	        		getModel().add(xref,DCTerms.description,xs.getDescription());
	        	if (xs.getVersion() != null)
	        		getModel().add(xref,DCTerms.hasVersion,xs.getVersion());
	        	if (xs.getUrl()!=null)
	        		getModel().add(xref,RDFS.seeAlso,xs.getUrl());
	        	references.put(xs.getName(),xs);
	      }		
        
		Collection<Identifiable> objects = new ArrayList<Identifiable>();
        objects.addAll(store.values(Processing.class));
		for ( Identifiable object: objects )
		{
			if ( object == null ) continue;
			if ( object instanceof MaterialProcessing )
				graphProcessing ( (MaterialProcessing) object );
			else if ( object instanceof DataAcquisition )
				graphProcessing ( (DataAcquisition) object );
			else if ( object instanceof DataProcessing )
				graphProcessing ( (DataProcessing) object );
		}
		objects.clear();
		//why there are no URL for the ontologies???
		//FIXME - use owl:ontology and ontology uri

	    objects.clear();
	    objects.addAll(store.values(Study.class));   
	    for ( Identifiable object: objects )  {
	    	Study xs = ((Study)object);
	    	Resource xref = getResource(object, ISA.Study);
	    	for (Assay assay :xs.getAssays()) {
	    		Resource xassay = getResourceID(assay, ISA.Assay);	
	    		getModel().add(xref,ISA.HASASSAY,xassay);
	    	}
	    	/*
	    	for (Investigation inv :xs.getInvestigations()) {
	    		Resource xinv = getResource(inv, ISA.Investigation);	
	    		getModel().add(xref,ISA.HASASSAY,xassay);
	    	}
	    	*/
	    }

	    objects.clear();
	    objects.addAll(store.values(Investigation.class));   
	    for ( Identifiable object: objects )  {
	    	Investigation xs = ((Investigation)object);
	    	Resource xref = getResource(object, ISA.Investigation);
	    	for (Study study :xs.getStudies()) {
	    		Resource xstudy = getResourceID(study, ISA.Study);	
	    		getModel().add(xref,ISA.HASSTUDY,xstudy);
	    	}
	    }	   

		return getModel();
	}
	
	

	/** IN/OUT Node prefix to be used with IDs and labels
	private String getNodePrefx ( Node<?, ?> node ) {
		if ( node instanceof MaterialNode ) return "MN_";
		if ( node instanceof DataNode) return "DN_";
		return "";
	}
	 */



	/** Works on a single processing step */
	private Resource graphProcessing ( Processing<?, ?> processing ) throws Exception
	{

		Resource resource = ISA.PROCESSINGNODE;
		if ( processing instanceof MaterialProcessing ) {
			resource = ISA.MaterialProcessing;
		}
		else if ( processing instanceof DataAcquisition ) {
			resource = ISA.DataAcquisition;
		}
		else if ( processing instanceof DataProcessing ) {
			resource = ISA.DataProcessing;
		}
		
		Resource processingNode = getResource(processing, resource);
		
		for ( ProtocolApplication protoApp: processing.getProtocolApplications () ) {
			Resource protoAppNode = getResource(protoApp, ISA.ProtocolApplication);
			processingNode.addProperty(ISA.hasProtocolApplication, protoAppNode);
			
			Resource protocolNode = getResource(protoApp.getProtocol(), ISA.Protocol);
			protocolNode.addLiteral(RDFS.label, protoApp.getProtocol ().getName ());
			
			protoAppNode.addProperty(ISA.APPLIESPROTOCOL, protocolNode);
			for (ParameterValue paramValue: protoApp.getParameterValues()) {
				Resource xpv = getResource(paramValue, ISA.ParameterValue);
				protoAppNode.addProperty(ISA.HASPARAMVALUE, xpv);
			}
			

		}

		// for each input: input -> processing
		for ( Node<?, ?> node: processing.getInputNodes () ) {
			Resource inputNode = graphNode(node,false);
			processingNode.addProperty(ISA.HASINPUT, inputNode);
		}
		
		// for each out: processing -> out
		for (  Node<?, ?> node: processing.getOutputNodes () ) {
			Resource outputNode = graphNode(node,false);
			processingNode.addProperty(ISA.HASOUTPUT, outputNode);
		}		
		return processingNode;
		

	}


	/** Forwards to the specific node */
	private Resource graphNode ( Node<?, ?> node , boolean output) throws Exception 
	{
		if ( node instanceof MaterialNode ) return graphMaterialNode ( (MaterialNode) node , output);
		if ( node instanceof DataNode ) return graphDataNode ( (DataNode) node ,output);
		throw new RuntimeException ( "Don't know what to do with node of type " + node.getClass ().getName () );
	}
	

	/** Works on a single IN/OUT node */
	private Resource graphMaterialNode ( MaterialNode node , boolean output ) throws Exception
	{
		Resource materialNode = getResource(node, ISA.MaterialNode);
		Resource material = getResource(node.getMaterial(), ISA.Material);
		if (material != null) materialNode.addProperty(ISA.HASMATERIAL, material);
		return materialNode;

	}
	
	/** Works on a single IN/OUT node */

	/** Works on a single node */
	private Resource graphDataNode ( DataNode node , boolean output) throws Exception 
	{
		Resource dataNode = getResource(node, ISA.DataNode);
		Resource data = getResource(node.getData(), ISA.Data);
		if (data != null) dataNode.addProperty(ISA.HASDATA, data);
		return dataNode;

	}

	/**
     * This is a hack to introduce ToxBank specific URIs into the investigation file.
	 * @param investigation
	 */
	final static String TB_consortium = "comment:Consortium URI";
	final static String TB_organisation = "comment:Owning Organisation URI";
	final static String TB_user = "comment:Owner URI";
	final static String TB_keywords = "comment:Investigation keywords";

	
	final static String TB_author_uri = "comment:Investigation Person URI";
	final static String TB_author_term = "comment:Investigation Person URI Term Accession Number";
	final static String TB_author_termref = "comment:Investigation Person URI Term Source REF";


	public static final String OBO = "http://purl.obolibrary.org/obo/";
	public static final String BIBO = "http://purl.org/ontology/bibo/";
	public static final String EFO = "http://purl.org/ontology/efo/";
	public static final String CHEBI = "http://purl.org/ontology/chebi/";
	//http://bibotools.googlecode.com/svn/bibo-ontology/trunk/doc/index.html
	//TODO get from the ontology definition

	protected void parseToxBankSpecifics(Investigation investigation, Resource investigationResource) throws MalformedURLException {
		ProjectIO projectIO = new ProjectIO();
		OrganisationIO orgIO = new OrganisationIO();
		UserIO userIO = new UserIO();
		for (Annotation annotation: investigation.getAnnotations()) {
			
			String[] multiEntries = annotation.getText().split(";");
			for (String multiEntry:multiEntries) {
				if (TB_consortium.equals(annotation.getType().getValue())) {
					String[] uri = multiEntry.split(":");
					if (uri.length<2) throw new MalformedURLException(annotation.getText());
					Project tbProject = new Project( new URL(String.format("%s%s/%s",TB_URI,Resources.project, uri[1])));
					Resource  resource = projectIO.objectToJena(getModel(),tbProject);
					getModel().add(resource, RDF.type, TOXBANK.PROJECT); 
					getModel().add(investigationResource,TOXBANK.HASPROJECT,resource);
				} else if (TB_organisation.equals(annotation.getType().getValue())) {
					String[] uri = multiEntry.split(":");
					if (uri.length<2) throw new MalformedURLException(annotation.getText());
					Organisation tbOrg = new Organisation( new URL(String.format("%s%s/%s",TB_URI,Resources.organisation, uri[1])));
					Resource  resource = orgIO.objectToJena(getModel(),tbOrg);
					getModel().add(resource, RDF.type, TOXBANK.ORGANIZATION); 
					getModel().add(investigationResource,TOXBANK.HASORGANISATION,resource);
				} else if (TB_user.equals(annotation.getType().getValue())) {
					String[] uri = multiEntry.split(":");
					if (uri.length<2) throw new MalformedURLException(annotation.getText());
					User tbUser = new User( new URL(String.format("%s%s/%s",TB_URI,Resources.user, uri[1])));
					Resource  resource = userIO.objectToJena(getModel(),tbUser);
					getModel().add(resource, RDF.type, TOXBANK.USER); //should be a ToxBank user
					getModel().add(investigationResource,TOXBANK.HASOWNER,resource);				
				} else if (TB_keywords.equals(annotation.getType().getValue())) {
					String keyword = multiEntry;
					String[] uri = keyword.split(":");
					if (uri.length<2) continue;
					if ("TBK".equals(uri[0]) && (!"".equals(uri[1].trim()))) {
						//may be consider keywords resources, not literals? and update the Protocol RDF IO as well
						investigationResource.addLiteral(TOXBANK.HASKEYWORD, String.format("%s%s",ISA.TBKeywordsNS,uri[1]));
					}
				}
			}
		}
		
		for (Contact contact: investigation.getContacts()) {
			
			List<Annotation> uri = contact.getAnnotation(TB_author_uri);
			if ((uri==null) || (uri.size()<1)) continue;
			List<Annotation> ref = contact.getAnnotation(TB_author_termref);
			if ((ref==null) || (ref.size()<1)) continue;
			List<Annotation> title = contact.getAnnotation(TB_author_term);
			if ((title==null) || (title.size()<1)) continue;
			//m/b smth is wrong with comments; why they come as separate annotations and  freetext types?
			/*
			System.out.println(uri.get(0).getText());
			System.out.println(ref.get(0).getText());
			System.out.println(title.get(0).getText());
			*/

			User tbUser = new User( new URL(String.format("%s%s/%s",TB_URI,Resources.user, uri.get(0).getText())));
			Resource  resource = userIO.objectToJena(getModel(),tbUser);
			getModel().add(resource, RDF.type, TOXBANK.USER); //should be a ToxBank user
			getModel().add(investigationResource,TOXBANK.HASAUTHOR,resource);			
			try {
				getModel().add(resource, OWL.sameAs, getResourceID(contact, ISA.Contact)); 
			} catch (Exception x) {}
			
		}
		/**
		 * CiTO
		 * http://purl.org/spar/cito/cites
		 * BIBO
		 * http://bibliontology.com/content/article
		 */
		/*
		//final String BIBO = "http://bibliontology.com/content"; 
		for (Publication pub : investigation.getPublications()) {
			getResourceID(pub,)
        	String uri = experiment.getURI()+"/publication/"+i;
        	Individual p = getClass(OBO,"IAO_0000311").createIndividual(uri);
        	p.addProperty(DC.title, pub.getTitle());
        	if (pub.getPmid()!=null)
            
            	addDatatypeProperty(p,DC,"identifier",inv.IDF.pubMedId.get(i));
            if (inv.IDF.publicationDOI.size() > i && inv.IDF.publicationDOI.get(i) != null)
            	addDatatypeProperty(p,DC,"identifier",inv.IDF.publicationDOI.get(i));
            if (inv.IDF.publicationAuthorList.size() > i && inv.IDF.publicationAuthorList.get(i) != null)
            	addDatatypeProperty(p,DC,"creator",inv.IDF.publicationAuthorList.get(i));
            addObjectProperty(experiment,OBO,"IAO_0000142",p);
        }		
        */
	}

	
	@Override
	public void processAnnotations(Investigation investigation, Resource investigationResource)
			throws Exception {
		parseToxBankSpecifics(investigation,investigationResource);
	}
	@Override
	public void processProtocolType(Protocol protocol, Resource protocolResource)
			throws Exception {
		if (protocol.getType()==null) return;
		if (protocol.getType().getSource()!=null) {
			String uri = protocol.getType().getSource().getUrl();
			if (!uri.endsWith("/")) uri = uri + "/";
			if (TBPROTOCOL_URI.equals(uri)) {
				getModel().add(protocolResource, RDF.type, TOXBANK.PROTOCOL); 
			}
		}
		//System.out.println(protocol.getAnnotations());
	}
	@Override
	protected String getProtocolURI(Protocol protocol) throws Exception {
		if ((protocol.getType()!=null) && (protocol.getType().getSource()!=null)) {
			String uri = protocol.getType().getSource().getUrl();
			if (uri!=null) {
				if (!uri.endsWith("/")) uri = uri + "/";
				if (TBPROTOCOL_URI.equals(uri)) 
					return String.format("%s%s%s",uri,uri.endsWith("/")?"":"/",protocol.getType().getAcc());
			}
		}
		return super.getProtocolURI(protocol);
	}
}