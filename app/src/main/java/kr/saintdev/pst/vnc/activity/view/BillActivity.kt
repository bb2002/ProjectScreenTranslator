package kr.saintdev.pst.vnc.activity.view

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.Constants
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.android.synthetic.main.activity_billing.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.consts.HTTP_INODE_BILLING
import kr.saintdev.pst.models.http.HttpRequester
import kr.saintdev.pst.models.http.HttpResponseObject
import kr.saintdev.pst.models.libs.appendComma
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.dialog.message.DialogManager
import kr.saintdev.pst.vnc.dialog.message.OnDialogButtonClickListener
import java.lang.Exception


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-09
 */
class BillActivity : CommonActivity(), BillingProcessor.IBillingHandler {
    private var billProcess: BillingProcessor? = null
    private var selectedProductId = "remain_increase_247"         // 기본 값은 247 회 추가 입니다.
    private val REQUEST_BILLING_CODE = 0x0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing)

        // bill process
        this.billProcess = BillingProcessor(this, R.string.license_key.str(), this)
        product_buy.setOnClickListener { buyNewTicket() }

        title = R.string.title_bill_activity.str()

        openProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(billProcess != null) billProcess!!.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(!billProcess!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * 구매가 준비되었습니다.
     * 대체적으로 아이템을 목록화 합니다.
     */
    override fun onBillingInitialized() {
        closeProgressDialog()

        product_select_item.adapter =
                ArrayAdapter.createFromResource(this, R.array.products_item_name, android.R.layout.simple_spinner_dropdown_item)
        product_select_item.onItemSelectedListener = onProductChangeListener        // Listener 등록
        selectDefaultItem()
    }

    /**
     * 구매 성공
     * 여기서 보상을 지급합니다.
     */
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        // 결제 정보를 통해, 서버에게 상품 지급을 요청한다.
        val accountToken = RepositoryManager.quicklyGet(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, this)

        if(accountToken != null && details != null) {
            val args = hashMapOf<String, Any>("account_token" to accountToken, "product_id" to details.productId, "product_token" to details.purchaseToken)
            val requestProduct = HttpRequester(HTTP_INODE_BILLING, args, REQUEST_BILLING_CODE, onProductBillingCallback)
            requestProduct.execute()

            openProgressDialog(R.string.product_processing.str())
        } else {
            openMessageDialog("An error occurred", "Account token is null.")
        }
    }

    /**
     * 결제 오류
     * 사용자가 취소하거나, 작업 중 오류가 발생 했습니다.
     */
    override fun onBillingError(errorCode: Int, error: Throwable?) {
        if(errorCode != Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            openMessageDialog(R.string.error_msg_title_fatal.str(), R.string.product_buy_failed.str())
        }

        product_buy.isEnabled = true      // 버튼을 비활성화 하고
        product_buy.text = R.string.product_pay.str()       // 결제 진행중 표시
    }

    override fun onPurchaseHistoryRestored() {

    }

    val onProductChangeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            // 선택된 값을 가져온다.
            val itemId = R.array.products_item_id.array()[product_select_item.selectedItemPosition]
            val product = billProcess?.getPurchaseListingDetails(itemId)

            if(product == null) {
                openMessageDialog(R.string.error_msg_title_warning.str(), R.string.product_not_found.str())
                selectDefaultItem()
            } else {
                // 제품을 표시한다.
                product_total_price.text = product.priceText
                selectedProductId = itemId
            }
        }
    }

    /**
     * Inode billing engine
     * 1 -> ProductItem not found
     * 2 -> Request failed
     * 3 -> internal server error.
     * 4 -> Already used item
     */
    val onProductBillingCallback = object : OnBackgroundWorkListener {
        override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>) {
            closeProgressDialog()

            val httpResponse = worker.result as HttpResponseObject
            if(requestCode == REQUEST_BILLING_CODE && httpResponse.isSuccess) {
                // 개시 성공.
                val response = httpResponse.message
                val productId = response.getString("product_id")
                val dm = DialogManager(this@BillActivity)
                dm.setTitle(R.string.info_msg_title.str())
                dm.setDescription(R.string.product_buy_success.str())
                dm.setDialogButtonClickListener(object : OnDialogButtonClickListener {
                    override fun onPositiveClick(dialog: DialogInterface?, reqId: Int) {
                        finish()
                    }

                    override fun onNegativeClick(dialog: DialogInterface?, reqId: Int) {}
                })
                dm.setOnYesButtonClickListener(R.string.common_positive_ok.str())
                dm.show()

                billProcess!!.consumePurchase(productId)
            } else {
                // 개시 실패
                openMessageDialog(R.string.error_msg_title_fatal.str(),
                    when(httpResponse.errorCode) {
                        1 -> R.string.product_notice_failed_1.str()
                        2 -> R.string.product_notice_failed_2.str()
                        3 -> R.string.product_notice_failed_3.str()
                        4 -> R.string.product_notice_failed_4.str()
                        else -> R.string.product_notice_other.str()
                    }
                )
            }
        }

        override fun onFailed(requestCode: Int, ex: Exception?) {
            closeProgressDialog()
            openMessageDialog(R.string.error_msg_title_fatal.str(), R.string.product_notice_failed.str())
        }
    }

    /**
     * Functions
     */
    fun selectDefaultItem() {
        product_select_item.setSelection(1)         // 기본 아이템 선택
    }

    fun buyNewTicket() {
        if(billProcess != null) {
            if (billProcess!!.isPurchased(selectedProductId)) {
                billProcess!!.consumePurchase(selectedProductId)
            }

            billProcess!!.purchase(this, selectedProductId)
            product_buy.isEnabled = false       // 버튼을 비활성화 하고
            product_buy.text = R.string.product_pay_running.str()       // 결제 진행중 표시
        } else {
            openMessageDialog(R.string.error_msg_title_fatal.str(), "Bill process is null")
        }
    }
}