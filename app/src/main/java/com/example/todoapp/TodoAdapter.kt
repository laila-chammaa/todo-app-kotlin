package com.example.todoapp

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_todo.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TodoAdapter(
    private val todos: MutableList<Todo>
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    //an inner class that inherits from recyclerView.ViewHolder
    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private val todosCollectionReference = Firebase.firestore.collection("todos")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_todo,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val curTodo = todos[position]
        holder.itemView.apply {
            tvTodoTitle.text = curTodo.title
            cbDone.isChecked = curTodo.checked
            toggleStrikeThrough(tvTodoTitle, curTodo.checked)
            cbDone.setOnCheckedChangeListener { _, checked ->
                toggleStrikeThrough(tvTodoTitle, checked)
                updateCheckedDatabase(curTodo)
                curTodo.checked = !curTodo.checked
            }
        }
    }

    fun addTodo(todo: Todo) {
        todos.add(todo)
        notifyItemInserted(todos.size - 1)
    }

    fun deleteDoneTodos() {
        todos.removeAll{todo ->
            todo.checked
        }
        notifyDataSetChanged()
    }

    private fun toggleStrikeThrough(tvTodoTitle : TextView, checked: Boolean) {
        if (checked) {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
        } else {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    //needed in the todoAdapter since it needs to be called in the cbDone.setOnCheckedChangeListener
    private fun updateCheckedDatabase(todo : Todo) = CoroutineScope(Dispatchers.IO).launch {
        val tasksQuery = todosCollectionReference
            .whereEqualTo("title", todo.title)
            .whereEqualTo("id", todo.id)
            .get().await()
        //this code will only run after the add function is finished because of the await()
        if (tasksQuery.documents.isNotEmpty()) {
            //it should just return one, unless we are very unlucky with two of the same id
            try {
                todosCollectionReference.document(tasksQuery.documents[0].id).update("checked", todo.checked)
            } catch (e : Exception) {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                Log.d("Updating a task error:", e.message)
            }
        } else {
            Log.d("Updating a task error:", "No task matched the query")
        }
    }
}