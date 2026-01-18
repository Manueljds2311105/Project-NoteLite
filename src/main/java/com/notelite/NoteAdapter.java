package com.notelite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();

    // 1. Interface Klik Biasa (Buat Edit)
    private OnItemClickListener listener;
    // 2. Interface Klik Tahan (Buat Hapus) - BARU
    private OnItemLongClickListener longListener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    // Interface baru buat Long Click
    public interface OnItemLongClickListener {
        void onItemLongClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longListener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);

        holder.tvTitle.setText(currentNote.getTitle());
        holder.tvContent.setText(currentNote.getContent());

        // KLIK BIASA -> EDIT
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentNote);
            }
        });

        // KLIK TAHAN -> HAPUS (BARU)
        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(currentNote);
                return true; // true artinya event selesai disini, gak lanjut ke klik biasa
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}