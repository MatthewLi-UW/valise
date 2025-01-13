package com.valise.mobile.view

//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.combinedClickable
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import com.valise.mobile.controller.Controller
//import com.valise.mobile.entities.Task
//
//// types of actions that our controller understands
//enum class LoginViewEvent {Refresh}
//
//@Composable
//fun TaskList(
//    taskListViewModel: OverviewViewModel,
//    taskListController: Controller
//) {
//    val viewModel by remember { mutableStateOf(taskListViewModel) }
//    val controller by remember { mutableStateOf(taskListController) }
//    var selectedIndex by remember { mutableStateOf(1) }
//    var menuExpanded by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        for (task in viewModel.list) {
////            TaskRow(
////                task, selectedIndex,
////                selected = { selectedIndex = task.index },
////                pressed = { menuExpanded = !menuExpanded })
//        }
//
//        DropdownMenu(
//            expanded = menuExpanded,
//            onDismissRequest = { menuExpanded = false }
//        ) {
//            DropdownMenuItem(
//                text = { Text("Add") },
//                onClick = { controller.invoke(LoginViewEvent.Add, Task(content = "This is a new task")) }
//            )
//            DropdownMenuItem(
//                text = { Text("Edit") },
//                onClick = { controller.invoke(LoginViewEvent.Update, viewModel.list[selectedIndex]) }
//            )
//            DropdownMenuItem(
//                text = { Text("Delete") },
//                onClick = { controller.invoke(LoginViewEvent.Del, viewModel.list[selectedIndex - 1]) }
//            )
//            DropdownMenuItem(
//                text = { Text("Save") },
//                onClick = { controller.invoke(LoginViewEvent.Save, selectedIndex) }
//            )
//        }
//    }
//}
//
//@Composable
//fun RowColor(selected: Boolean) = if (selected) Color.Yellow else Color.Transparent
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun TaskRow(
//    task: Task,
//    index: Int,
//    selected: () -> Unit,
//    pressed: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(RowColor(index == task.index))
//            .combinedClickable(
//                onClick = selected,
//                onLongClick = pressed
//            )
//    ) {
//        Text(
//            text = "[${task.index}] ${task.content}",
//            style = MaterialTheme.typography.bodyLarge
//        )
//    }
//}