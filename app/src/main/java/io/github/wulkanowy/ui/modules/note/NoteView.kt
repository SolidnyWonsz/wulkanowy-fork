package io.github.wulkanowy.ui.modules.note

import io.github.wulkanowy.data.db.entities.Note
import io.github.wulkanowy.ui.base.BaseView

interface NoteView : BaseView {

    val isViewEmpty: Boolean

    fun initView()

    fun updateData(data: List<Note>)

    fun updateItem(item: Note, position: Int)

    fun clearData()

    fun showEmpty(show: Boolean)

    fun showErrorView(show: Boolean)

    fun setErrorDetails(message: String)

    fun showProgress(show: Boolean)

    fun enableSwipe(enable: Boolean)

    fun showContent(show: Boolean)

    fun showRefresh(show: Boolean)

    fun showNoteDialog(note: Note)

    fun resetView()
}
