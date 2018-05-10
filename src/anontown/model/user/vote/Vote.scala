package anontown.model.user.vote;

import anontown.model.res.Res;
import anontown.model.user.User
import anontown.model.user.Attestation
import anontown.model.user.token.Token

object Vote {
    def apply(token: Token, res: Res): Vote = {
        return new Vote(0, token.user, res.id);
    }
}

case class Vote private[vote] (id: Int, user: Int, res: Int) {
    private[vote] def withId(id: Int): Vote = {
        return this.copy(id = id);
    }
}
