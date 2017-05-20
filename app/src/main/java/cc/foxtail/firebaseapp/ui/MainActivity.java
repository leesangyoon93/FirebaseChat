package cc.foxtail.firebaseapp.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.InputStream;

import cc.foxtail.firebaseapp.R;
import cc.foxtail.firebaseapp.model.ChatModel;
import cc.foxtail.firebaseapp.model.OnCompleteListener;
import cc.foxtail.firebaseapp.model.OnDataChangedListener;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 100;

    ProgressBar progressBar;
    Button chatButton;
    Button photoButton;

    private ChatModel model = new ChatModel();
    Uri currentImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText chatEdit = (EditText) findViewById(R.id.chat_edit);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);
        chatButton = (Button) findViewById(R.id.chat_button);
        photoButton = (Button) findViewById(R.id.photo_button);

        showProgressBar();

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                String message = chatEdit.getText().toString();
                if (currentImageUri != null) {
                    showProgressBar();
                    model.sendMessageWithImage(message, getInputStreamFromUri(currentImageUri));
                    currentImageUri = null;
                } else {
                    if (message.length() > 0) {
                        model.sendMessage(message);
                    }
                    else {
                        showSnackbarWithMessage("메세지를 입력해주세요.");
                    }
                }
            }
        });

        model.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onSuccess() {
                hideProgressBar();
                showSnackbarWithMessage("사진 메세지 전송이 완료되었습니다.");
            }

            @Override
            public void onFailure() {
                hideProgressBar();
                showSnackbarWithMessage("사진 메세지 전송에 실패했습니다 다시 시도해주세요.");
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new RecyclerView.Adapter<ChatHolder>() {
            @Override
            public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View view = layoutInflater.inflate(R.layout.recycler_item_chat, parent, false);
                return new ChatHolder(view);
            }

            @Override
            public void onBindViewHolder(ChatHolder holder, int position) {
                String message = model.getMessage(position);
                holder.setText(message);

                String imageUrl = model.getImageURL(position);
                holder.setImage(imageUrl);
            }

            @Override
            public int getItemCount() {
                return model.getMessageCount();
            }
        });

        model.setOnDataChangedListener(new OnDataChangedListener() {
            @Override
            public void onDataChanged() {
                hideProgressBar();
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private InputStream getInputStreamFromUri(Uri uri) {
        InputStream is = null;
        try {
            ContentResolver resolver = getContentResolver();
            is = resolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return is;
    }

    private void showSnackbarWithMessage(String message) {
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.main_layout);
        Snackbar.make(relativeLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        chatButton.setEnabled(false);
        photoButton.setEnabled(false);

    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        chatButton.setEnabled(true);
        photoButton.setEnabled(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            currentImageUri = data.getData();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(getBaseContext())
                .setMessage("앱을 종료하시겠습니까?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();


        dialog.show();
    }

    class ChatHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;


        public ChatHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.chat_text_view);
            imageView = (ImageView) itemView.findViewById(R.id.chat_image_view);
        }

        public void setText(String text) {
            textView.setText(text);
        }

        public void setImage(String imageUrl) {
            Glide.with(MainActivity.this)
                    .load(imageUrl)
                    .into(imageView);
        }
    }
}



















