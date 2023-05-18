package shoppingService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;

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
     * @param auth Token d'authentification
     * @return Une réponse http
     */
    @GetMapping(value = "/book", produces = "text/plain")
    public ResponseEntity<String> bookRequest(@RequestParam(value = "isbn", defaultValue = "") String isbn,  @RequestHeader("Authorization") String auth) {
        if (validateIsbn(isbn)) {
            ResponseEntity<String> responseGetStock;
            try {
                responseGetStock = sendRequestToGetStock(isbn, auth);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("StockService unreachable " + e.getMessage());
            }

            // Get the json response
            String entity = responseGetStock.getBody();

            switch (responseGetStock.getStatusCode()) {
                case OK:
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode entityJson;
                    try {
                        entityJson = mapper.readTree(entity);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Problem with StockService");
                    }
                    return ResponseEntity.status(HttpStatus.OK).body("Il reste " + entityJson.get("stock").asText() + " exemplaire(s) du livre " + entityJson.get("name").asText());
                case INTERNAL_SERVER_ERROR:
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Problem with StockService");
                default:
                    return ResponseEntity.status(responseGetStock.getStatusCode()).body(entity);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Isbn invalid");
    }

    /**
     * Permet d'acheter un livre en le désignant par son isbn et en indiquant la quantité à acheter
     * @param isbn Identifie le livre et doit être valide au format ISBN-13
     * @param quantity Quantité du livre à acheter
     * @param auth Token d'authentification
     * @return Une réponse http
     */
    @PostMapping(value = "/buy", produces = "text/plain")
    public ResponseEntity<String> buyRequest(
            @RequestParam("isbn") String isbn,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            @RequestHeader("Authorization") String auth
            ) {

        if (validateIsbn(isbn)) {
            ResponseEntity<String> responseGetStock;
            ResponseEntity<String> responseDecreaseStock;
            ResponseEntity<String> responseOrderStock;
            try {
                responseGetStock = sendRequestToGetStock(isbn, auth);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("StockService unreachable " + e.getMessage());
            }

            // Get the json response
            String entity = responseGetStock.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode entityJson;

            try {
                entityJson = mapper.readTree(entity);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Problem with StockService");
            }

            switch (responseGetStock.getStatusCode()) {
                case OK:
                    int stock = Integer.parseInt(entityJson.get("stock").asText());

                    String bookName = entityJson.get("name").asText();

                    if (stock < quantity) {

                        while (stock < quantity) {
                            try {
                                responseOrderStock = sendRequestToOrder(isbn, auth);
                                stock = stock + 5;
                            } catch (Exception e) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("StockService unreachable " + e.getMessage());
                            }

                            switch (responseOrderStock.getStatusCode()) {
                                case OK:
                                    break;
                                case INTERNAL_SERVER_ERROR:
                                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                            .body("Problem with StockService");
                                default:
                                    return ResponseEntity.status(responseOrderStock.getStatusCode())
                                            .body("An error occured");
                            }
                        }

                    }

                    try {
                        responseDecreaseStock = sendRequestToDecreaseStock(isbn, quantity, auth);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("StockService unreachable " + e.getMessage());
                    }

                    switch (responseDecreaseStock.getStatusCode()) {
                        case OK:
                            return ResponseEntity.status(HttpStatus.OK)
                                    .body("You bought '" + bookName + "' in " + quantity + " copie(s)");
                        case INTERNAL_SERVER_ERROR:
                            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                    .body("Problem with StockService");
                        default:
                            return ResponseEntity.status(responseDecreaseStock.getStatusCode())
                                    .body("An error occured");
                    }

                case INTERNAL_SERVER_ERROR:
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body("Problem with StockService");
                default:
                    return ResponseEntity.status(responseGetStock.getStatusCode())
                            .body(entity);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Isbn invalid");
    }

    /**
     * Permet de valider un isbn au format ISBN-13
     * @param isbn L'isbn à valider
     * @return Si l'isbn est valide ou non
     */
    private boolean validateIsbn(String isbn)
    {
        // Supprime tous les tirets et espaces éventuels
        isbn = isbn.replaceAll("-", "").replaceAll(" ", "");

        // Vérifie si la longueur de l'ISBN est de 13 chiffres
        if (isbn.length() != 13) {
            return false;
        }

        try {
            // Calcule la somme de contrôle
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(isbn.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int checkDigit = Character.getNumericValue(isbn.charAt(12));

            // Vérifie si la somme de contrôle est valide
            int remainder = sum % 10;
            int calculatedCheckDigit = (remainder == 0) ? 0 : 10 - remainder;

            return checkDigit == calculatedCheckDigit;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Permet d'envoyer une requête au stockservice pour obtenir la quantité d'un livre en stock
     * @param isbn Isbn du livre
     * @param auth Token d'authentification
     * @return La réponse http de la requête
     */
    private ResponseEntity<String> sendRequestToGetStock(String isbn, String auth) {
        String url = "https://stock-service.herokuapp.com/stockservice/stock?isbn=" + isbn;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", auth);

        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

    }

    /**
     * Permet d'envoyer une requête au stockservice pour acheter un livre en "X" quantité
     * @param isbn Isbn du livre
     * @param quantity Quantité du livre à acheter
     * @return La réponse http de la requête
     */
    private ResponseEntity<String> sendRequestToDecreaseStock(String isbn, int quantity, String auth) throws Exception {
        String url = "https://stock-service.herokuapp.com/stockservice/decrease?isbn=" + isbn + "&quantity=" + quantity;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", auth);
        
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    /**
     * Permet d'envoyer une requête au wholesalerservice pour commander des livres afin d'alimenter le stock
     * @param isbn Isbn du livre
     * @return La réponse http de la requête
     */
      private ResponseEntity<String> sendRequestToOrder(String isbn, String auth) {
        String url = "https://whole-saler-service.herokuapp.com/wholesalerservice/order?isbn=" + isbn;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", auth);
        
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}
