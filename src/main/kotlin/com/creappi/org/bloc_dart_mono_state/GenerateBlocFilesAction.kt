package com.creappi.org.bloc_dart_mono_state

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.util.Locale

class GenerateBlocFilesAction(private val inputName: String? = null) : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val directory = event.getData(CommonDataKeys.VIRTUAL_FILE)

        // Demande un nom si `inputName` est null
        val textInput = inputName ?: Messages.showInputDialog(
            project,
            "Enter the block name (no spaces allowed):",
            "Generate BLoC Files",
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
                createDartFiles(directory, textInput)
            }
        }
    }

    fun createDartFiles(directory: VirtualFile, text: String) {
        val snakeCaseText = toSnakeCase(text)
        val camelCaseText = toCamelCase(text)

        val fileNames = arrayOf("${snakeCaseText}_bloc.dart", "${snakeCaseText}_event.dart", "${snakeCaseText}_state.dart")
        val fileContents = arrayOf(
            generateBlocContent(camelCaseText),
            generateEventContent(camelCaseText),
            generateStateContent(camelCaseText)
        )

        for (i in fileNames.indices) {
            try {
                val newFile = directory.createChildData(this, fileNames[i])
                VfsUtil.saveText(newFile, fileContents[i])
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private fun toSnakeCase(input: String): String = input.lowercase().replace(" ", "_")
    private fun toCamelCase(input: String): String = input.split(" ").joinToString("") { it ->
        it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }

    private fun generateBlocContent(className: String): String {
        return """
            import 'dart:async';

            import 'package:bloc/bloc.dart';
            import 'package:equatable/equatable.dart';

            part '${className.lowercase(Locale.getDefault())}_event.dart';
            part '${className.lowercase(Locale.getDefault())}_state.dart';

            class ${className}Bloc extends Bloc<${className}Event, ${className}State> {
              ${className}Bloc() : super(const ${className}State()) {
                on<${className}Initial>(_on${className}Initial);
              }

              FutureOr<void> _on${className}Initial(
                ${className}Initial event,
                Emitter<${className}State> emit,
              ) {}
            }
        """.trimIndent()
    }

    private fun generateEventContent(className: String): String {
        return """
            part of '${className.lowercase(Locale.getDefault())}_bloc.dart';

            sealed class ${className}Event extends Equatable {
              const ${className}Event();
              @override
              List<Object> get props => [];
            }

            final class ${className}Initial extends ${className}Event {
              const ${className}Initial();
            }
        """.trimIndent()
    }

    private fun generateStateContent(className: String): String {
        return """
            part of '${className.lowercase(Locale.getDefault())}_bloc.dart';

            class ${className}State extends Equatable {
              const ${className}State();
              @override
              List<Object> get props => [];
            }
        """.trimIndent()
    }
}
