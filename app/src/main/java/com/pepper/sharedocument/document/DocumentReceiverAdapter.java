package com.pepper.sharedocument.document;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pepper.sharedocument.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by prasanth.mathavan on 16,April,2020
 */
public class DocumentReceiverAdapter extends RecyclerView.Adapter<DocumentReceiverAdapter.DocumentHolder> {


    private static List<DocumentReceived> documentList = new ArrayList<>();
    private static OnItemClickListener onItemClickListener;

    public DocumentReceiverAdapter(List<DocumentReceived> document, OnItemClickListener clickListener) {
        documentList = document;
        onItemClickListener = clickListener;
    }

    @NonNull
    @Override
    public DocumentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_holder, parent, false);
        return new DocumentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentHolder holder, int position) {
        DocumentReceived documentReceived = documentList.get(position);
        holder.name.setText(documentReceived.getFileName());
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public static class DocumentHolder extends RecyclerView.ViewHolder {
        private TextView name;

        public DocumentHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(v -> {
                DocumentReceived documentReceived = DocumentReceiverAdapter.documentList.get(getAdapterPosition());
                String type = getMimeType(documentReceived.getFile(), v.getContext());
                onItemClickListener.openMimeType(type, documentReceived.getFile());
                //Toast.makeText(v.getContext(), "" + type, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static String getMimeType(File file, Context context) {
        Uri uri = Uri.fromFile(file);
        String mimeType;
        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public interface OnItemClickListener {
        void openMimeType(String mimeType, File file);
    }

}
