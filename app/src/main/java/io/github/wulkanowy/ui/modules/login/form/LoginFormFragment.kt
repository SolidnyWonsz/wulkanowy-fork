package io.github.wulkanowy.ui.modules.login.form

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.AdminMessage
import io.github.wulkanowy.data.pojos.RegisterUser
import io.github.wulkanowy.data.repositories.PreferencesRepository
import io.github.wulkanowy.databinding.FragmentLoginFormBinding
import io.github.wulkanowy.ui.base.BaseFragment
import io.github.wulkanowy.ui.modules.captcha.CaptchaDialog
import io.github.wulkanowy.ui.modules.dashboard.viewholders.AdminMessageViewHolder
import io.github.wulkanowy.ui.modules.login.LoginActivity
import io.github.wulkanowy.ui.modules.login.LoginData
import io.github.wulkanowy.ui.modules.login.support.LoginSupportDialog
import io.github.wulkanowy.ui.modules.login.support.LoginSupportInfo
import io.github.wulkanowy.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFormFragment : BaseFragment<FragmentLoginFormBinding>(R.layout.fragment_login_form),
    LoginFormView {

    @Inject
    lateinit var presenter: LoginFormPresenter

    @Inject
    lateinit var appInfo: AppInfo

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    companion object {
        fun newInstance() = LoginFormFragment()
    }

    override val formUsernameValue: String
        get() = binding.loginFormUsername.text.toString()

    override val formPassValue: String
        get() = binding.loginFormPass.text.toString()

    override val formHostValue: String
        get() = hostValues.getOrNull(hostKeys.indexOf(binding.loginFormHost.text.toString()))
            .orEmpty()

    override val formDomainSuffix: String
        get() = binding.loginFormDomainSuffix.text.toString()

    override val formHostSymbol: String
        get() = hostSymbols.getOrNull(hostKeys.indexOf(binding.loginFormHost.text.toString()))
            .orEmpty()

    override val nicknameLabel: String
        get() = getString(R.string.login_nickname_hint)

    override val emailLabel: String
        get() = getString(R.string.login_email_hint)

    private lateinit var hostKeys: Array<String>

    private lateinit var hostValues: Array<String>

    private lateinit var hostSymbols: Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginFormBinding.bind(view)
        presenter.onAttachView(this)
        initializeCaptchaResultObserver()
    }

    private fun initializeCaptchaResultObserver() {
        setFragmentResultListener(CaptchaDialog.CAPTCHA_SUCCESS) { _, _ ->
            presenter.onRetryAfterCaptcha()
        }
    }

    override fun initView() {
        (requireActivity() as LoginActivity).showActionBar(false)

        hostKeys = resources.getStringArray(R.array.hosts_keys)
        hostValues = resources.getStringArray(R.array.hosts_values)
        hostSymbols = resources.getStringArray(R.array.hosts_symbols)

        with(binding) {
            loginFormUsername.doOnTextChanged { _, _, _, _ -> presenter.onUsernameTextChanged() }
            loginFormPass.doOnTextChanged { _, _, _, _ -> presenter.onPassTextChanged() }
            loginFormHost.setOnItemClickListener { _, _, _, _ -> presenter.onHostSelected() }
            loginFormDomainSuffix.doOnTextChanged { _, _, _, _ -> presenter.onDomainSuffixChanged() }
            loginFormSignIn.setOnClickListener { presenter.onSignInClick() }
            loginFormAdvancedButton.setOnClickListener { presenter.onAdvancedLoginClick() }
            loginFormPrivacyLink.setOnClickListener { presenter.onPrivacyLinkClick() }
            loginFormFaq.setOnClickListener { presenter.onFaqClick() }
            loginFormContactEmail.setOnClickListener { presenter.onEmailClick() }
            loginFormRecoverLink.setOnClickListener { presenter.onRecoverClick() }
            loginFormRecoverLinkSecond.setOnClickListener { presenter.onRecoverClick() }
            loginFormPass.setOnEditorDoneSignIn { loginFormSignIn.callOnClick() }
            loginFormHost.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) requireActivity().hideSoftInput()
            }
        }

        with(binding.loginFormHost) {
            setText(hostKeys.getOrNull(0).orEmpty())
            setAdapter(
                LoginSymbolAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    hostKeys
                )
            )
            setOnClickListener { if (binding.loginFormContainer.visibility == GONE) dismissDropDown() }
        }
    }

    override fun getHostsValues(): List<String> = hostValues.toList()

    override fun setCredentials(username: String, pass: String) {
        with(binding) {
            loginFormUsername.setText(username)
            loginFormPass.setText(pass)
        }
    }

    override fun setHost(host: String) {
        binding.loginFormHost.setText(
            hostKeys.getOrNull(hostValues.indexOf(host)).orEmpty()
        )
    }

    override fun setUsernameLabel(label: String) {
        binding.loginFormUsernameLayout.hint = label
    }

    override fun setErrorUsernameRequired() {
        with(binding.loginFormUsernameLayout) {
            error = getString(R.string.error_field_required)
        }
    }

    override fun setErrorLoginRequired() {
        with(binding.loginFormUsernameLayout) {
            error = getString(R.string.login_invalid_login)
        }
    }

    override fun setErrorEmailRequired() {
        with(binding.loginFormUsernameLayout) {
            error = getString(R.string.login_invalid_email)
        }
    }

    override fun setErrorPassRequired(focus: Boolean) {
        with(binding.loginFormPassLayout) {
            error = getString(R.string.error_field_required)
            setEndIconTintList(requireContext().getAttrColorStateList(R.attr.colorError))
        }
    }

    override fun setErrorPassInvalid(focus: Boolean) {
        with(binding.loginFormPassLayout) {
            error = getString(R.string.login_invalid_password)
            setEndIconTintList(requireContext().getAttrColorStateList(R.attr.colorError))
        }
    }

    override fun setErrorPassIncorrect(message: String?) {
        with(binding) {
            loginFormUsernameLayout.error = " "
            loginFormPassLayout.error = " "
            loginFormPassLayout.setEndIconTintList(requireContext().getAttrColorStateList(R.attr.colorError))
            loginFormHostLayout.error = " "
            loginFormErrorBox.text = message ?: getString(R.string.login_incorrect_password_default)
            loginFormErrorBox.isVisible = true
        }
    }

    override fun setErrorEmailInvalid(domain: String) {
        with(binding.loginFormUsernameLayout) {
            error = getString(R.string.login_invalid_custom_email, domain)
        }
    }

    override fun setDomainSuffixInvalid() {
        with(binding.loginFormDomainSuffixLayout) {
            error = getString(R.string.login_invalid_domain_suffix)
        }
    }

    override fun clearUsernameError() {
        binding.loginFormUsernameLayout.error = null
        binding.loginFormErrorBox.isVisible = false
    }

    override fun clearPassError() {
        binding.loginFormPassLayout.error = null
        binding.loginFormPassLayout.setEndIconTintList(
            requireContext().getAttrColorStateList(R.attr.colorOnSurface)
        )
        binding.loginFormErrorBox.isVisible = false
    }

    override fun clearHostError() {
        binding.loginFormHostLayout.error = null
        binding.loginFormErrorBox.isVisible = false
    }

    override fun clearDomainSuffixError() {
        binding.loginFormDomainSuffixLayout.error = null
    }

    override fun showSoftKeyboard() {
        activity?.showSoftInput()
    }

    override fun hideSoftKeyboard() {
        activity?.hideSoftInput()
    }

    override fun showProgress(show: Boolean) {
        binding.loginFormProgress.visibility = if (show) VISIBLE else GONE
    }

    override fun showContent(show: Boolean) {
        binding.loginFormContainer.visibility = if (show) VISIBLE else GONE
    }

    override fun showAdminMessage(message: AdminMessage?) {
        AdminMessageViewHolder(
            binding = binding.loginFormMessage,
            onAdminMessageDismissClickListener = presenter::onAdminMessageDismissed,
            onAdminMessageClickListener = presenter::onAdminMessageSelected,
            onPanicButtonClickListener = {},
        ).bind(message)
        binding.loginFormMessage.root.isVisible = message != null
    }

    override fun openInternetBrowser(url: String) {
        requireContext().openInternetBrowser(url)
    }

    override fun showDomainSuffixInput(show: Boolean) {
        binding.loginFormDomainSuffixLayout.isVisible = show
    }

    override fun showOtherOptionsButton(show: Boolean) {
        binding.loginFormAdvancedButton.isVisible = show
    }

    @SuppressLint("SetTextI18n")
    override fun showVersion() {
        binding.loginFormVersion.text = "v${appInfo.versionName}"
    }

    override fun showContact(show: Boolean) {
        binding.loginFormContact.isVisible = show
    }

    override fun openPrivacyPolicyPage() {
        context?.openInternetBrowser(
            "https://wulkanowy.github.io/polityka-prywatnosci.html",
            ::showMessage
        )
    }

    override fun navigateToSymbol(loginData: LoginData) {
        (activity as? LoginActivity)?.navigateToSymbolFragment(loginData)
    }

    override fun navigateToStudentSelect(loginData: LoginData, registerUser: RegisterUser) {
        (activity as? LoginActivity)?.navigateToStudentSelect(loginData, registerUser)
    }

    override fun openAdvancedLogin() {
        (activity as? LoginActivity)?.onAdvancedLoginClick()
    }

    override fun onRecoverClick() {
        (activity as? LoginActivity)?.onRecoverClick()
    }

    override fun onDestroyView() {
        presenter.onDetachView()
        super.onDestroyView()
    }

    override fun openFaqPage() {
        context?.openInternetBrowser(
            "https://wulkanowy.github.io/czesto-zadawane-pytania/dlaczego-nie-moge-sie-zalogowac",
            ::showMessage
        )
    }

    override fun onResume() {
        super.onResume()
        presenter.updateUsernameLabel()
        presenter.updateCustomDomainSuffixVisibility()
    }

    override fun openEmail(supportInfo: LoginSupportInfo) {
        LoginSupportDialog.newInstance(supportInfo).show(childFragmentManager, "support_dialog")
    }
}
