package kr.saintdev.pst.vnc.activity.view

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_signin.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.consts.HTTP_GOOGLE_LOGIN
import kr.saintdev.pst.models.libs.manager.AuthManager
import kr.saintdev.pst.models.http.HttpRequester
import kr.saintdev.pst.models.http.HttpResponseObject
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.vnc.activity.CommonActivity
import java.lang.Exception

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-01
 */
class SignInActivity : CommonActivity() {
    private val RQ_GOOGLE_SIGN_IN = 0x0
    private val RQ_GOOGLE_AUTH_PSCT = 0x1

    private val fbAuth = FirebaseAuth.getInstance()
    private val fbAuthListener = FirebaseAuth.AuthStateListener { firebaseAuthCallback(it.currentUser) }
    private val googleLoginAuthCallback = OnGoogleAuthCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        actionBar(false)

        signin_google_login.setOnClickListener {
            val signinIntent =
                    Auth.GoogleSignInApi.getSignInIntent(
                            AuthManager.Common.googleApiClient(GoogleApiClient.OnConnectionFailedListener { connectionFailed() }
                                    , this@SignInActivity)
                    )
            startActivityForResult(signinIntent, RQ_GOOGLE_SIGN_IN)
        }
    }

    override fun onStart() {
        super.onStart()
        this.fbAuth.addAuthStateListener(fbAuthListener)
    }

    override fun onStop() {
        super.onStop()
        this.fbAuth.removeAuthStateListener(fbAuthListener)
    }

    var data: Intent? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RQ_GOOGLE_SIGN_IN && data != null) {
            val idToken = AuthManager.Manager.idToken(data)
            this.data = data

            if (idToken == null) {
                // 로그인 처리에 실패헀습니다!
                openMessageDialog(
                        R.string.error_msg_title_warning.str(),
                        R.string.error_msg_auth_error.str())
            } else {
                // 로그인에 성공 했습니다.
                // 스크린 번역기 서버에서 토큰을 검증하고 마칩니다.
                val param = hashMapOf<String, Any>("token" to idToken)
                val googleAuthRequester = HttpRequester(HTTP_GOOGLE_LOGIN, param, RQ_GOOGLE_AUTH_PSCT, googleLoginAuthCallback)
                googleAuthRequester.execute()

                openProgressDialog()
            }
        }
    }

    /**
     * Firebase 로그인에 대한 콜백
     */
    private fun firebaseAuthCallback(user: FirebaseUser?) {

    }

    /**
     * 구글 로그인 및 서버 처리 에 대한 응답
     */
    inner class OnGoogleAuthCallback : OnBackgroundWorkListener {
        override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>) {
            closeProgressDialog()

            val result = worker.result
            if(result is HttpResponseObject) {
                if(result.isSuccess) {
                    // 처리 성공
                    val responseData = result.message
                    val accountToken = responseData.getString("account_token")

                    if(data != null) {
                        AuthManager.Manager.login(data!!, accountToken, this@SignInActivity)
                    }

                    finish()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                } else {
                    // 처리 실패
                    openMessageDialog(
                            R.string.error_msg_title_warning.str(),
                            R.string.error_msg_content_common.str() + "\n${result.errorMessage}"
                    )
                }
            }
        }

        override fun onFailed(requestCode: Int, ex: Exception) {
            closeProgressDialog()

            openMessageDialog(
                    R.string.error_msg_title_fatal.str(),
                    R.string.error_msg_content_common.str() + "\n${ex.message}"
            )
        }
    }

    /**
     * Connection failed 에 대한 Listener
     */
    private fun connectionFailed() {
        openMessageDialog(
                R.string.error_msg_title_fatal.str(),
                R.string.login_connection_error.str()
        )
    }
}