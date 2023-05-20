<?php

require 'vendor/autoload.php';

use GuzzleHttp\Client;

$isbn = "978-600-119-125-1";

$url_stock = "https://stock-service.herokuapp.com/stockservice/";
$url_shopping = "https://shoppingservice-385621.oa.r.appspot.com/shoppingservice/";


for($i = 0; $i < 5; $i++){
    $client = new Client();

    // Ajouter un user
    $addUser = $client->post($url_stock . 'adduser?username=Alice');

    $result = $addUser->getBody()->getContents();
    echo $result . "\n\n";

    $correlationId = $result;

    // Affiche le stock du livre qui possède l'isbn $isbn
    $book = $client->get($url_shopping . 'book?isbn=' . $isbn, [
        'headers' => [
            'Authorization' => $correlationId
        ]
    ]);

    $result = $book->getBody()->getContents();
    echo $result . "\n\n";

    // Achete le livre qui possède l'isbn $isbn
    $buy = $client->post($url_shopping . 'buy?isbn=' . $isbn . '&quantity=3', [
        'headers' => [
            'Authorization' => $correlationId
        ]
    ]);

    $result = $buy->getBody()->getContents();
    echo $result . "\n\n";

    // Affiche le stock du livre qui possède l'isbn $isbn
    $book = $client->get($url_shopping . 'book?isbn=' . $isbn, [
        'headers' => [
            'Authorization' => $correlationId
        ]
    ]);

    $result = $book->getBody()->getContents();
    echo $result . "\n\n";

    sleep(1);
}