<?php

/**
    This script connects to the server and starts guessing letters.
    The received messages are shown via standard output.
    
    There is a high chance of failing the first levels,
    so we just run it until we complete them all.
*/

$ip = '52.49.91.111';
$port = '9988';

$sock = fsockopen($ip, $port);
$words = str_replace("\r", '', file_get_contents('words.txt'));

$started = false;

while (!feof($sock)) {
    echo $msg = fread($sock, 1024);
    
    if (strpos($msg, "Time remaining") === FALSE) {
        // Not playing
        $used = [];
        fwrite($sock, "\n");
        
    } else {
        preg_match("'([A-Z\_] )+[A-Z\_]'", $msg, $match);
        
        if ($match) {
            // We need to fill a word
            $regex = "'^" . str_replace(' ', '', str_replace('_', '[A-Z]', $match[0])) . "$'m";
            
            $invalids = [];
            foreach($used as $c => $true) {
                if (strpos($match[0], $c) === FALSE) {
                    $invalids[] = $c;
                }
            }
            
            if (preg_match_all($regex, $words, $matches)) {
                $options = [];
                $length = strlen($matches[0][0]);
                
                foreach($matches[0] as $match) {
                    $ok = true;
                    foreach($invalids as $invalid) {
                        if (strpos($match, $invalid) !== FALSE) {
                            $ok = false;
                            break;
                        }
                    }
                    if (!$ok) continue;
                
                    for ($i=0; $i<$length; $i++) {
                        $c = $match[$i];
                        if (!$used[$c]) {
                            $options[$c]++;
                        }
                    }
                }
                
                arsort($options);
                
                // Get the most frequent letter among the possible words
                $option = array_keys($options)[0];
                
                echo "Option selected: $option\n";
                $used[$option] = true;
                fwrite($sock, "{$option}");
            }
            
        }
    }
}