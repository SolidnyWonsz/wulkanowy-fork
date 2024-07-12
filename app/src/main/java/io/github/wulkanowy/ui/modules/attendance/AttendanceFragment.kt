package io.github.wulkanowy.ui.modules.attendance

import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.*
import android.view.View.*
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.Attendance
import io.github.wulkanowy.databinding.DialogExcuseBinding
import io.github.wulkanowy.databinding.FragmentAttendanceBinding
import io.github.wulkanowy.ui.base.BaseFragment
import io.github.wulkanowy.ui.modules.attendance.calculator.AttendanceCalculatorFragment
import io.github.wulkanowy.ui.modules.attendance.summary.AttendanceSummaryFragment
import io.github.wulkanowy.ui.modules.main.MainActivity
import io.github.wulkanowy.ui.modules.main.MainView
import io.github.wulkanowy.ui.modules.message.send.SendMessageActivity
import io.github.wulkanowy.ui.widgets.DividerItemDecoration
import io.github.wulkanowy.utils.dpToPx
import io.github.wulkanowy.utils.firstSchoolDayInSchoolYear
import io.github.wulkanowy.utils.getThemeAttrColor
import io.github.wulkanowy.utils.openMaterialDatePicker
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceFragment : BaseFragment<FragmentAttendanceBinding>(R.layout.fragment_attendance),
    AttendanceView, MainView.MainChildView,
    MainView.TitledView {

    @Inject
    lateinit var presenter: AttendancePresenter

    @Inject
    lateinit var attendanceAdapter: AttendanceAdapter

    override val excuseSuccessString: String
        get() = getString(R.string.attendance_excuse_success)

    override val excuseNoSelectionString: String
        get() = getString(R.string.attendance_excuse_no_selection)

    companion object {
        private const val SAVED_DATE_KEY = "CURRENT_DATE"

        fun newInstance() = AttendanceFragment()
    }

    override val titleStringId get() = R.string.attendance_title

    override val isViewEmpty get() = attendanceAdapter.items.isEmpty()

    override val currentStackSize get() = (activity as? MainActivity)?.currentStackSize

    override val excuseActionMode: Boolean get() = attendanceAdapter.excuseActionMode

    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.context_menu_attendance, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = getString(R.string.attendance_excuse_title)
            return presenter.onPrepareActionMode()
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            presenter.onDestroyActionMode()
            actionMode = null
        }

        override fun onActionItemClicked(mode: ActionMode, menu: MenuItem): Boolean {
            return when (menu.itemId) {
                R.id.excuseMenuSubmit -> presenter.onExcuseSubmitButtonClick()
                else -> false
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAttendanceBinding.bind(view)
        messageContainer = binding.attendanceRecycler
        presenter.onAttachView(this, savedInstanceState?.getLong(SAVED_DATE_KEY))
    }

    override fun initView() {
        with(attendanceAdapter) {
            onClickListener = presenter::onAttendanceItemSelected
            onExcuseCheckboxSelect = presenter::onExcuseCheckboxSelect
        }

        with(binding.attendanceRecycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = attendanceAdapter
            addItemDecoration(DividerItemDecoration(context))
        }

        with(binding) {
            attendanceSwipe.setOnRefreshListener(presenter::onSwipeRefresh)
            attendanceSwipe.setColorSchemeColors(requireContext().getThemeAttrColor(R.attr.colorPrimary))
            attendanceSwipe.setProgressBackgroundColorSchemeColor(
                requireContext().getThemeAttrColor(R.attr.colorSwipeRefresh)
            )
            attendanceErrorRetry.setOnClickListener { presenter.onRetry() }
            attendanceErrorDetails.setOnClickListener { presenter.onDetailsClick() }

            attendancePreviousButton.setOnClickListener { presenter.onPreviousDay() }
            attendanceNavDate.setOnClickListener { presenter.onPickDate() }
            attendanceNextButton.setOnClickListener { presenter.onNextDay() }

            attendanceExcuseButton.setOnClickListener { presenter.onExcuseButtonClick() }

            attendanceNavContainer.elevation = requireContext().dpToPx(3f)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_menu_attendance, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.attendanceMenuSummary) presenter.onSummarySwitchSelected()
        else if (item.itemId == R.id.attendanceMenuCalculator) presenter.onCalculatorSwitchSelected()
        else false
    }

    override fun updateData(data: List<Attendance>) {
        with(attendanceAdapter) {
            items = data
            notifyDataSetChanged()
        }
    }

    override fun updateNavigationDay(date: String) {
        binding.attendanceNavDate.text = date
    }

    override fun showNavigation(show: Boolean) {
        binding.attendanceNavContainer.isVisible = show
    }

    override fun clearData() {
        with(attendanceAdapter) {
            items = emptyList()
            notifyDataSetChanged()
        }
    }

    override fun resetView() {
        binding.attendanceRecycler.smoothScrollToPosition(0)
    }

    override fun onFragmentReselected() {
        if (::presenter.isInitialized) presenter.onViewReselected()
    }

    override fun onFragmentChanged() {
        if (::presenter.isInitialized) presenter.onMainViewChanged()
    }

    override fun popView() {
        (activity as? MainActivity)?.popView()
    }

    override fun showEmpty(show: Boolean) {
        binding.attendanceEmpty.visibility = if (show) VISIBLE else GONE
    }

    override fun showErrorView(show: Boolean) {
        binding.attendanceError.visibility = if (show) VISIBLE else GONE
    }

    override fun setErrorDetails(message: String) {
        binding.attendanceErrorMessage.text = message
    }

    override fun showProgress(show: Boolean) {
        binding.attendanceProgress.visibility = if (show) VISIBLE else GONE
    }

    override fun enableSwipe(enable: Boolean) {
        binding.attendanceSwipe.isEnabled = enable
    }

    override fun showContent(show: Boolean) {
        binding.attendanceRecycler.visibility = if (show) VISIBLE else GONE
    }

    override fun showRefresh(show: Boolean) {
        binding.attendanceSwipe.isRefreshing = show
    }

    override fun showPreButton(show: Boolean) {
        binding.attendancePreviousButton.visibility = if (show) VISIBLE else INVISIBLE
    }

    override fun showNextButton(show: Boolean) {
        binding.attendanceNextButton.visibility = if (show) VISIBLE else INVISIBLE
    }

    override fun showExcuseButton(show: Boolean) {
        binding.attendanceExcuseButton.isVisible = show
    }

    override fun showAttendanceDialog(lesson: Attendance) {
        (activity as? MainActivity)?.showDialogFragment(AttendanceDialog.newInstance(lesson))
    }

    override fun showDatePickerDialog(selectedDate: LocalDate) {
        openMaterialDatePicker(
            selected = selectedDate,
            rangeStart = selectedDate.firstSchoolDayInSchoolYear,
            rangeEnd = LocalDate.now().plusWeeks(1),
            onDateSelected = {
                presenter.onDateSet(it.year, it.monthValue, it.dayOfMonth)
            }
        )
    }

    override fun showExcuseDialog() {
        val dialogBinding = DialogExcuseBinding.inflate(LayoutInflater.from(context))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.attendance_excuse_title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .apply {
                setButton(
                    BUTTON_POSITIVE,
                    getString(R.string.attendance_excuse_dialog_submit)
                ) { _, _ ->
                    presenter.onExcuseDialogSubmit(
                        dialogBinding.excuseReason.text?.toString().orEmpty()
                    )
                }
            }.show()
    }

    override fun openSummaryView() {
        (activity as? MainActivity)?.pushView(AttendanceSummaryFragment.newInstance())
    }

    override fun openCalculatorView() {
        (activity as? MainActivity)?.pushView(AttendanceCalculatorFragment.newInstance())
    }

    override fun startActionMode() {
        actionMode = (activity as MainActivity?)?.startSupportActionMode(actionModeCallback)
    }

    override fun startSendMessageIntent(date: LocalDate, numbers: String, reason: String) {
        val reasonFullText = getString(
            R.string.attendance_excuse_formula,
            date,
            numbers,
            if (reason.isNotBlank()) " ${getString(R.string.attendance_excuse_reason)} " else "",
            reason.ifBlank { "" }
        )
        startActivity(SendMessageActivity.getStartIntent(requireContext(), reasonFullText))
    }

    override fun showExcuseCheckboxes(show: Boolean) {
        with(attendanceAdapter) {
            excuseActionMode = show
            notifyDataSetChanged()
        }
    }

    override fun showDayNavigation(show: Boolean) {
        binding.attendanceNavContainer.isVisible = show
    }

    override fun finishActionMode() {
        actionMode?.finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.currentDate?.let {
            outState.putLong(SAVED_DATE_KEY, it.toEpochDay())
        }
    }

    override fun onDestroyView() {
        presenter.onDetachView()
        super.onDestroyView()
    }
}
