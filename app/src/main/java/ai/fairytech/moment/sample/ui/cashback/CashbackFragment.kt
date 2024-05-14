package ai.fairytech.moment.sample.ui.cashback

import ai.fairytech.moment.MomentSDK
import ai.fairytech.moment.exception.MomentException
import ai.fairytech.moment.proto.CashbackProgram
import ai.fairytech.moment.sample.databinding.FragmentCashbackBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

class CashbackFragment : Fragment() {
    private var _binding: FragmentCashbackBinding? = null
    private val binding get() = _binding!!
    private val cashbackAdapter = CashbackAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 캐시백 프로그램을 보여주는 RecyclerView
        requireContext().let {
            with(binding.recyclerCashback) {
                adapter = cashbackAdapter.apply {
                    setListener(object : CashbackAdapter.ItemClickListener {
                        override fun onItemClick(cashbackProgram: CashbackProgram) {
                            // 선택시, 상세조건 페이지로 이동.
                            MomentSDK.getInstance(it).goToCashback(
                                cashbackProgram.businessId,
                                object : MomentSDK.ResultCallback {
                                    override fun onSuccess() {}

                                    override fun onFailure(exception: MomentException) {
                                        Toast.makeText(
                                            context,
                                            exception.errorCode.name,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        }
                    })
                }
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }

            /* Cashback 프로그램을 받아와서 Recycler 뷰에 그림 */
            MomentSDK.getInstance(it).listCashback(object : MomentSDK.ListCashbackResultCallback {
                override fun onSuccess(cashbackPrograms: MutableList<CashbackProgram>) {
                    Log.e("MomentSDK", "listCashback onSuccess")
                    cashbackAdapter.setData(cashbackPrograms);
                }

                override fun onFailure(exception: MomentException) {
                    Toast.makeText(context, "캐시백 프로그램을 받아오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e(
                        "MomentSDK",
                        "listCashback onFailure(${exception.errorCode.name}): ${exception.message}"
                    )
                }
            })
        }
    }
}