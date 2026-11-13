
package com.wordsuperlite;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private EditText editor;
    private TextView status;

    private String currentName = "Dokumen-1.docx";

    private static final int PICK_OPEN = 10;
    private static final int PICK_SAVE = 11;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        buildInterface();
        updateStatus();
    }

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private void buildInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(238, 239, 241));

        // TITLE BAR
        TextView title = new TextView(this);
        title.setText("Word Super Lite     ▾     " + currentName);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, dp(12), 0);
        title.setBackgroundColor(Color.rgb(47, 91, 150));

        root.addView(title,
                new LinearLayout.LayoutParams(-1, dp(42)));

        // RIBBON TABS
        HorizontalScrollView tabsScroll =
                new HorizontalScrollView(this);

        tabsScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);

        String[] names = {
                "File", "Home", "Insert", "Layout",
                "References", "Review", "View"
        };

        for (final String name : names) {
            Button button = tabButton(name);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTab(name);
                }
            });

            tabs.addView(button);
        }

        tabsScroll.addView(tabs);

        root.addView(tabsScroll,
                new LinearLayout.LayoutParams(-1, dp(42)));

        // RIBBON TOOLBAR
        HorizontalScrollView ribbonScroll =
                new HorizontalScrollView(this);

        ribbonScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout ribbon = new LinearLayout(this);
        ribbon.setGravity(Gravity.CENTER_VERTICAL);
        ribbon.setPadding(dp(4), dp(3), dp(4), dp(3));
        ribbon.setBackgroundColor(Color.WHITE);

        addAction(ribbon, "B", new Runnable() {
            @Override
            public void run() {
                applyStyle(Typeface.BOLD);
            }
        });

        addAction(ribbon, "I", new Runnable() {
            @Override
            public void run() {
                applyStyle(Typeface.ITALIC);
            }
        });

        addAction(ribbon, "U", new Runnable() {
            @Override
            public void run() {
                applyUnderline();
            }
        });

        addAction(ribbon, "−", new Runnable() {
            @Override
            public void run() {
                changeSize(-1);
            }
        });

        addAction(ribbon, "11", new Runnable() {
            @Override
            public void run() {
                changeSize(1);
            }
        });

        addAction(ribbon, "A+", new Runnable() {
            @Override
            public void run() {
                changeSize(2);
            }
        });

        addAction(ribbon, "Left", new Runnable() {
            @Override
            public void run() {
                editor.setGravity(Gravity.TOP | Gravity.LEFT);
            }
        });

        addAction(ribbon, "Center", new Runnable() {
            @Override
            public void run() {
                editor.setGravity(
                        Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            }
        });

        addAction(ribbon, "Right", new Runnable() {
            @Override
            public void run() {
                editor.setGravity(Gravity.TOP | Gravity.RIGHT);
            }
        });

        addAction(ribbon, "Justify", new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,
                        "Justify akan ditambahkan pada mesin tata letak",
                        Toast.LENGTH_SHORT).show();
            }
        });

        ribbonScroll.addView(ribbon);

        root.addView(ribbonScroll,
                new LinearLayout.LayoutParams(-1, dp(54)));

        // RULER
        HorizontalScrollView rulerScroll =
                new HorizontalScrollView(this);

        TextView ruler = new TextView(this);

        ruler.setText(
                "  0      1      2      3      4      5      " +
                "6      7      8      9      10      11      " +
                "12      13      14"
        );

        ruler.setTextColor(Color.DKGRAY);
        ruler.setTextSize(11);
        ruler.setPadding(dp(12), dp(5), dp(12), dp(5));
        ruler.setBackgroundColor(Color.rgb(247, 247, 247));

        rulerScroll.addView(ruler);

        root.addView(rulerScroll,
                new LinearLayout.LayoutParams(-1, dp(25)));

        // DOCUMENT WORKSPACE
        FrameLayout workspace = new FrameLayout(this);

        workspace.setBackgroundColor(Color.rgb(224, 226, 229));

        ScrollView pageScroll = new ScrollView(this);
        pageScroll.setFillViewport(false);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.WHITE);

        page.setPadding(dp(34), dp(42), dp(34), dp(42));

        // Elevation hanya tersedia pada Android 5.0 ke atas.
        if (Build.VERSION.SDK_INT >= 21) {
            page.setElevation(dp(2));
        }

        // TEXT EDITOR
        editor = new EditText(this);

        editor.setTextSize(16);
        editor.setTextColor(Color.BLACK);
        editor.setHint("Mulai mengetik dokumen Anda...");
        editor.setGravity(Gravity.TOP | Gravity.LEFT);
        editor.setBackgroundColor(Color.TRANSPARENT);
        editor.setPadding(0, 0, 0, 0);
        editor.setMinLines(28);

        editor.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        page.addView(editor,
                new LinearLayout.LayoutParams(-1, -2));

        pageScroll.addView(page);

        FrameLayout.LayoutParams pageParams =
                new FrameLayout.LayoutParams(-1, -1);

        pageParams.setMargins(
                dp(14), dp(12), dp(14), dp(12));

        workspace.addView(pageScroll, pageParams);

        root.addView(workspace,
                new LinearLayout.LayoutParams( -1, 0, 1));

        // STATUS BAR
        status = new TextView(this);

        status.setTextColor(Color.DKGRAY);
        status.setTextSize(11);
        status.setPadding(dp(10), dp(7), dp(10), dp(7));
        status.setBackgroundColor(Color.rgb(248, 248, 248));

        root.addView(status,
                new LinearLayout.LayoutParams(-1, dp(30)));

        editor.addTextChangedListener(
                new android.text.TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                updateStatus();
            }

            @Override
            public void afterTextChanged(
                    android.text.Editable editable) {
            }
        });

        setContentView(root);
    }

    private Button tabButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(12);
        button.setTextColor(Color.rgb(35, 65, 105));
        button.setAllCaps(false);
        button.setPadding(dp(10), 0, dp(10), 0);

        return button;
    }

    private void addAction(
            LinearLayout row,
            String label,
            final Runnable action) {

        Button button = new Button(this);

        button.setText(label);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setMinWidth(dp(42));
        button.setPadding(dp(5), 0, dp(5), 0);

        row.addView(button,
                new LinearLayout.LayoutParams(-2, dp(44)));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
            }
        });
    }

    // RIBBON TAB ACTIONS
    private void showTab(String tab) {

        if ("File".equals(tab)) {

            new AlertDialog.Builder(this)
                    .setTitle("File")
                    .setItems(
                            new String[]{
                                    "New",
                                    "Open DOCX",
                                    "Save DOCX",
                                    "Save as DOCX"
                            },
                            (dialog, position) -> {

                                if (position == 0) {
                                    newDocument();
                                } else if (position == 1) {
                                    openDocument();
                                } else {
                                    saveDocument(position == 3);
                                }
                            })
                    .show();

        } else if ("Home".equals(tab)) {

            Toast.makeText(this,
                    "Home: pemformatan dasar tersedia di ribbon.",
                    Toast.LENGTH_SHORT).show();

        } else {

            String message;

            if ("Insert".equals(tab)) {
                message = "Fitur berikutnya: tabel, gambar, page break.";
            } else if ("Layout".equals(tab)) {
                message = "Fitur berikutnya: ukuran kertas, margin, orientasi.";
            } else if ("References".equals(tab)) {
                message = "Fitur berikutnya: daftar isi dan catatan kaki.";
            } else if ("Review".equals(tab)) {
                message = "Kata: " + wordCount() +
                        ". Pemeriksaan ejaan menyusul.";
            } else {
                message = "Tampilan halaman dan zoom akan dikembangkan.";
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    // TEXT FORMATTING
    private void applyStyle(int style) {

        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();

        if (start == end) {
            Toast.makeText(this,
                    "Pilih teks terlebih dahulu.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        android.text.Editable editable = editor.getText();

        editable.setSpan(
                new StyleSpan(style),
                Math.min(start, end),
                Math.max(start, end),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void applyUnderline() {
        int start = Math.min(editor.getSelectionStart(), editor.getSelectionEnd());
        int end = Math.max(editor.getSelectionStart(), editor.getSelectionEnd());
        if (start == end) {
            Toast.makeText(this, "Pilih teks yang akan digarisbawahi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        android.text.Editable editable = editor.getText();
        UnderlineSpan[] spans = editable.getSpans(start, end, UnderlineSpan.class);
        boolean alreadyUnderlined = false;
        for (UnderlineSpan span : spans) {
            if (editable.getSpanStart(span) <= start && editable.getSpanEnd(span) >= end) {
                alreadyUnderlined = true;
                editable.removeSpan(span);
                break;
            }
        }
        if (!alreadyUnderlined) {
            editable.setSpan(new UnderlineSpan(), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void changeSize(int delta) {

        float size = editor.getTextSize() /
                getResources().getDisplayMetrics().scaledDensity;

        editor.setTextSize(
                Math.max(8, Math.min(36, size + delta))
        );
    }

    // WORD COUNTER
    private int wordCount() {

        String text = editor.getText().toString().trim();

        return text.length() == 0
                ? 0
                : text.split("\\s+").length;
    }

    private void updateStatus() {

        if (status != null) {
            status.setText(
                    "Halaman 1     " +
                    wordCount() +
                    " kata     Bahasa: Indonesia     100%"
            );
        }
    }

    // NEW DOCUMENT
    private void newDocument() {

        editor.setText("");
        currentName = "Dokumen-baru.docx";

        Toast.makeText(this,
                "Dokumen baru dibuat.",
                Toast.LENGTH_SHORT).show();
    }

    // OPEN DOCX
    private void openDocument() {

        Intent intent;

        if (Build.VERSION.SDK_INT >= 19) {

            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

        } else {

            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        try {

            startActivityForResult(
                    Intent.createChooser(intent, "Buka dokumen DOCX"),
                    PICK_OPEN
            );

        } catch (Exception exception) {

            Toast.makeText(this,
                    "Tidak ada aplikasi pemilih file yang tersedia.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // SAVE DOCX
    private void saveDocument(boolean saveAs) {

        if (Build.VERSION.SDK_INT >= 19) {

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            intent.addCategory(Intent.CATEGORY_OPENABLE);

            intent.setType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            intent.putExtra(Intent.EXTRA_TITLE, currentName);

            try {

                startActivityForResult(intent, PICK_SAVE);

            } catch (Exception exception) {

                Toast.makeText(this,
                        "Pemilih lokasi penyimpanan tidak tersedia.",
                        Toast.LENGTH_LONG).show();
            }

        } else {

            saveLegacy();
        }
    }

    // LEGACY STORAGE FOR ANDROID 4.2
    private void saveLegacy() {

        try {

            File directory = new File(
                    Environment.getExternalStorageDirectory(),
                    "WordSuperLite"
            );

            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException(
                        "Folder penyimpanan tidak dapat dibuat."
                );
            }

            File output = new File(directory, currentName);

            if (!output.getName().toLowerCase().endsWith(".docx")) {
                output = new File(
                        directory,
                        output.getName() + ".docx"
                );
            }

            FileOutputStream stream = new FileOutputStream(output);

            try {
                DocxStore.writeStyledText(stream, editor.getText());
            } finally {
                stream.close();
            }

            Toast.makeText(this,
                    "Disimpan: " + output.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception exception) {

            Toast.makeText(this,
                    "Gagal menyimpan: " + exception.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // FILE PICKER RESULT
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {

            if (requestCode == PICK_OPEN) {

                CharSequence text = DocxStore.readStyledText(
                        getContentResolver().openInputStream(uri)
                );

                editor.setText(text);

                currentName = uri.getLastPathSegment() == null
                        ? "Dokumen.docx"
                        : uri.getLastPathSegment();

                Toast.makeText(this,
                        "Dokumen dibuka. Periksa hasil tata letak " +
                        "sebelum menyunting dokumen kompleks.",
                        Toast.LENGTH_LONG).show();

            } else if (requestCode == PICK_SAVE) {

                java.io.OutputStream output =
                        getContentResolver().openOutputStream(uri);

                if (output == null) {
                    throw new IOException(
                            "Tidak dapat membuka lokasi penyimpanan."
                    );
                }

                try {
                    DocxStore.writeStyledText(output, editor.getText());
                } finally {
                    output.close();
                }

                Toast.makeText(this,
                        "DOCX berhasil disimpan.",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception exception) {

            new AlertDialog.Builder(this)
                    .setTitle("Tidak dapat memproses DOCX")
                    .setMessage(
                            exception.getMessage() == null
                                    ? "Terjadi kesalahan saat membaca atau menyimpan file."
                                    : exception.getMessage()
                    )
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // PHYSICAL KEYBOARD SHORTCUTS
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.isCtrlPressed()) {

            if (keyCode == KeyEvent.KEYCODE_S) {
                saveDocument(false);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_O) {
                openDocument();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_N) {
                newDocument();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_A) {
                editor.selectAll();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
