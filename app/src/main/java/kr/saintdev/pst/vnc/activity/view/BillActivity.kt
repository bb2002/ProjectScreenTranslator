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
import kr.saintdev.pst.vnc.adapter.BillItemAdapter
import kr.saintdev.pst.vnc.adapter.ProductItemData
import kr.saintdev.pst.vnc.dialog.message.DialogManager
import kr.saintdev.pst.vnc.dialog.message.OnDialogButtonClickListener
import java.lang.Exception


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-09
 */
class BillActivity : CommonActivity(), BillingProcessor.IBillingHandler {
    private lateinit var billProcess: BillingProcessor
    private val REQUEST_BILLING_CODE = 0x0
    private val billItemAdapter = BillItemAdapter()
    private val onItemClickListener = AdapterView.OnItemClickListener {
        adapter, view, postion, id ->
        val itemIdArray = resources.getStringArray(R.array.vf_products_item_id)
        buyNewTicket(itemIdArray[postion])
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing)

        // Backbutton 을 만듭니다.
        vf_bill_toolbar.title = R.string.menu_item_new_pay.str()
        vf_bill_toolbar.setNavigationIcon(R.drawable.ic_back_white)
        vf_bill_toolbar.setNavigationOnClickListener { finish() }

        // bill process
        this.billProcess = BillingProcessor(this, R.string.license_key.str(), this)
        vf_bill_content.onItemClickListener = onItemClickListener

        openProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        billProcess.release()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(!billProcess.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * 구매가 준비되었습니다.
     * 대체적으로 아이템을 목록화 합니다.
     */
    override fun onBillingInitialized() {
        closeProgressDialog()

        val itemNameArray = resources.getStringArray(R.array.vf_products_item_name)
        val itemContentArray = resources.getStringArray(R.array.vf_products_item_content)
        val colorArray = resources.getIntArray(R.array.rainbow)

        val productItemID = arrayListOf<String>()
        productItemID.addAll(resources.getStringArray(R.array.vf_products_item_id))
        val products = billProcess.getPurchaseListingDetails(productItemID)     // 아이템 목록을 불러온다.
        products.sortBy { it.priceLong }

        for(i in 0 until products.size) {
            val product = products[i]

            billItemAdapter.addItem(ProductItemData(
                    itemNameArray[i],
                    itemContentArray[i],
                    colorArray[i],
                    product.priceText
            ))
        }

        vf_bill_content.adapter = billItemAdapter
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
    }

    override fun onPurchaseHistoryRestored() {

    }

    val onProductChangeListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            // 선택된 값을 가져온다.

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

    fun buyNewTicket(productId: String) {
        // 구매된 아이템은 소멸 처리 한다.
        if (billProcess.isPurchased(productId)) {
            billProcess.consumePurchase(productId)
        }

        billProcess.purchase(this, productId)
    }
}