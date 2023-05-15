package stockService;

import book.Book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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
            ResultSet rs = stmt.executeQuery("SELECT * FROM stock WHERE isbn = '" + isbn + "' LIMIT 1");
            if (rs.next()) {
                Book book = new Book(rs.getString("isbn"), rs.getString("name"), rs.getString("stock"));
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(book);
                return Response.status(200).entity(json).build();
            }else {
                return Response.status(200).entity("No book found").build();
            }
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                }
            }
        }
    }

    @GET
    @Path("stockall")
    @Produces(MediaType.APPLICATION_JSON)
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
            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                Book book = new Book(rs.getString("isbn"), rs.getString("name"), rs.getString("stock"));
                books.add(book);
            }
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(books);
            return Response.status(200).entity(json).build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                }
            }
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
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                }
            }
        }
    }

    @POST
    @Path("increase")
    public Response increaseStockRequest(@QueryParam("isbn") String isbn)
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
            ResultSet rs = stmt.executeQuery("SELECT * FROM stock WHERE isbn = '" + isbn + "' LIMIT 1");
            int quantity = 0;
            if (rs.next()) {
                quantity = Integer.parseInt(rs.getString("stock"));
            }else {
                return Response.status(500).entity("No book found").build();
            }
            stmt.executeUpdate("UPDATE stock SET stock = '" + (quantity + 5) + "' WHERE isbn = '" + isbn + "'");
            return Response.status(200).entity("Stock updated").build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                }
            }
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
