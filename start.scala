:power

object state_ {
  var i = 0
  var level = 1
  var prompts: List[Prompt] = Nil
  var local = false

  def curr = prompts(i)
  def next() { i = i + 1 }

  def hasType[T: Manifest](ident: String) = repl.typeForIdent(ident).exists(_ == implicitly[Manifest[T]].toString)
  def hasTypeStr(ident: String, typ: String)(unused: String) = repl.typeForIdent(ident).exists(_ == typ)
  def hasValue[T: Manifest](v: T)(ident: String) = hasType[T](ident) && {
    val vOpt = repl.interpretExpr(ident)
    vOpt.isDefined && vOpt.get.asInstanceOf[T] == v
  }
  def cond[T: Manifest](pred: T => Boolean)(ident: String) = hasType[T](ident) && {
    val vOpt = repl.interpretExpr(ident)
    vOpt.isDefined && pred(vOpt.get.asInstanceOf[T])
  }
  def declared[T: Manifest](ident: String, v: T)(unused: String) = hasValue[T](v)(ident)
  def declared[T: Manifest](ident: String)(unused: String) = hasType[T](ident)
  def eval[T: Manifest](exp: String, v: T)(unused: String) = {
    val vOpt = repl.interpretExpr(exp)
    vOpt.isDefined && vOpt.get.asInstanceOf[T] == v
  }

  var lastVar = "res0"

  case class Prompt(prompt: List[String], checks: List[String]) {
    def check() = {
      lastVar = repl.mostRecentVar
      checks.forall(c => {
	val cmd = "state_."+c+"(\""+lastVar+"\")"
	repl.interpretExpr(cmd).get.asInstanceOf[Boolean]
      })
    }
    def show() = {
      prompt.foreach(p => println(p.format(lastVar)))
    }
  }

  def eol() {
    println("You are now a Level %d coder! To move up to the next level, type 'next' and hit enter.".format(level))
    println()
    println("If you want to stop now and come back later, type ':quit' to exit. When you come back,")
    println("type 'level(%d)' instead of 'start'.".format(level+1))
  }

  def show() {
    if (i < prompts.length) curr.show() else eol()
  }

  def parse(lines: List[String]): List[Prompt] = {
    if (lines.isEmpty) Nil
    else {
      val first = lines.takeWhile(_ != "===")
      val rest = lines.drop(first.length+1)
      val text = first.takeWhile(_ != "---")
      val checks = first.drop(text.length+1)
      Prompt(text, checks) :: parse(rest)
    }
  }

  def loadUrl(url: String) = {
    val stream = new java.net.URL(url).openConnection.getInputStream
    val scan = new java.util.Scanner(stream)
    def allLines(s: java.util.Scanner): List[String] = if (s.hasNext) s.nextLine :: allLines(s) else Nil
    parse(allLines(scan))
  }

  def loadFile(file: String) = {
    parse(scala.io.Source.fromFile(file).getLines.toList)
  }

  def loadLevel(l: Int) { 
    level = l
    prompts = if (local) loadFile("levels/"+l) else loadUrl("https://raw.github.com/jliszka/scalacademy/master/levels/"+l)
    i = 0
    show()
  }

  def nextLevel() { 
    level = level + 1
    loadLevel(level) 
  }

  def restartLevel() {
    i = 0
    show()
  }

  def check() {
    if (curr.check()) {
      next()
      println()
      show()
    } else {
      println()
      println("Oops, try again!")
    }
  }
}

def ok { 
  state_.check()
}

def start {
  state_.loadLevel(1)
}

def level(l: Int) { 
  state_.loadLevel(l) 
}

def next { 
  state_.nextLevel()
}

def restart { 
  state_.restartLevel()
}

def repeat {
  state_.show()
}
