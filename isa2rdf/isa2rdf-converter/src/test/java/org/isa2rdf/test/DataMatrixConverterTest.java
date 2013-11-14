package org.isa2rdf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.Charset;

import org.isa2rdf.datamatrix.DataMatrixConverter;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DataMatrixConverterTest {
	@Test
	public void test() throws Exception {
		DataMatrixConverter conv = new DataMatrixConverter();
		String file = "D:/src-toxbank/isa-tab-files/NOTOX-APAP-Tx/NOTOX_PILOT_APAP_DerivedTxDataMatrix.txt";
		File rdf = File.createTempFile("isa",".rdf");
		rdf.deleteOnExit();
		System.out.println(rdf.getAbsolutePath());
		FileOutputStream out = null;
		FileReader fr = null;
		Model model = null;
		try {
			fr = new FileReader(file);
			out = new FileOutputStream(rdf);
			conv.writeRDF(fr,"test", 5, out);
			//test 

		} catch (Exception x) {
			throw x;
		} finally {
			try {fr.close();} catch (Exception x) {}
			try {out.close();} catch (Exception x) {}
		}
		//now check if rdf is ok
		FileInputStream in = null;
		try {
			in = new FileInputStream(rdf);
			model = ModelFactory.createDefaultModel();
  			model.read(in, "http://example.org", "RDF/XML");
		} catch (Exception x) {
			throw x;
		} finally {
			in.close();
		}
		
	}
}
