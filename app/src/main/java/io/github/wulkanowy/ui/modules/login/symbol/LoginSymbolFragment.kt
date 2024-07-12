package io.github.wulkanowy.ui.modules.login.symbol

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_NULL
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.AdminMessage
import io.github.wulkanowy.data.pojos.RegisterUser
import io.github.wulkanowy.data.repositories.PreferencesRepository
import io.github.wulkanowy.databinding.FragmentLoginSymbolBinding
import io.github.wulkanowy.ui.base.BaseFragment
import io.github.wulkanowy.ui.modules.dashboard.viewholders.AdminMessageViewHolder
import io.github.wulkanowy.ui.modules.login.LoginActivity
import io.github.wulkanowy.ui.modules.login.LoginData
import io.github.wulkanowy.ui.modules.login.support.LoginSupportDialog
import io.github.wulkanowy.ui.modules.login.support.LoginSupportInfo
import io.github.wulkanowy.utils.AppInfo
import io.github.wulkanowy.utils.hideSoftInput
import io.github.wulkanowy.utils.openInternetBrowser
import io.github.wulkanowy.utils.serializable
import io.github.wulkanowy.utils.showSoftInput
import javax.inject.Inject

@AndroidEntryPoint
class LoginSymbolFragment :
    BaseFragment<FragmentLoginSymbolBinding>(R.layout.fragment_login_symbol), LoginSymbolView {

    @Inject
    lateinit var presenter: LoginSymbolPresenter

    @Inject
    lateinit var appInfo: AppInfo

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    companion object {
        private const val SAVED_LOGIN_DATA = "LOGIN_DATA"

        fun newInstance(loginData: LoginData) = LoginSymbolFragment().apply {
            arguments = bundleOf(SAVED_LOGIN_DATA to loginData)
        }
    }

    override val symbolValue: String? get() = binding.loginSymbolName.text?.toString()

    override val symbolNameError: CharSequence?
        get() = binding.loginSymbolNameLayout.error

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginSymbolBinding.bind(view)
        presenter.onAttachView(
            view = this,
            loginData = requireArguments().serializable(SAVED_LOGIN_DATA),
        )
    }

    override fun initView() {
        (requireActivity() as LoginActivity).showActionBar(true)

        with(binding) {
            loginSymbolSignIn.setOnClickListener { presenter.attemptLogin() }
            loginSymbolFaq.setOnClickListener { presenter.onFaqClick() }
            loginSymbolContactEmail.setOnClickListener { presenter.onEmailClick() }

            loginSymbolName.doOnTextChanged { _, _, _, _ -> presenter.onSymbolTextChanged() }

            loginSymbolName.apply {
                setOnEditorActionListener { _, id, _ ->
                    if (id == IME_ACTION_DONE || id == IME_NULL) loginSymbolSignIn.callOnClick() else false
                }
                setAdapter(
                    ArrayAdapter(
                        context,
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.symbols_values)
                    )
                )
            }
        }
    }

    override fun setLoginToHeading(login: String) {
        binding.loginSymbolHeader.text =
            getString(R.string.login_header_symbol, login).parseAsHtml()
    }

    override fun setErrorSymbolIncorrect() {
        binding.loginSymbolNameLayout.apply {
            requestFocus()
            error = getString(R.string.login_incorrect_symbol)
        }
    }

    override fun setErrorSymbolInvalid() {
        with(binding.loginSymbolNameLayout) {
            requestFocus()
            error = getString(R.string.login_invalid_symbol)
        }
    }

    override fun setErrorSymbolDefinitelyInvalid() {
        with(binding.loginSymbolNameLayout) {
            requestFocus()
            error = getString(R.string.login_invalid_symbol_definitely)
        }
    }

    override fun setErrorSymbolRequire() {
        setErrorSymbol(getString(R.string.error_field_required))
    }

    override fun setErrorSymbol(message: String) {
        with(binding.loginSymbolNameLayout) {
            requestFocus()
            error = message
        }
    }

    override fun clearSymbolError() {
        binding.loginSymbolNameLayout.error = null
    }

    override fun clearAndFocusSymbol() {
        binding.loginSymbolNameLayout.apply {
            editText?.text = null
            requestFocus()
        }
    }

    override fun showSoftKeyboard() {
        activity?.showSoftInput()
    }

    override fun hideSoftKeyboard() {
        activity?.hideSoftInput()
    }

    override fun showProgress(show: Boolean) {
        binding.loginSymbolProgress.visibility = if (show) VISIBLE else GONE
    }

    override fun showContent(show: Boolean) {
        binding.loginSymbolContainer.visibility = if (show) VISIBLE else GONE
    }

    override fun navigateToStudentSelect(loginData: LoginData, registerUser: RegisterUser) {
        (activity as? LoginActivity)?.navigateToStudentSelect(loginData, registerUser)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(SAVED_LOGIN_DATA, presenter.loginData)
    }

    override fun showContact(show: Boolean) {
        binding.loginSymbolContact.visibility = if (show) VISIBLE else GONE
    }

    override fun onDestroyView() {
        presenter.onDetachView()
        super.onDestroyView()
    }

    override fun openFaqPage() {
        context?.openInternetBrowser(
            "https://wulkanowy.github.io/czesto-zadawane-pytania/co-to-jest-symbol",
            ::showMessage
        )
    }

    override fun openSupportDialog(supportInfo: LoginSupportInfo) {
        LoginSupportDialog.newInstance(supportInfo).show(childFragmentManager, "support_dialog")
    }

    override fun showAdminMessage(adminMessage: AdminMessage?) {
        AdminMessageViewHolder(
            binding = binding.loginSymbolAdminMessage,
            onAdminMessageDismissClickListener = presenter::onAdminMessageDismissed,
            onAdminMessageClickListener = presenter::onAdminMessageSelected,
            onPanicButtonClickListener = {},
        ).bind(adminMessage)
        binding.loginSymbolAdminMessage.root.isVisible = adminMessage != null
    }

    override fun openInternetBrowser(url: String) {
        requireContext().openInternetBrowser(url)
    }
}
