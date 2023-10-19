package com.cerebus.workerthreadexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.util.Objects
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var textView: TextView
    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tasksQue = BlockingDeque()
        textView = findViewById(R.id.textView)


        val workerThread = Thread {
            textView.post { textView.text = "Worker thread started" }
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted) {
                        println("I am interrupted!")
                        return@Thread
                    }
                    val task = tasksQue.getTask()
                    task.run()
                } catch (e: InterruptedException) {
                    textView.post { textView.text = "Worker thread stopped interrupted!" }
                }

            }
            textView.post { textView.text = "Worker thread stopped" }
        }


        val btn1 = findViewById<Button>(R.id.button)
        btn1.setOnClickListener {
            flag = false
            workerThread.start()
        }
        val btn2 = findViewById<Button>(R.id.button1)
        btn2.setOnClickListener {
            flag = true
            workerThread.interrupt()
        }
        val btn3 = findViewById<Button>(R.id.button3)
        btn3.setOnClickListener {
            tasksQue.putTask(generateTask(textView))
        }
    }

     companion object {
         private var taskCounter = 0
         fun generateTask(textView: TextView): Runnable {
             taskCounter++
             return object : Runnable {
                 override fun run() {
                     textView.post { textView.text = "Task ${taskCounter} in progress!" }
                     try {
                         Thread.sleep(1000)
                     } catch (e: InterruptedException) {
                         e.printStackTrace()
                     }
                     textView.post { textView.text = "Task ${taskCounter} is finished!" }
                 }
             }
         }
     }
}


class BlockingDeque() {
    val deque = ArrayDeque<Runnable>()

    @Synchronized
    fun putTask(runnable: Runnable) {
        deque.addLast(runnable)
        (this as? Object)?.notify()
    }

    @Synchronized
    fun getTask(): Runnable {
        while (deque.isEmpty()) {
            try {
                (this as? Object)?.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return deque.removeFirst()
    }
}

