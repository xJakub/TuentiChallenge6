<?php

// Get the key from the map
$con = file_get_contents('map.txt');
$key = '';

// top and bottom characters
preg_match_all("'([0-9a-f]{2,})'", $con, $topBottomMatches);
preg_match_all("'^#([0-9a-f])#'m", $con, $leftMatches);
preg_match_all("'#([0-9a-f])#$'m", $con, $rightMatches);

$key = '';

// top
$key .= $topBottomMatches[1][0];
// right
$key .= implode('', $rightMatches[1]);
// bottom
$key .= strrev($topBottomMatches[1][1]);
// left
$key .= strrev(implode('', $leftMatches[1]));

echo $key;