package com.boostcamp.planj.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.boostcamp.planj.R
import com.boostcamp.planj.data.model.Repetition
import com.boostcamp.planj.databinding.DialogRepetitionSettingBinding

class RepetitionSettingDialog(
    private val repetition: Repetition?,
    private val repetitionSettingDialogListener: RepetitionSettingDialogListener
) : DialogFragment() {

    private var _binding: DialogRepetitionSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRepetitionSettingBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setVisibility()
        setListener()
        setBtnClicked()
    }

    override fun onResume() {
        super.onResume()

        context?.setDialogSize(this)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setVisibility() {
        with(binding) {
            rgDialogRepetitionMode.setOnCheckedChangeListener { _, i ->
                layoutDialogRepetitionDay.visibility =
                    if (i == R.id.rb_dialog_repetition_day) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                layoutDialogRepetitionWeek.visibility =
                    if (i == R.id.rb_dialog_repetition_week) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }
    }

    private fun setListener() {
        with(binding) {
            tvDialogRepetitionCancel.setOnClickListener {
                dismiss()
            }

            tvDialogRepetitionComplete.setOnClickListener {
                with(binding) {
                    val repetition = if (rbDialogRepetitionNo.isChecked) {
                        null
                    } else if (rbDialogRepetitionDay.isChecked) {
                        Repetition("DAILY", etDialogRepetitionDay.text.toString().toInt())
                    } else {
                        Repetition("WEEKLY", etDialogRepetitionWeek.text.toString().toInt())
                    }
                    repetitionSettingDialogListener.onClickComplete(repetition)
                    dismiss()
                }
            }
        }
    }

    private fun setBtnClicked() {
        with(binding) {
            if (repetition == null) {
                rbDialogRepetitionNo.isChecked = true
            } else if (repetition.cycleType == "DAILY") {
                rbDialogRepetitionDay.isChecked = true
                etDialogRepetitionDay.setText(repetition.cycleCount.toString())
            } else {
                rbDialogRepetitionWeek.isChecked = true
                etDialogRepetitionWeek.setText(repetition.cycleCount.toString())
            }
        }
    }
}