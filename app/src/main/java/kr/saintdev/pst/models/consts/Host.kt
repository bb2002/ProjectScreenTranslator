package kr.saintdev.pst.models.consts

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-02
 */

const val HTTP_BASE_URL = "http://saintdev.kr/psct/v3/"
const val HTTP_GOOGLE_LOGIN = HTTP_BASE_URL + "auth/google-login.php"
const val HTTP_INODE_UPDATER = HTTP_BASE_URL + "inode/inode-status.php"
const val HTTP_INODE_BILLING = HTTP_BASE_URL + "inode/inode-billing.php"
const val HTTP_UPDATE_CHECK = HTTP_BASE_URL + "data/last-version.php"
const val HTTP_BILLING_LOG = HTTP_BASE_URL + "data/billing-log.php"

/**
 * Web site
 */
const val WORDPRESS_NOTIFY = "http://saintdev.kr/psct/help/?cat=2"
const val WORDPRESS_HELP = "http://saintdev.kr/psct/help/?cat=1"
const val COMMENT_PAGE = "http://saintdev.kr/psct/dashboard/comment/list.php?user_uuid=%s"

/*
        실제 API 서버
     */
const val BING_TRANSLATE_API = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0"
const val BING_OCR_API = "https://eastasia.api.cognitive.microsoft.com/vision/v1.0/ocr"
const val TTS_HOST = "https://naveropenapi.apigw.ntruss.com/voice/v1/tts"

const val TRANSLATE_VIA_SCT = HTTP_BASE_URL + "apis/translation.php"       // 스크린번역기 번역 API 보고 서버
const val TTS_VIA_SCT = HTTP_BASE_URL + "apis/tts.php"                     // 스크린번역기 TTS API 보고 서버
const val OCR_VIA_SCT = HTTP_BASE_URL + "apis/ocr.php"                     // 스크린번역기 OCR 보고 서버