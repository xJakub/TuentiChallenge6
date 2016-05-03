<?php
$con = file_get_contents('memory.obj');

$im = imagecreatetruecolor(256, 192);
$black = imagecolorallocate($im, 0, 0, 0);
$white = imagecolorallocate($im, 255, 255, 255);

for($y=0; $y<192; $y++) {
    for ($x=0; $x<256; $x++) {
        $address = 0x4000;
        $address += ($x >> 3);
        $address += ($y & 0x7) << 8;
        $address += (($y >> 3) & 0x7) << 5;
        $address += (($y >> 6) & 0x3) << 11;
        
        $bit = $x & 7;
        $ord = ord($con[$address]);
        if (($ord >> (7-$bit)) & 1) {
            imagesetpixel($im, $x, $y, $white);
        }
    }
}

imagepng($im, 'output.png');