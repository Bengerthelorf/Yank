package homes.snaix.app.yank.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPrimarySheet(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var value by remember { mutableStateOf(initial) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.edit_primary_header),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.edit_primary_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.edit_primary_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                Button(
                    onClick = {
                        onSave(value)
                        onDismiss()
                    },
                    enabled = value.isNotBlank() && value != initial,
                ) { Text(stringResource(R.string.action_save)) }
            }
        }
    }
}
