package com.creappi.org.bloc_dart_mono_state

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

class GeneratePageFilesAction(private val inputName: String? = null) : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val directory = event.getData(CommonDataKeys.VIRTUAL_FILE)

        // Demande un nom si `inputName` est null
        val textInput = inputName ?: Messages.showInputDialog(
            project,
            "Enter the page name (no spaces allowed):",
            "Generate Page Files",
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
                createPageFiles(directory, textInput)
            }
        }
    }

    fun createPageFiles(directory: VirtualFile, text: String) {
        val snakeCaseText = toSnakeCase(text)
        val camelCaseText = toCamelCase(text)

        val fileNames = arrayOf("${snakeCaseText}_page.dart", "${snakeCaseText}_view.dart")
        val fileContents = arrayOf(
            generatePageContent(camelCaseText, snakeCaseText),
            generateViewContent(camelCaseText)
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
    private fun toCamelCase(input: String): String = input.split(" ").joinToString("") { it.capitalize() }

    private fun generatePageContent(className: String, snakeCaseName: String): String {
        return """
            import 'package:flutter/material.dart';
            import '${snakeCaseName}_view.dart';

            class ${className}Page extends StatelessWidget {
              const ${className}Page({super.key});

              static Page<dynamic> page() => const MaterialPage<dynamic>(
                    child: ${className}Page(),
                  );

              static Route<dynamic> route() {
                return MaterialPageRoute<void>(
                  builder: (_) => const ${className}Page(),
                );
              }

              @override
              Widget build(BuildContext context) {
                return const ${className}View();
              }
            }
        """.trimIndent()
    }

    private fun generateViewContent(className: String): String {
        return """
            import 'package:flutter/material.dart';

            class ${className}View extends StatelessWidget {
              const ${className}View({super.key});

              @override
              Widget build(BuildContext context) {
                return Container();
              }
            }
        """.trimIndent()
    }
}
