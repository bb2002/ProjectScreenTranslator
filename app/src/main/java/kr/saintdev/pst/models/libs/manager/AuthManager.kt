package kr.saintdev.pst.models.libs.manager

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kr.saintdev.pst.R
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.activity.view.SignInActivity

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-01
 */
class AuthManager {
    /**
     * 로그인에 관련된 처리를 진행합니다.
     */
    object Manager {
        // 로그인 되어 있다면 True 를 리턴합니다.
        fun isLoginned(context: Context) =
                RepositoryManager.quicklyGet(
                    RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, context) != null

        // 로그인 화면을 엽니다.
        fun openSigninActivity(context: Context) =
                context.startActivity(Intent(context, SignInActivity::class.java))

        // 로그인 정보를 파이어베이스에 저장합니다.
        fun login(data: Intent, accountToken: String, context: Context) : Boolean {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            return if(result.isSuccess) {
                val token = result.signInAccount!!.idToken
                val cert = GoogleAuthProvider.getCredential(token, null)
                Account.firebaseAuth.signInWithCredential(cert)     // Firebase 로그인 처리

                val repoManager = RepositoryManager.getInstance(context)
                repoManager.createHashValue(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, accountToken)
                repoManager.createHashValue(RepositoryKey.PSCT_AUTH_CRYPT_TOKEN, result.signInAccount!!.id.toString())
                true
            } else {
                false
            }
        }

        // idToken 을 가져옵니다.
        fun idToken(data: Intent) : String? {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            return if(result.isSuccess) {
                result.signInAccount!!.idToken
            } else {
                null
            }
        }
    }

    /**
     * 로그인 된 계정에 대한 처리를 진행합니다.
     */
    object Account {
        val firebaseAuth = FirebaseAuth.getInstance()

        // Account 정보를 가져옵니다. (null 이 반환 될 수 있슴)
        fun account() : FirebaseUser? = firebaseAuth.currentUser

        fun getToken(context: Context, name: String) : String? {
            val shardPrep = context.getSharedPreferences("pst.account_token", Context.MODE_PRIVATE)
            return shardPrep.getString(name, null)
        }
    }

    /**
     * Auth Manager 의 기본 함수
     */
    object Common {
        var googleApiClientObject: GoogleApiClient? = null

        fun googleApiClient(
                connFailedListener: GoogleApiClient.OnConnectionFailedListener? = null, context: CommonActivity
        ) = if(googleApiClientObject == null) {
                googleApiClientObject = GoogleApiClient.Builder(context)
                        .enableAutoManage(context, connFailedListener)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, googleSigninOption(context))
                        .build()

            googleApiClientObject
            } else {
            googleApiClientObject
            }



        fun googleSigninOption(context: Context) =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail().build()
    }
}