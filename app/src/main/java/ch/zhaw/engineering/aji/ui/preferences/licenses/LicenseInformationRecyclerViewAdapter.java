package ch.zhaw.engineering.aji.ui.preferences.licenses;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentLicenseInformationBinding;
import ch.zhaw.engineering.aji.ui.preferences.licenses.data.Licenses.LicenseInformation;

public class LicenseInformationRecyclerViewAdapter extends RecyclerView.Adapter<LicenseInformationRecyclerViewAdapter.ViewHolder> {

    private final List<LicenseInformation> mValues;
    private final LicenseInformationFragment.LicenseListFragmentListener mListener;
    private Context mContext;

    public LicenseInformationRecyclerViewAdapter(List<LicenseInformation> items, LicenseInformationFragment.LicenseListFragmentListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_license_information, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.licenseInformation = mValues.get(position);
        holder.binding.libraryName.setText(holder.licenseInformation.getLibraryName());

        SpannableString content = new SpannableString(mContext.getString(holder.licenseInformation.getUrl()));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.binding.libraryUrl.setText(content);

        holder.binding.libraryUrl.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onLibraryUrlClicked(holder.licenseInformation);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onLicenseSelected(holder.licenseInformation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FragmentLicenseInformationBinding binding;
        LicenseInformation licenseInformation;

        ViewHolder(View view) {
            super(view);
            binding = FragmentLicenseInformationBinding.bind(view);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + licenseInformation.getLibraryName() + "'";
        }
    }
}
