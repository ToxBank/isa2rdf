package net.toxbank.isa.rest;

import net.idea.restnet.c.TaskApplication;
import net.idea.restnet.c.filter.RESTnetTunnelFilter;
import net.idea.restnet.c.html.FavIconResource;
import net.idea.restnet.c.resource.RESTNetStatusService;
import net.idea.restnet.c.resource.TaskResource;
import net.idea.restnet.c.routers.MyRouter;
import net.idea.restnet.c.routers.TaskRouter;
import net.idea.restnet.sparql.TDBEndpointRouter;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.service.TunnelService;

public class ISARESTApplication extends TaskApplication<String>{

	public ISARESTApplication() {
		super();
		setName("ISA-TAB REST services (demo)");
		setDescription("ISA-TAB REST services (demo)");
		setOwner("Ideaconsult Ltd.");
		setAuthor("Ideaconsult Ltd.");		

		setStatusService(new RESTNetStatusService());
		setTunnelService(new TunnelService(true,true) {
			@Override
			public Filter createInboundFilter(Context context) {
				return new RESTnetTunnelFilter(context);
			}
		});
		getTunnelService().setUserAgentTunnel(true);
		getTunnelService().setExtensionsTunnel(false);
		getTunnelService().setMethodTunnel(true);

		getMetadataService().setEnabled(true);
		getMetadataService().addExtension("rdf", MediaType.APPLICATION_RDF_XML, true);
		getMetadataService().addExtension("n3", MediaType.TEXT_RDF_N3, true);

	}
	
	@Override
	public Restlet createInboundRoot() {
		Router router = new MyRouter(this.getContext());
		
		//router.attach("/", OpenSSOUserResource.class);
		//router.attach("", OpenSSOUserResource.class);
		
		/**		 *  /admin 
		 *  Various admin tasks, like database creation
		 */
		
		//router.attach(String.format("/%s",AdminResource.resource),createProtectedResource(createAdminRouter(),"admin"));


		/**  /task  */
		router.attach(TaskResource.resource, new TaskRouter(getContext()));

		/**
		 * OpenSSO login / logout
		 * Sets a cookie with OpenSSO token
		 */
		//router.attach("/"+OpenSSOUserResource.resource,createOpenSSOLoginRouter() );
		
		Router tdbRouter = new TDBEndpointRouter(getContext());
		router.attach(TDBEndpointRouter.resource,tdbRouter);
		
		/**
		 * Images, styles, favicons, applets
		 */
		attachStaticResources(router);

        
       // router.attach(String.format("/%s",PolicyResource.resource),PolicyResource.class);

	
	     router.setDefaultMatchingMode(Template.MODE_STARTS_WITH); 
	     router.setRoutingMode(Router.MODE_BEST_MATCH); 
	     
	     //StringWriter w = new StringWriter();
	     //AmbitApplication.printRoutes(router,">",w);
	     //System.out.println(w.toString());

		 return router;
	}
	
	

		
	/**
	 * Images, styles, icons
	 * Works if packaged as war only!
	 * @return
	 */
	protected void attachStaticResources(Router router) {
		/*  router.attach("/images",new Directory(getContext(), LocalReference.createFileReference("/webapps/images")));   */

		 Directory metaDir = new Directory(getContext(), "war:///META-INF");
		 Directory imgDir = new Directory(getContext(), "war:///images");
		 Directory jmolDir = new Directory(getContext(), "war:///jmol");
		 Directory jmeDir = new Directory(getContext(), "war:///jme");
		 Directory styleDir = new Directory(getContext(), "war:///style");
		 Directory jquery = new Directory(getContext(), "war:///jquery");

		 
		 router.attach("/meta/", metaDir);
		 router.attach("/images/", imgDir);
		 router.attach("/jmol/", jmolDir);
		 router.attach("/jme/", jmeDir);
		 router.attach("/jquery/", jquery);
		 router.attach("/style/", styleDir);
		 router.attach("/favicon.ico", FavIconResource.class);
		 router.attach("/favicon.png", FavIconResource.class);
	}


	/**
	 * Standalone, for testing mainly
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        
        // Create a component
        Component component = new ISARESTComponent();
        final Server server = component.getServers().add(Protocol.HTTP, 8080);
        component.start();
   
        System.out.println("Server started on port " + server.getPort());
        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        component.stop();
        System.out.println("Server stopped");
    }
    	
   
	
}
