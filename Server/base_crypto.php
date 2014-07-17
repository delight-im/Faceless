<?php

require_once(__DIR__.'/public_html/classes/Encryption.php');

function encrypt($data, $messageSecret) {
    $e = new Encryption(MCRYPT_RIJNDAEL_128, MCRYPT_MODE_CBC);
    return $e->encrypt($data, createKey($messageSecret));
}

function decrypt($data, $messageSecret) {
    $e = new Encryption(MCRYPT_RIJNDAEL_128, MCRYPT_MODE_CBC);
    $res = $e->decrypt($data, createKey($messageSecret));
    if ($res !== false) {
        return $res;
    }
    else {
        // delete this branch after some transition period
        $e = new Encryption(MCRYPT_BLOWFISH, MCRYPT_MODE_CBC);
        return $e->decrypt($data, createKey($messageSecret));
    }
}

?>