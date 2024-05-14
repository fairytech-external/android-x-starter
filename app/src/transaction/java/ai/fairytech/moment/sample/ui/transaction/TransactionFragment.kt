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

import ai.fairytech.moment.sample.data.TransactionStatus
import ai.fairytech.moment.sample.databinding.FragmentTransactionBinding
import ai.fairytech.moment.sample.firebase.FirebaseFunctionsManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * 사용자가 받은 모든 캐시백 구매내역을 보여줌
 */
class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null

    // 캐시백 구매내역 Adapter
    private val transactionAdapter = TransactionAdapter(mutableListOf())
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivAsk.setOnClickListener {
            // go to CS ask website
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://sites.google.com/fairytech.ai/cashback-help")
            }
            startActivity(intent)
        }

        // 캐시백 구매내역 (Transaction) 을 보여줄 RecyclerView
        with(binding.recyclerTransactions) {
            adapter = transactionAdapter.apply {
                setListener(object : TransactionAdapter.ItemClickListener {
                    override fun onItemClick() {

                    }
                })
            }
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        CoroutineScope(Dispatchers.Main).launch {
            // 서버로 부터 캐시백 구매내역을 받아옴
            val transactionResponse =
                FirebaseFunctionsManager.getInstance()?.getTransactions()?.await()
            if (transactionResponse != null) {
                // 모든 구매내역
                val allTransactions = transactionResponse.allTransactions.get(
                    TransactionStatus.CREATED.toString()
                );
                // 취소된 구매내역 (예: 환불)
                val canceledTransactions = transactionResponse.allTransactions.get(
                    TransactionStatus.CANCELED.toString()
                );
                // 실적이 확정된 구매내역 (캐시로 전환 가능)
                val confirmedTransactions = transactionResponse.allTransactions.get(
                    TransactionStatus.CONFIRMED.toString()
                );

                // 모든 내역으로부터, 확정과 취소를 제거. 했을시, allTransactions에는 예상 실적만 남게됨
                canceledTransactions?.forEach { transactionId, transaction ->
                    allTransactions?.remove(transactionId)
                }
                // 확정 내역 Sum up
                var confirmedAmount = 0f;
                confirmedTransactions?.forEach { transactionId, transaction ->
                    allTransactions?.remove(transactionId)
                    confirmedAmount = transaction.commission.amount;
                }
                // 실적 예상 내역 내역 Sum up
                var expectedAmount = 0f;
                allTransactions?.forEach { transactionId, transaction ->
                    expectedAmount += transaction.commission.amount;
                }


                // 역시간순으로 예상되거나, 확정된 구매내역을 보여줌.
                val uniqueTransactionsList = (allTransactions?.values?.toList() ?: listOf())
                    .plus(confirmedTransactions?.values?.toList() ?: listOf())
                uniqueTransactionsList.sortedByDescending { transaction -> transaction.createdTimestampMillis }
                activity?.runOnUiThread {
                    binding.totalCash.text = (confirmedAmount + expectedAmount).toString()
                    binding.expectedCash.text = expectedAmount.toString()
                    transactionAdapter.setData(uniqueTransactionsList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}