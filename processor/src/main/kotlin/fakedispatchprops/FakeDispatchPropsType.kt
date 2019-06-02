package com.github.rougsig.mvifake.processor.fakedispatchprops

import com.github.rougsig.mvifake.processor.extensions.*
import com.github.rougsig.mvifake.runtime.FakeDispatchProps
import com.google.auto.common.MoreElements.asExecutable
import com.google.common.base.Preconditions.checkElementIndex
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import java.util.stream.Collectors.joining
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementKindVisitor8


internal data class FakeDispatchPropsType(
  val methods: List<MethodType>,
  val isInternal: Boolean,
  val dispatchPropsElement: TypeElement,
  val dispatchPropsName: String,
  val packageName: String
) {
  companion object {
    fun get(env: KotlinProcessingEnvironment, targetElement: TypeElement): FakeDispatchPropsType? {
      val annotation = targetElement.getAnnotationMirror(FakeDispatchProps::class)

      val annotationMirror = annotation!!.getFieldByName("dispatchPropsClass")!!.value as TypeMirror
      val dispatchPropsElement = annotationMirror.asTypeElement(env)

      val metadata = (dispatchPropsElement.kotlinMetadata as? KotlinClassMetadata)?.data ?: return null

      val methods = metadata.classProto.functionList
        .mapNotNull { MethodType.get(env, it, metadata.nameResolver) }
      val isInternal = dispatchPropsElement.isInternal

      return FakeDispatchPropsType(
        methods,
        isInternal,
        dispatchPropsElement,
        targetElement.className.simpleName,
        targetElement.className.packageName
      )
    }
  }
}

private val ELEMENT_TO_STRING = object : ElementKindVisitor8<String, Unit?>() {
  override fun visitExecutable(executableElement: ExecutableElement, aUnit: Unit?): String {
    return enclosingTypeAndMemberName(executableElement)
      .append(
        executableElement.parameters.stream()
          .map { parameter -> parameter.simpleName.toString() }
          .collect(joining(", ", "(", ")")))
      .toString()
  }

  override fun visitVariableAsParameter(parameter: VariableElement, aUnit: Unit?): String {
    val methodOrConstructor = asExecutable(parameter.enclosingElement)
    return enclosingTypeAndMemberName(methodOrConstructor)
      .append('(')
      .append(
        formatArgumentInList(
          methodOrConstructor.parameters.indexOf(parameter),
          methodOrConstructor.parameters.size,
          parameter.simpleName))
      .append(')')
      .toString()
  }

  override fun visitVariableAsField(field: VariableElement, aUnit: Unit?): String {
    return enclosingTypeAndMemberName(field).toString()
  }

  override fun visitType(type: TypeElement, aUnit: Unit?): String {
    return type.qualifiedName.toString()
  }

  protected override fun defaultAction(element: Element, aUnit: Unit?): String {
    throw UnsupportedOperationException(
      "Can't determine string for " + element.getKind() + " element " + element)
  }

  private fun enclosingTypeAndMemberName(element: Element): StringBuilder {
    val name = StringBuilder(element.getEnclosingElement().accept(this, null))
    if (!element.getSimpleName().contentEquals("<init>")) {
      name.append('.').append(element.getSimpleName())
    }
    return name
  }
}


fun formatArgumentInList(index: Int, size: Int, name: CharSequence): String {
  checkElementIndex(index, size)
  val builder = StringBuilder()
  if (index > 0) {
    builder.append("…, ")
  }
  builder.append(name)
  if (index < size - 1) {
    builder.append(", …")
  }
  return builder.toString()
}
