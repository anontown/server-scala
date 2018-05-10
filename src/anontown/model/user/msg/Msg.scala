package anontown.model.user.msg;

import java.time.LocalDateTime;

import anontown.model.user.User;
import anontown.model.MarkDown

object Msg {
    def apply(receiver: Option[User], text: String): Msg = {
        return new Msg(0, if (receiver.isDefined) receiver.get.id else 0, text,MarkDown(text), LocalDateTime.now());
    }
}

case class Msg private[msg] (id: Int, receiver: Int, text: String,mdtext:String, date: LocalDateTime) {
    private[user] def withId(id: Int): Msg = {
        return this.copy(id = id);
    }
}
