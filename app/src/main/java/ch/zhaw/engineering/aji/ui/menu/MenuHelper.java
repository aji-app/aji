package ch.zhaw.engineering.aji.ui.menu;

import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public final class MenuHelper {

    private MenuHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void setupSearchView(SortResource sortResource, AppViewModel appViewModel, Menu menu) {
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setMaxWidth((Resources.getSystem().getDisplayMetrics().widthPixels / 2));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                appViewModel.changeSearchText(sortResource, newText);
                return true;
            }
        });
        String searchText = appViewModel.getSearchString(sortResource);
        if (searchText != null && searchText.length() > 0) {
            search.setQuery(searchText, false);
            search.setIconified(false);
        }
    }

    public static boolean onOptionsItemSelected(SortResource sortResource, AppViewModel appViewModel, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.direction_asc:
                appViewModel.changeSortDirection(sortResource, true);
                return true;
            case R.id.direction_desc:
                appViewModel.changeSortDirection(sortResource, false);
                return true;
            default:
                return false;
        }
    }
}
