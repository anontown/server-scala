CREATE TABLE user(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    sn varchar(100) NOT NULL UNIQUE,
    pass text NOT NULL,
    lv int NOT NULL,
    lastRes datetime NOT NULL,
    lastTopic datetime NOT NULL,
    INDEX(id)
)
ENGINE=InnoDB;

CREATE TABLE msg(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    receiver INT,
    text TEXT NOT NULL,
    mdtext TEXT NOT NULL,
    date DATETIME NOT NULL,
    INDEX(id),
    INDEX(receiver),
    FOREIGN KEY (receiver) REFERENCES user(id)
)
ENGINE=InnoDB;

CREATE TABLE profile(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    sid varchar(100) NOT NULL UNIQUE,
    user INT NOT NULL,
    name TEXT NOT NULL,
    text TEXT NOT NULL,
    mdtext TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    INDEX(id),
    INDEX(user),
    FOREIGN KEY (user) REFERENCES user(id)
)
ENGINE=InnoDB;

CREATE TABLE client(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name TEXT NOT NULL,
    url TEXT NOT NULL,
    user INT NOT NULL,
    INDEX(id),
    INDEX(user),
    FOREIGN KEY (user) REFERENCES user(id)
)
ENGINE=InnoDB;

CREATE TABLE token(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    token TEXT NOT NULL,
    client INT NOT NULL,
    user INT NOT NULL,
    active BOOLEAN NOT NULL,
    INDEX(id),
    INDEX(client),
    UNIQUE(user,client),
    FOREIGN KEY (client) REFERENCES client(id),
    FOREIGN KEY (user) REFERENCES user(id)
)
ENGINE=InnoDB;

CREATE TABLE token_req(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `key` TEXT NOT NULL,
    token INT NOT NULL,
    expire_date DATETIME NOT NULL,
    INDEX(id),
    INDEX(token),
    FOREIGN KEY (token) REFERENCES token(id)
)
ENGINE=InnoDB;

CREATE TABLE storage(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user INT NOT NULL,
    token INT NOT NULL UNIQUE,
    value TEXT NOT NULL,
    INDEX(id),
    INDEX(user),
    INDEX(token),
    FOREIGN KEY (user) REFERENCES user(id),
    FOREIGN KEY (token) REFERENCES token(id)
)
ENGINE=InnoDB;

CREATE TABLE topic(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title text NOT NULL,
    category text NOT NULL,
    text text NOT NULL,
    mdtext TEXT NOT NULL,
    updatetime DATETIME NOT NULL,
    user INT NOT NULL,
    date DATETIME NOT NULL,
    INDEX(id),
    INDEX(user),
    FOREIGN KEY (user) REFERENCES user(id)
)
ENGINE=InnoDB;

CREATE TABLE history(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title text NOT NULL,
    category text NOT NULL,
    text text NOT NULL,
    mdtext TEXT NOT NULL,
    date DATETIME NOT NULL,
    hash TEXT NOT NULL,
    user INT NOT NULL,
    topic INT NOT NULL,
    INDEX(id),
    INDEX(user),
    INDEX(topic),
    FOREIGN KEY (user) REFERENCES user(id),
    FOREIGN KEY (topic) REFERENCES topic(id)
)
ENGINE=InnoDB;

CREATE TABLE res(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    topic int NOT NULL,
    date datetime NOT NULL,
    user int NOT NULL,
    name text NOT NULL,
    text text NOT NULL,
    mdtext TEXT NOT NULL,
    reply int,
    deleteFlag int NOT NULL,
    vote INT NOT NULL,
    lv int NOT NULL,
    hash text NOT NULL,
    replyCount int NOT NULL,
    profile INT,
    INDEX(id),
    INDEX(topic),
    INDEX(user),
    INDEX(reply),
    FOREIGN KEY (topic) REFERENCES topic(id),
    FOREIGN KEY (user) REFERENCES user(id),
    FOREIGN KEY (reply) REFERENCES res(id),
    FOREIGN KEY (profile) REFERENCES profile(id)
)
ENGINE=InnoDB;

CREATE TABLE vote(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user INT NOT NULL,
    res int NOT NULL,
    UNIQUE(user,res),
    INDEX(user),
    INDEX(res),
    FOREIGN KEY (user) REFERENCES user(id),
    FOREIGN KEY (res) REFERENCES res(id)
)
ENGINE=InnoDB;