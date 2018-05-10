package anontown.model.res

import java.sql.Connection;
import java.sql.ResultSet;
import scala.collection.mutable.ListBuffer;

import anontown.model.SQL;
import anontown.model.DeleteFlag
import anontown.model.exceptions.NotFoundException
import java.time.LocalDateTime
import anontown.model.topic.Topic
import anontown.model.user.User
import anontown.model.user.Attestation
import anontown.model.user.token.Token
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.exceptions.MisdirectedRequestException
import java.sql.Statement
import java.sql.Types
import java.sql.Timestamp

object ResRepository {
    private def toRes(rs: ResultSet): List[Res] = {
        val r= SQL.toObjList(rs, { rs =>
            Res(rs.getInt(1),
                rs.getInt(2),
                rs.getTimestamp(3).toLocalDateTime(),
                rs.getInt(4),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7),
                rs.getInt(8),
                DeleteFlag.values(rs.getInt(9)),
                rs.getInt(10),
                rs.getInt(11),
                rs.getString(12),
                rs.getInt(13),
                rs.getInt(14));
        });
        rs.close();
        return r;
    }
}

class ResRepository(private val con: Connection) {
    def add(res: Res): Res = {
        val ps = this.con.prepareStatement("INSERT INTO res VALUES(NULL,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, res.topic);
        ps.setTimestamp(2, Timestamp.valueOf(res.date));
        ps.setInt(3, res.user);
        ps.setString(4, res.name);
        ps.setString(5, res.text);
        ps.setString(6, res.mdtext);
        if (res.reply != 0){
            ps.setInt(7, res.reply);
        }else{
            ps.setNull(7,Types.INTEGER);
        }
        ps.setInt(8, res.delete.id);
        ps.setInt(9, res.vote);
        ps.setInt(10, res.lv);
        ps.setString(11, res.hash);
        ps.setInt(12, res.replyCount);
        if (res.profile != 0){
            ps.setInt(13, res.profile);
        }else{
            ps.setNull(13,Types.INTEGER);
        }

        ps.executeUpdate();

        val r=res.withId(SQL.getAutoIncrement(ps));
        ps.close();
        return r;
    }

    def save(res: Res) {
        val ps = this.con.prepareStatement("""UPDATE res
SET deleteFlag=?,vote=?,replyCount=?
WHERE id=?""");
        ps.setInt(1, res.delete.id);
        ps.setInt(2, res.vote);
        ps.setInt(3, res.replyCount);
        ps.setInt(4, res.id);

        ps.executeUpdate();
        ps.close();
    }

    def read(id: Int): Res = {
        if (id < 1) {
            throw new MisdirectedRequestException("IDは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("SELECT * FROM res WHERE id=?");
        ps.setInt(1, id);

        val list = ResRepository.toRes(ps.executeQuery());
        if (list.length == 0) {
            throw new NotFoundException("レスが見つかりません");
        }
        ps.close();
        return list(0);

    }

    def readAfter(topic: Topic, min: Int, limit: Int): List[Res] = {
        if (min < 1) {
            throw new MisdirectedRequestException("IDは1以上でなければいけません");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE topic=? AND id>=? ORDER BY id DESC LIMIT ?""");
        ps.setInt(1, topic.id);
        ps.setInt(2, min);
        ps.setInt(3, limit);

        val r= ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readBefore(topic: Topic, max: Int, limit: Int): List[Res] = {
        if (max < 1) {
            throw new MisdirectedRequestException("IDは1以上でなければいけません");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE topic=? AND id<=? ORDER BY id DESC LIMIT ?""");
        ps.setInt(1, topic.id);
        ps.setInt(2, max);
        ps.setInt(3, limit);

        val r=ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readNew(topic: Topic, limit: Int): List[Res] = {
        if (limit < 1) {
            throw new MisdirectedRequestException("limitは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE topic=?
ORDER BY id DESC LIMIT ?""");
        ps.setInt(1, topic.id);
        ps.setInt(2, limit);

        val r= ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readHash(topic: Topic, hash: String): List[Res] = {
        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE topic=? AND hash=?""");

        ps.setInt(1, topic.id);
        ps.setString(2, hash);

        val r=ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readReply(res: Res): List[Res] = {
        val ps = this.con.prepareStatement("SELECT * FROM res WHERE reply=?");
        ps.setInt(1, res.id);
        val r= ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readNoticeAfter(token: Token, min: Int, limit: Int): List[Res] = {
        if (min < 1) {
            throw new MisdirectedRequestException("IDは1以上でなければいけません");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE reply IN(SELECT id FROM res WHERE user=?) AND id>=?
ORDER BY id DESC LIMIT ?""");

        ps.setInt(1, token.user);
        ps.setInt(2, min);
        ps.setInt(3, limit);

        val r= ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readNoticeBefore(token: Token, max: Int, limit: Int): List[Res] = {
        if (max < 1) {
            throw new MisdirectedRequestException("IDは1以上でなければいけません");
        }
        if (limit < 1) {
            throw new MisdirectedRequestException("limitは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE reply IN(SELECT id FROM res WHERE user=?) AND id<=?
ORDER BY id DESC LIMIT ?""");

        ps.setInt(1, token.user);
        ps.setInt(2, max);
        ps.setInt(3, limit);

        val r= ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }

    def readNoticeNew(token: Token, limit: Int): List[Res] = {
        if (limit < 1) {
            throw new MisdirectedRequestException("limitは1以上でなければいけません");
        }

        val ps = this.con.prepareStatement("""SELECT * FROM res
WHERE reply IN(SELECT id FROM res WHERE user=?)
ORDER BY id DESC LIMIT ?""");

        ps.setInt(1, token.user);
        ps.setInt(2, limit);

        val r= ResRepository.toRes(ps.executeQuery());
        ps.close();
        return r;
    }
}