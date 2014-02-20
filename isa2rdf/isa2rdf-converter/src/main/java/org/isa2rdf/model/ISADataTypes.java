package org.isa2rdf.model;

public enum ISADataTypes {

	metabolomics {
		@Override
		public boolean isNative() {
			return false;
		}
	},
	microarray_derived_data,
	ms_spec_derived_data,
	nmr_spec_derived_data,	
	generic_assay_derived_data;
	
	public boolean isNative() {
		return true;
	}
	public String toString() {
		return "http://onto.toxbank.net/isa/bii/data_types/"+name();
	};

}
