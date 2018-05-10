package anontown.model.topic.history;

import java.time.LocalDateTime;
import anontown.model.MarkDown

object TopicHistory {
    def apply(title: String,
        category: String,
        text: String,
        date: LocalDateTime,
        hash: String,
        user: Int,
        topic: Int): TopicHistory = {
        //エラーチェックはしない(Topicでしているので)
        return new TopicHistory(0, title, category, text,MarkDown(text), date, hash, user, topic);
    }
}

case class TopicHistory private[history] (id: Int,
        title: String,
        category: String,
        text: String,
        mdtext:String,
        date: LocalDateTime,
        hash: String,
        user: Int,
        topic: Int) {

    def withId(id: Int): TopicHistory = {
        return this.copy(id = id);
    }

    def withTopic(topic: Int): TopicHistory = {
        return this.copy(topic = topic);
    }
}
