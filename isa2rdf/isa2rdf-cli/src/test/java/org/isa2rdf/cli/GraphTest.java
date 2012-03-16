package org.isa2rdf.cli;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.isatools.tablib.utils.BIIObjectStore;

import uk.ac.ebi.bioinvindex.model.Identifiable;
import uk.ac.ebi.bioinvindex.model.processing.Processing;
import uk.ac.ebi.bioinvindex.utils.DotGraphGenerator;

public class GraphTest {

	
	@org.junit.Test
	public void testGraph() throws Exception {
		URL url = getClass().getClassLoader().getResource("isa-tab/BII-I-1");
		Assert.assertNotNull(url);
		String filesPath = url.getFile();
        Collection<Identifiable> objects = new ArrayList<Identifiable>();
        IsaClient cli = new IsaClient();

		BIIObjectStore store = cli.validate(filesPath);
        objects.addAll(store.values(Processing.class));
        Assert.assertNotNull(store);
        
        DotGraphGenerator gen = new DotGraphGenerator(objects);
        
        String dotFileName = filesPath + "/isatab.dot";
        gen.createGraph(dotFileName);
    	System.out.println("\n\nExperimental Graph written in " + dotFileName);
		
	}		  
}
