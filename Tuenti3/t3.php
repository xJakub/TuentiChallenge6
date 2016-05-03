<?php

if (!extension_loaded('yaml')) {
    die('This problem uses the YAML PECL library. It needs to be downloaded and enabled in your PHP installation to make this code work.');
}

$yaml = yaml_parse_file('submitInput.sql');
$output = "";

/*
    We read the input file using a YAML parser. Then, for each tape, we just
    follow the actions until we get to the "end" state.
*/

foreach($yaml['tapes'] as $tapeId => $tape) {
    $state = 'start';
    $offset = 0;
    
    while ($state != 'end') {
        if ($offset < strlen($tape)) {
            $c = $tape[$offset];
        } else {
            $c = ' ';
        }
        
        if (!isset($yaml['code'][$state][$c])) {
            die("Error: there is no action for character '{$c}' at state {$state}.");
        }
        
        $action = $yaml['code'][$state][$c];
        
        if (isset($action['write'])) {
            $tape[$offset] = $action['write'];
        }
        
        if (isset($action['move'])) {
            if ($action['move'] == 'left') {
                $offset--;
            } else if ($action['move'] == 'right') {
                $offset++;
            }
        }
        
        if (isset($action['state'])) {
            $state = $action['state'];
        }
    }
    
    echo $result = "Tape #{$tapeId}: $tape\n";
    $output .= $result; 
}

file_put_contents('output.txt', $output);