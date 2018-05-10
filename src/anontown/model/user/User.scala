package anontown.model.user;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import anontown.model.Config;
import anontown.model.Hash;
import anontown.model.res.Res;
import anontown.model.topic.Topic;
import anontown.model.user.profile.Profile;
import anontown.model.user.storage.Storage;
import anontown.model.user.vote.Vote;
import anontown.model.user.token.Token
import anontown.model.exceptions.UnauthorizedException
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.exceptions.ForbiddenException

object User {
    def apply(sn: String, pass: String): User = {
        if (!pass.matches(Config.USER_PASS)) {
            throw new MisdirectedRequestException("パスワードが" + Config.USER_PASS + "に一致しません");
        }
        if (!sn.matches(Config.USER_SCREEN_NAME)) {
            throw new MisdirectedRequestException("スクリーンネームが" + Config.USER_SCREEN_NAME + "に一致しません");
        }
        val now = LocalDateTime.now();

        return new User(0, sn, Hash.hashLong(pass + Config.SALT_PASS), 1, now, now);
    }
}

case class User private[user] (id: Int,
        sn: String,
        pass: String,
        lv: Int,
        lastRes: LocalDateTime,
        lastTopic: LocalDateTime) {

    def withId(id: Int): User = {
        return this.copy(id = id);
    }

    //{{setter
    def withPass(oldPass: String, newPass: String): User = {
        if (!this.attestation(oldPass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }

        if (!newPass.matches(Config.USER_PASS)) {
            throw new MisdirectedRequestException("パスワードが" + Config.USER_PASS + "に一致しません");
        }

        return this.copy(pass = Hash.hashLong(newPass + Config.SALT_PASS));
    }

    def attestation(pass: String): Boolean = {
        return this.pass == Hash.hashLong(pass + Config.SALT_PASS);
    }

    def withSn(pass: String, sn: String): User = {
        if (!this.attestation(pass)) {
            throw new UnauthorizedException("認証に失敗しました");
        }

        if (!sn.matches(Config.USER_SCREEN_NAME)) {
            throw new MisdirectedRequestException("スクリーンネームが" + Config.USER_SCREEN_NAME + "に一致しません");
        }

        return this.copy(sn = sn);
    }

    def withLv(lv:Int): User = {
        val l=if(lv<1){
            1;
        }else if(lv>1000){
            1000;
        }else{
            lv;
        }
        return this.copy(lv = l);
    }

    def withLastRes(lastRes: LocalDateTime,isAuto:Boolean):User = {
        if((!Config.WAIT)||isAuto||lastRes.isAfter(this.lastRes.plusSeconds((60 * 10) / this.lv + 10))){
            return this.copy(lastRes = lastRes);
        }else{
            throw new ForbiddenException("連続書き込みはできません");
        }
    }
    def withLastTopic(lastTopic: LocalDateTime):User ={
        if((!Config.WAIT)||lastTopic.isAfter(this.lastTopic.plusMinutes((60 * 24) / this.lv + 30))){
            return this.copy(lastTopic = lastTopic);
        }else{
            throw new ForbiddenException("連続書き込みはできません");
        }

    }

    //}}
}
