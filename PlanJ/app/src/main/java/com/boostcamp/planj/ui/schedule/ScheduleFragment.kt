package com.boostcamp.planj.ui.schedule

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boostcamp.planj.R
import com.boostcamp.planj.data.model.Alarm
import com.boostcamp.planj.data.model.Repetition
import com.boostcamp.planj.databinding.FragmentScheduleBinding
import com.boostcamp.planj.ui.PlanjAlarm
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ScheduleFragment : Fragment(), RepetitionSettingDialogListener, AlarmSettingDialogListener {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScheduleViewModel by activityViewModels()

    private val args: ScheduleFragmentArgs by navArgs()

    private val repetitionSettingDialog by lazy {
        RepetitionSettingDialog()
    }

    private val alarmSettingDialog by lazy {
        AlarmSettingDialog()
    }

    private val participantDialog by lazy {
        ScheduleParticipantDialog()
    }

    private val datePickerBuilder by lazy {
        MaterialDatePicker.Builder.datePicker()
    }

    private val timePickerBuilder by lazy {
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
    }

    private val planjAlarm by lazy {
        PlanjAlarm(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner

        initAdapter()
        setObserver()
        setListener()

        if (args.startLocation != null || args.location != null) {
            viewModel.setLocation(args.startLocation, args.location)
        }

        repetitionSettingDialog.setRepetitionDialogListener(this)
        alarmSettingDialog.setAlarmSettingDialogListener(this)

        binding.executePendingBindings()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun initAdapter() {
        val adapter = ScheduleParticipantAdapter(viewModel.participants.value)
        binding.rvScheduleParticipants.adapter = adapter
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditMode.collect { isEditMode ->
                updateToolbar(isEditMode)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isComplete.collect { isComplete ->
                if (isComplete) activity?.finish()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alarmEventFlow.collectLatest { alarmEvent ->
                when (alarmEvent) {
                    is AlarmEvent.Set -> {
                        val alarmInfo = alarmEvent.alarmInfo
                        planjAlarm.setAlarm(alarmInfo)
                    }

                    is AlarmEvent.Delete -> {
                        planjAlarm.deleteAlarm(alarmEvent.scheduleId.hashCode())
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryList.collect { categoryList ->
                val arrayAdapter =
                    ArrayAdapter(requireContext(), R.layout.item_dropdown, categoryList)
                binding.actvScheduleSelectedCategory.setAdapter(arrayAdapter)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scheduleAlarm.collect { alarm ->

                    if (alarm != null && alarm.alarmType == "DEPARTURE") {
                        binding.tvScheduleLocationAlarm.text = "위치 알람 해제"
                        binding.tvScheduleLocationAlarm.setBackgroundResource(R.drawable.round_r8_red)

                    } else {
                        binding.tvScheduleLocationAlarm.text = "위치 알람 설정"
                        binding.tvScheduleLocationAlarm.setBackgroundResource(R.drawable.round_r8_main2)
                    }
                }
            }
        }
    }

    private fun setListener() {
        binding.toolbarSchedule.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_schedule_edit -> {
                    viewModel.startEditingSchedule()
                    true
                }

                R.id.item_schedule_delete -> {
                    viewModel.deleteSchedule()
                    activity?.finish()
                    true
                }

                R.id.item_schedule_complete -> {
                    viewModel.completeEditingSchedule()
                    true
                }

                else -> {
                    false
                }
            }
        }

        binding.toolbarSchedule.setNavigationOnClickListener {
            activity?.finish()
        }

        binding.tvScheduleRepetitionSetting.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("repetitionInfo", viewModel.scheduleRepetition.value)
            repetitionSettingDialog.arguments = bundle
            if (!repetitionSettingDialog.isAdded) {
                repetitionSettingDialog.show(childFragmentManager, "반복 설정")
            }
        }

        binding.tvScheduleAlarmSetting.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("alarmInfo", viewModel.scheduleAlarm.value)
            alarmSettingDialog.arguments = bundle
            if (!alarmSettingDialog.isAdded) {
                alarmSettingDialog.show(childFragmentManager, "알림 설정")
            }
        }

        binding.ivScheduleMap.setOnClickListener {
            val action =
                ScheduleFragmentDirections.actionScheduleFragmentToScheduleMapFragment(viewModel.endScheduleLocation.value)
            findNavController().navigate(action)
        }

        binding.ivScheduleStartMap.setOnClickListener {
            val action =
                ScheduleFragmentDirections.actionScheduleFragmentToScheduleStartMapFragment(
                    endLocation = viewModel.endScheduleLocation.value,
                    startLocation = viewModel.startScheduleLocation.value
                )
            findNavController().navigate(action)
        }

        binding.tvScheduleAllParticipants.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelableArrayList(
                "participantsInfo",
                ArrayList(viewModel.participants.value)
            )
            participantDialog.arguments = bundle
            if (!participantDialog.isAdded) {
                participantDialog.show(childFragmentManager, "전체 참가자")
            }
        }

        binding.tvScheduleLocationUrlScheme.setOnClickListener {
            activity?.let {

                val startLocation =
                    viewModel.startScheduleLocation.value ?: return@setOnClickListener
                val endLocation = viewModel.endScheduleLocation.value ?: return@setOnClickListener
                val url =
                    "nmap://route/public?slat=${startLocation.latitude}&slng=${startLocation.longitude}&sname=${startLocation.placeName}&dlat=${endLocation.latitude}&dlng=${endLocation.longitude}&dname=${endLocation.placeName}&appname=com.boostcamp.planj"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addCategory(Intent.CATEGORY_BROWSABLE)

                try {
                    it.startActivity(intent)
                } catch (e: Exception) {
                    if (e is ActivityNotFoundException) {
                        it.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.nhn.android.nmap")
                            )
                        )
                    } else {
                        Log.d("PLANJDEBUG", "${e.message}")
                    }
                }

            }
        }

        binding.tvScheduleLocationAlarm.setOnClickListener {
            viewModel.response.value?.let {
                if (binding.tvScheduleLocationAlarm.text == "위치 알람 해제") {
                    viewModel.setAlarm(null)
                } else {
                    val bottomSheet =
                        ScheduleBottomSheetDialog(it.route.trafast[0].summary.duration) { min ->
                            viewModel.setAlarm(Alarm("DEPARTURE", min))
                        }
                    bottomSheet.show(childFragmentManager, tag)
                }
            }
        }
    }

    private fun updateToolbar(isEditMode: Boolean) {
        with(binding.toolbarSchedule.menu) {
            findItem(R.id.item_schedule_edit).isVisible = !isEditMode
            findItem(R.id.item_schedule_delete).isVisible = !isEditMode
            findItem(R.id.item_schedule_complete).isVisible = isEditMode
        }
        binding.tvScheduleTop.text = if (!isEditMode) "일정" else "일정 편집"
    }

    fun setStartTime() {
        var dateMillis = 0L
        val datePicker = setDatePicker(viewModel.getStartDate())
        datePicker.show(childFragmentManager, "시작 날짜 설정")

        val scheduleStartTime = viewModel.scheduleStartTime.value
        val timePicker = setTimePicker(scheduleStartTime?.hour ?: 0, scheduleStartTime?.minute ?: 0)

        datePicker.addOnPositiveButtonClickListener {
            dateMillis = datePicker.selection ?: 0
            timePicker.show(childFragmentManager, "시작 시간 설정")
        }
        timePicker.addOnPositiveButtonClickListener {
            viewModel.setStartTime(dateMillis, timePicker.hour, timePicker.minute)
        }
    }

    fun setEndTime() {
        var dateMillis = 0L
        val datePicker = setDatePicker(viewModel.getEndDate())
        datePicker.show(childFragmentManager, "시작 날짜 설정")

        val scheduleEndTime = viewModel.scheduleEndTime.value
        val timePicker = setTimePicker(scheduleEndTime.hour, scheduleEndTime.minute)

        datePicker.addOnPositiveButtonClickListener {
            dateMillis = datePicker.selection ?: 0
            timePicker.show(childFragmentManager, "시작 시간 설정")
        }
        timePicker.addOnPositiveButtonClickListener {
            viewModel.setEndTime(dateMillis, timePicker.hour, timePicker.minute)
        }
    }

    private fun setDatePicker(selectedDate: Long?): MaterialDatePicker<Long> {
        if (selectedDate != null) {
            datePickerBuilder.setSelection(selectedDate)
        }
        return datePickerBuilder.build()
    }

    private fun setTimePicker(hour: Int = 0, minute: Int = 0): MaterialTimePicker {
        timePickerBuilder.setHour(hour)
        timePickerBuilder.setMinute(minute)
        return timePickerBuilder.build()
    }

    override fun onClickComplete(repetition: Repetition?) {
        viewModel.setRepetition(repetition)
    }

    override fun onClickComplete(alarm: Alarm?) {
        viewModel.setAlarm(alarm)
    }
}