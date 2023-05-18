<?php

require 'vendor/autoload.php';

use GuzzleHttp\Client;

$client = new Client();

$isbn = "978-0-306-40615-7";
// $correlationId = "136489304656392047930278404540";

$url_stock = "https://stock-service.herokuapp.com/stockservice/";
$url_shopping = "https://shoppingservice-385621.oa.r.appspot.com/shoppingservice/";

// Ajouter un user
$addUser = $client->post($url_stock . 'adduser?username=Jean');

$result = $addUser->getBody()->getContents();
echo $result . "\n\n";

$correlationId = $result;

// Affiche le stock de tous les livres
$stockAll = $client->get($url_stock . 'stockall', [
    'headers' => [
        'Authorization' => $correlationId
    ]
]);

$result = $stockAll->getBody()->getContents();
echo $result . "\n\n";

// Affiche le stock du livre qui possède l'isbn 978-0-306-40615-7
$book = $client->get($url_shopping . 'book?isbn=' . $isbn, [
    'headers' => [
        'Authorization' => $correlationId
    ]
]);

$result = $book->getBody()->getContents();
echo $result . "\n\n";

// Achete le livre qui possède l'isbn 978-0-306-40615-7
$buy = $client->post($url_shopping . 'buy?isbn=' . $isbn, [
    'headers' => [
        'Authorization' => $correlationId
    ]
]);

$result = $buy->getBody()->getContents();
echo $result . "\n\n";



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
 */