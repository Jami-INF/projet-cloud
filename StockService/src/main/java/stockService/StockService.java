package stockService;

import book.Book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("stockservice")
public class StockService {

    private Connection connection;

    /**
     * Permet de récupérer le stock d'un livre en fonction de son isbn
     * @param isbn isbn du livre
     * @param auth token de l'utilisateur
     * @return Response contenant le stock du livre
     */
    @GET
    @Path("stock")
    @Produces("text/plain")
    public Response getStockRequest(@QueryParam("isbn") String isbn, @HeaderParam("Authorization") String auth) {
        if (!validateUser(auth)) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
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
                } else {
                    return Response.status(200).entity("No book found").build();
                }
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                    }
                }
            }
        }
    }

    /**
     * Permet de récupérer le stock de tous les livres
     * @param auth token de l'utilisateur
     * @return Response contenant le stock de tous les livres
     */
    @GET
    @Path("stockall")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStockAllRequest(@HeaderParam("Authorization") String auth) {

        if (!validateUser(auth)) {
            return Response.status(401).entity("Unauthorized").build();
        }else {


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
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                    }
                }
            }
        }
    }

    /**
     * Permet de réduire le stock d'un livre en fonction de son isbn
     * @param auth token de l'utilisateur
     * @param isbn isbn du livre
     * @param quantity quantité à réduire
     */
    @POST
    @Path("decrease")
    @Produces("text/plain")
    public Response decreaseStockRequest(@HeaderParam("Authorization") String auth, @QueryParam("isbn") String isbn, @QueryParam("quantity") int quantityToDecrease)
    {
        if (!validateUser(auth)) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
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
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                    }
                }
            }
        }
    }


    /**
     * Permet d'augmenter le stock d'un livre en fonction de son isbn
     * @param auth token de l'utilisateur
     * @param isbn isbn du livre
     * @param quantity quantité à augmenter
     */
    @POST
    @Path("increase")
    @Produces("text/plain")
    public Response increaseStockRequest(@HeaderParam("Authorization") String auth, @QueryParam("isbn") String isbn)
    {
        if (!validateUser(auth)) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
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
                } else {
                    return Response.status(500).entity("No book found").build();
                }
                stmt.executeUpdate("UPDATE stock SET stock = '" + (quantity + 5) + "' WHERE isbn = '" + isbn + "'");
                return Response.status(200).entity("Stock updated").build();
            } catch (Exception e) {
                return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        return Response.status(500).entity("Impossible to close the connection " + e.getMessage()).build();
                    }
                }
            }
        }
    }

    /**
     * Permet d'initialiser la base de données
     * @param auth token de l'utilisateur
     * @return Response
     */
    @POST
    @Path("initdb")
    @Produces("text/plain")
    public Response initDbRequest(@HeaderParam("Authorization") String auth) {
        if (!validateUser(auth)) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
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
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-600-119-125-1', 'The Great Gatsby', '2')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-601-7151-13-3', 'The Grapes of Wrath', '12')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-1-2345-6789-7', 'Nineteen Eighty-Four', '44')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-439-02349-8', 'Lolita', '4')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-440-32033-5', 'Catch-22', '90')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-375-70114-8', 'Lord of the Flies', '2')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-671-02735-3', 'On the Road', '1')");
                stmt.executeUpdate("INSERT INTO stock VALUES ('978-0-06-112008-4', 'Heart of Darkness', '0')");

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
    }

    /**
     * Permet d'ajouter un utilisateur
     * @param username nom de l'utilisateur
     * @return Response contenant le token de l'utilisateur
     */
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
            String token = generateToken();
            stmt.executeUpdate("INSERT INTO customer VALUES ('" + username + "', '" + token + "')");
            return Response.status(200).entity(token).build();
        } catch (Exception e) {
            return Response.status(500).entity("Impossible to execute the query " + e.getMessage()).build();
        }

    }

    /**
     * Permet de valider un utilisateur
     * @param token token de l'utilisateur
     * @return boolean true si l'utilisateur est valide, false sinon
     */
    private boolean validateUser(String token) {
        if (connection == null) {
            try {
                connection = getConnection();
            } catch (Exception e) {
                return false;
            }
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customer WHERE token = '" + token + "' LIMIT 1");
            if (rs.next()) {
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Permet de générer un token
     * @return String token
     */
    private String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            token.append((int) (Math.random() * 10));
        }
        return token.toString();
    }

    /**
     * Permet de se connecter à la base de données
     * @return Connection
     */
    private Connection getConnection() throws Exception {
        // Class.forName("org.postgresql.Driver");
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

        return DriverManager.getConnection(dbUrl, username, password);
    }
}
