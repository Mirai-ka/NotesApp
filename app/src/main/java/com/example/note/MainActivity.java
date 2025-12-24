// MainActivity.java (изменения: шапка, поиск в макете, клик на просмотр, long click на удаление, updateFullList)
package com.example.note;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList;
    private FloatingActionButton addButton;
    private static final String FILE_NAME = "notes.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Делаем статус-бар прозрачным
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Тёмные иконки в статус-баре (если Toolbar тёмный)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Или убери, если хочешь светлые

        // Ключевой код для обработки insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Элементы
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        SearchView searchView = findViewById(R.id.searchView);

        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Поиск
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        loadNotesFromFile();

        setupListeners();

        if (noteList.isEmpty()) {
            noteList.add(new Note("Пример", "Это первая заметка Алёны"));
            adapter.notifyDataSetChanged();
            adapter.updateFullList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_save) {
            saveNotesToFile();
            Toast.makeText(this, "Заметки сохранены", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_clear_all) {
            clearAllNotes();
            return true;
        } else if (id == R.id.menu_exit) {
            saveNotesToFile();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupListeners() {
        addButton.setOnClickListener(v -> showNoteDialog(-1));

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Теперь клик открывает полную заметку
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                intent.putExtra("note", noteList.get(position));
                intent.putExtra("position", position);
                startActivityForResult(intent, 1);  // Для возврата изменений
            }

            @Override
            public void onItemLongClick(int position) {
                showDeleteDialog(position);
            }
        });
    }

    private void showNoteDialog(final int position) {
        // Диалог только для добавления/редактирования (теперь вызывается из ViewNoteActivity или FAB)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(position >= 0 ? "Редактировать заметку" : "Добавить заметку");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null);
        final EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        final EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        if (position >= 0) {
            Note note = noteList.get(position);
            titleEditText.setText(note.getTitle());
            descriptionEditText.setText(note.getDescription());
        }

        builder.setView(dialogView);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (!title.isEmpty()) {
                Note note = new Note(title, description);

                if (position >= 0) {
                    noteList.set(position, note);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Обновлено", Toast.LENGTH_SHORT).show();
                } else {
                    noteList.add(note);
                    adapter.notifyItemInserted(noteList.size() - 1);
                    Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();
                }

                adapter.updateFullList();
                saveNotesToFile();
            } else {
                Toast.makeText(this, "Введите заголовок", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showDeleteDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление")
                .setMessage("Удалить заметку?")
                .setPositiveButton("Да", (dialog, which) -> {
                    noteList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.updateFullList();
                    saveNotesToFile();
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void clearAllNotes() {
        if (noteList.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Очистка")
                .setMessage("Удалить все?")
                .setPositiveButton("Да", (dialog, which) -> {
                    noteList.clear();
                    adapter.notifyDataSetChanged();
                    adapter.updateFullList();
                    saveNotesToFile();
                    Toast.makeText(this, "Очищено", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void saveNotesToFile() {
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for (Note note : noteList) {
                osw.write(note.toFileString() + "\n");
            }

            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNotesFromFile() {
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            if (!file.exists()) return;

            FileInputStream fis = openFileInput(FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Note note = Note.fromFileString(line);
                    if (!note.getTitle().isEmpty()) {
                        noteList.add(note);
                    }
                }
            }

            br.close();
            fis.close();

            adapter.notifyDataSetChanged();
            adapter.updateFullList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            Note updatedNote = (Note) data.getSerializableExtra("updated_note");
            if (position != -1 && updatedNote != null) {
                noteList.set(position, updatedNote);
                adapter.notifyItemChanged(position);
                adapter.updateFullList();
                saveNotesToFile();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNotesToFile();
    }
}