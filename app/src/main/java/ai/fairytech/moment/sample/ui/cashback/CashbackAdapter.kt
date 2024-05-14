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

package ai.fairytech.moment.sample.ui.cashback

import ai.fairytech.moment.sample.util.ImageUtil
import ai.fairytech.moment.sample.R
import ai.fairytech.moment.sample.databinding.IvCashbackProgramHolderBinding
import ai.fairytech.moment.proto.CashbackCommissionUnit
import ai.fairytech.moment.proto.CashbackProgram
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * 캐시백 프로그램을 보여주는 Adapter
 */
class CashbackAdapter :
    ListAdapter<CashbackProgram, CashbackAdapter.CashbackProgramViewHolder>(DIFF_CALLBACK) {

    interface ItemClickListener {
        fun onItemClick(cashbackProgram: CashbackProgram) {}
    }

    private var mListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashbackProgramViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.iv_cashback_program_holder, parent, false)
        val binding = IvCashbackProgramHolderBinding.bind(view)
        return CashbackProgramViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CashbackProgramViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setListener(listener: ItemClickListener) {
        mListener = listener
    }

    fun setData(programList: List<CashbackProgram>) {
        submitList(programList)
    }

    inner class CashbackProgramViewHolder(private val binding: IvCashbackProgramHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cashbackProgram: CashbackProgram) {
            // 비지니스 이미지 로딩
            ImageUtil.loadImageWithUrl(cashbackProgram.businessImageUrl, binding.imageBusinessLogo)
            // 비지니스 이름
            binding.textBusinessName.text = cashbackProgram.businessName;
            binding.root.setOnClickListener {
                mListener?.onItemClick(cashbackProgram)
            }

            // 커미션율
            val commission = cashbackProgram.getProducts(0).commission
            binding.textCommision.text = "${commission.amount} ${getUnitStr(commission.unit)}"
        }

        private fun getUnitStr(unit: CashbackCommissionUnit): String {
            // 커미션 유닛 사용자에게 맞춰 보여짐
            return when (unit) {
                CashbackCommissionUnit.PERCENTAGE_COMMISSION_UNIT -> "%"
                CashbackCommissionUnit.KRW_COMMISSION_UNIT -> "원"
                else -> {
                    ""
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CashbackProgram>() {
            override fun areItemsTheSame(oldItem: CashbackProgram, newItem: CashbackProgram) =
                oldItem.businessId == newItem.businessId

            override fun areContentsTheSame(oldItem: CashbackProgram, newItem: CashbackProgram) =
                oldItem.businessName == newItem.businessName
                        && oldItem.businessImageUrl == newItem.businessImageUrl
                        && oldItem.getProducts(0).commission.amount == newItem.getProducts(0).commission.amount
        }
    }
}