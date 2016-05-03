<?php

$ip = '52.49.91.111';
$port = '1986';

$sock = fsockopen($ip, $port);

$visited = [];
$explored = [];
$charMap = [];

$curX = 0;
$curY = 0;
$turn = 1;
$minX = 0;
$maxX = 0;

while (!feof($sock)) {
    echo $msg = fread($sock, 1024);
    $lines = explode("\n", $msg);
    
    $visited[$curX][$curY] = true;
    $explored[$curX][$curY] = true;
    
    foreach($lines as $lineIndex => $line) {
        $cy = $curY + ($lineIndex - 3);
        for ($i=0; $i<strlen($line); $i++) {
            $c = $line[$i];
            $cx = $curX + ($i - 3);
            $charMap[$cy][$cx] = $c;
        }
    }
    
    // Get the next move for visiting every possible tile
    $option = getNextMove($curX, $curY);
    
    if (!$option) {
        echo "-- Failed. Allowing returns --\n";
        
        // No option. Clear visited tiles
        $visited = [];
        // Try to get a best option again
        $option = getNextMove($curX, $curY);
        
        if (!$option) {
            echo "-- No exit found. --\n";
            exit;
        }
    }
    
    $minX = min($minX, $curX);
    $maxX = max($maxX, $curX);
    
    $curX = $option[2];
    $curY = $option[3];
    
    // select any option, as long as it doesn't end up
    // hitting a wall
    echo "-> $turn. {$option[1]} ($curX, $curY)\n";
    fwrite($sock, "{$option[1]}\n");
    
    $turn++;
    
    // store the map in map.txt
    $con = "";
    ksort($charMap);
    foreach($charMap as $charArr) {
        $line = str_repeat(' ', ($maxX+3)-($minX-3)+1);
        foreach($charArr as $cX => $cVal) {
            $line[$cX-($minX-3)] = $cVal;
        }
        $con .= "{$line}\n";
    }
    file_put_contents("map.txt", $con);
}


function getNextMove($x, $y, $disabledTiles=[]) {
    global $charMap, $visited, $explored;

    // possible movements
    $mayBeOptions = ['r'=>[$x+1, $y], 'l'=>[$x-1, $y], 'u'=>[$x, $y-1], 'd'=>[$x, $y+1]];
    
    // process only the physically possible ones
    $options = [];
    $bestResult = false;
    
    foreach($mayBeOptions as $key => $option) {
        list($newX, $newY) = $option;
    
        if (!isset($disabledTiles[$newX][$newY]) && !isset($visited[$newX][$newY]) && $charMap[$newY][$newX] != '#') {
            // we want to explore every tile
            if (!isset($explored[$newX][$newY])) {
                return [0, $key, $newX, $newY];
            }
        
            $disabledTiles[$newX][$newY] = 1;
            if ($oldResult = getNextMove($newX, $newY, $disabledTiles)) {
                $result = [$oldResult[0]+1, $key, $newX, $newY];
                if (!$bestResult || $result[0] < $bestResult[0]) {
                    $bestResult = $result;
                }
            }
        }
    }
    return $bestResult;
}