package compaas.core.engine

import org.graalvm.polyglot.*
import java.io.File

object PolyglotContext {
  def apply() = new PolyglotContext(Context.create())
  def apply(langId: LanguageId) = new PolyglotContext(
    Context.create(langId.codename)
  )

  def newBuilder() = new PolyglotContextBuilder(Context.newBuilder())
  class PolyglotContextBuilder(private val builder: Context#Builder):
    def build(): PolyglotContext = new PolyglotContext(builder.build())

}

class PolyglotContext(val context: Context) extends AutoCloseable {
  def evalModule(langInfo: LanguageInfo, code: String, moduleName: String): Module = {
    val source = Source.newBuilder(langInfo.id.codename, code, moduleName).build

    new Module(context.eval(source))
  }

  def evalModule(langInfo: LanguageInfo, codeFile: File): Module = {
    val source = Source.newBuilder(langInfo.id.codename, codeFile).build

    new Module(context.eval(source))
  }

  override def close(): Unit = context.close
}
