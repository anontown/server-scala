package anontown.model.exceptions;

class NchException(val status: Int, msg: String) extends Exception(msg)

//認証
class UnauthorizedException(msg: String) extends NchException(401, msg)

//書き込み間隔/規制
class ForbiddenException(msg: String) extends NchException(403, msg)

//見つからない
class NotFoundException(msg: String) extends NchException(404, msg)

//競合
class ConflictException(msg: String) extends NchException(409, msg)

//パラメータがおかしい
class MisdirectedRequestException(msg: String) extends NchException(421, msg)