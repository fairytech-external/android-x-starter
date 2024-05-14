/*
 * Fairy Technologies CONFIDENTIAL
 * __________________
 *
 * Copyright (C) Fairy Technologies, Inc - All Rights Reserved
 *
 * NOTICE:  All information contained herein is, and remains the property of Fairy
 * Technologies Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to Fairy Technologies Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents, patents in
 * process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information,or reproduction or modification of this material
 * is strictly forbidden unless prior written permission is obtained from Fairy
 * Technologies Incorporated.
 *
 */

package ai.fairytech.moment.sample.ui.transaction

import ai.fairytech.moment.sample.R
import ai.fairytech.moment.sample.databinding.IvTransactionHolderBinding
import ai.fairytech.moment.sample.util.ImageUtil
import ai.fairytech.moment.sample.data.Transaction
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


/**
 * 구매내역을 보여주는 Adapter
 */
class TransactionAdapter (private val mTransactions: MutableList<Transaction> = mutableListOf()) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface ItemClickListener {
        fun onItemClick() {}
    }

    private var mListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.iv_transaction_holder, parent, false)
        val binding = IvTransactionHolderBinding.bind(view)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(mTransactions[position])
    }

    override fun getItemCount(): Int = mTransactions.size

    fun setListener(listener: ItemClickListener) {
        mListener = listener
        notifyDataSetChanged()
    }

    fun setData(awards: List<Transaction>) {
        mTransactions.clear()
        mTransactions.addAll(awards)
        notifyDataSetChanged()
    }

    var dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    inner class TransactionViewHolder(private val binding: IvTransactionHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            ImageUtil.loadImageWithUrl(transaction.businessImageUrl, binding.imageBusinessLogo)
            binding.textBusinessName.text = transaction.businessName;
            binding.textSeeMore.setOnClickListener { 
                //  클릭시 상세페이지로 이동 (미구현)
            }

            // transaction Id 고유값
            binding.textId.text = transaction.transactionId;

            // 사용자가 얻게될 캐시
            binding.textCash.text =
                transaction.commission.amount.toString() + " " + (transaction.commission.currency?.name?:"원");

            var date = Calendar.getInstance()
            date.timeInMillis = transaction.createdTimestampMillis;
            // 적립 예정일
            binding.textDate.text = "적립 예정일 " + dateFormat.format(date.time);
            binding.textStatus.text = transaction.status.name;
        }
    }
}