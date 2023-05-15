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
    @Produces("text/plain")
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
    @Produces("text/plain")
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


    @POST
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
            stmt.executeUpdate("DELETE FROM stock");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-306-40615-7', 'The Catcher in the Rye', '1')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-27778-7', 'The Great Gatsby', '2')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-40939-1', 'The Grapes of Wrath', '12')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-54886-7', 'Nineteen Eighty-Four', '44')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-70047-7', 'Lolita', '4')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-70066-8', 'Catch-22', '90')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-70069-9', 'Lord of the Flies', '2')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-70074-3', 'On the Road', '1')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-70075-0', 'Heart of Darkness', '0')");
            stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-307-70081-1', 'Slaughterhouse-Five', '0')");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customer (username varchar(255), token varchar(255))");
            stmt.executeUpdate("DELETE FROM customer");
            stmt.executeUpdate("INSERT INTO customer VALUES ('admin', '136489304656392047930278404540')");
            stmt.executeUpdate("INSERT INTO customer VALUES ('user1', '648928653764839044899283908398')");
            stmt.executeUpdate("INSERT INTO customer VALUES ('user2', '890988723784230984903287542323')");

            return Response.status(200).entity("Database initialized").build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }

    @POST
    @Path("adduser")
    @Produces("text/plain")
    public Response addUserRequest(@QueryParam("username") String username) {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to be connected to the database " + e.getMessage()).build();
            }
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO customer VALUES ('" + username + "', '" + generateToken() + "')");
            return Response.status(200).entity("User added").build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            token.append((int) (Math.random() * 10));
        }
        return token.toString();
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
