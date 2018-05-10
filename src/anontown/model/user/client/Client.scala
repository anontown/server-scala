package anontown.model.user.client

import anontown.model.user.User
import anontown.model.StringMethod
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.exceptions.UnauthorizedException

object Client{
    def apply(name:String,url:String,user:User,pass:String):Client={
        if(!StringMethod.isUrl(url)){
            throw new MisdirectedRequestException("URLが不正です");
        }
        if (!user.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }

        return new Client(0,name,url,user.id);
    }
}

case class Client private[client](id:Int,name:String,url:String,user:Int) {
    private[client] def withId(id: Int): Client = {
        return this.copy(id = id);
    }

    def withData(name:String,url:String,user:User,pass:String):Client={
        if(this.user!=user.id||(!user.attestation(pass))){
            throw new UnauthorizedException("認証に失敗しました");
        }
        if(!StringMethod.isUrl(url)){
            throw new MisdirectedRequestException("URLが不正です");
        }
        return this.copy(name=name,url=url);
    }
}