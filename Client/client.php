<?php

require 'vendor/autoload.php';

use GuzzleHttp\Client;

$client = new Client();
$response = $client->get('https://stock-service.herokuapp.com/stockservice/stockall');
$result = $response->getBody()->getContents();

echo $result;