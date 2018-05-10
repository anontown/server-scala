package anontown.model;

object DeleteFlag {
    case object ALIVE extends DeleteFlag(0)
    case object SELF extends DeleteFlag(1)
    case object VOTE extends DeleteFlag(2)
    case object FREEZE extends DeleteFlag(3)

    val values = Array(ALIVE, SELF, VOTE, FREEZE)
}

sealed abstract class DeleteFlag(val id: Int) {
    val name = toString
}
