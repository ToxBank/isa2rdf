package net.toxbank.isa.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import net.toxbank.isa.AStudy;
import net.toxbank.isa.AnAssay;
import net.toxbank.isa.TemplateAssay;
import net.toxbank.isa.TemplateStudy;
import net.toxbank.isa2rdf.ColumnHeader;
import net.toxbank.isa2rdf.ISA;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

public class TestISAObjects {

	
	public void testCreateAnonymousStudy() throws Exception {
		AStudy study = new AStudy(new TemplateStudy(ISA.createModel()));
		printModel(study.getModel());
	}
	
	@Test
	public void testCreateStudy() throws Exception {
		String uri = "http://example.com/study/";
		OntModel model = ISA.createModel();

		//model.setNsPrefix("UO", "http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=UO");
		model.setNsPrefix("CHEBI", "http://bioportal.bioontology.org/ontologies/46336/");
		
			

		//model.setNsPrefix("", uri);
		TemplateStudy ts = new TemplateStudy(String.format("%sDR",uri),"Dose Response study",model);
		ColumnHeader[] headers1 = new ColumnHeader[] { 
				new ColumnHeader("Source Name", 0),
				new ColumnHeader("Factor Value[compound]", 1),
				new ColumnHeader("Term Source REF", 2),
				new ColumnHeader("Term Accession Number", 3),	
				new ColumnHeader("Factor Value[dose]", 4),
				new ColumnHeader("UNIT", 5),
				new ColumnHeader("Term Source REF", 6),
				new ColumnHeader("Term Accession Number", 7),					
				new ColumnHeader("Factor Value[plate_replicate]", 8),
				new ColumnHeader("Protocol REF", 9),
				new ColumnHeader("Sample Name", 10),
		};
		
		ts.parseHeader(headers1, String.format("%s/header",ts.getResource().getURI()));
		
		String[][] tabs = new String[8][11]; 
		/*
		{{
				  "plate1", //1
				  "paracetamol", //2
				  "CHEBI", //3
				  "46195", //4
				  "2.5", //5
				  "mg/l", //6
				  "UO", //7
				  "273", //8
				  "1", //9
				  "Prepare test concentrations", //10
				  "C1-plate1" //11
				  },
				  {
					  "plate1", //1
					  "paracetamol", //2
					  "CHEBI", //3
					  "46195", //4
					  "5", //5
					  "mg/l", //6
					  "UO", //7
					  "273", //8
					  "1", //9
					  "Prepare test concentrations", //10
					  "C2-plate1" //11
				  }	,		
				  {
					  "plate1", //1
					  "paracetamol", //2
					  "CHEBI", //3
					  "46195", //4
					  "12.5", //5
					  "mg/l", //6
					  "UO", //7
					  "273", //8
					  "1", //9
					  "Prepare test concentrations", //10
					  "C3-plate1" //11
				  }						  
				  };
				  */
		for (int i=0;i < 8;i++) {
			tabs[i]= new String[]
			  {
				  "plate1", //1
				  "paracetamol", //2
				  "CHEBI", //3
				  "46195", //4
				  Double.toString((i+1)*0.5), //5
				  "mg/l", //6
				  "UO", //7
				  "273", //8
				  "1", //9
				  "Prepare test concentrations", //10
				  String.format("C%d-plate1",i+1) //11
			  }		;
		}
		
		AStudy study = ts.parse(headers1, tabs);
		
		
		TemplateAssay ta = new TemplateAssay(String.format("%s/assay1",ts.getResource().getURI()),
					"Dose Response Assay",model);
	
		ColumnHeader[] headers2 = new ColumnHeader[] { 
				new ColumnHeader("Sample Name", 0),
				new ColumnHeader("Characteristics[well]", 1),
				new ColumnHeader("Factor Value[replicate]", 2),
				new ColumnHeader("Protocol REF", 3),
				new ColumnHeader("Assay Name", 4),
				new ColumnHeader("RAW Data File", 5),
				new ColumnHeader("Protocol REF", 6),
				new ColumnHeader("Dose Response Name", 7),
				new ColumnHeader("Derived Data File", 8),
		};
		ta.parseHeader(headers2, String.format("%s/header",ta.getResource().getURI()));		
		/*
		tabs = new String[][] {{
			  "C1-plate1", //1
			  "B3", //2
			  "r1", //3
			  "Neutral Red Uptake (NRU) Cytotoxicity Test", //4
			  "C1-plate1-B3", //5
			  "acetaminophen-plate1-data.txt", //6
			  "Hill function analysis", //7
			  "acetaminophen-plate1-ic50", //8
			  "ic50.txt" //9
			  },
			  {
				  "C2-plate1", //1
				  "B4", //2
				  "r1", //3
				  "Neutral Red Uptake (NRU) Cytotoxicity Test", //4
				  "C2-plate1-B4", //5
				  "acetaminophen-plate1-data.txt", //6
				  "Hill function analysis", //7
				  "acetaminophen-plate1-ic50", //8
				  "ic50.txt" //9
			  }	,		
			  {
				  "C3-plate1", //1
				  "B5", //2
				  "r1", //3
				  "Neutral Red Uptake (NRU) Cytotoxicity Test", //4
				  "C3-plate1-B5", //5
				  "acetaminophen-plate1-data.txt", //6
				  "Hill function analysis", //7
				  "acetaminophen-plate1-ic50", //8
				  "ic50.txt" //9
			  }						  
			  };
		*/
		tabs = new String[8][9];
		for (int i=0;i < 8;i++) {
			tabs[i]= new String[]
			  {
					  String.format("C%d-plate1",i+1),
					  String.format("B%d",i+3), //2
					  "r1", //3
					  "Neutral Red Uptake (NRU) Cytotoxicity Test", //4
					  String.format("C%d-plate1-B%d",i+1,i+3), //5
					  "acetaminophen-plate1-data.txt", //6
					  "Hill function analysis", //7
					  "acetaminophen-plate1-ic50", //8
					  "ic50.txt" //9
			  }		;
		}
		AnAssay assay = ta.parse(headers2, tabs);
		
		study.addAssay(assay);
		/*
	/*
		AStudy study = new AStudy(String.format("%s/Study/S1",ts.getResource().getURI()),ts);
		Assert.assertNotNull(study.getModel().getResource(uri));
		Assert.assertTrue(study.getResource() instanceof Individual);
		
		model.setNsPrefix("s1", study.getResource().getURI()+"/Row/");
		
		RowStudy rows1 = study.addRow(String.format("%s/Row/R%d", study.getResource().getURI(),1),
				headers1, 
				new String[] {"AAA",
							  "A1",
							  "paracetamol",
							  "0.01",
							  "mg",
							  "A",
							  "NR",
							  "that's it"
							  }
		);
		rows1.setLabel("label1");
		
		RowStudy rows2 = study.addRow("http://example.com/study/row2",headers2,new String[] {"AAA","CCC","CCC","FFF"});
		rows2.setLabel("label2");
		
		AnAssay assay = study.addAssay("http://example.com/assay",ta);
		RowAssay row1 = assay.addRow("http://example.com/study/Assay1",
											headers1,
											new String[] {"DDD","12345","P1","X"});
		row1.setLabel("a1");
		RowAssay row2 = assay.addRow("http://example.com/study/Assay2",
											headers2,
											new String[] {"FFF","12345","P2","Y"});
		row2.setLabel("l2");
		//row1.addNode("http://example.com/study/Assay1_node1");
		//row2.addNode("http://example.com/study/Assay1_node2");
		*/
		printModel(ts.getModel());
		
		FileOutputStream output = new FileOutputStream(new File("d:\\","isatab.owl"));
		ISA.writeStream(ts.getModel(), output, "application/rdf+xml", true);
		
		output.close();
	}	
	/*
	@Test
	public void testCreateStudySubclass() throws Exception {
		String uri = "http://example.com/study";
		String uri1 = "http://example.com/studytemplate1";
		String uri2 = "http://example.com/studytemplate2";
		TemplateStudy st = new TemplateStudy(uri1,ISA.createModel());
		
		AStudy study = new AStudy(uri,st.createSubclass(uri2));
		printModel(study.getModel());
		Assert.assertNotNull(study.getModel().getResource(uri));
		
	}		
	*/
	public void printModel(Model model) throws Exception {
		StringWriter w = new StringWriter();
		ISA.write(model,  w, "text/n3", true);
		System.out.println(w);
	}
}
