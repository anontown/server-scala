package anontown.model.user;

/**
 * 認証のフラグ
 */
object Attestation {
    /**
     * いいえ
     */
    case object NONE extends Attestation(0);

    /**
     * サブパスワードで
     */
    case object SUB extends Attestation(1);

    /**
     * マスターパスワードで
     */
    case object MASTER extends Attestation(2);

    val values = Array(NONE, SUB, MASTER)
}

sealed abstract class Attestation(val id: Int) {
    val name = toString
}
