package anontown.model.user.token

import anontown.model.user.User
import java.time.LocalDateTime
import anontown.model.Config
import anontown.model.Hash
import scala.util.Random
import anontown.model.user.storage.Storage
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.exceptions.UnauthorizedException
import anontown.model.user.client.Client

object Token {
    def apply(user: User, pass: String, client:Client): Token = {
        if (!pass.matches(Config.USER_PASS)) {
            throw new MisdirectedRequestException("パスワードが" + Config.USER_PASS + "に一致しません");
        }

        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }

        //トークン生成
        val token = Token.makeToken;

        return new Token(0, token,client.id, user.id, true);
    }

    def makeToken():String={
        return Hash.hashLong(Random.alphanumeric.take(30).mkString + Config.TOKEN_SALT);
    }
}

case class Token private[token] (id: Int, token: String,client:Int, user: Int, active: Boolean,isAuth:Boolean=false) {

    private[token] def withId(id: Int): Token = {
        return this.copy(id = id);
    }

    def disable(user:User,pass:String): Token = {
        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        return this.copy(active = false);
    }

    def enable(user:User,pass:String): Token = {
        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        return this.copy(active = true);
    }

    def tokenChange(user:User,pass:String):Token={
        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        return this.copy(token=Token.makeToken());
    }

    def auth(token:String):Token={
        if (this.token != token) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        if (!this.active) {
            throw new UnauthorizedException("認証に失敗しました");
        }
        return this.copy(isAuth=true);
    }
}