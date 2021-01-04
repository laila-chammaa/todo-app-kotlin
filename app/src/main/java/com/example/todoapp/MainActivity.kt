package com.example.todoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var todoAdapter : TodoAdapter

    private val todosCollectionReference = Firebase.firestore.collection("todos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //if there is previous data, it will show that in the todoAdapter, otherwise, it'll show an empty list
        todoAdapter = TodoAdapter(mutableListOf())
        retrieveTodosDatabase()

        rvTodoItems.adapter = todoAdapter
        rvTodoItems.layoutManager = LinearLayoutManager(this)

        //on click listeners for our two buttons
        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                todoAdapter.addTodo(todo)
                etTodoTitle.text.clear()
                //adding to the database
                addTodoDatabase(todo)
            }
        }

        btnDeleteDone.setOnClickListener {
            todoAdapter.deleteDoneTodos()
            deleteTodoDatabase()
        }
    }

    private fun addTodoDatabase(todo : Todo) = CoroutineScope(Dispatchers.IO).launch {
        try {
            todosCollectionReference.add(todo).await()
            //this code will only run after the add function is finished because of the await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully saved data", Toast.LENGTH_SHORT).show()
            }
        } catch (e : Exception) {
            //switching to the main context to show the error in a toast
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveTodosDatabase() = CoroutineScope(Dispatchers.Main).launch {
        try {
            val querySnapshot = todosCollectionReference.get().await()
            //this code will only run after the add function is finished because of the await()
            val todoList = mutableListOf<Todo>()
            for (document in querySnapshot.documents) {
                val todo = document.toObject(Todo::class.java)
                if (todo != null) {
                    todoList.add(todo)
                    todoAdapter.addTodo(todo)
                }
            }
        } catch (e : Exception) {
            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteTodoDatabase() = CoroutineScope(Dispatchers.IO).launch {
        try {
            //creating a query that'll return all the tasks that have "checked" equal to true
            val tasksQuery = todosCollectionReference.whereEqualTo("checked", true).get().await()

            //this code will only run after the add function is finished because of the await()
            if (tasksQuery.documents.isNotEmpty()) {
                for (document in tasksQuery) {
                    try {
                        todosCollectionReference.document(document.id).delete().await()
                    } catch (e : Exception) {
                        //switching to the main context to show the error in a toast
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully deleted data", Toast.LENGTH_SHORT).show()
            }
        } catch (e : Exception) {
            //switching to the main context to show the error in a toast
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}