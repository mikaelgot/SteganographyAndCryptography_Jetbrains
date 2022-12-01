package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.system.exitProcess

val baseDir = ""

fun main() {
    while(true) {
        println("Task (hide, show, exit):")
        val input = readln().trim()
        if (input == "exit") {
            println("Bye!")
            exitProcess(0)
        } else if (input == "hide") {
            try {
                hide()
            } catch (e: Exception) {
                println("Can't read input file!")
                continue
            }
            //println("Hiding message in image.")
            continue
        }
        else if (input == "show") {
            show()
            continue
        }
        else {
            println("Wrong task: $input")
            continue
        }
    }
}
fun show() {
    println("Input image file:")
    val opFile = File(baseDir + readln().trim())
    println("Password:")
    val pass = readln().trim().toByteArray()
    val opImage = ImageIO.read(opFile)
    val wholeMessage = mutableListOf<Byte>()
    var count = 0
    var msg = 0
    loop@ for (y in 0 until opImage.height) {
        for (x in 0 until opImage.width) {
            var mask = 1
            val i = Color(opImage.getRGB(x, y))
            val medium = i.blue and mask //only last bit set all other zeros
            msg = msg shl 1
            msg = medium or msg
            if (count < 7) count++
            else {
                wholeMessage.add(msg.toByte())
                if (wholeMessage.takeLast(3) == listOf<Byte>(0, 0, 3)) {
                    println("Message:")
                    val decrypted = decrypt(wholeMessage.toByteArray(), pass)
                    println(decrypted.decodeToString())
                    break@loop
                }
                count = 0
                msg = 0
            }
        }
    }
}

fun intAs8bitBinary(int: Int): String {
    return String.format("%8s", int.toString(2)).replace(" ", "0")
}
fun encrypt(message: ByteArray, pass: ByteArray): ByteArray {
    val encrypted = ByteArray(message.size)
    for (i in message.indices) {
        encrypted[i] = message[i] xor pass[i % pass.size]
    }
    return encrypted + listOf<Byte>(0, 0, 3).toByteArray()
}
fun decrypt(encrypted: ByteArray, pass: ByteArray): ByteArray {
    val decrypted = ByteArray(encrypted.size - 3)
    for (i in decrypted.indices) {
        decrypted[i] = encrypted[i] xor pass[i % pass.size]
    }
    return decrypted
}

fun hide() {
    println("Input image file:")
    val inFile = File(baseDir  + readln().trim())
    println("Output image file:")
    val outFile = File(baseDir  + readln().trim())
    //println("Input Image: ${inFile.path.replace(File.separator, "/")}")
    //println("Output Image: ${outFile.path.replace(File.separator, "/")}")
    val inImage = ImageIO.read(inFile)
    //println("inImage width: ${inImage.width}, height: ${inImage.height}")
    println("Message to hide:")
    val message = readln().trim().toByteArray()



    println("Password:")
    val pass = readln().trim().toByteArray()
    val encrypted = encrypt(message, pass)
    val encryptedBinString = encrypted.joinToString(separator = "", transform = { intAs8bitBinary(it.toInt()) })
    //println("message: $message, msgBinString: $encryptedBinString, length: ${encryptedBinString.length}")

    val imageByteCapacity = (inImage.width * inImage.height) / 8
    if (encrypted.size > imageByteCapacity) {
        println("The input image is not large enough to hold this message.")
    }
    else {
        val outImage = BufferedImage(inImage.width, inImage.height, inImage.type)
        for (y in 0 until inImage.height) {
            for (x in 0 until inImage.width) {
                val i = Color(inImage.getRGB(x, y))
                var color = Color(i.red, i.green, i.blue)

                val countPixels = y * inImage.width + x
                if (countPixels < encryptedBinString.length) {
                    val lastBit = encryptedBinString[countPixels]
                    val blue = if (lastBit == '0') i.blue and 254 else i.blue or 1
                    //println("Blue before: ${intAs8bitBinary(i.blue)}")
                    //println("Blue after : ${intAs8bitBinary(blue)}")
                    //println("Hiding message in $x,$y: msgBinString length: ${msgBinString.length}, char: ${msgBinString[countPixels]}")
                    color = Color(i.red, i.green, blue)
                    //color = Color(0, 0, 0)
                }
                    //val color = Color(i.red, i.green, blue)
                //println("output pixel $x,$y, countPixels = $countPixels: red: ${color.red}, green: ${color.green}, blue: ${color.blue}")
                outImage.setRGB(x, y, color.rgb)
            }
        }
        try {
            ImageIO.write(outImage, "png", outFile)
        } catch (e: Exception) {
            println("Error in writing!")
        }
        println("Message saved in ${outFile/*.path.replace(File.separator, "/")*/} image.")
    }
}

