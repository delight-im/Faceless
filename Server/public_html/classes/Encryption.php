<?php

/**
 * Secure encryption and decryption of arbitrary data
 *
 * + provides key rotation for cipher and auth keys
 * + uses key stretching with a PBKDF2 derivation
 * + ensures integrity and authenticity of encrypted data with HMAC
 * + hides the initialization vector (IV)
 *
 * @author Anthony Ferrara
 */
class Encryption {

    /** @var string $cipher the mcrypt cipher to use */
    protected $cipher = '';
    /** @var int $mode the mcrypt cipher mode to use */
    protected $mode = '';
    /** @var int $rounds the number of rounds to feed into PBKDF2 for key generation */
    protected $rounds = 16;

    /**
     * Constructs a new Encryption instance that can be used to call encrypt(...) or decrypt(...)
     *
     * @param string $cipher the MCRYPT_* cypher to use
     * @param int $mode the MCRYPT_MODE_* mode to use
     * @param int $rounds the number of PBKDF2 rounds to do on the key
     */
    public function __construct($cipher, $mode, $rounds = 16) {
        $this->cipher = $cipher;
        $this->mode = $mode;
        $this->rounds = (int) $rounds;
    }

    /**
     * Decrypts the data with the provided key
     *
     * @param string $data the encrypted data to decrypt
     * @param string $key the key to use for decryption
     * @return string|false the decrypted string or false
     */
    public function decrypt($data, $key) {
        $salt = substr($data, 0, 128);
        $enc = substr($data, 128, -64);
        $mac = substr($data, -64);

        list ($cipherKey, $macKey, $iv) = $this->getKeys($salt, $key);

        if (!hash_equals($mac, hash_hmac('sha512', $enc, $macKey, true))) {
             return false;
        }

        $dec = mcrypt_decrypt($this->cipher, $cipherKey, $enc, $this->mode, $iv);

        $data = $this->unpad($dec);

        return $data;
    }

    /**
     * Encrypts the data with the provided key
     * 
     * @param string $data the decrypted data to encrypt
     * @param string $key the key to use for encryption
     * @return string the encrypted data
     */
    public function encrypt($data, $key) {
        $salt = mcrypt_create_iv(128, MCRYPT_DEV_URANDOM);
        list ($cipherKey, $macKey, $iv) = $this->getKeys($salt, $key);

        $data = $this->pad($data);

        $enc = mcrypt_encrypt($this->cipher, $cipherKey, $data, $this->mode, $iv);

        $mac = hash_hmac('sha512', $enc, $macKey, true);
        return $salt . $enc . $mac;
    }

    /**
     * Generates a set of keys given a random salt and a master key
     *
     * @param string $salt a random string to change the keys each encryption
     * @param string $key the supplied key to encrypt with
     * @return array an array of keys (a cipher key, a mac key, and a IV)
     */
    protected function getKeys($salt, $key) {
        $ivSize = mcrypt_get_iv_size($this->cipher, $this->mode);
        $keySize = mcrypt_get_key_size($this->cipher, $this->mode);
        $length = 2 * $keySize + $ivSize;

        $key = $this->pbkdf2('sha512', $key, $salt, $this->rounds, $length);

        $cipherKey = substr($key, 0, $keySize);
        $macKey = substr($key, $keySize, $keySize);
        $iv = substr($key, 2 * $keySize);
        return array($cipherKey, $macKey, $iv);
    }

    /**
     * Stretch the key using the PBKDF2 algorithm
     *
     * @param string $algo the algorithm to use
     * @param string $key the key to stretch
     * @param string $salt a random salt
     * @param int $rounds the number of rounds to derive
     * @param int $length the length of the output key
     * @return string the derived key
     */
    protected function pbkdf2($algo, $key, $salt, $rounds, $length) {
        $size = strlen(hash($algo, '', true));
        $len = ceil($length / $size);
        $result = '';
        for ($i = 1; $i <= $len; $i++) {
            $tmp = hash_hmac($algo, $salt . pack('N', $i), $key, true);
            $res = $tmp;
            for ($j = 1; $j < $rounds; $j++) {
                 $tmp  = hash_hmac($algo, $tmp, $key, true);
                 $res ^= $tmp;
            }
            $result .= $res;
        }
        return substr($result, 0, $length);
    }

    protected function pad($data) {
        $length = mcrypt_get_block_size($this->cipher, $this->mode);
        $padAmount = $length - strlen($data) % $length;
        if ($padAmount == 0) {
            $padAmount = $length;
        }
        return $data . str_repeat(chr($padAmount), $padAmount);
    }

    protected function unpad($data) {
        $length = mcrypt_get_block_size($this->cipher, $this->mode);
        $last = ord($data[strlen($data) - 1]);
        if ($last > $length) return false;
        if (substr($data, -1 * $last) !== str_repeat(chr($last), $last)) {
            return false;
        }
        return substr($data, 0, -1 * $last);
    }

}

?>