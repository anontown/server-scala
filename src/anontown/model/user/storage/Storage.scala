package anontown.model.user.storage;

import anontown.model.Config;
import anontown.model.user.User
import anontown.model.user.token.Token

object Storage {
    def apply(token: Token): Storage = {
        return new Storage(0, token.user, token.id, "");
    }
}

case class Storage private[storage] (id: Int, user: Int, token: Int, value: String) {
    private[storage] def withId(id: Int): Storage = {
        return this.copy(id = id);
    }

    def withValue(value: String): Storage = {
        return this.copy(value = value);
    }
}
