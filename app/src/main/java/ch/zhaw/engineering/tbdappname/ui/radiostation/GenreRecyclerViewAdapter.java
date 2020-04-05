package ch.zhaw.engineering.tbdappname.ui.radiostation;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentGenreListitemBinding;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

public class GenreRecyclerViewAdapter extends RecyclerView.Adapter<GenreRecyclerViewAdapter.ViewHolder> {
    private List<String> mValues;
    private final Context mContext;
    private boolean mEditMode;

    public GenreRecyclerViewAdapter(List<String> items, Context context) {
        mValues = new ArrayList<>(items);
        mContext = context;
        setHasStableIds(true);
    }

    @Override
    public GenreRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_genre_listitem, parent, false);
        return new GenreRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return mValues.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(final GenreRecyclerViewAdapter.ViewHolder holder, int position) {
        String genre = mValues.get(position);
        holder.bind(genre);
        holder.mBinding.genreName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mValues.set(position, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setEditMode(boolean editMode) {
        mEditMode = editMode;
        if (!mEditMode) {
            mValues = getGenres();
        }
        notifyDataSetChanged();
    }

    public void addEmptyGenre() {
        mValues.add("");
        notifyItemInserted(mValues.size());
    }

    public List<String> getGenres() {
        List<String> genres = new ArrayList<>(mValues.size());
        for (String genre : mValues) {
            if (genre.length() > 0) {
                genres.add(genre);
            }
        }
        return genres;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentGenreListitemBinding mBinding;
        public String mItem;

        public ViewHolder(View view) {
            super(view);
            mBinding = FragmentGenreListitemBinding.bind(view);
        }

        public void bind(String genre) {
            mItem = genre;
            mBinding.genreName.setText(genre);
            mBinding.genreName.setEditMode(mEditMode);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mBinding.genreName.getText() + "'";
        }
    }
}
