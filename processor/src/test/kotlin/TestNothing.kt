package com.github.rougsig.mviautomock.processor

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.cli.common.output.writeAllTo
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.BindingTrace
import java.io.File

class TestNothing : LightPlatformCodeInsightFixtureTestCase() {
  fun testNothing() {
  }
}

fun compileFileTo(ktFile: KtFile, environment: KotlinCoreEnvironment, output: File): ClassFileFactory =
    compileFilesTo(listOf(ktFile), environment, output)

fun compileFilesTo(files: List<KtFile>, environment: KotlinCoreEnvironment, output: File): ClassFileFactory =
    compileFiles(files, environment).factory.apply {
      writeAllTo(output)
    }

fun compileFile(ktFile: KtFile, environment: KotlinCoreEnvironment): ClassFileFactory =
    compileFiles(listOf(ktFile), environment).factory

fun compileFiles(
    files: List<KtFile>,
    environment: KotlinCoreEnvironment,
    classBuilderFactory: ClassBuilderFactory = ClassBuilderFactories.TEST,
    trace: BindingTrace = NoScopeRecordCliBindingTrace()
): GenerationState =
    compileFiles(files, environment.configuration, classBuilderFactory, environment::createPackagePartProvider, trace)

fun compileFiles(
    files: List<KtFile>,
    configuration: CompilerConfiguration,
    classBuilderFactory: ClassBuilderFactory,
    packagePartProvider: (GlobalSearchScope) -> PackagePartProvider,
    trace: BindingTrace = NoScopeRecordCliBindingTrace()
): GenerationState {
  val analysisResult = analyzeAndCheckForErrors(files.first().project, files, configuration, packagePartProvider, trace)
  analysisResult.throwIfError()

  val state = GenerationState.Builder(
      files.first().project, classBuilderFactory, analysisResult.moduleDescriptor, analysisResult.bindingContext,
      files, configuration
  ).codegenFactory(
      if (configuration.getBoolean(JVMConfigurationKeys.IR)) JvmIrCodegenFactory else DefaultCodegenFactory
  ).build()
  if (analysisResult.shouldGenerateCode) {
    KotlinCodegenFacade.compileCorrectFiles(state, CompilationErrorHandler.THROW_EXCEPTION)
  }

  // For JVM-specific errors
  try {
    AnalyzingUtils.throwExceptionOnErrors(state.collectedExtraJvmDiagnostics)
  } catch (e: Throwable) {
    throw RuntimeException(e)
  }

  return state
}

fun analyzeAndCheckForErrors(
    project: Project,
    files: Collection<KtFile>,
    configuration: CompilerConfiguration,
    packagePartProvider: (GlobalSearchScope) -> PackagePartProvider,
    trace: BindingTrace = CliBindingTrace()
): AnalysisResult {
  for (file in files) {
    try {
      AnalyzingUtils.checkForSyntacticErrors(file)
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  return analyze(project, files, configuration, packagePartProvider, trace).apply {
    try {
      AnalyzingUtils.throwExceptionOnErrors(bindingContext)
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }
}

private fun analyze(
    project: Project,
    files: Collection<KtFile>,
    configuration: CompilerConfiguration,
    packagePartProviderFactory: (GlobalSearchScope) -> PackagePartProvider,
    trace: BindingTrace = CliBindingTrace()
): AnalysisResult {
  return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
      project, files, trace, configuration, packagePartProviderFactory
  )
}
