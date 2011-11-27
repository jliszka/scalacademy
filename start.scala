:power

object state_ {
  var i = 0
  var level = 1
  var prompts: List[Prompt] = Nil
  var local = false

  val rand = new scala.util.Random
  val (id, secret) = findOrGetId()
  val baseUrl = "http://www.scalacademy.com/%%s?id=%d&secret=%d".format(id, secret)

  def curr = prompts(i)
  def next() { i = i + 1 }

  def cleanTypeName(t: String) = t.replaceAll("scala.collection.immutable.", "").replaceAll("java.lang.", "")
  def hasType[T: Manifest](ident: String) = repl.typeForIdent(ident).exists(t => cleanTypeName(t) == cleanTypeName(implicitly[Manifest[T]].toString))
  def hasTypeStr(ident: String, typ: String)(unused: String) = repl.typeForIdent(ident).exists(t => cleanTypeName(t) == cleanTypeName(typ))
  def hasTypeFn(ident: String, args: String, ret: String)(unused: String) = {
    val ok = hasTypeStr(ident, args+ret)(unused)
    if (!ok) {
      println("Oh no, I expected %s to be a function with arguments %s and return type %s!".format(ident, args, ret))
    }
    ok
  }

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
  def eval[T: Manifest](exp: String, expected: T)(unused: String) = {
    val vOpt = repl.interpretExpr(exp)
    vOpt.isDefined && {
      val actualStr = vOpt.get.asInstanceOf[T].toString
      val expectedStr = expected.toString
      if (actualStr == expectedStr) {
        println("%s = %s. Good!".format(exp, actualStr))
      } else {
        println("%s = %s. Oops! Should be '%s'!".format(exp, actualStr, expectedStr))
      }
      actualStr == expectedStr
    }
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
      prompt.foreach(p => 
	println(p.format(lastVar)
		.replaceAll("<<", Console.BOLD)
		.replaceAll(">>", Console.RESET)))
    }
  }

  def eol() {
    println("""You are now a Level %d Scala Coder! To move up to the next level, type 'next' and hit enter.
	    
If you want to stop now and come back later, type ':quit' to exit. When you come back,
type 'level(%d)' instead of 'start'.

At any time you can type 'help' for help remembering what each command does.""".format(level, level+1))
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
    allLines(scan)
  }

  def loadFile(file: String) = {
    scala.io.Source.fromFile(file).getLines.toList
  }

  def loadLevel(l: Int) { 
    level = l
    prompts = parse(if (local) loadFile("levels/"+l) else loadUrl("https://raw.github.com/jliszka/scalacademy/master/levels/"+l))
    i = 0
  }

  def findOrGetId() = {
    import java.io.{File, PrintWriter}
    val idFile = new File("id")
    if (idFile.exists) {
      val parts = loadFile("id").mkString("").trim.split(":")
      (parts(0).toInt, parts(1).toInt)
    } else {
      val secret = math.abs(rand.nextInt() % 10000000)
      val id = loadUrl("http://www.scalacademy.com/id?secret="+secret).mkString("").trim.toInt
      val p = new PrintWriter(idFile)
      p.println(id+":"+secret)
      p.close()
      (id, secret)
    }
  }

  def nextLevel() { 
    level = level + 1
    loadLevel(level)
  }

  def restartLevel() {
    i = 0
  }

  def sendProgress(correct: Boolean) {
    try {
      val c = if (correct) 1 else 0
      val url = "%s&level=%d&step=%s&correct=%d".format(baseUrl.format("inc"), level, i, c)
      new java.net.URL(url).openConnection.getInputStream
    } catch {
      case e =>
    }
  }

  def resume() {
    val url = baseUrl.format("last")
    val parts = loadUrl(url).mkString("").trim.split(":")
    level = parts(0).toInt
    loadLevel(level)
    i = parts(1).toInt + 1
  }

  def check() {
    if (curr.check()) {
      sendProgress(true)
      next()
      println()
      show()
    } else {
      sendProgress(false)
      println()
      println("Oops, try again!")
    }
  }

  def progress() {
    println("You are on prompt %d of %d.".format(i+1, prompts.length))
  }
}

def ok { 
  state_.check()
}

def start {
  state_.loadLevel(1)
  state_.show()
}

def level(l: Int) { 
  state_.loadLevel(l) 
  state_.show()
}

def next { 
  state_.nextLevel()
  state_.show()
}

def restart { 
  state_.restartLevel()
  state_.show()
}

def repeat {
  state_.show()
}

def progress {
  state_.progress()
}

def resume {
  state_.resume()
  state_.show()
}

def help {
  println("""
Using Scalacademy
-------------------------

ok           Check your work and move on to the next prompt.

repeat       Repeat the current prompt.

level(n)     Jump to level n, e.g., level(3).

next         Jump to the next level.

restart      Restart the current level.

progress     Show how far along you are in this level.

resume       Resume from where you last left off (progress is
             saved automatically -- although definitions you made
             in the console are not).

help         This help message.

:quit        Leave the program. When you return to Scalacademy
             you can pick up where you left off by typing 'resume'.

:load file   Read in the contents of a file as if you had
             typed it.

For more help, tweet at @scalacademy on twitter!
""")
}

println("""

  Welcome to Scalacademy!

  To get started, type 'start' at the 'scala>' prompt below and hit enter.

  Type 'help' at any time for help using Scalacademy.
""")
