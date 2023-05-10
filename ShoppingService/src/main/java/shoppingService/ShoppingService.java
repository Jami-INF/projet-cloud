package shoppingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.regex.*;

@RestController
@RequestMapping("/shoppingservice")
public class ShoppingService {

    private final RestTemplate restTemplate;

    public ShoppingService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Permet de connaître la quantité d'un livre en stock
     * @param isbn Identifie le livre et doit être valide au format ISBN-13
     * @return Une réponse http
     */
    @GetMapping("/book")
    public ResponseEntity<String> bookRequest(@RequestParam(value = "isbn", defaultValue = "") String isbn) {
        if (validateIsbn(isbn)) {
            ResponseEntity<String> responseGetStock;
            try {
                responseGetStock = sendRequestToGetStock(isbn);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("StockService unreachable " + e.getMessage());
            }

            String entity = responseGetStock.getBody();

            System.out.println("entity : " + entity);

            switch (responseGetStock.getStatusCode()) {
                case OK:
                    return ResponseEntity.status(HttpStatus.OK).body(entity);
                case INTERNAL_SERVER_ERROR:
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Problem with StockService");
                default:
                    return ResponseEntity.status(responseGetStock.getStatusCode()).body(entity);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Isbn invalid");
    }

//    /**
//     * Permet d'acheter un livre en le désignant par son isbn et en indiquant la quantité à acheter
//     * @param isbn Identifie le livre et doit être valide au format ISBN-13
//     * @param quantity Quantité du livre à acheter
//     * @return Une réponse http
//     */
//    @GetMapping("/buy")
//    public ResponseEntity<String> buyRequest(
//            @RequestParam(value = "isbn", defaultValue = "") String isbn,
//            @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
//
//        if (validateIsbn(isbn)) {
//            ResponseEntity<String> responseGetStock;
//            ResponseEntity<String> responseDecreaseStock;
//            ResponseEntity<String> responseOrderStock;
//            try {
//                responseGetStock = sendRequestToGetStock(isbn);
//            } catch (Exception e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body("StockService unreachable " + e.getMessage());
//            }
//
//            String entity = responseGetStock.getBody();
//            String bookName = entity.split(";")[0];
//
//            switch (responseGetStock.getStatusCode()) {
//                case 200:
//                    int stock = Integer.parseInt(entity.split(";")[1]);
//
//                    if (stock < quantity) {
//                        try {
//                            responseOrderStock = sendRequestToOrder(isbn);
//                        } catch (Exception e) {
//                            return ResponseEntity<String>.status(Response.Status.NOT_FOUND)
//                                    .entity("StockService unreachable " + e.getMessage())
//                                    .build();
//                        }
//
//                        switch (responseOrderStock.getStatus()) {
//                            case 204:
//                                break;
//                            case 500:
//                                return Response.status(Response.Status.BAD_GATEWAY)
//                                        .entity("Problem with StockService")
//                                        .build();
//                            default:
//                                return Response.status(responseOrderStock.getStatus())
//                                        .entity("")
//                                        .build();
//                        }
//                    }
//
//                    try {
//                        responseDecreaseStock = sendRequestToDecreaseStock(isbn, quantity);
//                    } catch (Exception e) {
//                        return Response.status(Response.Status.NOT_FOUND)
//                                .entity("StockService unreachable" + e.getMessage())
//                                .build();
//                    }
//
//                    switch (responseDecreaseStock.getStatus()) {
//                        case 204:
//                            return Response.status(Response.Status.OK)
//                                    .entity("You bought '" + bookName + "' in " + quantity + " copie(s)")
//                                    .build();
//                        case 500:
//                            return Response.status(Response.Status.BAD_GATEWAY)
//                                    .entity("Problem with StockService")
//                                    .build();
//                        default:
//                            return Response.status(responseDecreaseStock.getStatus())
//                                    .entity("")
//                                    .build();
//                    }
//
//                case 500:
//                    return Response.status(Response.Status.BAD_GATEWAY)
//                            .entity("Problem with StockService")
//                            .build();
//                default:
//                    return Response.status(responseGetStock.getStatus())
//                            .entity(entity)
//                            .build();
//            }
//        }
//
//        return Response.status(Response.Status.BAD_REQUEST)
//                .entity("Isbn invalid")
//                .build();
//    }

    /**
     * Permet de valider un isbn au format ISBN-13 par une expression régulière
     * @param isbnToValidate L'isbn à valider
     * @return Si l'isbn est valide ou non
     */
    private boolean validateIsbn(String isbnToValidate)
    {
        /*Pattern pattern = Pattern.compile("^97[89]-[0-9]-[0-9]{4}-[0-9]{4}-[0-9]$");
        Matcher matcher = pattern.matcher(isbnToValidate);

        return matcher.matches();

         */
        return true;
    }

    /**
     * Permet d'envoyer une requête au stockservice pour obtenir la quantité d'un livre en stock
     * @param isbn Isbn du livre
     * @return La réponse http de la requête
     * @throws Exception Erreur lors de la communication avec le stockservice
     */
    private ResponseEntity<String> sendRequestToGetStock(String isbn) throws Exception {
        String url = "https://stock-service.herokuapp.com/stockservice/stock?isbn=" + isbn;
        return restTemplate.getForEntity(url, String.class);
    }

    /**
     * Permet d'envoyer une requête au stockservice pour acheter un livre en "X" quantité
     * @param isbn Isbn du livre
     * @param quantity Quantité du livre à acheter
     * @return La réponse http de la requête
     * @throws Exception Erreur lors de la communication avec le stockservice
     */
    private ResponseEntity<String> sendRequestToDecreaseStock(String isbn, int quantity) throws Exception {
        String url = "https://stock-service.herokuapp.com/stockservice/decrease?isbn=" + isbn + "&quantity=" + quantity;
        return restTemplate.postForEntity(url, "", String.class);
    }

    /**
     * Permet d'envoyer une requête au wholesalerservice pour commander des livres afin d'alimenter le stock
     * @param isbn Isbn du livre
     * @return La réponse http de la requête
     * @throws Exception Erreur lors de la communication avec le wholesalerservice
     */
    private ResponseEntity<String> sendRequestToOrder(String isbn) throws Exception {
        String url = "https://whole-saler-service.herokuapp.com/wholesalerservice/order?isbn=" + isbn;
        return restTemplate.postForEntity(url, "", String.class);
    }
}
