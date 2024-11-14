package com.creappi.org.bloc_dart_mono_state

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile


class GenerateCombinedFilesAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val directory = event.getData(CommonDataKeys.VIRTUAL_FILE)

        // Demande le nom du bloc/page
        val textInput = Messages.showInputDialog(
            project,
            "Enter the name (no spaces allowed):",
            "Generate Combined Files",
            Messages.getQuestionIcon()
        )

        if (textInput.isNullOrEmpty() || textInput.contains(" ")) {
            Messages.showMessageDialog(
                project,
                "The input should not contain spaces. Please enter a single word.",
                "Invalid Input",
                Messages.getErrorIcon()
            )
            return
        }

        if (directory != null) {
            ApplicationManager.getApplication().runWriteAction {
                // Créer le dossier principal
                val mainDir = directory.createChildDirectory(this, toSnakeCase(textInput))

                // Obtenir le chemin relatif au dossier `lib`
                val relativePath = getRelativeLibPath(directory)

                // Créer les sous-dossiers "bloc" et "view"
                val blocDir = mainDir.createChildDirectory(this, "bloc")
                val viewDir = mainDir.createChildDirectory(this, "view")

                // Exécuter GenerateBlocFilesAction et GeneratePageFilesAction
                GenerateBlocFilesAction(textInput).createDartFiles(blocDir, textInput)
                GeneratePageFilesAction(textInput).createPageFiles(viewDir, textInput)
            }
        }
    }

    private fun toSnakeCase(input: String): String = input.lowercase().replace(" ", "_")

    private fun getRelativeLibPath(directory: VirtualFile): String {
        val path = directory.path
        val libIndex = path.indexOf("lib/")
        return if (libIndex != -1) path.substring(libIndex) else ""
    }
}