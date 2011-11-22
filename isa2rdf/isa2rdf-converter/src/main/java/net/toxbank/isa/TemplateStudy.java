package net.toxbank.isa;


import net.toxbank.isa2rdf.ColumnHeader;
import net.toxbank.isa2rdf.ISA;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TemplateStudy extends TemplateCollection {
	
	public TemplateStudy(OntModel model) {
		this(null,null,model);
	}
	public TemplateStudy(String uri,String name,OntModel model) {
		super(model.createClass(uri));
		
		
		OntClass node = ISA.ISAClass.ISANode.createOntClass(model);
		OntClass namedNode = ISA.ISAClass.NamedNode.createOntClass(model);
		getModel().add(namedNode,RDFS.subClassOf,node);
		
		OntClass study = ISA.ISAClass.Study.createOntClass(model);
		OntClass assay = ISA.ISAClass.Assay.createOntClass(model);
		getModel().add(resource,RDFS.subClassOf,study);
		
		OntClass collection = ISA.ISAClass.ISACollection.createOntClass(model);
		model.add(study,RDFS.subClassOf,collection);
		Property hasAssay = ISA.ISAObjectProperty.hasAssay.createProperty(model);
		model.add(hasAssay,RDFS.domain,study);
		model.add(hasAssay,RDFS.range,assay);
		
		OntClass studyEntry = ISA.ISAClass.StudyEntry.createOntClass(model);
		Property isPartOf = ISA.ISAObjectProperty.isPartOfStudy.createProperty(model);
		model.add(isPartOf,RDFS.domain,studyEntry);
		model.add(isPartOf,RDFS.range,study);
		Property pi = ISA.ISAObjectProperty.isPartOfStudy.createInverse(model);
		model.add(pi,RDFS.range,studyEntry);
		model.add(pi,RDFS.domain,study);
		
		Property isPartOfCollection = ISA.ISAObjectProperty.isPartOfCollection.createProperty(model);
		getModel().add(isPartOf,RDFS.subPropertyOf,isPartOfCollection);

		
		OntClass studyNode = ISA.ISAClass.StudyNode.createOntClass(model);
		getModel().add(studyNode,RDFS.subClassOf,namedNode);
		Property isPartOfEntry = ISA.ISAObjectProperty.isPartOfStudyEntry.createProperty(model);
		model.add(isPartOfEntry,RDFS.domain,studyNode);
		model.add(isPartOfEntry,RDFS.range,studyEntry);
		pi = ISA.ISAObjectProperty.isPartOfStudyEntry.createInverse(model);
		model.add(pi,RDFS.range,studyNode);
		model.add(pi,RDFS.domain,studyEntry);

		Property p = ISA.ISAObjectProperty.isPartOfEntry.createProperty(model);
		getModel().add(isPartOfEntry,RDFS.subPropertyOf,p);
		ISA.ISAObjectProperty.isPartOfEntry.createInverse(model);

		
		OntClass entry = ISA.ISAClass.ISAEntry.createOntClass(model);
		getModel().add(studyEntry,RDFS.subClassOf,entry);
		model.add(isPartOfCollection,RDFS.domain, entry);
		model.add(isPartOfCollection,RDFS.range,collection);

		OntClass protocol = ISA.ISAClass.Protocol.createOntClass(model);
		getModel().add(protocol,RDFS.subClassOf,node);
		
		Property param = ISA.ISAObjectProperty.hasParameter.createProperty(getModel());
		getModel().add(param,RDFS.domain,protocol);
		getModel().add(param,RDFS.range,ISA.ISAClass.Parameter.createOntClass(getModel()));			

		
		Property factors = ISA.ISAObjectProperty.hasFactor.createProperty(getModel());
		getModel().add(factors,RDFS.domain,namedNode);
		getModel().add(factors,RDFS.range,ISA.ISAClass.Value.createOntClass(getModel()));		
		
		Property chars = ISA.ISAObjectProperty.hasCharacteristic.createProperty(getModel());
		getModel().add(chars,RDFS.domain,namedNode);
		getModel().add(chars,RDFS.range,ISA.ISAClass.Value.createOntClass(getModel()));	
		
		Property comm = ISA.ISAObjectProperty.hasCharacteristic.createProperty(getModel());
		getModel().add(comm,RDFS.domain,namedNode);
		getModel().add(comm,RDFS.range,ISA.ISAClass.Value.createOntClass(getModel()));		
		
		Property next = ISA.ISAObjectProperty.hasNext.createProperty(getModel());
		getModel().add(next,RDFS.domain,node);
		getModel().add(next,RDFS.range,node);		
		
		if (name != null) model.add(resource,RDFS.label,name);
	}
	/*
	public static TemplateStudy createFromISATAB(String studyName,ColumnHeader[] headers,String uri,OntModel model) {
		TemplateStudy template = new TemplateStudy(uri,model);
	}
	*/
	
	protected TemplateStudy(String uri,OntClass clazz) {
		super(uri,clazz);
	}
	/*
	public TemplateStudy createSubclass(String uri) throws Exception {
		OntModel model = ((OntModel)resource.getModel());
		OntClass subclass = uri==null?model.createClass():model.createClass(uri);
		model.add(subclass,RDFS.subClassOf,resource);
		return new TemplateStudy(uri,subclass);
	}
	*/
	@Override
	public TemplateRow createRowTemplate(ColumnHeader[] headers, String uri)
			throws Exception {
		return new TemplateRowStudy(uri,this);
	}
	
	@Override
	protected TemplateNode createNode(ColumnHeader header,String uri) throws Exception {
		if (rowTemplate==null) throw new Exception("No row template is set!");
		return new TemplateNodeStudy(header, uri, (TemplateRowStudy)rowTemplate);
	}
	
	public AStudy parse(ColumnHeader[] headers,String[][] tabs) throws Exception {
		AStudy study = new AStudy(String.format("%s/Study/S1",getResource().getURI()),this);
		for (int i=0; i < tabs.length;i++) {
			RowStudy row = study.addRow(String.format("%s/Row/R%d", study.getResource().getURI(),i+1));
			row.parse(headers,tabs[i]);
			
		}
		return study;
	}
	/*
	@Override
	public OntClass addProtocolNodeClass(ColumnHeader header, String uri) throws Exception {
		OntClass p = super.addProtocolNodeClass(header, uri);
		getModel().add(p,RDFS.subClassOf,ISA.ISAClass.StudyNode.createOntClass(getModel()));
		return p;
	}
	*/
}
