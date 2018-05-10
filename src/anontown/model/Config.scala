package anontown.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.net.URL
import javax.servlet.ServletContext

object Config{
    private val properties = new Properties();
    private val inputStream = new FileInputStream(System.getProperty("anontown.config"));
    properties.load(inputStream);
    inputStream.close();

    val DB_URL = properties.getProperty("DB_URL");
    val DB_USER = properties.getProperty("DB_USER");
    val DB_PASS = properties.getProperty("DB_PASS");

    val USER_SCREEN_NAME = properties.getProperty("USER_SCREEN_NAME");
    val USER_PASS = properties.getProperty("USER_PASS");
    val USER_LV_MAX = Integer.parseInt(properties.getProperty("USER_LV_MAX"));

    val THREAD_TITLE_MAX_LENGTH = Integer.parseInt(properties.getProperty("THREAD_TITLE_MAX_LENGTH"));
    val THREAD_CATEGORY = properties.getProperty("THREAD_CATEGORY");
    val THREAD_CATEGORY_MAX_LENGTH = Integer.parseInt(properties.getProperty("THREAD_CATEGORY_MAX_LENGTH"));
    val THREAD_TEXT_MAX_LENGTH = Integer.parseInt(properties.getProperty("THREAD_TEXT_MAX_LENGTH"));

    val RESPONSE_NAME_MAX_LENGTH = Integer.parseInt(properties.getProperty("RESPONSE_NAME_MAX_LENGTH"));
    val RESPONSE_TEXT_MAX_LENGTH = Integer.parseInt(properties.getProperty("RESPONSE_TEXT_MAX_LENGTH"));

    val SALT_PASS = properties.getProperty("SALT_PASS");
    val SALT_HASH = properties.getProperty("SALT_HASH");

    val DEFAULT_NAME = properties.getProperty("DEFAULT_NAME");

    val WAIT = properties.getProperty("WAIT") == "true";

    val PROFILE_SID = properties.getProperty("PROFILE_SID");
    val PROFILE_NAME_LENGTH = properties.getProperty("PROFILE_NAME_LENGTH").toInt;
    val PROFILE_TEXT_LENGTH = properties.getProperty("PROFILE_TEXT_LENGTH").toInt;

    val TOKEN_SALT = properties.getProperty("TOKEN_SALT");
    val TOKEN_NAME_LENGTH = properties.getProperty("TOKEN_NAME_LENGTH").toInt;
}