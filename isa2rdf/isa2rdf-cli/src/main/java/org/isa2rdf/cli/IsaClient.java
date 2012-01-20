package org.isa2rdf.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;


public class IsaClient {
	protected String dir;
	
	public void run() {
		
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
	    	else cli.run();	
	    		
		} catch (Exception x ) {
			x.printStackTrace();
			printHelp(options,x.getMessage());
		} finally {
			
		}
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
			if ((argument==null) || "".equals(argument.trim())) 

				throw new IllegalArgumentException("Not a valid HTTP URI "+argument);
			this.dir = argument;
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
	
	
	protected static String exampleAuth() {
		return
		"Verify authorization:\n"+
		"\tjava -jar aacli\n"+
		"\t-n http://opensso.in-silico.ch/opensso/identity\n"+
		"\t-z http://opensso.in-silico.ch/Pol/opensso-pol\n"+			
		"\t-u guest\n"+
		"\t-p guest\n"+
		"\t-r https://ambit.uni-plovdiv.bg:8443/ambit2/dataset/1\n"+
		"\t-c authorize";
	}
}
