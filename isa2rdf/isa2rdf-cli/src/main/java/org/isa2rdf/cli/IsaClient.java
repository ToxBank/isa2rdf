package org.isa2rdf.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import net.toxbank.client.io.rdf.TOXBANK;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
import org.isa2rdf.datamatrix.DataMatrixConverter;
import org.isa2rdf.model.ISA;
import org.isatools.isatab.ISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.mapping.TabMappingContext;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Identifiable;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class IsaClient {
	private Logger logger = Logger.getLogger(IsaClient.class);
	protected String dir;
	protected String outfile;
	protected String outDatafilesDir;
	protected String investigationURI;
	protected String toxbankuri;
	
	private final static String metabolomics = "http://onto.toxbank.net/isa/bii/data_types/metabolomics";
	private final static String microarray_derived_data = "http://onto.toxbank.net/isa/bii/data_types/microarray_derived_data";
	private final static String ms_spec_derived_data =  "http://onto.toxbank.net/isa/bii/data_types/ms_spec_derived_data";
	private final static String nmr_spec_derived_data =  "http://onto.toxbank.net/isa/bii/data_types/nmr_spec_derived_data";
	private final static String generic_assay_derived_data =  "http://onto.toxbank.net/isa/bii/data_types/generic_assay_derived_data";
	
	public Model  processAndSave() throws Exception {
		Model model = process(dir);
		Writer writer = null;
		try {
			if (outfile==null) writer = new OutputStreamWriter(System.out);
			else {
				File out = new File(outfile);
				writer = new FileWriter(out);
			}
			IsaClient.write(model, writer, (outfile==null)||outfile.endsWith(".n3")?"text/n3":
							 (outfile.endsWith(".nt")?"text/n-triples":"application/rdf+xml"), true);
			
			writer.close(); writer = null;
			
			
			if (outDatafilesDir!=null) {
				logger.info("Converted data files will be written to "+outDatafilesDir);
				final String[] supported_datatype = {
							microarray_derived_data,
							ms_spec_derived_data,
							nmr_spec_derived_data,
							generic_assay_derived_data
						};
				for (String datatype: supported_datatype) {
					String mdatatype = datatype;
					Hashtable<String,Hashtable<String,String>> lookup = null;
					if (nmr_spec_derived_data.equals(datatype) || ms_spec_derived_data.equals(datatype)) {//metabolite files are linked to sample nodes, not data nodes! 
						lookup = getMaterialEntries(model, datatype);
						if (lookup.size()==0)
							lookup = getDataEntries(model, datatype);
						else mdatatype = "metabolomics"; 
					} else 
						lookup = getDataEntries(model, datatype);
					logger.info(String.format("%s data files of type ",(lookup!=null && lookup.size()>0)?"Found ":"Not found ",datatype));
					logger.info(lookup);
					Enumeration<String> keys = lookup.keys();
					while (keys.hasMoreElements()) {
						String fileName = keys.nextElement();
						DataMatrixConverter matrix = new DataMatrixConverter(mdatatype,lookup.get(fileName),investigationURI);
						FileReader reader = null;
						FileOutputStream out = null;
						try {
							File file = new File(dir,fileName);
							File outFile = new File(outDatafilesDir,FilenameUtils.removeExtension(file.getName())+".rdf");
							out = new FileOutputStream(outFile);
							if (file.exists()) {
								reader = new FileReader(file);
								matrix.writeRDF(reader, fileName , -1, out);
							}	else throw new FileNotFoundException(file.getAbsolutePath());
							logger.info("Converted data files written to "+outfile);
						} catch (Exception x) {
							logger.error(x);
						} finally {
							try {if (reader!=null)reader.close();} catch (Exception x) {}
							try {if (out!=null) out.close();} catch (Exception x) {}
						}
					}
					
			}
			} else 
				logger.info("-a or --outdatafilesdir not specified, skipping data files processing ");
			return model;
		} catch (Exception x) {
			throw x;
		} finally {
			if (writer!=null) try { writer.close(); } catch (Exception x) {}
		}
		
	}
	public Model process() throws Exception {
		return process(dir);
	}
	
	public BIIObjectStore validate(String filesPath) throws Exception {
		
		ISATABLoader loader = new ISATABLoader(filesPath);
		FormatSetInstance isatabInstance = loader.load();

		ISATABValidator validator = new ISATABValidator(isatabInstance);
		
	    if (GUIInvokerResult.WARNING == validator.validate()) {
	         //vlog.warn("ISA-Configurator Validation reported problems, see the messages above or the log file");
	    }
	    return validator.getStore();
	}    
	public Model process(String filesPath) throws Exception {
		
        BIIObjectStore store = validate(filesPath);
        
        Set<Class<? extends Identifiable>> types = store.types();
        //add id
        long tempIdCounter = 1;
        for (Class<? extends Identifiable> t : types) {
            for (Identifiable c : store.values(t)) try {
            	if (c instanceof TabMappingContext) continue;
            	Object id = c.getId();
            	if (id!=null) continue;
            	c.setId(tempIdCounter);
            	tempIdCounter++;
            	//String prefix = "Node";
            	//if (c instanceof DataNode) prefix = "DN_";
            	//if (c instanceof MaterialNode) prefix = "MN_";
            } catch (Exception x) {
    			PrintWriter writer = new PrintWriter(System.err);
    			x.printStackTrace(writer);
    			writer.flush();
    			writer.close();
    			
            }
           
        }
        
        /*
class org.isatools.isatab.mapping.FactorTypeHelper
class org.isatools.isatab.mapping.AssayGroup
class uk.ac.ebi.bioinvindex.model.Protocol
class uk.ac.ebi.bioinvindex.model.xref.ReferenceSource
class uk.ac.ebi.bioinvindex.model.processing.Node
class uk.ac.ebi.bioinvindex.model.Publication
class uk.ac.ebi.bioinvindex.model.processing.Assay
class uk.ac.ebi.bioinvindex.model.Study
class org.isatools.tablib.mapping.TabMappingContext
class uk.ac.ebi.bioinvindex.model.processing.Processing
class uk.ac.ebi.bioinvindex.model.Investigation
class uk.ac.ebi.bioinvindex.model.Material
class org.isatools.isatab.mapping.StudyWrapper
class uk.ac.ebi.bioinvindex.model.Contact

         */
        File file = new File(filesPath);
        String prefix = null;
        if (investigationURI==null) 
        	prefix = String.format("%s%s",ISA.URI,file.getName().replace("-","").replace(" ","").trim());
        else {
        	if (investigationURI.startsWith("http"))
        		prefix = investigationURI;
        	else
        		throw new Exception("Invalid investigation URI "+investigationURI);
        }	
        ProcessingPipelineRDFGenerator gen = new ProcessingPipelineRDFGenerator(
        		toxbankuri==null?"http://toxbanktest1.opentox.org:8080/toxbank":toxbankuri,prefix,store);
        gen.setTempIdCounter(tempIdCounter);
        return gen.createGraph();
	}
	public static void main(String[] args) {

    	Options options = createOptions();
    	
    	IsaClient cli=null;
    	CommandLineParser parser = new PosixParser();
		try {
			cli = new IsaClient();
		    CommandLine line = parser.parse( options, args,false );
		    if (line.hasOption(_option.help.name())) {
		    	printHelp(options, null);
		    	return;
		    }
		    boolean nooptions = true;	
	    	for (_option o: _option.values()) 
	    		if (line.hasOption(o.getShortName())) try {
	    			cli.setOption(o,line.getOptionValue(o.getShortName()));
	    			nooptions = false;
	    		} catch (Exception x) {
	    			printHelp(options,x.getMessage());
	    			return;
	    		}
	    	if (nooptions) printHelp(options,null);
	    	else {
	    		cli.processAndSave();
	    		
	    	}
	    		
		} catch (Exception x ) {
			PrintWriter writer = new PrintWriter(System.err);
			x.printStackTrace(writer);
			writer.flush();
			writer.close();
			System.exit(1);
		} finally {
			
		}
		System.exit(0);
	}
	
	enum _option {

		dir {

			@Override
			public String getArgName() {
				return "dir";
			}

			@Override
			public String getDescription() {
				return "Directory with ISA-TAB files";
			}

			@Override
			public String getShortName() {
				return "d";
			}
			@Override
			public String getDefaultValue() {
				return null;
			}
			public Option createOption() {
		    	Option option   = OptionBuilder.withLongOpt(name())
		    	.withArgName(getArgName())
		        .withDescription(getDescription())
		        .hasArg()
		        .create(getShortName());

		    	return option;
			}
			
			
		},
		output {

			@Override
			public String getArgName() {
				return "output file";
			}

			@Override
			public String getDescription() {
				return "Output file .n3|.rdf|.nt";
			}

			@Override
			public String getShortName() {
				return "o";
			}
			@Override
			public String getDefaultValue() {
				return null;
			}
			public Option createOption() {
		    	Option option   = OptionBuilder.withLongOpt(name())
		    	.withArgName(getArgName())
		        .withDescription(getDescription())
		        .hasArg()
		        .create(getShortName());

		    	return option;
			}
			
		},	
		outdatafilesdir {

			@Override
			public String getArgName() {
				return "dir";
			}

			@Override
			public String getDescription() {
				return "Directory to write the data files converted to RDF";
			}

			@Override
			public String getShortName() {
				return "a";
			}
			@Override
			public String getDefaultValue() {
				return null;
			}
			public Option createOption() {
		    	Option option   = OptionBuilder.withLongOpt(name())
		    	.withArgName(getArgName())
		        .withDescription(getDescription())
		        .hasArg()
		        .create(getShortName());

		    	return option;
			}
			
			
		},		
		toxbankuri {

			@Override
			public String getArgName() {
				return "ToxBank protocol service root URI ";
			}

			@Override
			public String getDescription() {
				return "ToxBank protocol service root URI e.g. http://toxbanktest1.opentox.org:8080/toxbank";
			}

			@Override
			public String getShortName() {
				return "t";
			}
			@Override
			public String getDefaultValue() {
				return null;
			}
			public Option createOption() {
		    	Option option   = OptionBuilder.withLongOpt(name())
		    	.withArgName(getArgName())
		        .withDescription(getDescription())
		        .hasArg()
		        .create(getShortName());

		    	return option;
			}
			
		},				
		investigationuri {

			@Override
			public String getArgName() {
				return "URI";
			}

			@Override
			public String getDescription() {
				return "URI to be assigned to this investigation e.g. http://services.toxbank.net/investigation/12345";
			}

			@Override
			public String getShortName() {
				return "i";
			}
			@Override
			public String getDefaultValue() {
				return null;
			}
			public Option createOption() {
		    	Option option   = OptionBuilder.withLongOpt(name())
		    	.withArgName(getArgName())
		        .withDescription(getDescription())
		        .hasArg()
		        .create(getShortName());

		    	return option;
			}
			
		},			
		help {
			@Override
			public String getArgName() {
				return null;
			}
			@Override
			public String getDescription() {
				return "ISA2RDF client";
			}
			@Override
			public String getShortName() {
				return "h";
			}
			@Override
			public String getDefaultValue() {
				return null;
			}
			public Option createOption() {
		    	Option option   = OptionBuilder.withLongOpt(name())
		        .withDescription(getDescription())
		        .create(getShortName());

		    	return option;
			}
		}				
		;
		public abstract String getArgName();
		public abstract String getDescription();
		public abstract String getShortName();
		public String getDefaultValue() { return null; }
			
		public Option createOption() {
			String defaultValue = getDefaultValue();
	    	Option option   = OptionBuilder.withLongOpt(name())
	        .hasArg()
	        .withArgName(getArgName())
	        .withDescription(String.format("%s %s %s",getDescription(),defaultValue==null?"":"Default value: ",defaultValue==null?"":defaultValue))
	        .create(getShortName());

	    	return option;
		}
	}
	protected static Options createOptions() {
    	Options options = new Options();
    	for (_option o: _option.values()) {
    		options.addOption(o.createOption());
    	}

    	
    	return options;
	}	
	
	public void setOption(_option option, String argument) throws Exception {
		if (argument!=null) argument = argument.trim();
		switch (option) {
		case dir: {
			this.dir = argument;
			break;
		}
		case output: {
			this.outfile = argument;
			break;			
		}
		case outdatafilesdir: {
			this.outDatafilesDir = argument;
			break;
		}
		case toxbankuri: {
			this.toxbankuri = argument;
			break;
		}
		case investigationuri: {
			this.investigationURI = argument;
			break;
		}	
		default: 
		}
	}
		
	protected static void printHelp(Options options,String message) {
		if (message!=null) System.out.println(message);
		System.out.println("ISA-TAB to RDF conversion");
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp( IsaClient.class.getName(), options );
	    System.out.println("Examples:");
	    /*
	    System.out.println(exampleAuth());
	    System.out.println(exampleRetrievePoliciesPerURI());
	    System.out.println(exampleRetrievePolicyContent());
	    System.out.println(exampleArchivePolicies());
	    System.out.println(exampleCreatePolicies());
	    System.out.println(exampleDeletePolicy());
	    System.out.println(exampleDeletePolicyURI());
	    System.out.println(exampleDeletePolicyUser());
	    */

	    Runtime.getRuntime().runFinalization();						 
		Runtime.getRuntime().exit(0);	
	}	
	
	
	public static void writeStream(Model jenaModel, OutputStream output, String mediaType, boolean isXml_abbreviation) throws IOException {
		write(jenaModel,new OutputStreamWriter(output),mediaType,isXml_abbreviation);
	}
    public static void write(Model jenaModel, Writer output, String mediaType, boolean isXml_abbreviation) throws IOException {
    	try {
    		RDFFormat format = RDFFormat.TTL;
    		//RDFWriter fasterWriter = null;
			if ("application/rdf+xml".equals(mediaType)) {
				
				if (isXml_abbreviation)
					format = RDFFormat.RDFXML_ABBREV ;	
					//fasterWriter = jenaModel.getWriter("RDF/XML-ABBREV");//lot smaller ... but could be slower
				else
					format = RDFFormat.RDFXML_PLAIN ;
					//fasterWriter = jenaModel.getWriter("RDF/XML");
				//fasterWriter.setProperty("xmlbase",jenaModel.getNsPrefixURI(""));
				//fasterWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
				//fasterWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);
			}
			else if (mediaType.equals("application/x-turtle"))
				//fasterWriter = jenaModel.getWriter("TURTLE");
				format = RDFFormat.TURTLE ;
			else if (mediaType.equals("text/n3"))
				format = RDFFormat.TTL ;
			else if (mediaType.equals("text/n-triples"))
				format = RDFFormat.NTRIPLES ;
			else {
				format = RDFFormat.RDFXML_ABBREV;
				/*
				fasterWriter = jenaModel.getWriter("RDF/XML-ABBREV");
				fasterWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
				fasterWriter.setProperty("showDoctypeDeclaration", Boolean.TRUE);	//essential to get XSD prefixed
				fasterWriter.setProperty("xmlbase",jenaModel.getNsPrefixURI(""));
				*/
			}
			//fasterWriter.write(jenaModel,output,ISA.URI);
			RDFDataMgr.write(output, jenaModel, format) ;

    	} catch (Exception x) {
    		Throwable ex = x;
    		while (ex!=null) {
    			if (ex instanceof IOException) 
    				throw (IOException)ex;
    			ex = ex.getCause();
    		}
    	} finally {

    		try {if (output !=null) output.flush(); } catch (Exception x) { 
    			PrintWriter writer = new PrintWriter(System.err);
    			x.printStackTrace(writer);
    			writer.flush();
    			writer.close();
    		}
    	}
    }
    
	/**
	 * 
	 * @param model
	 * @param datatype  e.g. <http://onto.toxbank.net/isa/bii/data_types/microarray_derived_data>
	 * @return
	 */
	public Hashtable<String,Hashtable<String,String>> getDataEntries(Model model,String datatype) {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?node ?file ?accession where {\n" +
				"	?node isa:hasOntologyTerm  <%s>." +
				"   ?node rdfs:seeAlso ?file."+
				"   ?node isa:hasAccessionID ?accession."+
				"} \n",
				TOXBANK.URI,
				ISA.URI,datatype);

		Hashtable<String,Hashtable<String,String>> lookup = new Hashtable<String, Hashtable<String,String>>();
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode node = qs.get("node");
			RDFNode accession = qs.get("accession");
			RDFNode file = qs.get("file");
			logger.info(node + "\t" + file + "\t" + accession);
			
			Hashtable<String,String> map = lookup.get(file.asLiteral().getString());
			if (map==null) {
				map = new Hashtable<String, String>();
				lookup.put(file.asLiteral().getString(),map);
			}
			map.put(accession.asLiteral().getString(), node.asResource().getURI());
		}	
		return lookup;
	}

	/**
	 * Returns all sample entries
	 * @param model
	 * @param datatype
	 * @return
	 */
	public Hashtable<String,Hashtable<String,String>> getMaterialEntries(Model model,String datatype) {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?m ?file ?accession ?compound where {\n" +
				"   ?d rdf:type isa:MetabolightsData."+				
				"	?d isa:hasOntologyTerm  <%s>." +
				"   ?d rdfs:seeAlso ?file."+
				"	?dnode isa:hasData ?d." +
				"	?dnode isa:hasStudy ?study." +
				"	?mnode isa:hasStudy ?study." +
				"	?mnode isa:hasMaterial ?m." +
				"	?m rdf:type isa:Material." +
				"	?m isa:hasOntologyTerm  <http://onto.toxbank.net/isa/bii/roles/sample>." +
				"   ?m isa:hasAccessionID ?accession."+
				"   ?m isa:hasFactorValue ?fv."+
				"   ?fv isa:hasFactor ?factor."+
				"   ?factor dcterms:title ?ftype."+				
				"   ?fv isa:hasOntologyTerm ?compound."+
				//"   ?f isa:hasOntologyTerm <http://purl.obolibrary.org/chebi/CHEBI:24431>."+ same as the filter below
				"   FILTER (str(?ftype) = 'compound')"+
				"} GROUP BY ?m ?file ?accession ?compound \n",
				TOXBANK.URI,
				ISA.URI,datatype);

		Hashtable<String,Hashtable<String,String>> lookup = new Hashtable<String, Hashtable<String,String>>();
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		
		int row =0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode dnode = qs.get("dnode");
			RDFNode mnode = qs.get("mnode");
			
			RDFNode compound = qs.get("compound");
			RDFNode node = qs.get("m");
			RDFNode accession = qs.get("accession");
			RDFNode file = qs.get("file");
			logger.debug(  node + "\t" + file + "\t" + accession + "\t"+ compound);
			Hashtable<String,String> map = lookup.get(file.asLiteral().getString());
			if (map==null) {
				map = new Hashtable<String, String>();
				lookup.put(file.asLiteral().getString(),map);
			}
			map.put(accession.asLiteral().getString(), node.asResource().getURI());
			row++;
		}	
		return lookup;
	}
}
