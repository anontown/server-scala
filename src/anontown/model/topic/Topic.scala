package anontown.model.topic;

import java.time.LocalDateTime;
import java.util.Arrays;

import anontown.model.Config;
import anontown.model.StringMethod;
import anontown.model.topic.history.TopicHistory;
import anontown.model.user.Attestation;
import anontown.model.user.User;
import anontown.model.topic.history.TopicHistory
import anontown.model.topic.history.TopicHistoryRepository
import anontown.model.user.token.Token
import anontown.model.exceptions.MisdirectedRequestException
import anontown.model.MarkDown
import anontown.model.Hash
import java.time.ZoneId
import java.time.Instant
import anontown.model.res.Res

object Topic {
    def apply(title: String, category: List[String], text: String, user: User, token: Token): (Topic, User) = {
        Topic.checkTitle(title);
        Topic.checkCategory(category);
        Topic.checkText(text);

        val now = LocalDateTime.now();

        val result = new Topic(0,
            title,
            category,
            text,
            MarkDown(text),
            now,
            user.id,
            now);

        return (result, user.withLastTopic(now));
    }

    def checkTitle(title: String) {
        if (title.length() > Config.THREAD_TITLE_MAX_LENGTH) {
            throw new MisdirectedRequestException("タイトルは" + Config.THREAD_TITLE_MAX_LENGTH + "文字以内でなければいけません。");
        }
        else if (title.isEmpty()) {
            throw new MisdirectedRequestException("タイトルがありません");
        }
    }

    def checkCategory(category: List[String]) {
        if (category.length > Config.THREAD_CATEGORY_MAX_LENGTH) {
            throw new MisdirectedRequestException("カテゴリは" + Config.THREAD_CATEGORY_MAX_LENGTH + "階層以内でなければいけません。");
        }

        for (c <- category) {
            if (!c.matches(Config.THREAD_CATEGORY)) {
                throw new MisdirectedRequestException("カテゴリが" + Config.THREAD_CATEGORY + "に一致しません。");
            }
        }
    }

    def checkText(text: String) {
        if (text.length() > Config.THREAD_TEXT_MAX_LENGTH) {
            throw new MisdirectedRequestException("本文は" + Config.THREAD_TEXT_MAX_LENGTH + "文字以内でなければいけません。");
        }else if (text.isEmpty()) {
            throw new MisdirectedRequestException("本文がありません");
        }
    }

    //{{categoryConverter
    def toCategoryString(category: List[String]): String = {
        return category.mkString("/");
    }

    def toCategoryArray(category: String): List[String] = {
        if (category.isEmpty) {
            return List.empty[String];
        } else {
            return category.split("/").toList;
        }
    }

    //}}
    //{{titleEscape
    def titleEscape(title: String): String = {
        return StringMethod.line(title, " ");
    }

    //}}
}

case class Topic private[topic] (id: Int,
        title: String,
        category: List[String],
        text: String,
        mdtext:String,
        updatetime: LocalDateTime,
        user: Int,
        date: LocalDateTime) {
    //{{setter
    def withData(user: User, token: Token, title: String, category: List[String], text: String): (TopicHistory,Res,User,Topic) = {
        Topic.checkTitle(title);
        Topic.checkCategory(category);
        Topic.checkText(text);

        val t2 = this.copy(title = Topic.titleEscape(title), category = category, text = text,mdtext=MarkDown(text));
        return t2.getHistory(user,token);
    }

    def withUpdatetime(updatetime: LocalDateTime): Topic = {
        return this.copy(updatetime = updatetime);
    }

    private[topic] def withId(id: Int,user:User,token:Token): (TopicHistory,Res,User,Topic) = {
        return this.copy(id = id).getHistory(user,token);
    }

    //}}

    //{{method
    private def getHistory(user: User,token:Token): (TopicHistory,Res,User,Topic) = {
        val res=Res(this,user,token,"",Some("トピックデータ"),"トピックデータが編集されました",None,None);

        val date = LocalDateTime.now();

        return (TopicHistory(this.title,
            Topic.toCategoryString(this.category),
            this.text,
            date,
            this.hash(date, user),
            user.id,
            this.id),
            res._1,
            res._2,
            res._3);
    }

    def hash(date: LocalDateTime, user: User): String = {
        val ins = this.date.toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now()));

        return Hash.hashShort(
            //ユーザー依存
            user.id + " " +

                //書き込み年月日依存
                date.getYear() + " " + date.getMonth().getValue() + " " + date.getDayOfMonth() + " " +

                //トピ依存
                this.id +

                //ソルト依存
                Config.SALT_HASH);
    }

    def isCategory(category: Array[String]): Boolean = {
        //引数の方の長さが大きいなら
        if (category.length > this.category.length) {
            return false;
        }

        //全く同じなら
        if (this.category.sameElements(category)) {
            return true;
        }

        //thisカテゴリ is-a 引数カテゴリであるか確認
        for (i <- 0 until category.length) {
            if (category(i) != this.category(i)) {
                return false;
            }
        }

        return true;
    }
    //}}
}
