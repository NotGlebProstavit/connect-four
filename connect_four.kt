package connectfour


fun validateCmd(name: String): Int{
    while(true){
        val s = readCmd(name)
        when{
            s == "end" || s == "End" -> return -1
            Regex("\\d+").matches(s) -> return s.toInt()
            else -> {
                println("Incorrect column number")
            }
        }
    }
}

fun readName(order: String): String{
    println("$order player's name:")
    return readLine()!!
}

fun readCmd(name: String): String{
    println("$name's turn:")
    return readLine()!!
}

fun inputDimensions(): String{
    println("Set the board dimensions (Rows x Columns)")
    println("Press Enter for default (6 x 7)")
    return readLine()!!
}

fun printError(mes: String){
    println("Board $mes should be from 5 to 9")
}

fun validateInput(): String{
    var s = inputDimensions()
    if (s == "") return "6x7"

    while(true){
        when (isValid(s)){
            0 -> return s
            1 -> println("Invalid input")
            2 -> printError("rows")
            3 -> printError("columns")
        }

        s = inputDimensions()
    }
}

fun inRange(n: Int) = n in 5..9


fun isValid(s: String): Int{
    val listS = s.split(" ", "\t", "x", "X").filter{ it != ""}
    if(listS.size != 2) return 1
    val (rows, columns) = listS.map { it.toInt() }.toMutableList()
    if (!inRange(rows)) return 2
    if (!inRange(columns)) return 3
    return 0
}

class Point(
    val row: Int,
    val column: Int
): Comparable<Point> {
    override fun equals(other: Any?): Boolean {
        val p = other as Point
        return p.row==row && p.column==column
    }

    override fun hashCode(): Int {
        var result = row
        result = 31 * result + column
        return result
    }

    override fun compareTo(other: Point): Int {
        if(this == other) return 0

        return if(row < other.row) -1
        else if (row > other.row) 1
        else{
            if(column < other.column) -1
            else if (column > other.column) 1
            else 0
        }
    }

    override fun toString(): String {
        return "($row,$column)"
    }
}

class Player(
    val name: String,
    val symbol: String
){
    var score: Int=0
    val turns: MutableList<Point> = mutableListOf()

    fun addTurn(p: Point){
        this.turns.add(p)
    }
    fun increaseScore(score: Int){
        this.score+=score
    }

    override fun toString(): String {
        var s = ""
        for(point in turns) s += "$point; "
        return s;
    }
}

class Game(
    private val rows: Int,
    private val columns: Int,
    private val p1: Player,
    private val p2: Player,
    val countStage: Int
){
    var field: MutableList<MutableList<Boolean>> = mutableListOf()

    init {
        for(i in 0 until columns){
            field.add(mutableListOf())
            for(j in 0 until rows){
                field[i].add(false)
            }
        }
    }

    fun emptyField(){
        for(i in 0 until columns){
            for(j in 0 until rows){
                field[i][j] = false
            }
        }

        p1.turns.clear()
        p2.turns.clear()
    }

    fun drawField(){
        for (i in 1..columns){
            print(" $i")
        }
        println(" ")

        for(i in 1..rows){
            for (j in 1..columns){
                when (Point(i, j)) {
                    in p1.turns -> print("║${p1.symbol}")
                    in p2.turns -> print("║${p2.symbol}")
                    else -> print("║ ")
                }
            }
            println("║")
        }

        print("╚")
        for(i in 1 until columns){
            print("═╩")
        }
        println("═╝")
    }

    fun gameLogic(){
        var firstPlayer = 1
        for(i in 1..countStage) {
            var playerNumber = firstPlayer
            if(countStage > 1)println("Game #$i")
            drawField()
            var s = validateCmd(if(firstPlayer == 1) p1.name else p2.name)
            loop@ while (s != -1) {
                if (s in 1..columns) {
                    val count = field[s - 1].count { it }
                    if (count != rows) {
                        if (playerNumber == 1)
                            p1.addTurn(Point(rows - count, s))
                        else
                            p2.addTurn(Point(rows - count, s))
                        field[s - 1][rows - 1 - count] = true
                        drawField()

                        val w = win(playerNumber)
                        if (w == 1) {
                            if (playerNumber == 1) p1.increaseScore(2)
                            else p2.increaseScore(2)
                            println("Player ${if (playerNumber == 1) p1.name else p2.name} won")
                        } else if (w == -1) {
                            p1.increaseScore(1)
                            p2.increaseScore(1)
                            println("It is a draw")
                        }
                        if (w != 0){
                            if(firstPlayer == 1) firstPlayer = -1
                            else if(firstPlayer == -1) firstPlayer = 1
                            if(countStage > 1)println("Score\n${p1.name}: ${p1.score} ${p2.name}: ${p2.score}")
                            emptyField()
                            break@loop
                        }
                        playerNumber *= -1
                    } else {
                        println("Column $s is full")
                    }
                } else {
                    println("The column number is out of range (1 - $columns)")
                }
                s = validateCmd(if (playerNumber == 1) p1.name else p2.name)
            }
        }
        println("Game over!")
    }

    private fun win(playerNumber: Int): Int{
        val turns = if(playerNumber == 1) p1.turns else p2.turns
        for(i in 0 until turns.size){
            if(Point(turns[i].row, turns[i].column+1) in turns &&
                Point(turns[i].row, turns[i].column+2) in turns &&
                Point(turns[i].row, turns[i].column+3) in turns) return 1

            if(Point(turns[i].row+1, turns[i].column) in turns &&
                Point(turns[i].row+2, turns[i].column) in turns &&
                Point(turns[i].row+3, turns[i].column) in turns) return 1

            if(Point(turns[i].row-1, turns[i].column+1) in turns &&
                Point(turns[i].row-2, turns[i].column+2) in turns &&
                Point(turns[i].row-3, turns[i].column+3) in turns) return 1
        }
        var isDraw = true
        for(i in 0 until columns){
            if(field[i].count { it } != columns){
                isDraw = false
                break
            }
        }
        return if (isDraw) -1 else 0
    }
}

fun validInputCount(): Int{
    while (true){
        println("Do you want to play single or multiple games?\n" +
                "For a single game, input 1 or press Enter\n" +
                "Input a number of games:")
        val a = readLine()!!
        try{
            val count = a.toInt()
            if(count > 0) return count
        }catch(e: NumberFormatException){
            if(a == "") return 1
        }
        println("Invalid input")
    }
}

fun main() {
    println("Connect Four")
    val firstName: String = readName("First")
    val secondName: String = readName("Second")

    val s = validateInput()

    val (rows, columns) = s
        .split(" ", "\t", "x", "X")
        .filter{ it != ""}
        .map{it.toInt()}
        .toMutableList()

    val countStage = validInputCount()

    val game = Game(rows, columns, Player(firstName, "o"), Player(secondName, "*"), countStage)

    println("$firstName VS $secondName")
    println("$rows X $columns board")

    if(countStage == 1) println("Single game")
    else println("Total $countStage games")
    game.gameLogic()
}
