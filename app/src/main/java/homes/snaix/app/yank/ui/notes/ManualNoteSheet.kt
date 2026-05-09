package homes.snaix.app.yank.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import homes.snaix.app.yank.R

data class NoteDraft(
    val title: String = "",
    val body: String = "",
    val date: String = "",
    val time: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualNoteSheet(
    onDismiss: () -> Unit,
    onSave: (title: String?, body: String, date: String?, time: String?) -> Unit,
    initial: NoteDraft = NoteDraft(),
    headerRes: Int = R.string.note_new,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initial.title) }
    var body by remember { mutableStateOf(initial.body) }
    var date by remember { mutableStateOf(initial.date) }
    var time by remember { mutableStateOf(initial.time) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(headerRes))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.note_title_optional)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text(stringResource(R.string.note_body)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(R.string.note_date_label)) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text(stringResource(R.string.note_time_label)) }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                Button(onClick = {
                    onSave(title.ifBlank { null }, body, date.ifBlank { null }, time.ifBlank { null })
                    onDismiss()
                }, enabled = body.isNotBlank()) { Text(stringResource(R.string.action_save)) }
            }
        }
    }
}
