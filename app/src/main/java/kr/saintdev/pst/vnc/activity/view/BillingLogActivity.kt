package kr.saintdev.pst.vnc.activity.view

import android.os.Bundle
import android.view.View
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_billing_log.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.consts.HTTP_BILLING_LOG
import kr.saintdev.pst.models.http.HttpRequester
import kr.saintdev.pst.models.http.HttpResponseObject
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import kr.saintdev.pst.models.libs.productItemIdToItemName
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.adapter.BillLogAdapter
import kr.saintdev.pst.vnc.adapter.BillLogItem
import org.jetbrains.anko.listView
import org.json.JSONObject
import java.lang.Exception

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-11
 */
class BillingLogActivity : CommonActivity() {
    val adapter = BillLogAdapter()
    val REQUEST_BILL_LOG = 0x0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing_log)
        title = R.string.title_billog_activity.str()

        val accountToken = RepositoryManager.quicklyGet(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, this)
        billing_list.adapter = adapter

        if(accountToken != null) {
            val args = hashMapOf<String, Any>("token" to accountToken)
            val requester = HttpRequester(HTTP_BILLING_LOG, args, REQUEST_BILL_LOG, onBillLogUpdate)
            requester.execute()

            openProgressDialog()
        } else {
            openMessageDialog(R.string.error_msg_title_fatal.str(), "Account token is null.")
        }
    }

    val onBillLogUpdate = object : OnBackgroundWorkListener {
        override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>) {
            val response = worker.result as HttpResponseObject
            closeProgressDialog()

            if(requestCode == REQUEST_BILL_LOG && response.isSuccess) {
                // 로그 값을 ListView 으로 만든다.
                val message = response.message

                val length = message.getInt("length")
                if(length == 0) {
                    // 구매 이력이 전혀 없다.
                    billing_list.visibility = View.INVISIBLE
                    billing_empty.visibility = View.VISIBLE
                } else {
                    // 구매이력이 있다.
                    billing_list.visibility = View.VISIBLE
                    billing_empty.visibility = View.INVISIBLE
                    val dataArray = message.getJSONArray("data")

                    for(i in 0 until dataArray.length()) {
                        val obj = dataArray[i] as JSONObject

                        val billLog = BillLogItem(
                                obj.getString("created"),
                                productItemIdToItemName(obj.getString("product_item"), this@BillingLogActivity),
                                R.drawable.ic_ok_compo
                        )
                        adapter.addItem(billLog)
                    }

                    adapter.notifyDataSetChanged()
                }

            }
        }

        override fun onFailed(requestCode: Int, ex: Exception?) {
            closeProgressDialog()
            openMessageDialog(R.string.error_msg_title_fatal.str(), "${R.string.error_msg_content_common.str()}\n${ex?.message}")
        }
    }
}