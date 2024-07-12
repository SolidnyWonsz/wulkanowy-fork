package io.github.wulkanowy.ui.modules.timetable.additional

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.TimetableAdditional
import io.github.wulkanowy.databinding.FragmentTimetableAdditionalBinding
import io.github.wulkanowy.ui.base.BaseFragment
import io.github.wulkanowy.ui.modules.main.MainActivity
import io.github.wulkanowy.ui.modules.main.MainView
import io.github.wulkanowy.ui.modules.timetable.additional.add.AdditionalLessonAddDialog
import io.github.wulkanowy.ui.widgets.DividerItemDecoration
import io.github.wulkanowy.utils.dpToPx
import io.github.wulkanowy.utils.firstSchoolDayInSchoolYear
import io.github.wulkanowy.utils.getThemeAttrColor
import io.github.wulkanowy.utils.lastSchoolDayInSchoolYear
import io.github.wulkanowy.utils.openMaterialDatePicker
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class AdditionalLessonsFragment :
    BaseFragment<FragmentTimetableAdditionalBinding>(R.layout.fragment_timetable_additional),
    AdditionalLessonsView, MainView.TitledView {

    @Inject
    lateinit var presenter: AdditionalLessonsPresenter

    @Inject
    lateinit var additionalLessonsAdapter: AdditionalLessonsAdapter

    companion object {
        private const val SAVED_DATE_KEY = "CURRENT_DATE"

        fun newInstance() = AdditionalLessonsFragment()
    }

    override val titleStringId get() = R.string.additional_lessons_title

    override val isViewEmpty get() = additionalLessonsAdapter.items.isEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTimetableAdditionalBinding.bind(view)
        messageContainer = binding.additionalLessonsRecycler
        presenter.onAttachView(this, savedInstanceState?.getLong(SAVED_DATE_KEY))
    }

    override fun initView() {
        with(binding.additionalLessonsRecycler) {
            layoutManager = LinearLayoutManager(context)
            adapter = additionalLessonsAdapter.apply {
                onDeleteClickListener = { presenter.onDeleteLessonsSelected(it) }
            }
            addItemDecoration(DividerItemDecoration(context))
        }

        with(binding) {
            additionalLessonsSwipe.setOnRefreshListener(presenter::onSwipeRefresh)
            additionalLessonsSwipe.setColorSchemeColors(requireContext().getThemeAttrColor(R.attr.colorPrimary))
            additionalLessonsSwipe.setProgressBackgroundColorSchemeColor(
                requireContext().getThemeAttrColor(R.attr.colorSwipeRefresh)
            )
            additionalLessonsErrorRetry.setOnClickListener { presenter.onRetry() }
            additionalLessonsErrorDetails.setOnClickListener { presenter.onDetailsClick() }

            additionalLessonsPreviousButton.setOnClickListener { presenter.onPreviousDay() }
            additionalLessonsNavDate.setOnClickListener { presenter.onPickDate() }
            additionalLessonsNextButton.setOnClickListener { presenter.onNextDay() }

            openAddAdditionalLessonButton.setOnClickListener { presenter.onAdditionalLessonAddButtonClicked() }

            additionalLessonsNavContainer.elevation = requireContext().dpToPx(3f)
        }
    }

    override fun updateData(data: List<TimetableAdditional>) {
        with(additionalLessonsAdapter) {
            items = data
            notifyDataSetChanged()
        }
    }

    override fun clearData() {
        with(additionalLessonsAdapter) {
            items = emptyList()
            notifyDataSetChanged()
        }
    }

    override fun showSuccessMessage() {
        getString(R.string.additional_lessons_delete_success)
    }

    override fun updateNavigationDay(date: String) {
        binding.additionalLessonsNavDate.text = date
    }

    override fun hideRefresh() {
        binding.additionalLessonsSwipe.isRefreshing = false
    }

    override fun showEmpty(show: Boolean) {
        binding.additionalLessonsEmpty.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showErrorView(show: Boolean) {
        binding.additionalLessonsError.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun setErrorDetails(message: String) {
        binding.additionalLessonsErrorMessage.text = message
    }

    override fun showProgress(show: Boolean) {
        binding.additionalLessonsProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun enableSwipe(enable: Boolean) {
        binding.additionalLessonsSwipe.isEnabled = enable
    }

    override fun showContent(show: Boolean) {
        binding.additionalLessonsRecycler.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPreButton(show: Boolean) {
        binding.additionalLessonsPreviousButton.visibility =
            if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun showNextButton(show: Boolean) {
        binding.additionalLessonsNextButton.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun showAddAdditionalLessonDialog(currentDate: LocalDate) {
        (activity as? MainActivity)?.showDialogFragment(
            AdditionalLessonAddDialog.newInstance(
                currentDate
            )
        )
    }

    override fun showDatePickerDialog(selectedDate: LocalDate) {
        val now = LocalDate.now()

        openMaterialDatePicker(
            selected = selectedDate,
            rangeStart = now.firstSchoolDayInSchoolYear,
            rangeEnd = now.lastSchoolDayInSchoolYear,
            onDateSelected = {
                presenter.onDateSet(it.year, it.monthValue, it.dayOfMonth)
            }
        )
    }

    override fun showDeleteLessonDialog(timetableAdditional: TimetableAdditional) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.additional_lessons_delete_title))
            .setItems(
                arrayOf(
                    getString(R.string.additional_lessons_delete_one),
                    getString(R.string.additional_lessons_delete_series)
                )
            ) { _, position -> presenter.onDeleteDialogSelectItem(position, timetableAdditional) }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(SAVED_DATE_KEY, presenter.currentDate.toEpochDay())
    }

    override fun onDestroyView() {
        presenter.onDetachView()
        super.onDestroyView()
    }
}
