<?php

require 'vendor/autoload.php';

use GuzzleHttp\Client;

$isbn = "978-0-375-70114-8";
    
$url_stock = "https://stock-service.herokuapp.com/stockservice/";
$url_shopping = "https://shoppingservice-385621.oa.r.appspot.com/shoppingservice/";


for($i = 0; $i < 10; $i++){

    $client = new Client();
      
    // Ajouter un user
    $addUser = $client->post($url_stock . 'adduser?username=Pierre');
    
    $result = $addUser->getBody()->getContents();
    echo $result . "\n\n";
    
    $correlationId = $result;
    
    // Affiche le stock du livre qui possède l'isbn 978-0-306-40615-7
    $book = $client->get($url_shopping . 'book?isbn=' . $isbn, [
        'headers' => [
            'Authorization' => $correlationId
        ]
    ]);
    
    $result = $book->getBody()->getContents();
    echo $result . "\n\n";
    
    // Achete le livre qui possède l'isbn 978-0-306-40615-7
    $buy = $client->post($url_shopping . 'buy?isbn=' . $isbn . '&quantity=37', [
        'headers' => [
            'Authorization' => $correlationId
        ]
    ]);
    
    $result = $buy->getBody()->getContents();
    echo $result . "\n\n";
    
    // Affiche le stock du livre qui possède l'isbn 978-0-306-40615-7
    $book = $client->get($url_shopping . 'book?isbn=' . $isbn, [
        'headers' => [
            'Authorization' => $correlationId
        ]
    ]);
    
    $result = $book->getBody()->getContents();
    echo $result . "\n\n";
}




/**
 *  GetStock
 *  GetStockAll
 *  (Increase/Decrease)
 *  addUser
 *  initDb
 * 
 *  Order (Commande 5 livres)
 * 
 *  book ( Affiche le stock d'un livre)
 *  buy (Achete un ou plusieurs livre si disponible sinon --> commande 5 livres)
 * 
 * 
 * 
 */