package org.isa2rdf.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.isatools.isatab.ISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab_v1.ISATABLoader;
import org.isatools.tablib.mapping.TabMappingContext;
import org.isatools.tablib.schema.FormatSetInstance;
import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Identifiable;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;


public class IsaClient {
	protected String dir;
	protected String outfile;
	
	
	public void  processAndSave() throws Exception {
		Model model = process(dir);
		Writer writer = null;
		if (outfile==null) writer = new OutputStreamWriter(System.out);
		else {
			File out = new File(outfile);
			writer = new FileWriter(out);
		}
		IsaClient.write(model, writer, (outfile==null)||outfile.endsWith(".n3")?"text/n3":"application/rdf+xml", true);
		writer.close();
		
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
        
 
        File file = new File(filesPath);
        String prefix = String.format("%s%s",ISA.URI,file.getName().replace("-","").replace(" ","").trim());
        ProcessingPipelineRDFGenerator gen = new ProcessingPipelineRDFGenerator(prefix,store);
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
				return "ooutput file";
			}

			@Override
			public String getDescription() {
				return "Output file .n3|.rdf";
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
			
			fasterWriter.write(jenaModel,output,ISA.URI);

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
}
