/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft

import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.TaskWorker
import de.bixilon.kutil.concurrent.worker.tasks.Task
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.os.OSUtil
import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.minosoft.assets.file.ResourcesAssetsUtil
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.language.LanguageManager.Companion.load
import de.bixilon.minosoft.data.language.MultiLanguageManager
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.XStartOnFirstThreadWarning
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.dialog.StartingDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.modding.event.events.FinishInitializingEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.AutoConnect
import de.bixilon.minosoft.terminal.CLI
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.RenderPolling
import de.bixilon.minosoft.util.YggdrasilUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.worker.StartupTasks


object Minosoft {
    val MAIN_THREAD: Thread = Thread.currentThread()
    val MINOSOFT_ASSETS_MANAGER = ResourcesAssetsUtil.create(Minosoft::class.java, canUnload = false)
    val LANGUAGE_MANAGER = MultiLanguageManager()
    val START_UP_LATCH = CountUpAndDownLatch(1)

    @JvmStatic
    fun main(args: Array<String>) {
        CommandLineArguments.parse(args)
        KUtil.initUtilClasses()
        MINOSOFT_ASSETS_MANAGER.load(CountUpAndDownLatch(0))

        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Starting minosoft" }
        if (OSUtil.OS == OSUtil.OSs.MAC && !RunConfiguration.X_START_ON_FIRST_THREAD_SET && !RunConfiguration.DISABLE_RENDERING) {
            Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "You are using MacOS. To use rendering you have to add the jvm argument §9-XstartOnFirstThread§r. Please ensure it is set!" }
        }
        GitInfo.load()

        val taskWorker = TaskWorker(criticalErrorHandler = { _, exception -> exception.crash() })


        taskWorker += Task(identifier = StartupTasks.LOAD_VERSIONS, priority = ThreadPool.HIGH, executor = {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading versions..." }
            Versions.load()
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Versions loaded!" }
        })

        taskWorker += Task(identifier = StartupTasks.LOAD_PROFILES, priority = ThreadPool.HIGH, dependencies = arrayOf(StartupTasks.LOAD_VERSIONS), executor = {
            Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Loading profiles..." }
            GlobalProfileManager.initialize()
            Log.log(LogMessageType.PROFILES, LogLevels.INFO) { "Profiles loaded!" }
        })

        taskWorker += Task(identifier = StartupTasks.FILE_WATCHER, priority = ThreadPool.HIGH, optional = true, executor = {
            Log.log(LogMessageType.GENERAL, LogLevels.VERBOSE) { "Starting file watcher service..." }
            FileWatcherService.start()
            Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "File watcher service started!" }
        })


        taskWorker += Task(identifier = StartupTasks.LOAD_LANGUAGE_FILES, dependencies = arrayOf(StartupTasks.LOAD_PROFILES), executor = {
            val language = ErosProfileManager.selected.general.language
            ErosProfileManager.selected.general::language.profileWatch(this, true) {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading language files (${language})" }
                LANGUAGE_MANAGER.translators[ProtocolDefinition.MINOSOFT_NAMESPACE] = load(it, null, MINOSOFT_ASSETS_MANAGER, ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "language/"))
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Language files loaded!" }
            }
        })

        taskWorker += Task(identifier = StartupTasks.LOAD_DEFAULT_REGISTRIES, dependencies = arrayOf(StartupTasks.LOAD_PROFILES), executor = {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading default registries..." }

            AssetsVersionProperties.load()
            DefaultRegistries.load()

            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Default registries loaded!" }
        })


        taskWorker += Task(identifier = StartupTasks.LISTEN_LAN_SERVERS, dependencies = arrayOf(StartupTasks.LOAD_PROFILES), executor = {
            LANServerListener.listen()
        })

        taskWorker += Task(identifier = StartupTasks.INITIALIZE_CLI, executor = { CLI.initialize() })

        if (!RunConfiguration.DISABLE_EROS) {
            taskWorker += Task(identifier = StartupTasks.INITIALIZE_JAVAFX, executor = { JavaFXInitializer.start() })
            taskWorker += Task(identifier = StartupTasks.X_START_ON_FIRST_THREAD_WARNING, executor = { XStartOnFirstThreadWarning.show() }, dependencies = arrayOf(StartupTasks.LOAD_LANGUAGE_FILES, StartupTasks.INITIALIZE_JAVAFX))

            taskWorker += Task(identifier = StartupTasks.STARTUP_PROGRESS, executor = { StartingDialog(START_UP_LATCH).show() }, dependencies = arrayOf(StartupTasks.LOAD_LANGUAGE_FILES, StartupTasks.INITIALIZE_JAVAFX))

            Eros::class.java.forceInit()
        }
        taskWorker += Task(identifier = StartupTasks.LOAD_YGGDRASIL, executor = { YggdrasilUtil.load() })



        taskWorker.work(START_UP_LATCH)

        START_UP_LATCH.dec() // remove initial count
        START_UP_LATCH.await()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "All startup tasks executed!" }
        GlobalEventMaster.fireEvent(FinishInitializingEvent())


        RunConfiguration.AUTO_CONNECT_TO?.let { AutoConnect.autoConnect(it) }

        RenderPolling.pollRendering()
    }
}
