package anontown.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

object Hash {
    /**
     * SHA-64を使った長いハッシュを取得します
     * @param key キー
     * @return ハッシュ
     */
    def hashLong(key: String): String = {
        return Hash.hash(key, "SHA-256");
    }

    /**
     * MD5を使った短いハッシュを取得します
     * @param key キー
     * @return ハッシュ
     */
    def hashShort(key: String): String = {
        return Hash.hash(key, "MD5");
    }

    /**
     * ハッシュを返します
     * @param key キー
     * @param algo アルゴリズム
     * @return ハッシュ。アルゴリズムが見つからなければnull
     */
    def hash(key: String, algo: String): String = {
        try {
            //アルゴリズムを指定して、インスタンス取得
            val md = MessageDigest.getInstance(algo);

            //キーとなる文字列をバイナリに変えて設定
            md.update(key.getBytes());

            //ハッシュバイナリ取得
            val bs = md.digest();

            //base64エンコードして、=を削除して返す
            return new String(Base64.getEncoder().encode(bs)).replace("=", "");
        } catch {
            case e: NoSuchAlgorithmException => return null;
        }
    }
}
