package anontown.model.user.tokenReq

import java.time.LocalDateTime
import anontown.model.user.token.Token
import anontown.model.Hash
import scala.util.Random

object TokenReq {
    def apply(token:Token): TokenReq = {
        return new TokenReq(0,
                Hash.hashLong(Random.alphanumeric.take(30).mkString),
                token.id,
                LocalDateTime.now().plusMinutes(10));
    }
}

case class TokenReq private[tokenReq] (id: Int, key:String,token:Int,expireDate:LocalDateTime) {
    private[user] def withId(id: Int): TokenReq = {
        return this.copy(id = id);
    }

    def checkDate():Boolean={
        return this.expireDate.isAfter(LocalDateTime.now());
    }
}
