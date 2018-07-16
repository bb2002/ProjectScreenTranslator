package kr.saintdev.pst.models.libs.manager

import android.content.Context
import android.util.Log

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-07-03
 */

class RepositoryManager {
    private val db: DBHelper

    companion object {
        private var instance: RepositoryManager? = null

        fun getInstance(context: Context): RepositoryManager {
            if (instance == null) {
                instance = RepositoryManager(context)
            }

            return instance!!
        }

        fun quicklyGet(name: RepositoryKey, context: Context) = getInstance(context).getHashValue(name)
    }

    private constructor(context: Context) {
        this.db = DBHelper(context)
        this.db.open()
    }

    fun createHashValue(name: RepositoryKey, value: String) {
        val sql =
                if (getHashValue(name) == name.getDefaultValue()) "INSERT INTO `repository` (repo_key, repo_value) VALUES('${name.getRepositoryKey()}', '$value')"
                else "UPDATE repository SET repo_value = '$value' WHERE repo_key = '${name.getRepositoryKey()}'"

        db.sendWriteableQuery(sql)
    }

    fun getHashValue(name: RepositoryKey) : String? {
        val sql = "SELECT * FROM `repository` WHERE repo_key = '${name.getRepositoryKey()}' ORDER BY _id DESC"
        val cs = db.sendReadableQuery(sql)

        val tmp = when {
            cs.moveToNext() -> cs.getString(2)
            name.getDefaultValue() == null -> null
            else -> name.getDefaultValue().toString()
        }
        println(tmp)
        return tmp
    }

    fun clear() {
        val sql = "DELETE FROM repository"
        db.sendWriteableQuery(sql)
    }
}

enum class RepositoryKey(private val key: String, private val defaultVal: Any?) {
    PSCT_AUTH_ACCOUNT_TOKEN("auth.token", null),          // PSCT 서버 계정 토큰
    PSCT_AUTH_CRYPT_TOKEN("auth.crypt", null),            // 암호화 토큰
    INODE_REMAIN_PACKET("inode.remain_packet", "0"),    // 남은 inode 갯수
    MODE_SETTING("setting.mode", "default"),            // 기본 값
    INPUT_LANGUAGE("language.input", "0"),                // 자동 에서
    OUTPUT_LANGUAGE("language.output", "1"),               // 한국어로 가 기본 값 입니다.
    @Deprecated("Not used")
    TICKET_USING("inode.ticket", "false");               // 티켓 사용 여부, false 가 기본 값

    fun getRepositoryKey() = key
    fun getDefaultValue() = defaultVal
}
