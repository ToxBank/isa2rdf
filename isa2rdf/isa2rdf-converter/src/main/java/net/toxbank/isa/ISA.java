package net.toxbank.isa;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.toxbank.isa.parser.InvestigationParser;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ISA {
	/** <p>The RDF model that holds the vocabulary terms</p> */
	private static Model m_model = ModelFactory.createDefaultModel();
	/** <p>The namespace of the vocabalary as a string ({@value})</p> */
	public static final String _NS = "http://www.toxbank.net/isa.owl#%s";

	public static final String NS = String.format(_NS,"");
	
	public static String getURI() {return NS;}
	/** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    
	
	protected String prefix = "http://toxbank.net/isa/";
	
	public ISA()  {
		
	}
	public ISA(String prefix)  {
		this.prefix = prefix;
	}
	

	public static void main(String[] args) {
		if (args.length>0) try {
			ISA parser = new ISA();
			File dir = new File(args[0]);
			Model model = parser.parse(dir);
			String filename = dir.getName();
			if (!dir.isDirectory()) dir = dir.getParentFile();
			FileWriter output = new FileWriter(new File(dir,String.format("%s.n3",filename)));
			ISA.write(model, output, "text/n3", true);
			output.close();

			output = new FileWriter(new File(dir,String.format("%s.owl",filename)));
			ISA.write(model, output, "application/rdf+xml", true);
			output.close();
		} catch (Exception x) {
			x.printStackTrace();
		} else 
			System.out.println(String.format("%s filename\nFilename: An ISA-TAB investigation file or i_*.txt file or a directory with ISA-TAB file", 
							"ISAParser"));
			
	}
	public Model parse(File dir) throws Exception {
		String[] files;	
		if (!dir.isDirectory()) {
			InvestigationFileNameFilter filter = new InvestigationFileNameFilter();
			if (filter.accept(dir,dir.getName())) {//ok, investigation file
				files = new String[] {dir.getName()};
				dir = dir.getParentFile();
			} else
				throw new Exception(String.format("%s is not a directory",dir));
		} else 
			files = dir.list(new InvestigationFileNameFilter());

		OntModel model = ISA.createModel(true);
		final String prefixDir = String.format("%s%s/",prefix,dir.getName());
		model.setNsPrefix(dir.getName(), prefixDir);
		//investigation
		for (String filename:files) {
			FileReader reader = new FileReader(new File(dir,filename));
			InvestigationParser iparser = new InvestigationParser(dir,prefixDir,filename,reader, model);
			while (iparser.hasNext()) {
				iparser.next();
			}
			reader.close();
		}
		return model;
	}

    public static OntModel createModel(boolean initOntology) throws Exception {
    	OntModel model = createModel(OntModelSpec.OWL_DL_MEM);
    	initModel(model);
    	return model;
    }
	public static OntModel createModel(OntModelSpec spec) throws Exception {
		OntModel jenaModel = ModelFactory.createOntologyModel( spec,null);

		jenaModel.setNsPrefix( "isa", ISA.NS );
		jenaModel.setNsPrefix( "owl", OWL.NS );
		jenaModel.setNsPrefix( "dc", DC.NS );
		jenaModel.setNsPrefix( "dcterms", DCTerms.NS );
		jenaModel.setNsPrefix("xsd", XSDDatatype.XSD+"#");
		return jenaModel;
	}
	public static void writeStream(Model jenaModel, OutputStream output, String mediaType, boolean isXml_abbreviation) throws IOException {
		write(jenaModel,new OutputStreamWriter(output),mediaType,isXml_abbreviation);
	}
    public static void write(Model jenaModel, Writer output, String mediaType, boolean isXml_abbreviation) throws IOException {
    	try {
    		RDFWriter fasterWriter = null;
			if ("application/rdf+xml".equals(mediaType)) {
				if (isXml_abbreviation)
					fasterWriter = jenaModel.getWriter("RDF/XML-ABBREV");//lot smaller ... but could be slower
				else
					fasterWriter = jenaModel.getWriter("RDF/XML");
				fasterWriter.setProperty("xmlbase",jenaModel.getNsPrefixURI(""));
				fasterWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
				fasterWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);
			}
			else if (mediaType.equals("application/x-turtle"))
				fasterWriter = jenaModel.getWriter("TURTLE");
			else if (mediaType.equals("text/n3"))
				fasterWriter = jenaModel.getWriter("N3");
			else if (mediaType.equals("text/n-triples"))
				fasterWriter = jenaModel.getWriter("N-TRIPLE");	
			else {
				fasterWriter = jenaModel.getWriter("RDF/XML-ABBREV");
				fasterWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
				fasterWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);	//essential to get XSD prefixed
				fasterWriter.setProperty("xmlbase",jenaModel.getNsPrefixURI(""));
			}
			
			fasterWriter.write(jenaModel,output,"http://www.owl-ontologies.com/toxbank.owl");

    	} catch (Exception x) {
    		Throwable ex = x;
    		while (ex!=null) {
    			if (ex instanceof IOException) 
    				throw (IOException)ex;
    			ex = ex.getCause();
    		}
    	} finally {

    		try {if (output !=null) output.flush(); } catch (Exception x) { x.printStackTrace();}
    	}
    }
	public static void initModel(OntModel model) throws Exception {
		
		OntClass node = ISAClass.ISANode.createOntClass(model);
		OntClass namedNode = ISAClass.NamedNode.createOntClass(model);
		model.add(namedNode,RDFS.subClassOf,node);
		
		OntClass study = ISAClass.Study.createOntClass(model);
		OntClass assay = ISAClass.Assay.createOntClass(model);
		
		OntClass collection = ISAClass.ISACollection.createOntClass(model);
		model.add(study,RDFS.subClassOf,collection);
		Property hasAssay = ISAObjectProperty.hasAssay.createProperty(model);
		model.add(hasAssay,RDFS.domain,study);
		model.add(hasAssay,RDFS.range,assay);
		
		OntClass studyEntry = ISAClass.StudyEntry.createOntClass(model);
		Property isPartOf = ISAObjectProperty.isPartOfStudy.createProperty(model);
		model.add(isPartOf,RDFS.domain,studyEntry);
		model.add(isPartOf,RDFS.range,study);
		Property pi = ISAObjectProperty.isPartOfStudy.createInverse(model);
		model.add(pi,RDFS.range,studyEntry);
		model.add(pi,RDFS.domain,study);
		
		Property isPartOfCollection = ISAObjectProperty.isPartOfCollection.createProperty(model);
		model.add(isPartOf,RDFS.subPropertyOf,isPartOfCollection);

		
		OntClass studyNode = ISAClass.StudyNode.createOntClass(model);
		model.add(studyNode,RDFS.subClassOf,namedNode);
		Property isPartOfStudyEntry = ISAObjectProperty.isPartOfStudyEntry.createProperty(model);
		model.add(isPartOfStudyEntry,RDFS.domain,studyNode);
		model.add(isPartOfStudyEntry,RDFS.range,studyEntry);
		pi = ISAObjectProperty.isPartOfStudyEntry.createInverse(model);
		model.add(pi,RDFS.range,studyNode);
		model.add(pi,RDFS.domain,studyEntry);

		Property partOfEntry = ISAObjectProperty.isPartOfEntry.createProperty(model);
		model.add(isPartOfStudyEntry,RDFS.subPropertyOf,partOfEntry);
		ISAObjectProperty.isPartOfEntry.createInverse(model);

		
		OntClass entry = ISAClass.ISAEntry.createOntClass(model);
		model.add(studyEntry,RDFS.subClassOf,entry);
		model.add(isPartOfCollection,RDFS.domain, entry);
		model.add(isPartOfCollection,RDFS.range,collection);

		OntClass protocol = ISAClass.Protocol.createOntClass(model);
		model.add(protocol,RDFS.subClassOf,node);
		
		Property param = ISAObjectProperty.hasParameter.createProperty(model);
		model.add(param,RDFS.domain,protocol);
		model.add(param,RDFS.range,ISAClass.Parameter.createOntClass(model));			

		
		Property factors = ISAObjectProperty.hasFactor.createProperty(model);
		model.add(factors,RDFS.domain,namedNode);
		model.add(factors,RDFS.range,ISAClass.Value.createOntClass(model));		
		
		Property chars = ISAObjectProperty.hasCharacteristic.createProperty(model);
		model.add(chars,RDFS.domain,namedNode);
		model.add(chars,RDFS.range,ISAClass.Value.createOntClass(model));	
		
		Property comm = ISAObjectProperty.hasCharacteristic.createProperty(model);
		model.add(comm,RDFS.domain,namedNode);
		model.add(comm,RDFS.range,ISAClass.Value.createOntClass(model));		
		
		Property next = ISAObjectProperty.hasNext.createProperty(model);
		model.add(next,RDFS.domain,node);
		model.add(next,RDFS.range,node);		
		
		///

		model.add(assay,RDFS.subClassOf,ISAClass.ISACollection.createOntClass(model));
		
		OntClass assayEntry = ISAClass.AssayEntry.createOntClass(model);
		Property isPartOfAssay = ISAObjectProperty.isPartOfAssay.createProperty(model);
		model.add(isPartOfAssay,RDFS.domain,assayEntry);
		model.add(isPartOfAssay,RDFS.range,assay);
		model.add(isPartOfAssay,RDFS.subPropertyOf,isPartOfCollection);
		Property pia = ISAObjectProperty.isPartOfAssay.createInverse(model);
		model.add(pia,RDFS.range,assayEntry);
		model.add(pia,RDFS.domain,assay);
		
		OntClass assayNode = ISAClass.AssayNode.createOntClass(model);
		model.add(assayNode,RDFS.subClassOf,ISAClass.NamedNode.createOntClass(model));		
		Property isPartAssayOfEntry = ISAObjectProperty.isPartOfAssayEntry.createProperty(model);
		model.add(isPartAssayOfEntry,RDFS.domain,assayNode);
		model.add(isPartAssayOfEntry,RDFS.range,assayEntry);
		pi = ISAObjectProperty.isPartOfAssayEntry.createInverse(model);
		model.add(pi,RDFS.range,assayNode);
		model.add(pi,RDFS.domain,assayEntry);	
		
		model.add(isPartAssayOfEntry,RDFS.subPropertyOf,partOfEntry);
		
		model.add(assayEntry,RDFS.subClassOf,entry);
		
		Property files = ISAObjectProperty.hasDataset.createProperty(model);
		model.add(files,RDFS.domain,assayNode);
		model.add(files,RDFS.range,ISAClass.Dataset.createOntClass(model));		

	}
	/*
	public void parse(File dir) throws Exception {
		OntModel model = createModel(true);
		initModel(model);
		//final String prefixURI = String.format("http://toxbank.net/isa/%s",name);
		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("s_") && name.endsWith(".txt");
			}
		});
		for (int i=0; i < files.length; i++) {
			String file = files[i];
			final TemplateStudy ts = new TemplateStudy(String.format("%sDR",uri),"Dose Response study",model);
			final AStudy study = new AStudy(String.format("%s/Study/S1",getResource().getURI()),this);
			TabsParser<RowStudy> parser = new TabsParser<RowStudy>(new FileReader(new File(dir,file))) {
				@Override
				protected void readHeader() throws Exception {
					ts.parseHeader(header, String.format("%s/header",ts.getResource().getURI()));
				}
				@Override
				protected RowStudy transform(String[] tabs) throws Exception {
					return ts.parse(study, header, tabs,String.format("%s/Row/R%d", study.getResource().getURI(),i+1));
				}
			};
			while (parser.hasNext()) {
				RowStudy resource = parser.next();
			}
			parser.close();
		}	
		
		files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("a_") && name.endsWith(".txt");
			}
		});
		for (int i=0; i < files.length; i++) {
			String file = files[i];
			
			final TemplateAssay ta = new TemplateAssay(String.format("%s/assay1",ts.getResource().getURI()),
					"Dose Response Assay",model);
			AnAssay assay = new AnAssay(String.format("%s/Assay/A1",getResource().getURI()),this);

			TabsParser<RowAssay> parser = new TabsParser<RowAssay>(new FileReader(new File(dir,file))) {
				@Override
				protected void readHeader() throws Exception {
					ta.parseHeader(header, String.format("%s/header",ta.getResource().getURI()));		

				}
				@Override
				protected RowAssay transform(String[] tabs) throws Exception {
					RowAssay assay = ta.parse(header, tabs);
				}
			};
			while (parser.hasNext()) {
				RowAssay resource = parser.next();
				//study.addAssay(assay);
			}
			parser.close();
		}			
	}
		

	public void parseStudy(OntModel model, ColumnHeader[] headers,String[][] tabs) throws Exception {
		TemplateStudy ts = new TemplateStudy(String.format("%sDR",uri),"Dose Response study",model);
		ts.parseHeader(headers, String.format("%s/header",ts.getResource().getURI()));
		AStudy study = ts.parse(headers, tabs);
	}
	public void parseAssay(OntModel model,String studyURI, ColumnHeader[] headers,String[][] tabs) throws Exception {
	
		String uri = "http://example.com/study/";

		TemplateAssay ta = new TemplateAssay(String.format("%s/assay1",ts.getResource().getURI()),
				"Dose Response Assay",model);
		ta.parseHeader(headers, String.format("%s/header",ta.getResource().getURI()));		
	
		AnAssay assay = ta.parse(headers, tabs);
		study.addAssay(assay);
	}
	
	*/
	
}


class ISAFileNameFilter implements FilenameFilter {
	String prefix = "i_";
	public ISAFileNameFilter(String prefix) {
		this.prefix = prefix;
	}
	@Override
	public boolean accept(File dir, String name) {
		return name.startsWith(prefix) && name.endsWith(".txt");
	}
}

class AssayFileNameFilter extends ISAFileNameFilter {
	
	public AssayFileNameFilter() {
		super("a_");
	}
}

class StudyFileNameFilter extends ISAFileNameFilter {
	
	public StudyFileNameFilter() {
		super("s_");
	}
}

class InvestigationFileNameFilter extends ISAFileNameFilter {
	
	public InvestigationFileNameFilter() {
		super("i_");
	}
}

