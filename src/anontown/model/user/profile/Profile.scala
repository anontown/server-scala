package anontown.model.user.profile;

import anontown.model.Config
import anontown.model.user.User
import anontown.model.user.Attestation
import anontown.model.user.token.Token
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.MarkDown
import anontown.model.exceptions.ForbiddenException

object Profile {
    def apply(token: Token, sid: String, name: String, text: String): Profile = {
        if (name.length > Config.PROFILE_NAME_LENGTH) {
            throw new MisdirectedRequestException("名前が長すぎます");
        }
        else if (name.isEmpty()) {
            throw new MisdirectedRequestException("名前がありません");
        }
        if (text.length > Config.PROFILE_TEXT_LENGTH) {
            throw new MisdirectedRequestException("本文が長すぎます");
        }
        else if (text.isEmpty()) {
            throw new MisdirectedRequestException("本文がありません");
        }
        if (!sid.matches(Config.PROFILE_SID)) {
            throw new MisdirectedRequestException("sidが不正です");
        }

        return new Profile(0, sid, token.user, name, text,MarkDown(text), true);
    }
}

case class Profile private[profile] (id: Int,
        sid: String,
        user: Int,
        name: String,
        text: String,
        mdtext:String,
        active: Boolean) {

    private[profile] def withId(id: Int): Profile = {
        return this.copy(id = id);
    }
    def withData(token:Token,name: String, text: String): Profile = {
        if (name.length > Config.PROFILE_NAME_LENGTH) {
            throw new MisdirectedRequestException("名前が長すぎます");
        }
        if (text.length > Config.PROFILE_TEXT_LENGTH) {
            throw new MisdirectedRequestException("本文が長すぎます");
        }
        if(token.user!=this.user){
            throw new ForbiddenException("人のプロフィールの変更は出来ません");
        }
        this.copy(name = name, text = text,mdtext=MarkDown(text));
    }

    def delete(token:Token):Profile = {
        if(token.user!=this.user){
            throw new ForbiddenException("人のプロフィールの削除は出来ません");
        }
        this.copy(active = false);
    }

}
