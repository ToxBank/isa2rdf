package net.toxbank.isa;

import net.toxbank.isa2rdf.ColumnHeader;
import net.toxbank.isa2rdf.ISA;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

public abstract class TemplateCollection extends AnyISAObject<OntClass> {
	protected TemplateRow rowTemplate;
	
	public TemplateCollection(OntModel model) {
		this(null,model);
	}
	public TemplateCollection(String uri,OntModel model) {
		super(uri,ISA.ISAClass.ISACollection.createOntClass(model));
	}
	protected TemplateCollection(OntClass resource) {
		this(null,resource);
	}
	protected TemplateCollection(String uri,OntClass resource) {
		super(uri,resource);
	}
	
	protected abstract TemplateNode createNode(ColumnHeader header,String uri) throws Exception;
	public abstract TemplateRow createRowTemplate(ColumnHeader[] headers, String uri) throws Exception ;
	
	protected void setNextNode(String uri,OntClass node,OntClass nextNode) {
		
		Property nextProperty = getModel().createObjectProperty(uri,true);
		getModel().add(nextProperty,RDFS.subPropertyOf,ISA.ISAObjectProperty.hasNext.createProperty(getModel()));
		getModel().add(nextProperty,RDFS.domain,node);
		getModel().add(nextProperty,RDFS.range,nextNode);
		

	}

	public void parseHeader(ColumnHeader[] headers,String uri) throws Exception {
		rowTemplate = createRowTemplate(headers, uri);
		
		/*
		 * getModel().setNsPrefix("proto",String.format("%s/Protocol/",resource.getURI()));
		getModel().setNsPrefix("node",String.format("%s/node/",resource.getURI()));
		getModel().setNsPrefix("next",String.format("%s/Next/",resource.getURI()));

		getModel().setNsPrefix("factor",String.format("%s/Factor/",resource.getURI()));
		getModel().setNsPrefix("file",String.format("%s/File/",resource.getURI()));
	*/	
		int lastIndex = -1;
		TemplateNode node = null;
		OntClass classNode = null;
		
		for (int i=0; i < headers.length; i++) {
			if (headers[i].isNamedNode()) {
				node = createNode(headers[i],String.format("%s/node/N%d",resource.getURI(),i+1));
				headers[i].setResource(node);
				
				String nuri = String.format("%s/Next/N%d",resource.getURI(),i+1);
				if (classNode!=null) setNextNode(nuri, classNode, (OntClass) node.getResource());
				
				classNode = (OntClass) node.getResource();
				lastIndex = i;
			} else if (headers[i].isProtocol()) {
				String puri = String.format("%s/Protocol/P%d",resource.getURI(),i+1);
				OntClass protocolClass = addProtocolNodeClass(headers[i],puri);
				headers[i].setResource(protocolClass);

				String nuri = String.format("%s/Next%d",resource.getURI(),i+1);
				if (classNode!=null) setNextNode(nuri, classNode, protocolClass);
				classNode = protocolClass;
				//lastIndex = i;
			} else if (headers[i].isFactor()) {
				headers[i].setResource(node.addFactor(headers[i],String.format("%s/Factor/F%d",resource.getURI(),i+1)));
				lastIndex = i;
			} else if (headers[i].isCharacteristic()) {
				headers[i].setResource(node.addCharacteristics(headers[i],String.format("%s/Char/C%d",resource.getURI(),i+1)));
				lastIndex = i;
			} else if (headers[i].isComment()) {

				headers[i].setResource(node.addComment(headers[i],String.format("%s/Comment/C%d",resource.getURI(),i+1)));
				lastIndex = i;
			} else if (headers[i].isPerformer()) {

			//	header[i].setResource(node.addPerformer(node));
			} else if (headers[i].isParameter()) {

			//	header[i].setResource(node.addParameter(header[i]));
				lastIndex = i;
			} else if (headers[i].isDate()) {

			//	header[i].setResource(model.addDate(node, header[i], i+1));
			} else if (headers[i].isFile()) {

				headers[i].setResource(node.addFile(headers[i],String.format("%s/File/F%d",resource.getURI(),i+1)));
			} else if (headers[i].isUnit()) {
				if (lastIndex>=0) headers[lastIndex].setUnit(headers[i]);
				lastIndex = i; //units can have terms!
			} else if (headers[i].isTermREF()) {
				if (lastIndex>=0) headers[lastIndex].setTermREF(headers[i]);			
			} else if (headers[i].isTermNo()) {
				if (lastIndex>=0) headers[lastIndex].setTermNo(headers[i]);						
			} else {
				System.err.println("What we don't support yet???\t"+headers[i]+"\t"+headers[lastIndex]);
			}
			
			
		}
	
	}	
	

	public OntClass addProtocolNodeClass(ColumnHeader header, String uri) throws Exception {

		OntClass protocolClass = getModel().createClass(uri);
		getModel().add(protocolClass,RDFS.subClassOf,ISA.ISAClass.Protocol.createOntClass(getModel()));
		getModel().add(protocolClass,RDFS.label,header.getTitle());
		
		return protocolClass;

	}	


}
