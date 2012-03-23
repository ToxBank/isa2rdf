package net.toxbank.isa.rest;

import net.idea.restnet.c.RESTComponent;

import org.restlet.Application;
import org.restlet.Context;

/**
 * This is used as a servlet component instead of the core one, to be able to attach protocols 
 * @author nina
 *
 */
public class ISARESTComponent extends RESTComponent {
		public ISARESTComponent() {
			this(null);
		}
		public ISARESTComponent(Context context,Application[] applications) {
			super(context,applications);
			
		
		}
		public ISARESTComponent(Context context) {
			this(context,new Application[]{new ISARESTApplication()});
		}
		
		

}
