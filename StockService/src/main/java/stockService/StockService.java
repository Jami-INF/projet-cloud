package stockService;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("stockservice")
public class StockService {

    @GET
    @Path("stock")
    @Produces("text/plain")
    public Response getStockRequest(@QueryParam("isbn") String isbn)
    {
        return Response.status(200).entity("StockService: " + isbn).build();
    }
    @GET
    @Path("test")
    @Produces("text/plain")
    public Response testRequest()
    {
        return Response.status(200).entity("StockService: test").build();
    }



    private Connection getConnection() throws Exception {
        // Class.forName("org.postgresql.Driver");
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

        return DriverManager.getConnection(dbUrl, username, password);
    }

    private String showDatabase()
    {
        try {
            Connection connection = getConnection();

            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
            ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

            String out = "Hello!\n";
            while (rs.next()) {
                out += "Read from DB: " + rs.getTimestamp("tick") + "\n";
            }

            return out;
        } catch (Exception e) {
            return "There was an error: " + e.getMessage();
        }
    }
}
