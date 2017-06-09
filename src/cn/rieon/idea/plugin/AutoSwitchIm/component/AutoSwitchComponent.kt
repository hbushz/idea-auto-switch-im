package cn.rieon.idea.plugin.AutoSwitchIm.component

import cn.rieon.idea.plugin.AutoSwitchIm.provider.ConfigurationProvider
import cn.rieon.idea.plugin.AutoSwitchIm.util.InputSourceUtil
import com.intellij.ide.FrameStateListener
import com.intellij.ide.FrameStateManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.command.CommandAdapter
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger

import java.util.*

/**
 * @author Rieon Ke <rieon></rieon>@rieon.cn>
 * *
 * @version 1.0.0
 * *
 * @since 2017/5/20
 */
class AutoSwitchComponent : ApplicationComponent {

    /**
     * last input source
     */
    private var lastInputSource: String? = null

    private var inNormal: Boolean? = true

    private var configurationProvider: ConfigurationProvider? = null

    /**
     * init component and add listener
     */
    override fun initComponent() {

        LOG.info("INIT COMPONENT")


        lastInputSource = currentInputSourceId
        CommandProcessor.getInstance().addCommandListener(commandListener)
        FrameStateManager.getInstance().addListener(frameStateListener)

        configurationProvider = ConfigurationProvider.instance

        if (configurationProvider!!.selectedInputSource != null) {

            switchToInputSource = configurationProvider!!.selectedInputSource!!

            LOG.info("LOAD PREVIOUS CONFIG")
            LOG.info("USE CONFIG INPUT SOURCE " + switchToInputSource)
        }

        LOG.info("INIT SUCCESSFUL")
    }

    /**
     * auto switch input source while plugin state changed
     * @return CommandListener
     */
    private val commandListener: CommandListener
        get() = object : CommandAdapter() {
            override fun beforeCommandFinished(event: CommandEvent?) {

                val commandName = event!!.commandName

                if (VimStateSwitchToABCOnly.contains(commandName)) {
                    lastInputSource = currentInputSourceId
                    inNormal = true
                    if (lastInputSource == null || lastInputSource == switchToInputSource)
                        return
                    switchTo(switchToInputSource)
                } else if (VimStateSwitchToLastIm.contains(commandName)) {
                    val current = currentInputSourceId
                    inNormal = false
                    if (current == null || current == lastInputSource)
                        return
                    switchTo(lastInputSource)
                } else if ("Typing" == commandName) {
                    inNormal = false
                } else if ("" == commandName) {
                    if (inNormal!!)
                        switchTo(switchToInputSource)
                }
            }
        }

    /**
     * restore last input source while idea re-focused
     * @return FrameStateListener
     */
    private val frameStateListener: FrameStateListener
        get() = object : FrameStateListener.Adapter() {
            override fun onFrameDeactivated() {
                lastInputSource = currentInputSourceId
            }

            override fun onFrameActivated() {
                val current = currentInputSourceId
                if (current == null || current == lastInputSource)
                    return
                switchTo(lastInputSource)
            }
        }

    /**
     * get current input source
     * @return String currentInputSource
     */
    private val currentInputSourceId: String?
        get() {

            val current = InputSourceUtil.currentInputSource

            LOG.info("CURRENT INPUT SOURCE  " + current)
            if (current.isEmpty()) {
                val notification = Notification("Switch IME Error", "Switch IME Error", "Get current input source faild", NotificationType.ERROR)
                Notifications.Bus.notify(notification)
                LOG.info("GET CURRENT INPUT SOURCE FAILED")
            }

            return current

        }

    internal fun switchTo(source: String?) {

        LOG.info("SWITCH TO INPUT SOURCE  " + source!!)
        if (!InputSourceUtil.switchTo(source, InputSourceUtil.BY_ID)) {
            val notification = Notification("Switch IME Error", "Switch IME Error", "Switch IME Failed", NotificationType.ERROR)
            Notifications.Bus.notify(notification)
            LOG.info("SWITCH TO INPUT SOURCE FAILED")
        }
    }

    override fun disposeComponent() {
        // TODO: insert component disposal logic here
    }

    override fun getComponentName(): String {
        return "AutoSwitchComponent"
    }

    companion object {

        private val LOG = Logger.getInstance(AutoSwitchComponent::class.java)

        var switchToInputSource = "com.apple.keylayout.ABC"


        /**
         * switch input source in these plugin states
         */
        private val VimStateSwitchToABCOnly = Arrays.asList(
                "Vim Exit Visual Mode",
                "Vim Toggle Line Selection",
                "Vim Toggle Block Selection",
                "Vim Toggle Character Selection",
                "Vim Exit Insert Mode")

        /**
         * switch to last input source in these plugin states
         */
        private val VimStateSwitchToLastIm = Arrays.asList(
                "Vim Enter",
                "Vim Insert at Line Start",
                "Vim Insert New Line Above",
                "Vim Insert New Line Below",
                "Vim Insert Previous Text",
                "Vim Insert Previous Text",
                "Vim Insert Register",
                "Vim Toggle Insert/Replace",
                "Vim Change Line",
                "Vim Change Character",
                "Vim Change Characters",
                "Vim Replace",
                "Vim Insert After Cursor",
                "Vim Insert After Line End",
                "Vim Insert Before Cursor",
                "Vim Insert Before First non-Blank",
                "Vim Insert Character Above Cursor",
                "Vim Insert Character Below Cursor",
                "Vim Delete Inserted Text",
                "Vim Delete Previous Word")
    }

}
