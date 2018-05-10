package anontown.model;

object StringMethod {

    def line(str: String, after: String): String = {
        return str.replaceAll("\r\n|\r|\n", after);
    }

    def line(str: String): String = {
        return StringMethod.line(str, "\n");
    }

    def graphicEscape(str: String): String = {
        val result = new StringBuffer();
        for (c <- str.toCharArray()) {
            c match {
                case '★' =>
                    result.append('☆');
                case '●' =>
                    result.append('○');
                case '■' =>
                    result.append('□');
                case '▲' =>
                    result.append('△');
                case '▼' =>
                    result.append('▽');
                case '◆' =>
                    result.append('◇');
                case _ =>
                    result.append(c);
            }
        }
        return result.toString();
    }

    def isUrl(url:String):Boolean={
        return url.matches("""https?:\/\/.+""");
    }
}
