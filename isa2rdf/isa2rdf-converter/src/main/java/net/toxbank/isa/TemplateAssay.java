package net.toxbank.isa;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TemplateAssay extends TemplateCollection {
	public TemplateAssay(OntModel model) {
		this(null,null,model);
	}
	public TemplateAssay(String uri,String name,OntModel model) {
		super(model.createClass(uri));
		OntClass assay = ISAClass.Assay.createOntClass(model);
		getModel().add(resource,RDFS.subClassOf,assay);
		
		if (name != null) model.add(resource,RDFS.label,name);
	}
	protected TemplateAssay(String uri,OntClass clazz) {
		super(uri,clazz);
	}
	public TemplateAssay createSubclass(String uri) throws Exception {
		OntModel model = ((OntModel)resource.getModel());
		OntClass subclass = uri==null?model.createClass():model.createClass(uri);
		model.add(subclass,RDFS.subClassOf,resource);
		return new TemplateAssay(uri,subclass);
	}
	public TemplateRowAssay createRowTemplate() throws Exception {
		return new TemplateRowAssay(this);
	}

	@Override
	public TemplateRow createRowTemplate(ColumnHeader[] headers, String uri)
			throws Exception {
		return new TemplateRowAssay(uri,this);
	}
	
	
	@Override
	protected TemplateNode createNode(ColumnHeader header,String uri) throws Exception {
		return new TemplateNodeAssay(header, uri, (TemplateRowAssay)rowTemplate);
	}
	
	
	public AnAssay parse(ColumnHeader[] headers,String[][] tabs) throws Exception {
		AnAssay assay = new AnAssay(String.format("%s/Assay",getResource().getURI()),this);
		for (int i=0; i < tabs.length;i++) {
			String uri = String.format("%s/R%d", assay.getResource().getURI(),i+1);
			RowAssay row = parse(assay,headers,tabs[i],uri);
		}
		return assay;
	}	


	public RowAssay parse(AnAssay assay, ColumnHeader[] headers,String[] tabs, String uri) throws Exception {
		RowAssay row = assay.addRow(uri);
		row.parse(headers,tabs);
		return row;
	}		
	/*
	@Override
	public OntClass addProtocolNodeClass(ColumnHeader header, String uri) throws Exception {
		OntClass p = super.addProtocolNodeClass(header, uri);
		getModel().add(p,RDFS.subClassOf,ISA.ISAClass.AssayNode.createOntClass(getModel()));
		return p;
	}
	*/
}
