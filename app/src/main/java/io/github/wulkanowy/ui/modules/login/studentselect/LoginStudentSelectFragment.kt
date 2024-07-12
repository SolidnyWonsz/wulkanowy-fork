package io.github.wulkanowy.ui.modules.login.studentselect

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.AdminMessage
import io.github.wulkanowy.data.pojos.RegisterUser
import io.github.wulkanowy.data.repositories.PreferencesRepository
import io.github.wulkanowy.databinding.FragmentLoginStudentSelectBinding
import io.github.wulkanowy.ui.base.BaseFragment
import io.github.wulkanowy.ui.modules.dashboard.viewholders.AdminMessageViewHolder
import io.github.wulkanowy.ui.modules.login.LoginActivity
import io.github.wulkanowy.ui.modules.login.LoginData
import io.github.wulkanowy.ui.modules.login.support.LoginSupportDialog
import io.github.wulkanowy.ui.modules.login.support.LoginSupportInfo
import io.github.wulkanowy.utils.AppInfo
import io.github.wulkanowy.utils.openInternetBrowser
import io.github.wulkanowy.utils.serializable
import javax.inject.Inject

@AndroidEntryPoint
class LoginStudentSelectFragment :
    BaseFragment<FragmentLoginStudentSelectBinding>(R.layout.fragment_login_student_select),
    LoginStudentSelectView {

    @Inject
    lateinit var presenter: LoginStudentSelectPresenter

    @Inject
    lateinit var loginAdapter: LoginStudentSelectAdapter

    @Inject
    lateinit var appInfo: AppInfo

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private lateinit var symbolsNames: Array<String>
    private lateinit var symbolsValues: Array<String>

    override val symbols: Map<String, String> by lazy {
        symbolsValues.zip(symbolsNames).toMap()
    }

    companion object {
        private const val ARG_LOGIN = "LOGIN"
        private const val ARG_STUDENTS = "STUDENTS"

        fun newInstance(loginData: LoginData, registerUser: RegisterUser) =
            LoginStudentSelectFragment().apply {
                arguments = bundleOf(
                    ARG_LOGIN to loginData,
                    ARG_STUDENTS to registerUser,
                )
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginStudentSelectBinding.bind(view)

        symbolsNames = resources.getStringArray(R.array.symbols)
        symbolsValues = resources.getStringArray(R.array.symbols_values)

        presenter.onAttachView(
            view = this,
            loginData = requireArguments().serializable(ARG_LOGIN),
            registerUser = requireArguments().serializable(ARG_STUDENTS),
        )
    }

    override fun initView() {
        (requireActivity() as LoginActivity).showActionBar(true)

        with(binding) {
            loginStudentSelectSignIn.setOnClickListener { presenter.onSignIn() }
            loginStudentSelectRecycler.adapter = loginAdapter
        }
    }

    override fun updateData(data: List<LoginStudentSelectItem>) {
        loginAdapter.submitList(data)
    }

    override fun navigateToSymbol(loginData: LoginData) {
        (requireActivity() as LoginActivity).navigateToSymbolFragment(loginData)
    }

    override fun navigateToNext() {
        (requireActivity() as LoginActivity).navigateToNotifications()
    }

    override fun showProgress(show: Boolean) {
        binding.loginStudentSelectProgress.isVisible = show
    }

    override fun showContent(show: Boolean) {
        binding.loginStudentSelectContent.isVisible = show
    }

    override fun enableSignIn(enable: Boolean) {
        binding.loginStudentSelectSignIn.isEnabled = enable
    }

    override fun openDiscordInvite() {
        context?.openInternetBrowser("https://discord.gg/vccAQBr", ::showMessage)
    }

    override fun openEmail(supportInfo: LoginSupportInfo) {
        LoginSupportDialog.newInstance(supportInfo).show(childFragmentManager, "support_dialog")
    }

    override fun showAdminMessage(adminMessage: AdminMessage?) {
        AdminMessageViewHolder(
            binding = binding.loginStudentSelectAdminMessage,
            onAdminMessageDismissClickListener = presenter::onAdminMessageDismissed,
            onAdminMessageClickListener = presenter::onAdminMessageSelected,
            onPanicButtonClickListener = {},
        ).bind(adminMessage)
        binding.loginStudentSelectAdminMessage.root.isVisible = adminMessage != null
    }

    override fun openInternetBrowser(url: String) {
        requireContext().openInternetBrowser(url)
    }

    override fun onDestroyView() {
        presenter.onDetachView()
        super.onDestroyView()
    }
}
