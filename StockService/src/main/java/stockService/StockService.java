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

    private Connection connection;
                                                                        
    @GET
    @Path("stock")
    @Produces("text/plain")
    public Response getStockRequest(@QueryParam("isbn") String isbn) {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
            }
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM stock WHERE isbn = '" + isbn + "'");
            String out = "Hello!\n";
            while (rs.next()) {
                out += "Read from DB: " + rs.getString("isbn") + " " + rs.getString("stock") + " " + rs.getString("name") + "\n";
            }
            return Response.status(200).entity(out).build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }

    @GET
    @Path("stockall")
    @Produces("text/plain")
    public Response getStockAllRequest() {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
            }
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM stock");
            String out = "Hello!\n";
            while (rs.next()) {
                out += "Read from DB: " + rs.getString("isbn") + " " + rs.getString("stock") + "\n";
            }
            return Response.status(200).entity(out).build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }

    @POST
    @Path("decrease")
    public Response decreaseStockRequest(@QueryParam("isbn") String isbn, @QueryParam("quantity") int quantityToDecrease)
    {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
            }
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM stock WHERE isbn = '" + isbn + "'");
            int quantity = 0;
            while (rs.next()) {
                quantity = Integer.parseInt(rs.getString("stock"));
            }
            if (quantity < quantityToDecrease) {
                return Response.status(200).entity("Not enough books in stock").build();
            }
            stmt.executeUpdate("UPDATE stock SET stock = '" + (quantity - quantityToDecrease) + "' WHERE isbn = '" + isbn + "'");
            return Response.status(200).entity("Stock updated").build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }

    @POST
    @Path("increase")
    public Response increaseStockRequest(@QueryParam("isbn") String isbn, @QueryParam("quantity") int quantityToIncrease)
    {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
            }
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM stock WHERE isbn = '" + isbn + "'");
            int quantity = 0;
            while (rs.next()) {
                quantity = Integer.parseInt(rs.getString("stock"));
            }
            stmt.executeUpdate("UPDATE stock SET stock = '" + (quantity + quantityToIncrease) + "' WHERE isbn = '" + isbn + "'");
            return Response.status(200).entity("Stock updated").build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }


    @GET
    @Path("initdb")
    @Produces("text/plain")
    public Response initDbRequest() {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
            }
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS stock (isbn varchar(255), name varchar(255), stock varchar(255))");
            stmt.executeUpdate("INSERT INTO stock VALUES ('1234567890', 'harrypotter', '1')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('1234567891', 'harrypeaudbeurre', '2')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('1234567892', 'harrypotter2', '3')");
            return Response.status(200).entity("Database initialized").build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }

    private Connection getConnection() throws Exception {
        // Class.forName("org.postgresql.Driver");
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

        return DriverManager.getConnection(dbUrl, username, password);
    }
}
