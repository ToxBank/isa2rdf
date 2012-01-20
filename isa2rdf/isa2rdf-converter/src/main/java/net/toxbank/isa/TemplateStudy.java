package net.toxbank.isa;


import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TemplateStudy extends TemplateCollection {
	
	public TemplateStudy(OntModel model) {
		this(null,null,model);
	}
	public TemplateStudy(String uri,String name,OntModel model) {
		super(model.createClass(uri));
		
		OntClass study = ISAClass.Study.createOntClass(model);
		model.add(resource,RDFS.subClassOf,study);
		
		if (name != null) model.add(resource,RDFS.label,name);
	}

	protected TemplateStudy(String uri,OntClass clazz) {
		super(uri,clazz);
	}

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
			RowStudy row = parse(study,headers,tabs[i],String.format("%s/Row/R%d", study.getResource().getURI(),i+1));
			row.parse(headers,tabs[i]);
			
		}
		return study;
	}

	public RowStudy parse(AStudy study, ColumnHeader[] headers,String[] tabs, String uri) throws Exception {
		RowStudy row = study.addRow(uri);
		row.parse(headers,tabs);
		return row;
	}	
}
