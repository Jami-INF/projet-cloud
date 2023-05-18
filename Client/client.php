<?php

require 'vendor/autoload.php';

use GuzzleHttp\Client;

$client = new Client();

$isbn = "978-0-306-40615-7";

// Affiche le stock de tous les livres
$stockAll = $client->get('https://stock-service.herokuapp.com/stockservice/stockall');

$result = $stockAll->getBody()->getContents();
echo $result . "\n\n";

// Affiche le stock du livre qui possède l'isbn 978-0-306-40615-7
$book = $client->get('https://shoppingservice-385621.oa.r.appspot.com/shoppingservice/book?isbn=' . $isbn);

$result = $book->getBody()->getContents();
echo $result . "\n\n";

// Achete le livre qui possède l'isbn 978-0-306-40615-7
$buy = $client->post('https://shoppingservice-385621.oa.r.appspot.com/shoppingservice/buy?isbn=' . $isbn);

$result = $buy->getBody()->getContents();
echo $result . "\n\n";



/**
 *  GetStock
 *  GetStockAll
 *  (Increase/Decrease)
 * 
 *  Order (Commande 5 livres)
 * 
 *  book ( Affiche le stock d'un livre)
 *  buy (Achete un ou plusieurs livre si disponible sinon --> commande 5 livres)
 */