package anontown.model.res;

import java.time.LocalDateTime;

import anontown.model.Config;
import anontown.model.DeleteFlag;
import anontown.model.StringMethod;
import anontown.model.topic.Topic;
import anontown.model.user.Attestation;
import anontown.model.user.User;
import anontown.model.user.profile.Profile;
import anontown.model.user.vote.Vote
import anontown.model.user.token.Token
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.exceptions.ForbiddenException
import anontown.model.exceptions.ForbiddenException
import anontown.model.exceptions.ConflictException
import anontown.model.MarkDown
import anontown.model.exceptions.NotFoundException
import anontown.model.user.msg.Msg

object Res {
    def apply(topic: Topic, user: User, token: Token, name: String,autoName:Option[String], text: String, reply: Option[Res], profile: Option[Profile]): (Res, User,Topic) = {
        if (name.length() > Config.RESPONSE_NAME_MAX_LENGTH) {
            throw new MisdirectedRequestException("名前が長すぎます。");
        }
        if (text.length() > Config.RESPONSE_TEXT_MAX_LENGTH) {
            throw new MisdirectedRequestException("本文が長すぎます。");
        }
        else if (text.isEmpty()) {
            throw new MisdirectedRequestException("本文がありません");
        }

        val date = LocalDateTime.now();

        //名前生成
        val nameP = {
            val n=
            if(name.length()==0&&autoName.isEmpty&&profile.isEmpty){
                Config.DEFAULT_NAME;
            }else{
                StringMethod.graphicEscape(StringMethod.line(name, " "));
            }

            val n2=
            if (profile.isDefined) {
                n + "●" + profile.get.sid;
            } else {
                n;
            }

            if(autoName.isDefined){
                n2+"■"+autoName.get;
            }else{
                n2;
            }
        }

        val profileId =
            if (profile.isDefined) {
                //自分のプロフィールじゃない
                if(profile.get.user!=user.id){
                    throw new NotFoundException("プロフィールが見つかりません");
                }
                profile.get.id;
            } else {
                0;
            }

        return (new Res(0,
            topic.id,
            date,
            user.id,
            nameP,
            text,
            MarkDown(text),
            if (reply.isDefined) reply.get.id else 0,
            DeleteFlag.ALIVE,
            0,
            user.lv,
            topic.hash(date, user),
            0,
            if (profile.isDefined) profile.get.id else 0),
            user.withLastRes(date,autoName.isDefined),
            topic.withUpdatetime(date));
    }
}
case class Res private[res] (id: Int,
        topic: Int,
        date: LocalDateTime,
        user: Int,
        name: String,
        text: String,
        mdtext:String,
        reply: Int,
        delete: DeleteFlag,
        vote: Int,
        lv: Int,
        hash: String,
        replyCount: Int,
        profile: Int) {
    private[res] def withId(id: Int): Res = {
        return this.copy(id = id);
    }

    //}}
    //{{メソッド
    def uv(resUser:User,user: User, token: Token): (Res, Vote,User) = {
        if(user.id==this.user){
            throw new ForbiddenException("自分に投票は出来ません");
        }
        val lv=resUser.lv+user.lv/100+1;
        return (this.copy(vote = this.vote + user.lv), Vote(token, this),resUser.withLv(lv));
    }

    def dv(resUser:User,user: User, token: Token): (Res, Vote,User,Option[Msg]) = {
        if(user.id==this.user){
            throw new ForbiddenException("自分に投票は出来ません");
        }
        val vote = this.vote - user.lv;
        val delMsg= if (-vote > this.lv*5 && this.delete.id <= DeleteFlag.SELF.id){
            (DeleteFlag.VOTE,Some(Msg(Some(resUser),"投票により書き込みが削除されました。")));
        }else{
            (this.delete,None);
        }
        val lv=resUser.lv-user.lv/100-1;
        return (this.copy(vote = vote, delete = delMsg._1), Vote(token, this),resUser.withLv(lv),delMsg._2);
    }

    def delete(resUser:User,token: Token): (Res,User) = {
        if (token.user != this.user) {
            throw new ForbiddenException("人の書き込み削除は出来ません");
        }

        if (this.delete != DeleteFlag.ALIVE) {
            throw new ConflictException("既に削除済みです");
        }

        return (this.copy(delete = DeleteFlag.SELF),resUser.withId(resUser.lv-resUser.lv/100));
    }

    def replyAdd():Res={
        return this.copy(replyCount=this.replyCount+1);
    }
    //}}
}