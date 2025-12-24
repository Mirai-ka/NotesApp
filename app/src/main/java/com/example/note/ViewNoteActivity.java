// Новая ViewNoteActivity.java
package com.example.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ViewNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);

        TextView titleTextView = findViewById(R.id.viewTitleTextView);
        TextView descriptionTextView = findViewById(R.id.viewDescriptionTextView);
        MaterialButton editButton = findViewById(R.id.editButton);

        Note note = (Note) getIntent().getSerializableExtra("note");
        int position = getIntent().getIntExtra("position", -1);

        if (note != null) {
            titleTextView.setText(note.getTitle());
            descriptionTextView.setText(note.getDescription());
        }

        editButton.setOnClickListener(v -> {
            // Открываем диалог редактирования из MainActivity, но возвращаем результат
            Intent intent = new Intent();
            // Здесь можно открыть диалог, но проще вернуть в Main и там редактировать
            // Альтернатива: Реализовать диалог здесь и вернуть обновленную заметку
            showEditDialog(position, note);
        });
    }

    private void showEditDialog(int position, Note currentNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать заметку");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_note, null);
        EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        titleEditText.setText(currentNote.getTitle());
        descriptionEditText.setText(currentNote.getDescription());

        builder.setView(dialogView);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (!title.isEmpty()) {
                Note updatedNote = new Note(title, description);
                Intent result = new Intent();
                result.putExtra("updated_note", updatedNote);
                result.putExtra("position", position);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}