package book;

public class Book {
    private String isbn;
    private String name;
    private String stock;

    public Book() {
    }

    public Book(String isbn, String name, String stock) {
        this.isbn = isbn;
        this.name = name;
        this.stock = stock;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getName() {
        return name;
    }

    public String getStock() {
        return stock;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }
}
