package wholeSalerService;


import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@Path("wholesalerservice")
public class WholeSalerService {

    @POST
    @Path("order")
    @Produces("text/plain")
    public Response order(@QueryParam("isbn") String isbn){
        Response response = null;

        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("https://stock-service.herokuapp.com").path("stockservice/increase/").queryParam("isbn", isbn);
            response = target.request().get();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }

        if(response.getStatus() == 200){
            return Response.status(200).entity("Order done").build();
        }else{
            return Response.status(500).entity("Impossible to execute the query " + response.getStatus()).build();
        }
    }
}
